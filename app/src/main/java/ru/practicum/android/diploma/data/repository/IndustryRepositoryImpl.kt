package ru.practicum.android.diploma.data.repository

import ru.practicum.android.diploma.core.error.LogCategory
import ru.practicum.android.diploma.core.error.executeApiCall
import ru.practicum.android.diploma.core.error.executeDatabaseOperation
import ru.practicum.android.diploma.core.utils.InternetConnectionChecker
import ru.practicum.android.diploma.data.local.dao.IndustryDao
import ru.practicum.android.diploma.data.local.mapper.IndustryLocalMapper
import ru.practicum.android.diploma.data.remote.api.ApiService
import ru.practicum.android.diploma.data.remote.mapper.DomainResultMapper
import ru.practicum.android.diploma.data.remote.mapper.IndustryRemoteMapper
import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.IIndustryRepository

class IndustryRepositoryImpl(
    private val apiService: ApiService,
    private val industryDao: IndustryDao,
    private val industryRemoteMapper: IndustryRemoteMapper,
    private val industryLocalMapper: IndustryLocalMapper,
    private val internetConnectionChecker: InternetConnectionChecker
) : IIndustryRepository {

    // Изменено: возвращаем DomainResult вместо ApiResponse
    override suspend fun getIndustries(): DomainResult<List<Industry>> {
        return DomainResultMapper.mapToDomainResult(
            executeApiCall(
                category = LogCategory.INDUSTRY,
                onNoInternet = { !internetConnectionChecker.isConnected() }
            ) {
                val response = apiService.getIndustries()
                val industries = response.map { industryRemoteMapper.mapToDomain(it) }
                saveIndustriesToDatabase(industries)
                industries
            }
        )
    }

    override suspend fun getLocalIndustries(): List<Industry> {
        return industryDao.getAll().map { industryLocalMapper.mapFromDb(it) }
    }

    private suspend fun saveIndustriesToDatabase(industries: List<Industry>) {
        executeDatabaseOperation(
            category = LogCategory.INDUSTRY,
            operationName = "saving industries"
        ) {
            industryDao.clearAll()
            industryDao.insertAll(industries.map { industryLocalMapper.toEntity(it) })
        }
    }
}
