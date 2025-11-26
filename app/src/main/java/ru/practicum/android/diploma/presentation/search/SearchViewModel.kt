package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.core.utils.debounce
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchRequest
import ru.practicum.android.diploma.domain.usecases.GetCachedVacanciesUseCase
import ru.practicum.android.diploma.domain.usecases.SearchVacanciesUseCase

class SearchViewModel(
    private val searchUseCase: SearchVacanciesUseCase,
    private val getCachedVacanciesUseCase: GetCachedVacanciesUseCase
) : ViewModel() {

    // Состояния UI для экрана поиска вакансий
    sealed class SearchUiState {
        object Loading : SearchUiState()
        object EmptyQuery : SearchUiState()
        object EmptyResult : SearchUiState()
        data class Success(val vacancies: List<Vacancy>, val isLastPage: Boolean) : SearchUiState()
        data class Error(val message: String) : SearchUiState()
    }

    private val _uiState = MutableLiveData<SearchUiState>(SearchUiState.EmptyQuery)
    val uiState: LiveData<SearchUiState> = _uiState

    private val loadedVacancies = mutableListOf<Vacancy>()
    private var currentPage = 0
    private var totalPages = 1
    private var isLoadingPage = false
    private var searchJob: Job? = null
    private var lastQuery: String = ""

    // Создаем функцию debounce для поиска
    private val debouncedSearch = debounce<String>(
        delayMillis = DEBOUNCE_DELAY_MS,
        coroutineScope = viewModelScope,
        useLastParam = true
    ) { query ->
        if (query.isBlank()) {
            loadedVacancies.clear()
            currentPage = 0
            totalPages = 1
            _uiState.value = SearchUiState.EmptyQuery
        } else {
            searchVacancies(query, page = 0)
        }
    }

    init {
        loadCachedVacancies()
    }

    // Загружаем кэшированные вакансии при старте
    private fun loadCachedVacancies() {
        viewModelScope.launch {
            val cached = getCachedVacanciesUseCase()
            if (cached.isNotEmpty()) {
                loadedVacancies.clear()
                loadedVacancies.addAll(cached)
                _uiState.value = SearchUiState.Success(loadedVacancies.toList(), isLastPage = true)
            }
        }
    }

    // Вызывается при изменении текста в поисковом поле
    fun onSearchQueryChanged(query: String) {
        lastQuery = query
        debouncedSearch(query)
    }

    // Основная логика поиска и пагинации
    private fun searchVacancies(query: String, page: Int) {
        if (isLoadingPage || page >= totalPages) return

        isLoadingPage = true
        if (page == 0) _uiState.value = SearchUiState.Loading

        viewModelScope.launch {
            val request = VacancySearchRequest(text = query, page = page)
            when (val result = searchUseCase(request)) {
                is ApiResponse.Success -> {
                    totalPages = result.data.pages
                    currentPage = result.data.page

                    // Убираем дубли
                    val newVacancies = result.data.vacancies.filterNot { loadedVacancies.contains(it) }
                    loadedVacancies.addAll(newVacancies)

                    _uiState.value = if (loadedVacancies.isEmpty()) {
                        SearchUiState.EmptyResult
                    } else {
                        SearchUiState.Success(
                            vacancies = loadedVacancies.toList(),
                            isLastPage = currentPage >= totalPages - 1
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _uiState.value = SearchUiState.Error(result.message)
                }

                else -> {
                    // На всякий случай, если появятся новые типы ApiResponse
                    _uiState.value = SearchUiState.Error("Неизвестная ошибка при получении вакансий")
                }
            }
            isLoadingPage = false
        }
    }

    // Загружаем следующую страницу при скролле списка
    fun loadNextPage() {
        if (currentPage + 1 < totalPages && !isLoadingPage) {
            searchVacancies(lastQuery, currentPage + 1)
        }
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 2000L
    }
}
