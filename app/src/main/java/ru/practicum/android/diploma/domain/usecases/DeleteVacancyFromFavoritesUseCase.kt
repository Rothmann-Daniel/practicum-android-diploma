package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.repository.IFavoriteRepository

class DeleteVacancyFromFavoritesUseCase(private val repository: IFavoriteRepository) {

    suspend operator fun invoke(id: String) {
        repository.deleteVacancyFromFavorites(id)
    }
}
