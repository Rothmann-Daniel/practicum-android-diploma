package ru.practicum.android.diploma.data.repository

import android.util.Log
import retrofit2.HttpException
import ru.practicum.android.diploma.data.api.ApiService
import ru.practicum.android.diploma.data.api.mappers.VacancyMapper
import ru.practicum.android.diploma.data.api.request.VacancyRequest
import ru.practicum.android.diploma.data.api.response.ApiResponse
import ru.practicum.android.diploma.data.local.dao.VacancyDao
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancySearchResult
import ru.practicum.android.diploma.domain.repository.IVacancyRepository
import java.io.IOException
import java.net.SocketTimeoutException

class VacancyRepositoryImpl(
    private val apiService: ApiService,
    private val vacancyDao: VacancyDao,
    private val vacancyMapper: VacancyMapper
) : IVacancyRepository {

    override suspend fun getVacancies(
        request: VacancyRequest
    ): ApiResponse<VacancySearchResult> {
        return try {
            Log.d(TAG, "Fetching vacancies with request: $request")

            val response = fetchVacanciesFromApi(request)
            Log.d(
                TAG,
                "API response: found=${response.found}, pages=${response.pages}, " +
                    "items=${response.vacancies.size}"
            )

            val vacancies = mapVacanciesResponse(response.vacancies)
            Log.d(TAG, "Mapped ${vacancies.size} vacancies to domain models")

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
            val vacancy = vacancyMapper.toDomain(response)

            saveVacancyToDatabase(vacancy)

            ApiResponse.Success(vacancy)
        } catch (e: HttpException) {
            handleVacancyByIdHttpException(id, e)
        } catch (e: SocketTimeoutException) {
            handleVacancyByIdTimeoutException(id)
        } catch (e: IOException) {
            handleVacancyByIdNetworkException(id, e)
        }
    }

    override suspend fun getLocalVacancies(): List<Vacancy> {
        val result = runCatching {
            vacancyDao.getAll().map { vacancyMapper.toDomain(it) }
        }

        return result.getOrElse { exception ->
            when (exception) {
                is IOException -> {
                    Log.e(TAG, "Error loading local vacancies: ${exception.message}", exception)
                    EMPTY_VACANCY_LIST
                }
                is IllegalStateException -> {
                    Log.e(TAG, "Database state error: ${exception.message}", exception)
                    EMPTY_VACANCY_LIST
                }
                else -> throw exception
            }
        }
    }

    override suspend fun getLocalVacancyById(id: String): Vacancy? {
        val result = runCatching {
            vacancyDao.getById(id)?.let { vacancyMapper.toDomain(it) }
        }

        return result.getOrElse { exception ->
            when (exception) {
                is IOException -> {
                    Log.e(TAG, "Error loading local vacancy $id: ${exception.message}", exception)
                    null
                }
                is IllegalStateException -> {
                    Log.e(TAG, "Database state error for $id: ${exception.message}", exception)
                    null
                }
                else -> throw exception
            }
        }
    }

    private suspend fun fetchVacanciesFromApi(
        request: VacancyRequest
    ) = apiService.getVacancies(
        area = request.area,
        industry = request.industry,
        text = request.text,
        salary = request.salary,
        page = request.page,
        onlyWithSalary = request.onlyWithSalary
    )

    private fun mapVacanciesResponse(
        vacanciesDto: List<ru.practicum.android.diploma.data.api.response.VacancyDetailResponse>
    ): List<Vacancy> {
        return vacanciesDto.map { vacancyDto ->
            try {
                vacancyMapper.toDomain(vacancyDto)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Error mapping vacancy ${vacancyDto.id}: ${e.message}", e)
                throw e
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Error mapping vacancy ${vacancyDto.id}: ${e.message}", e)
                throw e
            }
        }
    }

    private suspend fun saveVacanciesToDatabase(vacancies: List<Vacancy>) {
        runCatching {
            val entities = vacancies.map { vacancyMapper.toEntity(it) }
            Log.d(TAG, "Converting to ${entities.size} entities for DB")
            vacancyDao.insertAll(entities)
            Log.d(TAG, "Successfully saved ${entities.size} vacancies to DB")
        }.onFailure { exception ->
            when (exception) {
                is IOException -> Log.w(TAG, "Error saving to database: ${exception.message}", exception)
                is IllegalStateException -> Log.w(TAG, "Database state error: ${exception.message}", exception)
                else -> throw exception
            }
        }
    }

    private suspend fun saveVacancyToDatabase(vacancy: Vacancy) {
        runCatching {
            vacancyDao.insertAll(listOf(vacancyMapper.toEntity(vacancy)))
            Log.d(TAG, "Saved vacancy ${vacancy.id} to DB")
        }.onFailure { exception ->
            when (exception) {
                is IOException -> Log.w(TAG, "Error saving vacancy: ${exception.message}", exception)
                is IllegalStateException -> Log.w(TAG, "Database state error: ${exception.message}", exception)
                else -> throw exception
            }
        }
    }

    private fun handleHttpException(e: HttpException): ApiResponse.Error {
        val errorMessage = when (e.code()) {
            HTTP_FORBIDDEN -> ERROR_ACCESS_DENIED
            HTTP_NOT_FOUND -> ERROR_NOT_FOUND
            HTTP_INTERNAL_ERROR -> ERROR_INTERNAL_SERVER
            else -> "HTTP ошибка: ${e.code()}"
        }
        Log.e(TAG, "HTTP error: $errorMessage", e)
        return ApiResponse.Error(errorMessage, e.code())
    }

    private fun handleTimeoutException(
        e: SocketTimeoutException
    ): ApiResponse.Error {
        Log.e(TAG, "Timeout error", e)
        return ApiResponse.Error(ERROR_TIMEOUT, null)
    }

    private fun handleNetworkException(e: IOException): ApiResponse.Error {
        Log.e(TAG, "Network error", e)
        return ApiResponse.Error("Ошибка сети: ${e.message}", null)
    }

    private fun handleVacancyByIdHttpException(
        id: String,
        e: HttpException
    ): ApiResponse.Error {
        val errorMessage = when (e.code()) {
            HTTP_FORBIDDEN -> ERROR_ACCESS_DENIED
            HTTP_NOT_FOUND -> ERROR_VACANCY_NOT_FOUND
            HTTP_INTERNAL_ERROR -> ERROR_INTERNAL_SERVER
            else -> "HTTP ошибка: ${e.code()}"
        }
        Log.e(TAG, "HTTP error for vacancy $id: $errorMessage", e)
        return ApiResponse.Error(errorMessage, e.code())
    }

    private fun handleVacancyByIdTimeoutException(
        id: String
    ): ApiResponse.Error {
        Log.e(TAG, "Timeout error for vacancy $id")
        return ApiResponse.Error(ERROR_TIMEOUT, null)
    }

    private fun handleVacancyByIdNetworkException(
        id: String,
        e: IOException
    ): ApiResponse.Error {
        Log.e(TAG, "Network error for vacancy $id", e)
        return ApiResponse.Error("Ошибка сети: ${e.message}", null)
    }

    companion object {
        private const val TAG = "VacancyRepository"

        // HTTP коды ошибок
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_INTERNAL_ERROR = 500

        // Сообщения об ошибках
        private const val ERROR_ACCESS_DENIED = "Доступ запрещён. Проверьте токен авторизации"
        private const val ERROR_NOT_FOUND = "Ресурс не найден"
        private const val ERROR_VACANCY_NOT_FOUND = "Вакансия не найдена"
        private const val ERROR_INTERNAL_SERVER = "Внутренняя ошибка сервера"
        private const val ERROR_TIMEOUT = "Превышено время ожидания ответа"

        // Fallback значения
        private val EMPTY_VACANCY_LIST = emptyList<Vacancy>()
    }
}
