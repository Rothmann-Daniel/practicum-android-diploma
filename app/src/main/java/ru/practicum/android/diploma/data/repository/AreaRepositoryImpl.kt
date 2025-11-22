package ru.practicum.android.diploma.data.repository

import ru.practicum.android.diploma.data.api.ApiService
import ru.practicum.android.diploma.data.api.mappers.AreaMapper
import ru.practicum.android.diploma.data.api.response.ApiResponse
import ru.practicum.android.diploma.data.local.dao.AreaDao
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.repository.IAreaRepository
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

class AreaRepositoryImpl(
    private val apiService: ApiService,
    private val areaDao: AreaDao,
    private val areaMapper: AreaMapper
) : IAreaRepository {

    override suspend fun getAreas(): ApiResponse<List<Area>> {
        return try {
            val response = apiService.getAreas()
            val areas = response.map { areaMapper.toDomain(it) }

            // Сохраняем всю иерархию в плоском виде
            val flatList = areas.flatMap { areaMapper.flattenHierarchy(it) }
            areaDao.clearAll()
            areaDao.insertAll(flatList)

            ApiResponse.Success(areas)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                403 -> "Доступ запрещён. Проверьте токен авторизации"
                404 -> "Ресурс не найден"
                500 -> "Внутренняя ошибка сервера"
                else -> "HTTP ошибка: ${e.code()}"
            }
            ApiResponse.Error(errorMessage, e.code())
        } catch (e: SocketTimeoutException) {
            ApiResponse.Error("Превышено время ожидания ответа", null)
        } catch (e: IOException) {
            ApiResponse.Error("Ошибка сети: ${e.message}", null)
        } catch (e: Exception) {
            ApiResponse.Error("Неизвестная ошибка: ${e.message}", null)
        }
    }

    override suspend fun getLocalAreas(): List<Area> {
        val flatList = areaDao.getAll()
        return areaMapper.buildHierarchy(flatList)
    }
}
