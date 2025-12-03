package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.IFavoriteRepository

class GetFavoriteVacanciesUseCase(private val repository: IFavoriteRepository) {

    suspend operator fun invoke(): List<Vacancy> {
        return repository.getFavoriteVacancies()
    }
}
