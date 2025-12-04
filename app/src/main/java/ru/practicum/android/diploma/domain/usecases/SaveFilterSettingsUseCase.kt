package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.FilterRepository

class SaveFilterSettingsUseCase(
    private val repository: FilterRepository
) {
    suspend operator fun invoke(settings: FilterSettings) {
        repository.saveFilterSettings(settings)
    }

    // Сохраняем выбранную отрасль
    suspend fun saveIndustry(industry: Industry?) {
        repository.saveIndustry(industry)
    }

    // Получаем сохранённую отрасль
    suspend fun getSavedIndustry(): Industry? {
        return repository.getSavedIndustry()
    }
}
