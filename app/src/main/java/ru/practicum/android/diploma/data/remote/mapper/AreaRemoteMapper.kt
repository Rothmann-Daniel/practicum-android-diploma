package ru.practicum.android.diploma.data.remote.mapper

import ru.practicum.android.diploma.data.remote.dto.response.AreaResponseDto
import ru.practicum.android.diploma.domain.models.Area

class AreaRemoteMapper {
    /**
     * Преобразует ответ API в domain модель (включая вложенную структуру)
     */
    fun mapToDomain(dto: AreaResponseDto): Area {
        return Area(
            id = dto.id,
            name = dto.name,
            parentId = dto.parentId,
            areas = dto.areas.map { mapToDomain(it) }
        )
    }
}
