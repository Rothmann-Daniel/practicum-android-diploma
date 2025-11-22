package ru.practicum.android.diploma.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "areas")
data class AreaEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val parentId: Int?
)
