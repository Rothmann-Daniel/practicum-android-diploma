package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.data.api.request.VacancyRequest
import ru.practicum.android.diploma.data.api.response.ApiResponse
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchResult

interface IVacancyRepository {
    suspend fun getVacancies(request: VacancyRequest): ApiResponse<VacancySearchResult>
    suspend fun getVacancyById(id: String): ApiResponse<Vacancy>
    suspend fun getLocalVacancies(): List<Vacancy>
    suspend fun getLocalVacancyById(id: String): Vacancy?
}
