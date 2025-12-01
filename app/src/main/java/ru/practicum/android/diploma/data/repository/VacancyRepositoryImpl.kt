package ru.practicum.android.diploma.data.repository

import android.util.Log
import ru.practicum.android.diploma.core.error.ApiError
import ru.practicum.android.diploma.core.error.LogCategory
import ru.practicum.android.diploma.core.error.executeApiCall
import ru.practicum.android.diploma.core.error.log
import ru.practicum.android.diploma.core.utils.InternetConnectionChecker
import ru.practicum.android.diploma.data.local.dao.VacancyDao
import ru.practicum.android.diploma.data.local.mapper.VacancyLocalMapper
import ru.practicum.android.diploma.data.remote.api.ApiService
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.data.remote.dto.response.VacancyDetailResponseDto
import ru.practicum.android.diploma.data.remote.mapper.VacancyRemoteMapper
import ru.practicum.android.diploma.data.remote.mapper.VacancyRequestMapper
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchRequest
import ru.practicum.android.diploma.domain.models.VacancySearchResult
import ru.practicum.android.diploma.domain.repository.IVacancyRepository

class VacancyRepositoryImpl(
    private val apiService: ApiService,
    private val vacancyDao: VacancyDao,
    private val vacancyRemoteMapper: VacancyRemoteMapper,
    private val vacancyLocalMapper: VacancyLocalMapper,
    private val vacancyRequestMapper: VacancyRequestMapper,
    private val internetConnectionChecker: InternetConnectionChecker
) : IVacancyRepository {

    override suspend fun getVacancies(
        request: VacancySearchRequest
    ): ApiResponse<VacancySearchResult> {
        return executeApiCall(
            category = LogCategory.VACANCY,
            onNoInternet = { !internetConnectionChecker.isConnected() }
        ) {
            Log.d(LogCategory.VACANCY.tag, "Fetching vacancies with request: $request")
            val dtoRequest = vacancyRequestMapper.toDto(request)

            val response = apiService.getVacancies(
                area = dtoRequest.area,
                industry = dtoRequest.industry,
                text = dtoRequest.text,
                salary = dtoRequest.salary,
                page = dtoRequest.page,
                onlyWithSalary = dtoRequest.onlyWithSalary
            )

            Log.d(
                LogCategory.VACANCY.tag,
                "API response: found=${response.found}, pages=${response.pages}, items=${response.vacancies.size}"
            )

            val vacancies = mapVacancies(response.vacancies)
            saveVacanciesToDatabase(vacancies)

            VacancySearchResult(
                found = response.found,
                pages = response.pages,
                page = response.page,
                vacancies = vacancies
            )
        }
    }

    override suspend fun getVacancyById(id: String): ApiResponse<Vacancy> {
        return executeApiCall(
            category = LogCategory.VACANCY,
            onNoInternet = { !internetConnectionChecker.isConnected() }
        ) {
            Log.d(LogCategory.VACANCY.tag, "Fetching vacancy by id: $id")
            val response = apiService.getVacancyById(id)
            val vacancy = vacancyRemoteMapper.mapToDomain(response)
            saveVacancyToDatabase(vacancy)
            vacancy
        }
    }

    override suspend fun getLocalVacancies(): List<Vacancy> {
        return vacancyDao.getAll().map { vacancyLocalMapper.mapFromDb(it) }
    }

    override suspend fun getLocalVacancyById(id: String): Vacancy? {
        return vacancyDao.getById(id)?.let { vacancyLocalMapper.mapFromDb(it) }
    }

    private fun mapVacancies(vacancyDtos: List<VacancyDetailResponseDto>): List<Vacancy> {
        val vacancies = vacancyDtos.mapNotNull { vacancyDto ->
            try {
                vacancyRemoteMapper.mapToDomain(vacancyDto)
            } catch (e: IllegalArgumentException) {
                ApiError.MappingError(vacancyDto.id).log(LogCategory.VACANCY, e)
                null
            } catch (e: IllegalStateException) {
                ApiError.MappingError(vacancyDto.id).log(LogCategory.VACANCY, e)
                null
            }
        }
        Log.d(LogCategory.VACANCY.tag, "Mapped ${vacancies.size} vacancies to domain models")
        return vacancies
    }

    private suspend fun saveVacanciesToDatabase(vacancies: List<Vacancy>) {
        val entities = vacancies.map { vacancyLocalMapper.toEntity(it) }
        Log.d(LogCategory.VACANCY.tag, "Converting to ${entities.size} entities for DB")
        vacancyDao.insertAll(entities)
        Log.d(LogCategory.VACANCY.tag, "Successfully saved ${entities.size} vacancies to DB")
    }

    private suspend fun saveVacancyToDatabase(vacancy: Vacancy) {
        vacancyDao.insertAll(listOf(vacancyLocalMapper.toEntity(vacancy)))
        Log.d(LogCategory.VACANCY.tag, "Saved vacancy ${vacancy.id} to DB")
    }
}
