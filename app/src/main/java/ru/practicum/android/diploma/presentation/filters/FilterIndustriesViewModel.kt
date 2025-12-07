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

    // флаг для отслеживания успешной загрузки данных
    private var isDataLoaded = false

    fun loadIndustries() {
        // Если данные уже загружены, не загружаем повторно
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

                    // Загружаем ранее выбранную отрасль
                    loadPreviouslySelectedIndustry()
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

    // МЕТОД: для мгновенной очистки поиска без задержки
    fun clearSearch() {
        currentQuery = ""
        searchJob?.cancel()
        filterIndustries("")
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
        // проверяем наличие загруженных данных
        if (!isDataLoaded || allIndustries.isEmpty()) {
            _filteredIndustries.value = emptyList()
            // Не меняем состояние, если данные еще не загружены
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

        // обновляем состояние только если данные успешно загружены
        if (isDataLoaded) {
            _state.value = if (filtered.isEmpty()) {
                IndustriesState.NoResults //  состояние для "нет результатов поиска"
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
        object NoResults : IndustriesState() // Нет результатов поиска
        object Empty : IndustriesState() // Пустой список от сервера
        object Error : IndustriesState() // Ошибка загрузки
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 300L
    }
}
