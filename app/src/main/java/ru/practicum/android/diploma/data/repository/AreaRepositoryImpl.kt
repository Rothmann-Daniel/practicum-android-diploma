package ru.practicum.android.diploma.data.repository

import android.util.Log
import retrofit2.HttpException
import ru.practicum.android.diploma.data.local.dao.AreaDao
import ru.practicum.android.diploma.data.local.mapper.AreaLocalMapper
import ru.practicum.android.diploma.data.remote.api.ApiService
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.data.remote.mapper.AreaRemoteMapper
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.repository.IAreaRepository
import java.io.IOException
import java.net.SocketTimeoutException

class AreaRepositoryImpl(
    private val apiService: ApiService,
    private val areaDao: AreaDao,
    private val areaRemoteMapper: AreaRemoteMapper, // для API
    private val areaLocalMapper: AreaLocalMapper // для БД
) : IAreaRepository {

    override suspend fun getAreas(): ApiResponse<List<Area>> {
        return try {
            val response = apiService.getAreas()
            val areas = response.map { areaRemoteMapper.mapToDomain(it) }

            saveAreasToDatabase(areas)

            ApiResponse.Success(areas)
        } catch (e: HttpException) {
            handleHttpException(e)
        } catch (e: SocketTimeoutException) {
            handleTimeoutException(e)
        } catch (e: IOException) {
            handleNetworkException(e)
        }
    }

    override suspend fun getLocalAreas(): List<Area> {
        val flatList = areaDao.getAll()
        return areaLocalMapper.buildHierarchy(flatList)
    }

    private suspend fun saveAreasToDatabase(areas: List<Area>) {
        runCatching {
            val flatList = areas.flatMap { areaLocalMapper.flattenHierarchy(it) }
            areaDao.clearAll()
            areaDao.insertAll(flatList)
        }.onFailure { exception ->
            when (exception) {
                is IOException -> Log.w(TAG, ERROR_SAVING_AREAS, exception)
                is IllegalStateException -> Log.w(TAG, ERROR_DATABASE_STATE, exception)
                else -> throw exception
            }
        }
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

    companion object {
        private const val TAG = "AreaRepository"
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_INTERNAL_ERROR = 500
        private const val ERROR_ACCESS_DENIED = "Доступ запрещён. Проверьте токен авторизации"
        private const val ERROR_NOT_FOUND = "Ресурс не найден"
        private const val ERROR_INTERNAL_SERVER = "Внутренняя ошибка сервера"
        private const val ERROR_TIMEOUT = "Превышено время ожидания ответа"
        private const val ERROR_HTTP_PREFIX = "HTTP ошибка:"
        private const val ERROR_NETWORK_PREFIX = "Ошибка сети:"
        private const val ERROR_SAVING_AREAS = "Error saving areas"
        private const val ERROR_DATABASE_STATE = "Database state error"
    }
}
