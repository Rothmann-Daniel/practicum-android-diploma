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

    private val _selectedIndustry = MutableLiveData<Industry?>(null)
    val selectedIndustry: LiveData<Industry?> = _selectedIndustry

    private val _showSelectButton = MutableLiveData<Boolean>(false)
    val showSelectButton: LiveData<Boolean> = _showSelectButton

    // Добавляем LiveData для ID выбранной отрасли
    private val _selectedIndustryId = MutableLiveData<Int?>(null)
    val selectedIndustryId: LiveData<Int?> = _selectedIndustryId

    private var allIndustries: List<Industry> = emptyList()
    private var searchJob: Job? = null
    private var currentQuery: String = ""

    fun loadIndustries() {
        viewModelScope.launch {
            _state.value = IndustriesState.Loading

            when (val result = getIndustriesUseCase()) {
                is DomainResult.Success -> {
                    allIndustries = result.data

                    if (result.data.isEmpty()) {
                        _state.value = IndustriesState.Empty
                    } else {
                        _state.value = IndustriesState.Content
                        filterIndustries(currentQuery)
                    }

                    // Загружаем ранее выбранную отрасль
                    loadPreviouslySelectedIndustry()
                }

                is DomainResult.Error -> {
                    _state.value = IndustriesState.Error
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

    fun selectIndustry(industry: Industry) {
        _selectedIndustry.value = industry
        _selectedIndustryId.value = industry.id // Обновляем ID выбранной отрасли
        updateSelectButtonVisibility()
    }

    fun saveSelectedIndustry() {
        viewModelScope.launch {
            saveFilterSettingsUseCase.saveIndustry(_selectedIndustry.value)
        }
    }

    fun clearSelection() {
        _selectedIndustry.value = null
        _selectedIndustryId.value = null // Сбрасываем ID выбранной отрасли
        updateSelectButtonVisibility()
        viewModelScope.launch {
            saveFilterSettingsUseCase.saveIndustry(null)
        }
    }

    private fun filterIndustries(query: String) {
        if (allIndustries.isEmpty()) {
            _filteredIndustries.value = emptyList()
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

        if (_state.value is IndustriesState.Content) {
            _state.value = if (filtered.isEmpty()) {
                IndustriesState.Empty
            } else {
                IndustriesState.Content
            }
        }
    }

    private fun loadPreviouslySelectedIndustry() {
        viewModelScope.launch {
            val savedIndustry = saveFilterSettingsUseCase.getSavedIndustry()
            _selectedIndustry.value = savedIndustry
            _selectedIndustryId.value = savedIndustry?.id // Устанавливаем ID сохраненной отрасли
            updateSelectButtonVisibility()
        }
    }

    private fun updateSelectButtonVisibility() {
        _showSelectButton.value = _selectedIndustry.value != null
    }

    fun findIndustryPosition(industry: Industry?): Int {
        if (industry == null) return -1
        return _filteredIndustries.value?.indexOfFirst { it.id == industry.id } ?: -1
    }

    sealed class IndustriesState {
        object Loading : IndustriesState()
        object Content : IndustriesState()
        object Empty : IndustriesState()
        object Error : IndustriesState()
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 300L
    }
}
