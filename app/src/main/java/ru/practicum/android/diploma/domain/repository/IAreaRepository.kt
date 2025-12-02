package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.models.DomainResult

interface IAreaRepository {
    suspend fun getAreas(): DomainResult<List<Area>>
    suspend fun getLocalAreas(): List<Area>
}
