package ru.practicum.android.diploma.data.api.mappers

import ru.practicum.android.diploma.data.api.response.FilterIndustryResponse
import ru.practicum.android.diploma.domain.models.Industry

class IndustryMapper {
    fun toDomain(response: FilterIndustryResponse): Industry {
        return Industry(
            id = response.id,
            name = response.name
        )
    }

    fun toEntity(){
        // fun toEntity(domain: Industry): IndustryEntity
    }

    fun toDomain(){
        // fun toDomain(entity: IndustryEntity): Industry
    }
}
