package ru.practicum.android.diploma.data.repository

import android.util.Log
import retrofit2.HttpException
import ru.practicum.android.diploma.data.local.dao.VacancyDao
import ru.practicum.android.diploma.data.local.mapper.VacancyLocalMapper
import ru.practicum.android.diploma.data.remote.api.ApiService
import ru.practicum.android.diploma.data.remote.dto.request.VacancyRequestDto
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.data.remote.dto.response.VacancyDetailResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.VacancyResponse
import ru.practicum.android.diploma.data.remote.mapper.VacancyRemoteMapper
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchResult
import ru.practicum.android.diploma.domain.repository.IVacancyRepository
import java.io.IOException
import java.net.SocketTimeoutException

class VacancyRepositoryImpl(
    private val apiService: ApiService,
    private val vacancyDao: VacancyDao,
    private val vacancyRemoteMapper: VacancyRemoteMapper,
    private val vacancyLocalMapper: VacancyLocalMapper
) : IVacancyRepository {

    override suspend fun getVacancies(
        request: VacancyRequestDto
    ): ApiResponse<VacancySearchResult> {
        return try {
            Log.d(TAG, "Fetching vacancies with request: $request")

            val response = apiService.getVacancies(
                area = request.area,
                industry = request.industry,
                text = request.text,
                salary = request.salary,
                page = request.page,
                onlyWithSalary = request.onlyWithSalary
            )

            logApiResponse(response)
            val vacancies = mapVacancies(response.vacancies)
            saveVacanciesToDatabase(vacancies)

            ApiResponse.Success(
                VacancySearchResult(
                    found = response.found,
                    pages = response.pages,
                    page = response.page,
                    vacancies = vacancies
                )
            )
        } catch (e: HttpException) {
            handleHttpException(e)
        } catch (e: SocketTimeoutException) {
            handleTimeoutException(e)
        } catch (e: IOException) {
            handleNetworkException(e)
        }
    }

    override suspend fun getVacancyById(id: String): ApiResponse<Vacancy> {
        return try {
            Log.d(TAG, "Fetching vacancy by id: $id")

            val response = apiService.getVacancyById(id)
            val vacancy = vacancyRemoteMapper.mapToDomain(response)

            saveVacancyToDatabase(vacancy)

            ApiResponse.Success(vacancy)
        } catch (e: HttpException) {
            handleVacancyByIdHttpException(id, e)
        } catch (e: SocketTimeoutException) {
            handleVacancyByIdTimeoutException(id, e)
        } catch (e: IOException) {
            handleVacancyByIdNetworkException(id, e)
        }
    }

    override suspend fun getLocalVacancies(): List<Vacancy> {
        return vacancyDao.getAll().map { vacancyLocalMapper.mapFromDb(it) }
    }

    override suspend fun getLocalVacancyById(id: String): Vacancy? {
        return vacancyDao.getById(id)?.let { vacancyLocalMapper.mapFromDb(it) }
    }

    private fun logApiResponse(response: VacancyResponse) {
        Log.d(TAG, "API response: found=${response.found}, pages=${response.pages}, items=${response.vacancies.size}")
    }

    private fun mapVacancies(vacancyDtos: List<VacancyDetailResponseDto>): List<Vacancy> {
        val vacancies = vacancyDtos.mapNotNull { vacancyDto ->
            try {
                vacancyRemoteMapper.mapToDomain(vacancyDto)
            } catch (e: IllegalArgumentException) {
                logMappingError(vacancyDto.id, e)
                null
            } catch (e: IllegalStateException) {
                logMappingError(vacancyDto.id, e)
                null
            }
        }
        Log.d(TAG, "Mapped ${vacancies.size} vacancies to domain models")
        return vacancies
    }

    private fun logMappingError(vacancyId: String?, e: Exception) {
        Log.e(TAG, "$ERROR_MAPPING_VACANCY $vacancyId", e)
    }

    private suspend fun saveVacanciesToDatabase(vacancies: List<Vacancy>) {
        val entities = vacancies.map { vacancyLocalMapper.toEntity(it) }
        Log.d(TAG, "Converting to ${entities.size} entities for DB")
        vacancyDao.insertAll(entities)
        Log.d(TAG, "Successfully saved ${entities.size} vacancies to DB")
    }

    private suspend fun saveVacancyToDatabase(vacancy: Vacancy) {
        vacancyDao.insertAll(listOf(vacancyLocalMapper.toEntity(vacancy)))
        Log.d(TAG, "Saved vacancy ${vacancy.id} to DB")
    }

    private fun handleHttpException(e: HttpException): ApiResponse.Error {
        val errorMessage = when (e.code()) {
            HTTP_FORBIDDEN -> ERROR_ACCESS_DENIED
            HTTP_NOT_FOUND -> ERROR_NOT_FOUND
            HTTP_INTERNAL_ERROR -> ERROR_INTERNAL_SERVER
            else -> "$ERROR_HTTP_PREFIX ${e.code()}"
        }
        Log.e(TAG, "HTTP error: $errorMessage", e)
        return ApiResponse.Error(errorMessage, e.code())
    }

    private fun handleTimeoutException(e: SocketTimeoutException): ApiResponse.Error {
        Log.e(TAG, "Timeout error", e)
        return ApiResponse.Error(ERROR_TIMEOUT, null)
    }

    private fun handleNetworkException(e: IOException): ApiResponse.Error {
        Log.e(TAG, "Network error", e)
        return ApiResponse.Error("$ERROR_NETWORK_PREFIX ${e.message}", null)
    }

    private fun handleVacancyByIdHttpException(
        id: String,
        e: HttpException
    ): ApiResponse.Error {
        val errorMessage = when (e.code()) {
            HTTP_FORBIDDEN -> ERROR_ACCESS_DENIED
            HTTP_NOT_FOUND -> ERROR_VACANCY_NOT_FOUND
            HTTP_INTERNAL_ERROR -> ERROR_INTERNAL_SERVER
            else -> "$ERROR_HTTP_PREFIX ${e.code()}"
        }
        Log.e(TAG, "HTTP error for vacancy $id: $errorMessage", e)
        return ApiResponse.Error(errorMessage, e.code())
    }

    private fun handleVacancyByIdTimeoutException(
        id: String,
        e: SocketTimeoutException
    ): ApiResponse.Error {
        Log.e(TAG, "Timeout error for vacancy $id", e)
        return ApiResponse.Error(ERROR_TIMEOUT, null)
    }

    private fun handleVacancyByIdNetworkException(
        id: String,
        e: IOException
    ): ApiResponse.Error {
        Log.e(TAG, "Network error for vacancy $id", e)
        return ApiResponse.Error("$ERROR_NETWORK_PREFIX ${e.message}", null)
    }

    companion object {
        private const val TAG = "VacancyRepository"
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_INTERNAL_ERROR = 500
        private const val ERROR_ACCESS_DENIED = "Доступ запрещён. Проверьте токен авторизации"
        private const val ERROR_NOT_FOUND = "Ресурс не найден"
        private const val ERROR_VACANCY_NOT_FOUND = "Вакансия не найдена"
        private const val ERROR_INTERNAL_SERVER = "Внутренняя ошибка сервера"
        private const val ERROR_TIMEOUT = "Превышено время ожидания ответа"
        private const val ERROR_HTTP_PREFIX = "HTTP ошибка:"
        private const val ERROR_NETWORK_PREFIX = "Ошибка сети:"
        private const val ERROR_MAPPING_VACANCY = "Error mapping vacancy"
    }
}
