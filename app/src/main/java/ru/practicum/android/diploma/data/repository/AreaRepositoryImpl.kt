package ru.practicum.android.diploma.data.repository

import android.util.Log
import ru.practicum.android.diploma.core.error.LogCategory
import ru.practicum.android.diploma.core.error.executeApiCall
import ru.practicum.android.diploma.core.error.executeDatabaseOperation
import ru.practicum.android.diploma.core.utils.InternetConnectionChecker
import ru.practicum.android.diploma.data.local.dao.AreaDao
import ru.practicum.android.diploma.data.local.mapper.AreaLocalMapper
import ru.practicum.android.diploma.data.remote.api.ApiService
import ru.practicum.android.diploma.data.remote.mapper.AreaRemoteMapper
import ru.practicum.android.diploma.data.remote.mapper.DomainResultMapper
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.repository.IAreaRepository

class AreaRepositoryImpl(
    private val apiService: ApiService,
    private val areaDao: AreaDao,
    private val areaRemoteMapper: AreaRemoteMapper,
    private val areaLocalMapper: AreaLocalMapper,
    private val internetConnectionChecker: InternetConnectionChecker
) : IAreaRepository {

    // Изменено: возвращаем DomainResult вместо ApiResponse
    override suspend fun getAreas(): DomainResult<List<Area>> {
        return DomainResultMapper.mapToDomainResult(
            executeApiCall(
                category = LogCategory.AREA,
                onNoInternet = {
                    try {
                        !internetConnectionChecker.isConnected()
                    } catch (e: SecurityException) {
                        Log.w(LogCategory.AREA.tag, "No ACCESS_NETWORK_STATE permission", e)
                        true
                    }
                }
            ) {
                val response = apiService.getAreas()
                val areas = response.map { areaRemoteMapper.mapToDomain(it) }
                saveAreasToDatabase(areas)
                areas
            }
        )
    }

    override suspend fun getLocalAreas(): List<Area> {
        val flatList = areaDao.getAll()
        return areaLocalMapper.buildHierarchy(flatList)
    }

    private suspend fun saveAreasToDatabase(areas: List<Area>) {
        executeDatabaseOperation(
            category = LogCategory.AREA,
            operationName = "saving areas"
        ) {
            val flatList = areas.flatMap { areaLocalMapper.flattenHierarchy(it) }
            areaDao.clearAll()
            areaDao.insertAll(flatList)
        }
    }
}
