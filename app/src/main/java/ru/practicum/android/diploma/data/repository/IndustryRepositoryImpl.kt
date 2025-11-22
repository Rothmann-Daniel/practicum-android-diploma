package ru.practicum.android.diploma.data.repository

import ru.practicum.android.diploma.data.api.ApiService
import ru.practicum.android.diploma.data.api.mappers.IndustryMapper
import ru.practicum.android.diploma.data.api.response.ApiResponse
import ru.practicum.android.diploma.data.local.dao.IndustryDao
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.IIndustryRepository
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

class IndustryRepositoryImpl(
    private val apiService: ApiService,
    private val industryDao: IndustryDao,
    private val industryMapper: IndustryMapper
) : IIndustryRepository {

    override suspend fun getIndustries(): ApiResponse<List<Industry>> {
        return try {
            val response = apiService.getIndustries()
            val industries = response.map { industryMapper.toDomain(it) }

            // Сохраняем в локальную БД
            industryDao.clearAll()
            industryDao.insertAll(industries.map { industryMapper.toEntity(it) })

            ApiResponse.Success(industries)
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

    override suspend fun getLocalIndustries(): List<Industry> {
        return industryDao.getAll().map { industryMapper.toDomain(it) }
    }
}
