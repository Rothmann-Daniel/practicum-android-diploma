package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.repository.IVacancyRepository

class IsVacancyInFavoritesUseCase(private val repository: IVacancyRepository) {

    suspend operator fun invoke(id: String): Boolean {
        return repository.checkIsVacancyInFavoritesById(id)
    }
}
