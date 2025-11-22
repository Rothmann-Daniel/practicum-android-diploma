package ru.practicum.android.diploma.data.api.response

data class FilterAreaResponse(
    val id: Int,
    val name: String,
    val parentId: Int?,
    val areas: List<FilterAreaResponse> = emptyList()
)
