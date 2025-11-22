package ru.practicum.android.diploma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.practicum.android.diploma.data.local.entities.IndustryEntity

@Dao
interface IndustryDao {
    @Query("SELECT * FROM industries")
    suspend fun getAll(): List<IndustryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(industries: List<IndustryEntity>)

    @Query("DELETE FROM industries")
    suspend fun clearAll()
}
