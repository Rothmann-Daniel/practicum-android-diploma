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

    // Текущие настройки (для UI)
    private val _filterSettings = MutableLiveData<FilterSettings>()
    val filterSettings: LiveData<FilterSettings> = _filterSettings

    // Сохраненные настройки (исходное состояние при открытии экрана)
    private var savedFilterSettings: FilterSettings = FilterSettings()

    init {
        loadFilters()
    }

    fun loadFilters() {
        viewModelScope.launch {
            val settings = getFilterUseCase()
            savedFilterSettings = settings
            _filterSettings.value = settings
        }
    }

    /**
     * Обновляет ТОЛЬКО UI, не сохраняя в SharedPreferences
     */
    fun updateIndustry(industry: Industry?) {
        val current = _filterSettings.value ?: FilterSettings()
        _filterSettings.value = current.copy(industry = industry)
    }

    /**
     * Обновляет ТОЛЬКО UI, не сохраняя в SharedPreferences
     */
    fun updateSalary(salary: Int?) {
        val current = _filterSettings.value ?: FilterSettings()
        _filterSettings.value = current.copy(salary = salary)
    }

    /**
     * Обновляет ТОЛЬКО UI, не сохраняя в SharedPreferences
     */
    fun updateOnlyWithSalary(onlyWithSalary: Boolean) {
        val current = _filterSettings.value ?: FilterSettings()
        _filterSettings.value = current.copy(onlyWithSalary = onlyWithSalary)
    }

    /**
     * ИСПРАВЛЕНО: Применяет все изменения СИНХРОННО
     * Не использует корутины, чтобы гарантировать немедленное сохранение
     */
    fun applyFilters() {
        viewModelScope.launch {
            val settings = _filterSettings.value ?: FilterSettings()

            // Сохраняем ВСЕ настройки последовательно
            saveFilterUseCase.saveIndustry(settings.industry)
            saveFilterUseCase.saveSalary(settings.salary)
            saveFilterUseCase.saveOnlyWithSalary(settings.onlyWithSalary)

            // Обновляем сохраненное состояние
            savedFilterSettings = settings
        }
    }

    /**
     * Сбрасывает все фильтры
     */
    fun clearAllFilters() {
        viewModelScope.launch {
            clearFilterUseCase()
            loadFilters()
        }
    }

    /**
     * Отменяет изменения и восстанавливает сохраненное состояние
     */
    fun cancelChanges() {
        _filterSettings.value = savedFilterSettings
    }

    /**
     * Проверяет, были ли изменения
     */
    fun hasChanges(): Boolean {
        return _filterSettings.value != savedFilterSettings
    }
}
