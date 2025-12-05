package ru.practicum.android.diploma.presentation.filters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.usecases.ClearFilterSettingsUseCase
import ru.practicum.android.diploma.domain.usecases.GetFilterSettingsUseCase
import ru.practicum.android.diploma.domain.usecases.SaveFilterSettingsUseCase

class FiltersViewModel(
    private val getFilterUseCase: GetFilterSettingsUseCase,
    private val saveFilterUseCase: SaveFilterSettingsUseCase,
    private val clearFilterUseCase: ClearFilterSettingsUseCase
) : ViewModel() {

    private val _filterSettings = MutableLiveData<FilterSettings>()
    val filterSettings: LiveData<FilterSettings> = _filterSettings

    init {
        loadFilters()
    }

    fun loadFilters() {
        viewModelScope.launch {
            _filterSettings.value = getFilterUseCase()
        }
    }

    fun saveIndustry(industry: Industry?) {
        viewModelScope.launch {
            saveFilterUseCase.saveIndustry(industry)
            _filterSettings.value = getFilterUseCase()
        }
    }

    fun saveSalary(salary: Int?) {
        viewModelScope.launch {
            saveFilterUseCase.saveSalary(salary)
            _filterSettings.value = getFilterUseCase()
        }
    }

    fun saveOnlyWithSalary(onlyWithSalary: Boolean) {
        viewModelScope.launch {
            saveFilterUseCase.saveOnlyWithSalary(onlyWithSalary)
            _filterSettings.value = getFilterUseCase()
        }
    }

    fun clearAllFilters() {
        viewModelScope.launch {
            clearFilterUseCase()
            loadFilters()
        }
    }

    suspend fun getSavedIndustry(): Industry? {
        return saveFilterUseCase.getSavedIndustry()
    }

    suspend fun getSavedSalary(): Int? {
        return saveFilterUseCase.getSavedSalary()
    }

    suspend fun getSavedOnlyWithSalary(): Boolean {
        return saveFilterUseCase.getSavedOnlyWithSalary()
    }

    // Можно также вернуть все настройки сразу
    suspend fun getFilterSettings() = getFilterUseCase()
}

