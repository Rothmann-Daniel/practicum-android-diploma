package ru.practicum.android.diploma.data.repository

import ru.practicum.android.diploma.data.local.dao.VacancyInFavoritesDao
import ru.practicum.android.diploma.data.local.mapper.FavoritesLocalMapper
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.IFavoriteRepository

class FavoriteRepositoryImpl(
    private val vacancyInFavoritesDao: VacancyInFavoritesDao,
    private val favoritesLocalMapper: FavoritesLocalMapper
) : IFavoriteRepository {
    override suspend fun addVacancyToFavorites(vacancy: Vacancy) {
        vacancyInFavoritesDao.insertVacancy(favoritesLocalMapper.toEntity(vacancy))
    }

    override suspend fun deleteVacancyFromFavorites(id: String) {
        vacancyInFavoritesDao.deleteVacancyById(id)
    }

    override suspend fun getFavoriteVacancies(): List<Vacancy> {
        return vacancyInFavoritesDao
            .getAll()
            .map { favoritesLocalMapper.mapFromDb(it) }
    }

    override suspend fun getFavoriteVacancyById(id: String): Vacancy? {
        val vacancyEntity = vacancyInFavoritesDao.getVacancyById(id)
        if (vacancyEntity == null) return null
        return favoritesLocalMapper.mapFromDb(vacancyEntity)
    }

    override suspend fun checkIsVacancyInFavoritesById(id: String): Boolean {
        return id in vacancyInFavoritesDao.getIdList()
    }
}
