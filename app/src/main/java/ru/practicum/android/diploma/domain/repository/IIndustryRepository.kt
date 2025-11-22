package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.data.api.response.ApiResponse
import ru.practicum.android.diploma.domain.models.Industry

interface IIndustryRepository {
    suspend fun getIndustries(): ApiResponse<List<Industry>>
    suspend fun getLocalIndustries(): List<Industry>
}
