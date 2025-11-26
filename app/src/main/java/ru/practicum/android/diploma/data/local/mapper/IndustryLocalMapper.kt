package ru.practicum.android.diploma.data.local.mapper

import ru.practicum.android.diploma.data.local.entities.IndustryEntity
import ru.practicum.android.diploma.domain.models.Industry

class IndustryLocalMapper {
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
