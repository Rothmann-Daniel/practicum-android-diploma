package ru.practicum.android.diploma.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.core.utils.SingleLiveEvent
import ru.practicum.android.diploma.core.utils.debounce
import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchRequest
import ru.practicum.android.diploma.domain.repository.FilterSettings
import ru.practicum.android.diploma.domain.usecases.SaveFilterSettingsUseCase
import ru.practicum.android.diploma.domain.usecases.SearchVacanciesUseCase

class SearchViewModel(
    private val searchUseCase: SearchVacanciesUseCase,
    private val saveFilterSettingsUseCase: SaveFilterSettingsUseCase
) : ViewModel() {

    var restorePreviousResults: Boolean = false
    private var allowRestoreFromCache: Boolean = false

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

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> = _errorEvent

    private val loadedVacancies = mutableListOf<Vacancy>()
    private var currentPage = 0
    private var totalPages = 1
    private var isLoadingPage = false
    private var lastQuery: String = ""

    private var filterSettings = FilterSettings()
    private var useFilter = false

    init {
        viewModelScope.launch {
            filterSettings = saveFilterSettingsUseCase.getFilterSettings()
            useFilter = filterSettings.industry != null || filterSettings.salary != null || filterSettings.onlyWithSalary
            _uiState.value = SearchUiState.EmptyQuery(useFilter)
        }
    }

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

    fun onSearchQueryChanged(query: String) {
        val state = _uiState.value
        if (state is SearchUiState.Error || state is SearchUiState.EmptyResult) {
            loadedVacancies.clear()
            currentPage = 0
            totalPages = 1
        }
        lastQuery = query
        debouncedSearch(query)
    }

    fun forceSearch(query: String) {
        lastQuery = query
        loadedVacancies.clear()
        currentPage = 0
        totalPages = 1

        _uiState.value = SearchUiState.Loading(useFilter)
        searchVacancies(query, page = 0)
    }

    private fun searchVacancies(query: String, page: Int) {
        if (isLoadingPage || page >= totalPages) {
            return
        }

        isLoadingPage = true

        if (page == 0) {
            _uiState.value = SearchUiState.Loading(useFilter)
        } else {
            _isLoadingNextPage.value = true
        }

        viewModelScope.launch {
            val request = VacancySearchRequest(text = query, page = page)

            when (val result = searchUseCase(request)) {
                is DomainResult.Success -> {
                    totalPages = result.data.pages
                    currentPage = result.data.page
                    updateVacanciesList(result.data.vacancies, page)
                    updateUiState(result.data.found)
                }

                is DomainResult.Error -> {
                    handleErrorResult(result, page)
                }
            }

            isLoadingPage = false
            _isLoadingNextPage.value = false
        }
    }

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

        if (isNetworkError) {
            _errorEvent.value = "Проверьте подключение к интернету"
        } else {
            _errorEvent.value = "Произошла ошибка"
        }
    }

    private fun updateVacanciesList(vacancies: List<Vacancy>, page: Int) {
        if (page == 0) {
            loadedVacancies.clear()
            loadedVacancies.addAll(vacancies)
        } else {
            val existingIds = loadedVacancies.map { it.id }.toSet()
            val unique = vacancies.filter { it.id !in existingIds }
            loadedVacancies.addAll(unique)
        }
    }

    private fun updateUiState(found: Int) {
        if (loadedVacancies.isEmpty()) {
            _uiState.value = SearchUiState.EmptyResult(useFilter)
        } else {
            _uiState.value = SearchUiState.Success(
                vacancies = loadedVacancies.toList(),
                isLastPage = currentPage >= totalPages - 1,
                found = found,
                useFilter = useFilter
            )
        }
    }

    fun loadNextPage() {
        if (currentPage + 1 >= totalPages || isLoadingPage) {
            return
        }
        searchVacancies(lastQuery, currentPage + 1)
    }

    fun clearSearchState() {
        loadedVacancies.clear()
        lastQuery = ""
        currentPage = 0
        totalPages = 1
        _uiState.value = SearchUiState.EmptyQuery(useFilter)
        allowRestoreFromCache = false
        restorePreviousResults = false
    }

    fun markRestoreForNavigation() {
        restorePreviousResults = true
        allowRestoreFromCache = true
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 2000L
    }
}
