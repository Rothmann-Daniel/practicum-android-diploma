package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.IVacancyRepository

class AddVacancyToFavoritesUseCase(private val repository: IVacancyRepository) {

    suspend operator fun invoke(vacancy: Vacancy) {
        repository.addVacancyToFavorites(vacancy)
    }
}
