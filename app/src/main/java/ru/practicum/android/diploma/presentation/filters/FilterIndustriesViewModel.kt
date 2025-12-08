package ru.practicum.android.diploma.presentation.filters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.usecases.GetIndustriesUseCase
import ru.practicum.android.diploma.domain.usecases.SaveFilterSettingsUseCase

class FilterIndustriesViewModel(
    private val getIndustriesUseCase: GetIndustriesUseCase,
    private val saveFilterSettingsUseCase: SaveFilterSettingsUseCase
) : ViewModel() {

    private val _state = MutableLiveData<IndustriesState>()
    val state: LiveData<IndustriesState> = _state

    private val _filteredIndustries = MutableLiveData<List<Industry>>(emptyList())
    val filteredIndustries: LiveData<List<Industry>> = _filteredIndustries

    // ВРЕМЕННЫЙ выбор (пока не нажали "Выбрать")
    private val _temporarySelectedIndustry = MutableLiveData<Industry?>(null)
    val temporarySelectedIndustry: LiveData<Industry?> = _temporarySelectedIndustry

    private val _showSelectButton = MutableLiveData<Boolean>(false)
    val showSelectButton: LiveData<Boolean> = _showSelectButton

    private val _selectedIndustryId = MutableLiveData<Int?>(null)
    val selectedIndustryId: LiveData<Int?> = _selectedIndustryId

    private var allIndustries: List<Industry> = emptyList()
    private var searchJob: Job? = null
    private var currentQuery: String = ""
    private var isDataLoaded = false

    fun loadIndustries() {
        if (isDataLoaded && allIndustries.isNotEmpty()) {
            filterIndustries(currentQuery)
            return
        }

        viewModelScope.launch {
            _state.value = IndustriesState.Loading

            when (val result = getIndustriesUseCase()) {
                is DomainResult.Success -> {
                    allIndustries = result.data
                    isDataLoaded = true

                    if (result.data.isEmpty()) {
                        _state.value = IndustriesState.Empty
                    } else {
                        _state.value = IndustriesState.Content
                        filterIndustries(currentQuery)
                    }

                    // Загружаем текущую отрасль из ПРИМЕНЁННЫХ фильтров
                    loadCurrentIndustry()
                }

                is DomainResult.Error -> {
                    _state.value = IndustriesState.Error
                    isDataLoaded = false
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        currentQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_DELAY_MS)
            filterIndustries(query)
        }
    }

    fun clearSearch() {
        currentQuery = ""
        searchJob?.cancel()
        filterIndustries("")
    }

    /**
     * ВРЕМЕННЫЙ выбор отрасли (только в UI, не сохраняется)
     */
    fun selectIndustry(industry: Industry) {
        _temporarySelectedIndustry.value = industry
        _selectedIndustryId.value = industry.id
        updateSelectButtonVisibility()
    }

    /**
     * Возвращает временно выбранную отрасль для передачи в FiltersFragment
     */
    fun getTemporarySelection(): Industry? {
        return _temporarySelectedIndustry.value
    }

    fun clearSelection() {
        _temporarySelectedIndustry.value = null
        _selectedIndustryId.value = null
        updateSelectButtonVisibility()
    }

    private fun filterIndustries(query: String) {
        if (!isDataLoaded || allIndustries.isEmpty()) {
            _filteredIndustries.value = emptyList()
            if (_state.value !is IndustriesState.Loading && _state.value !is IndustriesState.Error) {
                _state.value = IndustriesState.Empty
            }
            return
        }

        val filtered = if (query.isBlank()) {
            allIndustries
        } else {
            allIndustries.filter { industry ->
                industry.name.contains(query, ignoreCase = true)
            }
        }

        _filteredIndustries.value = filtered

        if (isDataLoaded) {
            _state.value = if (filtered.isEmpty()) {
                IndustriesState.NoResults
            } else {
                IndustriesState.Content
            }
        }
    }

    /**
     * Загружаем ПРИМЕНЁННУЮ отрасль (из SharedPreferences)
     * для отображения галочки на уже выбранной отрасли
     */
    private fun loadCurrentIndustry() {
        viewModelScope.launch {
            val savedIndustry = saveFilterSettingsUseCase.getSavedIndustry()
            _temporarySelectedIndustry.value = savedIndustry
            _selectedIndustryId.value = savedIndustry?.id
            updateSelectButtonVisibility()
        }
    }

    private fun updateSelectButtonVisibility() {
        _showSelectButton.value = _temporarySelectedIndustry.value != null
    }

    sealed class IndustriesState {
        object Loading : IndustriesState()
        object Content : IndustriesState()
        object NoResults : IndustriesState()
        object Empty : IndustriesState()
        object Error : IndustriesState()
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 300L
    }
}
