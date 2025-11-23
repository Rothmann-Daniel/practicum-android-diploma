package ru.practicum.android.diploma.data.repository

import android.util.Log
import retrofit2.HttpException
import ru.practicum.android.diploma.data.api.ApiService
import ru.practicum.android.diploma.data.api.mappers.IndustryMapper
import ru.practicum.android.diploma.data.api.response.ApiResponse
import ru.practicum.android.diploma.data.local.dao.IndustryDao
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.IIndustryRepository
import java.io.IOException
import java.net.SocketTimeoutException

class IndustryRepositoryImpl(
    private val apiService: ApiService,
    private val industryDao: IndustryDao,
    private val industryMapper: IndustryMapper
) : IIndustryRepository {

    override suspend fun getIndustries(): ApiResponse<List<Industry>> {
        return try {
            val response = apiService.getIndustries()
            val industries = response.map { industryMapper.toDomain(it) }

            saveIndustriesToDatabase(industries)

            ApiResponse.Success(industries)
        } catch (e: HttpException) {
            handleHttpException(e)
        } catch (e: SocketTimeoutException) {
            handleTimeoutException()
        } catch (e: IOException) {
            handleNetworkException(e)
        }
    }

    override suspend fun getLocalIndustries(): List<Industry> {
        return industryDao.getAll().map { industryMapper.toDomain(it) }
    }

    private suspend fun saveIndustriesToDatabase(industries: List<Industry>) {
        try {
            industryDao.clearAll()
            industryDao.insertAll(industries.map { industryMapper.toEntity(it) })
        } catch (e: IOException) {
            Log.e(TAG, "Error saving industries to database", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Database state error", e)
        }
    }

    private fun handleHttpException(e: HttpException): ApiResponse.Error {
        val errorMessage = when (e.code()) {
            HTTP_FORBIDDEN -> ERROR_ACCESS_DENIED
            HTTP_NOT_FOUND -> ERROR_NOT_FOUND
            HTTP_INTERNAL_ERROR -> ERROR_INTERNAL_SERVER
            else -> "HTTP ошибка: ${e.code()}"
        }
        return ApiResponse.Error(errorMessage, e.code())
    }

    private fun handleTimeoutException(): ApiResponse.Error {
        return ApiResponse.Error(ERROR_TIMEOUT, null)
    }

    private fun handleNetworkException(e: IOException): ApiResponse.Error {
        return ApiResponse.Error("Ошибка сети: ${e.message}", null)
    }

    companion object {
        private const val TAG = "IndustryRepository"
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_INTERNAL_ERROR = 500
        private const val ERROR_ACCESS_DENIED = "Доступ запрещён. Проверьте токен авторизации"
        private const val ERROR_NOT_FOUND = "Ресурс не найден"
        private const val ERROR_INTERNAL_SERVER = "Внутренняя ошибка сервера"
        private const val ERROR_TIMEOUT = "Превышено время ожидания ответа"
    }
}
