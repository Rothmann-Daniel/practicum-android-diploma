package ru.practicum.android.diploma.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class VacancyInFavoritesEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val salaryFrom: Int?,
    val salaryTo: Int?,
    val salaryCurrency: String?,
    val address: String?,
    val city: String?, // Добавил, чтобы отображался город в списке
    val experienceId: String?,
    val experienceName: String?,
    val scheduleId: String?,
    val scheduleName: String?,
    val employmentId: String?,
    val employmentName: String?,
    val employerId: String,
    val employerName: String,
    val employerLogo: String?, // Изменено на nullable
    val areaId: Int,
    val areaName: String,
    val skills: String, // Пустая строка по умолчанию, не null
    val url: String,
    val industryId: Int?,
    val industryName: String?
)
