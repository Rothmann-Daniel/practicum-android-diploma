package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.domain.models.Vacancy

interface IFavoriteRepository {
    suspend fun addVacancyToFavorites(vacancy: Vacancy)
    suspend fun deleteVacancyFromFavorites(id: String)
    suspend fun getFavoriteVacancies(): List<Vacancy>
    suspend fun getFavoriteVacancyById(id: String): Vacancy?
    suspend fun checkIsVacancyInFavoritesById(id: String): Boolean
}
