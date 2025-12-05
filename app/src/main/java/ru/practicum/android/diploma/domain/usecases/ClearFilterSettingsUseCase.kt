package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.repository.FilterRepository

class ClearFilterSettingsUseCase(
    private val repository: FilterRepository
) {
    suspend operator fun invoke() {
        repository.clearAllFilters()
    }
}
