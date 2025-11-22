package ru.practicum.android.diploma.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "industries")
data class IndustryEntity(
    @PrimaryKey
    val id: Int,
    val name: String
)
