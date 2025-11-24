package ru.practicum.android.diploma.data.api.mappers

import ru.practicum.android.diploma.data.api.response.FilterIndustryResponse
import ru.practicum.android.diploma.data.local.entities.IndustryEntity
import ru.practicum.android.diploma.domain.models.Industry

class IndustryMapper {
    fun mapToDomain(response: FilterIndustryResponse): Industry {
        return Industry(
            id = response.id,
            name = response.name
        )
    }

    fun toEntity(domain: Industry): IndustryEntity {
        return IndustryEntity(
            id = domain.id,
            name = domain.name
        )
    }

    fun mapFromDb(entity: IndustryEntity): Industry {
        return Industry(
            id = entity.id,
            name = entity.name
        )
    }
}
