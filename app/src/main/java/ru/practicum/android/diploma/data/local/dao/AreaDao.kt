package ru.practicum.android.diploma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.practicum.android.diploma.data.local.entities.AreaEntity

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas")
    suspend fun getAll(): List<AreaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(areas: List<AreaEntity>)

    @Query("DELETE FROM areas")
    suspend fun clearAll()
}
