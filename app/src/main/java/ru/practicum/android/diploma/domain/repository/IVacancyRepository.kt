package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.data.remote.dto.request.VacancyRequestDto
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchResult

interface IVacancyRepository {
    suspend fun getVacancies(request: VacancyRequestDto): ApiResponse<VacancySearchResult>
    suspend fun getVacancyById(id: String): ApiResponse<Vacancy>
    suspend fun getLocalVacancies(): List<Vacancy>
    suspend fun getLocalVacancyById(id: String): Vacancy?
}
