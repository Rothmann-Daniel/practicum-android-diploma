package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchRequest
import ru.practicum.android.diploma.domain.models.VacancySearchResult

interface IVacancyRepository {

    // теперь принимает доменную модель запроса и domain не импортирует DTO
    suspend fun getVacancies(request: VacancySearchRequest): ApiResponse<VacancySearchResult>
    suspend fun getVacancyById(id: String): ApiResponse<Vacancy>
    suspend fun getLocalVacancies(): List<Vacancy>
    suspend fun getLocalVacancyById(id: String): Vacancy?
}
