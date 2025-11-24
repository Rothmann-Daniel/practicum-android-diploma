package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.domain.models.Area

interface IAreaRepository {
    suspend fun getAreas(): ApiResponse<List<Area>>
    suspend fun getLocalAreas(): List<Area>
}
