package ru.practicum.android.diploma.data.repository

import android.util.Log
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
import retrofit2.HttpException

class VacancyRepositoryImpl(
    private val apiService: ApiService,
    private val vacancyDao: VacancyDao,
    private val vacancyMapper: VacancyMapper
) : IVacancyRepository {

    companion object {
        private const val TAG = "VacancyRepository"
    }

    override suspend fun getVacancies(request: VacancyRequest): ApiResponse<VacancySearchResult> {
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

            Log.d(TAG, "API response: found=${response.found}, pages=${response.pages}, items=${response.vacancies.size}")

            // Преобразуем в domain модели
            val vacancies = response.vacancies.map { vacancyDto ->
                try {
                    vacancyMapper.toDomain(vacancyDto)
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping vacancy ${vacancyDto.id}: ${e.message}", e)
                    throw e
                }
            }

            Log.d(TAG, "Mapped ${vacancies.size} vacancies to domain models")

            // Сохраняем в локальную БД
            try {
                val entities = vacancies.map { vacancyMapper.toEntity(it) }
                Log.d(TAG, "Converting to ${entities.size} entities for DB")

                vacancyDao.insertAll(entities)
                Log.d(TAG, "Successfully saved ${entities.size} vacancies to DB")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving to database: ${e.message}", e)
                // Не прерываем выполнение, просто логируем ошибку БД
            }

            val result = VacancySearchResult(
                found = response.found,
                pages = response.pages,
                page = response.page,
                vacancies = vacancies
            )

            ApiResponse.Success(result)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                403 -> "Доступ запрещён. Проверьте токен авторизации"
                404 -> "Ресурс не найден"
                500 -> "Внутренняя ошибка сервера"
                else -> "HTTP ошибка: ${e.code()}"
            }
            Log.e(TAG, "HTTP error: $errorMessage", e)
            ApiResponse.Error(errorMessage, e.code())
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout error", e)
            ApiResponse.Error("Превышено время ожидания ответа", null)
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            ApiResponse.Error("Ошибка сети: ${e.message}", null)
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error", e)
            ApiResponse.Error("Неизвестная ошибка: ${e.message}", null)
        }
    }

    override suspend fun getVacancyById(id: String): ApiResponse<Vacancy> {
        return try {
            Log.d(TAG, "Fetching vacancy by id: $id")

            val response = apiService.getVacancyById(id)
            val vacancy = vacancyMapper.toDomain(response)

            // Сохраняем в локальную БД
            try {
                vacancyDao.insertAll(listOf(vacancyMapper.toEntity(vacancy)))
                Log.d(TAG, "Saved vacancy $id to DB")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving vacancy to database: ${e.message}", e)
            }

            ApiResponse.Success(vacancy)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                403 -> "Доступ запрещён"
                404 -> "Вакансия не найдена"
                500 -> "Внутренняя ошибка сервера"
                else -> "HTTP ошибка: ${e.code()}"
            }
            Log.e(TAG, "HTTP error for vacancy $id: $errorMessage", e)
            ApiResponse.Error(errorMessage, e.code())
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout error for vacancy $id", e)
            ApiResponse.Error("Превышено время ожидания ответа", null)
        } catch (e: IOException) {
            Log.e(TAG, "Network error for vacancy $id", e)
            ApiResponse.Error("Ошибка сети: ${e.message}", null)
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error for vacancy $id", e)
            ApiResponse.Error("Неизвестная ошибка: ${e.message}", null)
        }
    }

    override suspend fun getLocalVacancies(): List<Vacancy> {
        return try {
            vacancyDao.getAll().map { vacancyMapper.toDomain(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local vacancies", e)
            emptyList()
        }
    }

    override suspend fun getLocalVacancyById(id: String): Vacancy? {
        return try {
            vacancyDao.getById(id)?.let { vacancyMapper.toDomain(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local vacancy $id", e)
            null
        }
    }
}
