package ru.practicum.android.diploma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.practicum.android.diploma.data.local.entities.VacancyEntity

@Dao
interface VacancyDao {
    @Query("SELECT * FROM vacancies")
    suspend fun getAll(): List<VacancyEntity>

    @Query("SELECT * FROM vacancies WHERE id = :id")
    suspend fun getById(id: String): VacancyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vacancies: List<VacancyEntity>)

    @Query("DELETE FROM vacancies")
    suspend fun clearAll()
}
