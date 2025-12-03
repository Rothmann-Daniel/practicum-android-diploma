package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.FilterRepository
import ru.practicum.android.diploma.domain.repository.FilterSettings

class SaveFilterSettingsUseCase(private val repository: FilterRepository) {

    suspend fun saveIndustry(industry: Industry?) {
        repository.saveIndustry(industry)
    }

    suspend fun getSavedIndustry(): Industry? {
        return repository.getSavedIndustry()
    }

    suspend fun saveSalary(salary: Int?) {
        repository.saveSalary(salary)
    }

    suspend fun getSavedSalary(): Int? {
        return repository.getSavedSalary()
    }

    suspend fun saveOnlyWithSalary(onlyWithSalary: Boolean) {
        repository.saveOnlyWithSalary(onlyWithSalary)
    }

    suspend fun getSavedOnlyWithSalary(): Boolean {
        return repository.getOnlyWithSalary()
    }

    suspend fun clearAllFilters() {
        repository.clearAllFilters()
    }

    // Новый метод для получения всех настроек сразу
    suspend fun getFilterSettings(): FilterSettings {
        return repository.getFilterSettings()
    }
}
