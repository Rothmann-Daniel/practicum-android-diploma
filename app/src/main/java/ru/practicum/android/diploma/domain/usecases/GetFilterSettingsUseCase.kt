package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.repository.FilterRepository

class GetFilterSettingsUseCase(
    private val repository: FilterRepository
) {
    suspend operator fun invoke(): FilterSettings {
        return repository.getFilterSettings()
    }
}
