package ru.practicum.android.diploma.data.remote.mapper

import ru.practicum.android.diploma.data.remote.dto.response.FilterIndustryResponseDto
import ru.practicum.android.diploma.domain.models.Industry

class IndustryRemoteMapper {
    fun mapToDomain(dto: FilterIndustryResponseDto): Industry {
        return Industry(
            id = dto.id,
            name = dto.name
        )
    }
}
