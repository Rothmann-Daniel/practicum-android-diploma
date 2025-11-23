package ru.practicum.android.diploma.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.practicum.android.diploma.data.local.dao.AreaDao
import ru.practicum.android.diploma.data.local.dao.IndustryDao
import ru.practicum.android.diploma.data.local.dao.VacancyDao
import ru.practicum.android.diploma.data.local.entities.AreaEntity
import ru.practicum.android.diploma.data.local.entities.IndustryEntity
import ru.practicum.android.diploma.data.local.entities.VacancyEntity

@Database(
    entities = [AreaEntity::class, IndustryEntity::class, VacancyEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao
    abstract fun industryDao(): IndustryDao
    abstract fun vacancyDao(): VacancyDao

    companion object {
        const val DATABASE_NAME = "practicum_db"
    }
}
