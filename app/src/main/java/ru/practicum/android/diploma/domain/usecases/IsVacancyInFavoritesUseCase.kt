package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.repository.IFavoriteRepository

class IsVacancyInFavoritesUseCase(private val repository: IFavoriteRepository) {

    suspend operator fun invoke(id: String): Boolean {
        return repository.checkIsVacancyInFavoritesById(id)
    }
}
