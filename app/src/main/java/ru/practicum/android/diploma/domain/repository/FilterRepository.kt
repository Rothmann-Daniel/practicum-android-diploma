package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.models.Industry

interface FilterRepository {
    suspend fun saveIndustry(industry: Industry?)
    suspend fun getSavedIndustry(): Industry?

    suspend fun saveSalary(salary: Int?)
    suspend fun getSavedSalary(): Int?

    suspend fun saveOnlyWithSalary(onlyWithSalary: Boolean)
    suspend fun getOnlyWithSalary(): Boolean

    suspend fun clearAllFilters()

    suspend fun getFilterSettings(): FilterSettings
}


