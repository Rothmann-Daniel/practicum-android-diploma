package ru.practicum.android.diploma.data.api.response

data class AreaResponse(
    val id: Int,
    val name: String,
    val parentId: Int?,
    val areas: List<AreaResponse> = emptyList()
)
