package ru.practicum.android.diploma.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class FilterIndustryResponseDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)
