package ru.practicum.android.diploma.data.api.mappers

import ru.practicum.android.diploma.data.api.response.AreaResponse
import ru.practicum.android.diploma.domain.models.Area

class AreaMapper {
    fun toDomain(response: AreaResponse): Area {
        return Area(
            id = response.id,
            name = response.name,
            parentId = response.parentId,
            areas = response.areas.map { toDomain(it) }
        )
    }

    fun toEntity() {
        // fun toEntity(domain: Area): AreaEntity
    }

    fun toDomain() {
        // fun toDomain(entity: AreaEntity): Area
    }
}
