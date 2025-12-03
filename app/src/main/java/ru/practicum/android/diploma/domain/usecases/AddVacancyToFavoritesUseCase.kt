package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.IFavoriteRepository

class AddVacancyToFavoritesUseCase(private val repository: IFavoriteRepository) {

    suspend operator fun invoke(vacancy: Vacancy) {
        repository.addVacancyToFavorites(vacancy)
    }
}
