package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.IVacancyRepository

class GetCachedVacanciesUseCase(
    private val repository: IVacancyRepository
) {
    suspend operator fun invoke(): List<Vacancy> {
        return repository.getLocalVacancies()
    }
}
