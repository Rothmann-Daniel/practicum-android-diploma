package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.IFavoriteRepository

class GetFavoriteVacancyByIdUseCase(private val repository: IFavoriteRepository) {

    suspend operator fun invoke(id: String): Vacancy? {
        return repository.getFavoriteVacancyById(id)
    }
}
