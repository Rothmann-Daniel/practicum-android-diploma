package ru.practicum.android.diploma.domain.models

data class Area(
    val id: Int,
    val name: String,
    val parentId: Int?,
    val areas: List<Area> = emptyList()
)
