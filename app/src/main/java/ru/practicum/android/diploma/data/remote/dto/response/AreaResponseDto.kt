package ru.practicum.android.diploma.data.remote.dto.response

data class AreaResponseDto(
    val id: Int,
    val name: String,
    val parentId: Int?,
    val areas: List<AreaResponseDto> = emptyList()
)
