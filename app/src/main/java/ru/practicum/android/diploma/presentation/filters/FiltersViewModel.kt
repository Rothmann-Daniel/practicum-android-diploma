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

    // Применённые фильтры (SharedPreferences)
    private val _appliedFilters = MutableLiveData<FilterSettings>()
    val appliedFilters: LiveData<FilterSettings> = _appliedFilters

    // Черновик — редактируемые фильтры, показ в UI
    private val _draftFilters = MutableLiveData<FilterSettings>()
    val draftFilters: LiveData<FilterSettings> = _draftFilters

    init {
        viewModelScope.launch {
            val saved = getFilterUseCase()
            _appliedFilters.value = saved.copy()          // применённые
            _draftFilters.value = saved.copy()     // черновик
        }
    }

    fun loadFilters() {
        viewModelScope.launch {
            _appliedFilters.value = getFilterUseCase()
        }
    }

    fun saveIndustry(industry: Industry?) {
        _appliedFilters.value = _appliedFilters.value?.copy(
            industry = industry
        )
    }

    fun saveSalary(salary: Int?) {
        _appliedFilters.value = _appliedFilters.value?.copy(
            salary = salary
        )
    }

    fun saveOnlyWithSalary(onlyWithSalary: Boolean) {
        _appliedFilters.value = _appliedFilters.value?.copy(
            onlyWithSalary = onlyWithSalary
        )
    }

    fun updateIndustryDraft(industry: Industry?) {
        val current = _draftFilters.value ?: FilterSettings()
        val updated = current.copy(industry = industry)
        _draftFilters.value = updated
        viewModelScope.launch { saveFilterUseCase.applyFilters(updated) } // сохраняем сразу
    }

    fun updateSalaryDraft(salary: Int?) {
        val current = _draftFilters.value ?: FilterSettings()
        val updated = current.copy(salary = salary)
        _draftFilters.value = updated
        viewModelScope.launch { saveFilterUseCase.applyFilters(updated) }
    }

    fun updateOnlyWithSalaryDraft(value: Boolean) {
        val current = _draftFilters.value ?: FilterSettings()
        val updated = current.copy(onlyWithSalary = value)
        _draftFilters.value = updated
        viewModelScope.launch { saveFilterUseCase.applyFilters(updated) }
    }

    fun clearAllFilters() {
        viewModelScope.launch {
            clearFilterUseCase()
            val empty = FilterSettings()              // пустые
            _appliedFilters.value = empty.copy()
            _draftFilters.value = empty.copy()
        }
    }

    fun applyFilters() {
        val settings = _draftFilters.value ?: return
        _appliedFilters.value = settings.copy()
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
