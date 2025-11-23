package ru.practicum.android.diploma.data.repository

import android.util.Log
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException
import ru.practicum.android.diploma.data.api.ApiService
import ru.practicum.android.diploma.data.api.mappers.AreaMapper
import ru.practicum.android.diploma.data.api.response.ApiResponse
import ru.practicum.android.diploma.data.local.dao.AreaDao
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.repository.IAreaRepository

class AreaRepositoryImpl(
    private val apiService: ApiService,
    private val areaDao: AreaDao,
    private val areaMapper: AreaMapper
) : IAreaRepository {

    override suspend fun getAreas(): ApiResponse<List<Area>> {
        return try {
            val response = apiService.getAreas()
            val areas = response.map { areaMapper.toDomain(it) }

            saveAreasToDatabase(areas)

            ApiResponse.Success(areas)
        } catch (e: HttpException) {
            handleHttpException(e)
        } catch (e: SocketTimeoutException) {
            handleTimeoutException()
        } catch (e: IOException) {
            handleNetworkException(e)
        }
    }

    override suspend fun getLocalAreas(): List<Area> {
        val flatList = areaDao.getAll()
        return areaMapper.buildHierarchy(flatList)
    }

    private suspend fun saveAreasToDatabase(areas: List<Area>) {
        try {
            val flatList = areas.flatMap { areaMapper.flattenHierarchy(it) }
            areaDao.clearAll()
            areaDao.insertAll(flatList)
        } catch (e: IOException) {
            Log.e(TAG, "Error saving areas to database", e)
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
        private const val TAG = "AreaRepository"
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_INTERNAL_ERROR = 500
        private const val ERROR_ACCESS_DENIED = "Доступ запрещён. Проверьте токен авторизации"
        private const val ERROR_NOT_FOUND = "Ресурс не найден"
        private const val ERROR_INTERNAL_SERVER = "Внутренняя ошибка сервера"
        private const val ERROR_TIMEOUT = "Превышено время ожидания ответа"
    }
}
