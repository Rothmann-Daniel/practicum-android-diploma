package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.Industry

interface IIndustryRepository {
    suspend fun getIndustries(): DomainResult<List<Industry>>  // Изменено
    suspend fun getLocalIndustries(): List<Industry>
}
