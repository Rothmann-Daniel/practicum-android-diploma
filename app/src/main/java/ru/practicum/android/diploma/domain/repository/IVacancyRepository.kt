package ru.practicum.android.diploma.domain.repository

import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchRequest
import ru.practicum.android.diploma.domain.models.VacancySearchResult

interface IVacancyRepository {
    suspend fun getVacancies(request: VacancySearchRequest): DomainResult<VacancySearchResult>
    suspend fun getVacancyById(id: String): DomainResult<Vacancy>
    suspend fun getLocalVacancies(): List<Vacancy>
    suspend fun getLocalVacancyById(id: String): Vacancy?
}
