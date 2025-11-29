package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.IVacancyRepository

class GetFavoriteVacanciesUseCase(private val repository: IVacancyRepository) {

    suspend operator fun invoke(): List<Vacancy> {
        return repository.getFavoriteVacancies()
    }
}
