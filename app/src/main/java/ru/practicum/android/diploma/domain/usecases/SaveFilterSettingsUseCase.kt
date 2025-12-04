package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.repository.FilterRepository

class SaveFilterSettingsUseCase(
    private val repository: FilterRepository
) {
    suspend operator fun invoke(settings: FilterSettings) {
        repository.saveIndustry(settings.industry)
        repository.saveSalary(settings.salary)
        repository.saveOnlyWithSalary(settings.onlyWithSalary)
    }
}
