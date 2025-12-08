package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.core.utils.SingleLiveEvent
import ru.practicum.android.diploma.core.utils.debounce
import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchRequest
import ru.practicum.android.diploma.domain.usecases.GetFilterSettingsUseCase
import ru.practicum.android.diploma.domain.usecases.SearchVacanciesUseCase

class SearchViewModel(
    private val searchUseCase: SearchVacanciesUseCase,
    private val getFilterSettingsUseCase: GetFilterSettingsUseCase
) : ViewModel() {

    // Флаги для восстановления состояния
    var restorePreviousResults: Boolean = false
    private var allowRestoreFromCache: Boolean = false

    // UI состояние поиска
    sealed class SearchUiState {
        abstract val useFilter: Boolean

        data class Loading(override val useFilter: Boolean) : SearchUiState()
        data class EmptyQuery(override val useFilter: Boolean) : SearchUiState()
        data class EmptyResult(override val useFilter: Boolean) : SearchUiState()
        data class Success(
            val vacancies: List<Vacancy>,
            val isLastPage: Boolean,
            val found: Int,
            override val useFilter: Boolean
        ) : SearchUiState()

        data class Error(
            val message: String,
            val isNetworkError: Boolean = false,
            override val useFilter: Boolean
        ) : SearchUiState()
    }

    // LiveData для UI состояния
    private val _uiState = MutableLiveData<SearchUiState>(SearchUiState.EmptyQuery(false))
    val uiState: LiveData<SearchUiState> = _uiState

    // LiveData для загрузки следующей страницы
    private val _isLoadingNextPage = MutableLiveData(false)
    val isLoadingNextPage: LiveData<Boolean> = _isLoadingNextPage

    // LiveData для ошибок (SingleLiveEvent)
    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> = _errorEvent

    // LiveData для подсветки кнопки фильтра
    private val _shouldHighlightFilter = MutableLiveData<Boolean>(false)
    val shouldHighlightFilter: LiveData<Boolean> = _shouldHighlightFilter

    // Внутренние переменные для управления поиском
    private val loadedVacancies = mutableListOf<Vacancy>()
    private var currentPage = 0
    private var totalPages = 1
    private var isLoadingPage = false
    private var lastQuery: String = ""

    // Текущие фильтры для поиска
    private var filterSettings = FilterSettings()

    // Флаг использования фильтров для текущего поиска
    private var useFilterInSearch = false

    // Инициализация ViewModel
    init {
        loadSavedFiltersOnInit()
    }

    /**
     * Загрузка сохраненных фильтров при инициализации ViewModel
     */
    private fun loadSavedFiltersOnInit() {
        viewModelScope.launch {
            try {
                val savedFilters = getFilterSettingsUseCase()

                // Обновляем подсветку кнопки на основе сохраненных фильтров
                updateFilterHighlight(savedFilters)

                // Инициализируем фильтры для поиска
                filterSettings = savedFilters.copy()
                useFilterInSearch = hasAnyFilters(savedFilters)

                // Обновляем UI состояние с учетом фильтров
                updateUseFilterInLiveData()

            } catch (e: Exception) {
                // В случае ошибки загрузки фильтров, используем пустые настройки
                _shouldHighlightFilter.value = false
                filterSettings = FilterSettings()
                useFilterInSearch = false
            }
        }
    }

    /**
     * Проверка наличия любых фильтров
     */
    private fun hasAnyFilters(filters: FilterSettings): Boolean {
        return filters.industry != null ||
            filters.salary != null ||
            filters.onlyWithSalary
    }

    /**
     * Обновление подсветки кнопки фильтра
     */
    private fun updateFilterHighlight(filters: FilterSettings) {
        val shouldHighlight = hasAnyFilters(filters)
        // Обновляем только если значение изменилось
        if (_shouldHighlightFilter.value != shouldHighlight) {
            _shouldHighlightFilter.value = shouldHighlight
        }
    }

    /**
     * Обновление флага useFilter в текущем UI состоянии
     */
    private fun updateUseFilterInLiveData() {
        val currentState = _uiState.value
        if (currentState != null) {
            val newState = when (currentState) {
                is SearchUiState.Loading -> currentState.copy(useFilter = useFilterInSearch)
                is SearchUiState.EmptyQuery -> currentState.copy(useFilter = useFilterInSearch)
                is SearchUiState.EmptyResult -> currentState.copy(useFilter = useFilterInSearch)
                is SearchUiState.Success -> currentState.copy(useFilter = useFilterInSearch)
                is SearchUiState.Error -> currentState.copy(useFilter = useFilterInSearch)
            }
            // Обновляем только если состояние изменилось
            if (currentState.useFilter != newState.useFilter) {
                _uiState.value = newState
            }
        }
    }

    /**
     * Дебаунсинг для поисковых запросов
     */
    private val debouncedSearch = debounce<String>(
        delayMillis = DEBOUNCE_DELAY_MS,
        coroutineScope = viewModelScope
    ) { query ->
        if (query.isBlank()) {
            // Очистка результатов при пустом запросе
            loadedVacancies.clear()
            currentPage = 0
            totalPages = 1
            _uiState.value = SearchUiState.EmptyQuery(useFilterInSearch)
        } else {
            // Запуск поиска
            searchVacancies(query, page = 0)
        }
    }

    /**
     * Обработка изменения поискового запроса (с дебаунсингом)
     */
    fun onSearchQueryChanged(query: String) {
        // Сброс состояния при ошибке или пустом результате
        val state = _uiState.value
        if (state is SearchUiState.Error || state is SearchUiState.EmptyResult) {
            loadedVacancies.clear()
            currentPage = 0
            totalPages = 1
        }

        lastQuery = query
        debouncedSearch(query)
    }

    /**
     * Принудительный запуск поиска (без дебаунсинга)
     */
    fun forceSearch(query: String) {
        lastQuery = query
        loadedVacancies.clear()
        currentPage = 0
        totalPages = 1

        _uiState.value = SearchUiState.Loading(useFilterInSearch)
        searchVacancies(query, page = 0)
    }

    /**
     * Выполнение поиска вакансий
     */
    private fun searchVacancies(query: String, page: Int) {
        // Проверка на возможность загрузки
        if (isLoadingPage || page >= totalPages) {
            return
        }

        isLoadingPage = true

        // Обновление UI состояния
        if (page == 0) {
            _uiState.value = SearchUiState.Loading(useFilterInSearch)
        } else {
            _isLoadingNextPage.value = true
        }

        viewModelScope.launch {
            try {
                // Формирование запроса с фильтрами
                val request = VacancySearchRequest(
                    text = query,
                    page = page,
                    industry = filterSettings.industry?.id,
                    salary = filterSettings.salary,
                    onlyWithSalary = filterSettings.onlyWithSalary
                )

                // Выполнение запроса
                when (val result = searchUseCase(request)) {
                    is DomainResult.Success -> {
                        // Обработка успешного результата
                        totalPages = result.data.pages
                        currentPage = result.data.page
                        updateVacanciesList(result.data.vacancies, page)
                        updateUiState(result.data.found)
                    }

                    is DomainResult.Error -> {
                        // Обработка ошибки
                        handleErrorResult(result, page)
                    }
                }
            } catch (e: Exception) {
                // Обработка исключений
                handleException(e, page)
            } finally {
                // Сброс флагов загрузки
                isLoadingPage = false
                _isLoadingNextPage.value = false
            }
        }
    }

    /**
     * Обновление списка вакансий
     */
    private fun updateVacanciesList(vacancies: List<Vacancy>, page: Int) {
        if (page == 0) {
            // Первая страница - очистка и добавление
            loadedVacancies.clear()
            loadedVacancies.addAll(vacancies)
        } else {
            // Последующие страницы - добавление уникальных вакансий
            val existingIds = loadedVacancies.map { it.id }.toSet()
            val unique = vacancies.filter { it.id !in existingIds }
            loadedVacancies.addAll(unique)
        }
    }

    /**
     * Обновление UI состояния после успешного поиска
     */
    private fun updateUiState(found: Int) {
        _uiState.value = if (loadedVacancies.isEmpty()) {
            SearchUiState.EmptyResult(useFilterInSearch)
        } else {
            SearchUiState.Success(
                vacancies = loadedVacancies.toList(),
                isLastPage = currentPage >= totalPages - 1,
                found = found,
                useFilter = useFilterInSearch
            )
        }
    }

    /**
     * Обработка ошибки поиска
     */
    private fun handleErrorResult(result: DomainResult.Error, page: Int) {
        val isNetworkError = result.type == DomainResult.ErrorType.NETWORK_ERROR

        if (page == 0) {
            // Ошибка при загрузке первой страницы
            _uiState.value = SearchUiState.Error(
                message = result.message,
                isNetworkError = isNetworkError,
                useFilter = useFilterInSearch
            )
        } else {
            // Ошибка при загрузке следующих страниц
            _uiState.value = SearchUiState.Success(
                vacancies = loadedVacancies.toList(),
                isLastPage = currentPage >= totalPages - 1,
                found = loadedVacancies.size,
                useFilter = useFilterInSearch
            )
        }

        // Публикация события ошибки
        if (isNetworkError) {
            _errorEvent.value = "Проверьте подключение к интернету"
        } else {
            _errorEvent.value = "Произошла ошибка при поиске"
        }
    }

    /**
     * Обработка исключений
     */
    private fun handleException(e: Exception, page: Int) {
        _uiState.value = SearchUiState.Error(
            message = e.message ?: "Неизвестная ошибка",
            isNetworkError = false,
            useFilter = useFilterInSearch
        )
        _errorEvent.value = "Произошла ошибка: ${e.message}"
    }

    /**
     * Загрузка следующей страницы результатов
     */
    fun loadNextPage() {
        if (currentPage + 1 >= totalPages || isLoadingPage) {
            return
        }
        searchVacancies(lastQuery, currentPage + 1)
    }

    /**
     * Очистка состояния поиска
     */
    fun clearSearchState() {
        loadedVacancies.clear()
        lastQuery = ""
        currentPage = 0
        totalPages = 1
        _uiState.value = SearchUiState.EmptyQuery(useFilterInSearch)
        allowRestoreFromCache = false
        restorePreviousResults = false
    }

    /**
     * Отметка для восстановления результатов при навигации
     */
    fun markRestoreForNavigation() {
        restorePreviousResults = true
        allowRestoreFromCache = true
    }

    /**
     * Получение обновлений фильтров от экрана фильтров
     *
     * @param filters новые настройки фильтров
     * @param isApply true если нажата кнопка "Применить" или "Сбросить", false если просто изменение
     */
    fun receiveFiltersUpdate(filters: FilterSettings, isApply: Boolean = false) {
        // Всегда обновляем подсветку кнопки на основе новых фильтров
        updateFilterHighlight(filters)

        if (isApply) {
            // При нажатии "Применить" или "Сбросить" обновляем фильтры для поиска
            filterSettings = filters.copy()
            useFilterInSearch = hasAnyFilters(filters)
            updateUseFilterInLiveData()

            // Запускаем поиск с новыми фильтрами (даже если они пустые)
            if (lastQuery.isNotBlank()) {
                forceSearch(lastQuery)
            }
        } else {
            // При простом изменении (без применения) только обновляем подсветку
            // но не меняем фильтры для текущего поиска
            // Это позволяет сохранить черновики без влияния на текущий поиск
        }
    }

    /**
     * Сброс всех фильтров
     */
    fun clearFilters() {
        viewModelScope.launch {
            try {
                // Получаем пустые настройки фильтров
                val empty = FilterSettings()

                // Обновляем подсветку кнопки
                _shouldHighlightFilter.value = false

                // Обновляем фильтры для поиска
                filterSettings = empty
                useFilterInSearch = false
                updateUseFilterInLiveData()

                // ВСЕГДА перезапускаем поиск без фильтров, даже если нет активного запроса
                // или если lastQuery пустой
                if (lastQuery.isNotBlank()) {
                    forceSearch(lastQuery)
                }
            } catch (e: Exception) {
                _errorEvent.value = "Ошибка при сбросе фильтров: ${e.message}"
            }
        }
    }

    /**
     * Получение текущих сохраненных фильтров (для отладки или тестирования)
     */
    suspend fun getCurrentSavedFilters(): FilterSettings {
        return getFilterSettingsUseCase()
    }

    /**
     * Проверка наличия активных фильтров
     */
    fun hasActiveFilters(): Boolean {
        return useFilterInSearch
    }

    /**
     * Получение текущего поискового запроса
     */
    fun getCurrentQuery(): String {
        return lastQuery
    }

    /**
     * Получение текущей страницы
     */
    fun getCurrentPage(): Int {
        return currentPage
    }

    /**
     * Получение общего количества страниц
     */
    fun getTotalPages(): Int {
        return totalPages
    }

    /**
     * Получение общего количества загруженных вакансий
     */
    fun getLoadedVacanciesCount(): Int {
        return loadedVacancies.size
    }

    companion object {
        // Константа для дебаунсинга (2 секунды)
        private const val DEBOUNCE_DELAY_MS = 2000L
    }
}
