package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.core.utils.debounce
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
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

    // Флаг для восстановления предыдущих результатов только при навигации
    var restorePreviousResults: Boolean = false

    private var allowRestoreFromCache: Boolean = false

    // Состояния UI для экрана поиска вакансий
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

    private val _uiState = MutableLiveData<SearchUiState>(SearchUiState.EmptyQuery(false))
    val uiState: LiveData<SearchUiState> = _uiState

    private val _isLoadingNextPage = MutableLiveData(false)
    val isLoadingNextPage: LiveData<Boolean> = _isLoadingNextPage

    private val loadedVacancies = mutableListOf<Vacancy>()
    private var currentPage = 0
    private var totalPages = 1
    private var isLoadingPage = false
    private var lastQuery: String = ""

    private var filterSettings = FilterSettings()
    private var useFilter = false

    fun receiveFilterInfo() {
        viewModelScope.launch {
            filterSettings = getFilterSettingsUseCase()
            useFilter = with(filterSettings) {
                industry != null || salary != null || onlyWithSalary
            }
            updateUseFilterInLiveData()
        }
    }

    private fun updateUseFilterInLiveData() {
        val previousState = _uiState.value
        if (previousState != null) {
            when (previousState) {
                is SearchUiState.Loading -> _uiState.value = previousState.copy(useFilter = useFilter)
                is SearchUiState.EmptyQuery -> _uiState.value = previousState.copy(useFilter = useFilter)
                is SearchUiState.EmptyResult -> _uiState.value = previousState.copy(useFilter = useFilter)
                is SearchUiState.Success -> _uiState.value = previousState.copy(useFilter = useFilter)
                is SearchUiState.Error -> _uiState.value = previousState.copy(useFilter = useFilter)
            }
        }
    }

    /**
     * Отложенный поиск с задержкой для уменьшения количества запросов при вводе текста
     */
    private val debouncedSearch = debounce<String>(
        delayMillis = DEBOUNCE_DELAY_MS,
        coroutineScope = viewModelScope
    ) { query ->
        if (query.isBlank()) {
            loadedVacancies.clear()
            currentPage = 0
            totalPages = 1
            _uiState.value = SearchUiState.EmptyQuery(useFilter)
        } else {
            searchVacancies(query, page = 0)
        }
    }

    /**
     * Обрабатывает изменение поискового запроса с отложенным поиском
     * @param query поисковый запрос
     */
    fun onSearchQueryChanged(query: String) {
        // Если до этого была ошибка или пустой результат — сбрасываем блокировку
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
     * Выполняет немедленный поиск без задержки
     * @param query поисковый запрос
     */
    fun forceSearch(query: String) {
        lastQuery = query

        // Сбросим состояние предыдущей ошибки / пустого результата
        loadedVacancies.clear()
        currentPage = 0
        totalPages = 1

        _uiState.value = SearchUiState.Loading(useFilter)

        // Запускаем моментальный поиск — БЕЗ debounce
        searchVacancies(query, page = 0)
    }

    /**
     * Выполняет поиск вакансий по запросу и странице
     * @param query поисковый запрос
     * @param page номер страницы для пагинации
     */
    private fun searchVacancies(query: String, page: Int) {
        if (isLoadingPage || page >= totalPages) return

        isLoadingPage = true
        if (page == 0) {
            _uiState.value = SearchUiState.Loading(useFilter)
        } else {
            _isLoadingNextPage.value = true
        }

        viewModelScope.launch {
            val request = VacancySearchRequest(
                text = query,
                page = page,
                industry = filterSettings.industry?.id,
                salary = filterSettings.salary,
                onlyWithSalary = filterSettings.onlyWithSalary
            )

            when (val result = searchUseCase(request)) {
                is DomainResult.Success -> { // Изменено с ApiResponse на DomainResult
                    totalPages = result.data.pages
                    currentPage = result.data.page
                    updateVacanciesList(result.data.vacancies, page)
                    updateUiState(result.data.found)
                }
                is DomainResult.Error -> handleErrorResult(result, page)
            }
            isLoadingPage = false
            _isLoadingNextPage.value = false
        }
    }

    /**
     * Обрабатывает ошибки при поиске вакансий
     * @param result объект ошибки
     * @param page номер страницы
     */
    private fun handleErrorResult(result: DomainResult.Error, page: Int) {
        val isNetworkError = result.type == DomainResult.ErrorType.NETWORK_ERROR

        if (page == 0) {
            _uiState.value = SearchUiState.Error(
                message = result.message,
                isNetworkError = isNetworkError,
                useFilter = useFilter
            )
        } else {
            _uiState.value = SearchUiState.Success(
                vacancies = loadedVacancies.toList(),
                isLastPage = currentPage >= totalPages - 1,
                found = loadedVacancies.size,
                useFilter = useFilter
            )
        }
    }

    /**
     * Обновляет список вакансий с учетом пагинации и устранения дубликатов
     * @param vacancies список новых вакансий
     * @param page номер страницы
     */
    private fun updateVacanciesList(vacancies: List<Vacancy>, page: Int) {
        if (page == 0) {
            loadedVacancies.clear()
            loadedVacancies.addAll(vacancies)
        } else {
            val existingIds = loadedVacancies.map { it.id }.toSet()
            val uniqueNewVacancies = vacancies.filter { it.id !in existingIds }
            loadedVacancies.addAll(uniqueNewVacancies)
        }
    }

    /**
     * Обновляет состояние UI на основе текущего списка вакансий
     * @param found общее количество найденных вакансий
     */
    private fun updateUiState(found: Int) {
        if (loadedVacancies.isEmpty()) {
            _uiState.value = SearchUiState.EmptyResult(useFilter)
        } else {
            SearchUiState.Success(
                vacancies = loadedVacancies.toList(),
                isLastPage = currentPage >= totalPages - 1,
                found = found,
                useFilter = useFilter
            )
        }
    }

    /**
     * Загружает следующую страницу результатов, если доступна
     */
    fun loadNextPage() {
        if (currentPage + 1 >= totalPages || isLoadingPage) return
        searchVacancies(lastQuery, currentPage + 1)
    }

    /**
     * Очищает состояние поиска и сбрасывает все данные
     */
    fun clearSearchState() {
        loadedVacancies.clear()
        lastQuery = ""
        currentPage = 0
        totalPages = 1
        _uiState.value = SearchUiState.EmptyQuery(useFilter)
        // Сброс флагов
        allowRestoreFromCache = false
        restorePreviousResults = false
    }

    /**
     * Помечает состояние для восстановления результатов при навигации назад
     */
    fun markRestoreForNavigation() {
        restorePreviousResults = true
        allowRestoreFromCache = true
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 2000L
    }
}
