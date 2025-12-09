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

    // Текущие настройки фильтра (автоматически сохраняются)
    private val _filterSettings = MutableLiveData<FilterSettings>()
    val filterSettings: LiveData<FilterSettings> = _filterSettings

    init {
        loadSavedSettings()
    }

    private fun loadSavedSettings() {
        viewModelScope.launch {
            val saved = getFilterUseCase()
            _filterSettings.value = saved
        }
    }

    fun updateIndustry(industry: Industry?) {
        val current = _filterSettings.value ?: FilterSettings()
        val updated = current.copy(industry = industry)
        _filterSettings.value = updated
        saveSettings(updated)
    }

    fun updateSalary(salary: Int?) {
        val current = _filterSettings.value ?: FilterSettings()
        val updated = current.copy(salary = salary)
        _filterSettings.value = updated
        saveSettings(updated)
    }

    fun updateOnlyWithSalary(value: Boolean) {
        val current = _filterSettings.value ?: FilterSettings()
        val updated = current.copy(onlyWithSalary = value)
        _filterSettings.value = updated
        saveSettings(updated)
    }

    private fun saveSettings(settings: FilterSettings) {
        viewModelScope.launch {
            // Сохраняем все настройки ОДНОВРЕМЕННО
            saveFilterUseCase.saveIndustry(settings.industry)
            saveFilterUseCase.saveSalary(settings.salary)
            saveFilterUseCase.saveOnlyWithSalary(settings.onlyWithSalary)
        }
    }

    fun clearAllFilters() {
        viewModelScope.launch {
            clearFilterUseCase()
            val empty = FilterSettings()
            _filterSettings.value = empty
        }
    }

    fun getCurrentSettings(): FilterSettings? {
        return _filterSettings.value
    }

    fun resetChangeTracking() {
        // В данной реализации не используется, оставляем для совместимости
    }

    fun hasAnyFilters(): Boolean {
        val settings = _filterSettings.value ?: return false
        return settings.industry != null ||
            settings.salary != null ||
            settings.onlyWithSalary
    }
}
