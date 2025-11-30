package ru.practicum.android.diploma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.practicum.android.diploma.data.local.entities.VacancyInFavoritesEntity

@Dao
interface VacancyInFavoritesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacancy(vacancy: VacancyInFavoritesEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteVacancyById(id: String)

    @Query("SELECT * FROM favorites")
    suspend fun getAll(): List<VacancyInFavoritesEntity>

    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getVacancyById(id: String): VacancyInFavoritesEntity?

    @Query("SELECT id FROM favorites")
    suspend fun getIdList(): List<String>
}

