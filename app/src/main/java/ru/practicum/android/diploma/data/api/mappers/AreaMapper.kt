package ru.practicum.android.diploma.data.api.mappers

import ru.practicum.android.diploma.data.api.response.AreaResponse
import ru.practicum.android.diploma.data.local.entities.AreaEntity
import ru.practicum.android.diploma.domain.models.Area

class AreaMapper {
    /**
     * Преобразует ответ API в domain модель (включая вложенную структуру)
     */
    fun toDomain(response: AreaResponse): Area {
        return Area(
            id = response.id,
            name = response.name,
            parentId = response.parentId,
            areas = response.areas.map { toDomain(it) }
        )
    }

    /**
     * Преобразует domain модель в entity для БД (только верхний уровень)
     */
    fun toEntity(domain: Area): AreaEntity {
        return AreaEntity(
            id = domain.id,
            name = domain.name,
            parentId = domain.parentId
        )
    }

    /**
     * Преобразует entity из БД в domain модель (без вложенных areas)
     * ВАЖНО: Для восстановления иерархии используем buildHierarchy()
     */
    fun toDomain(entity: AreaEntity): Area {
        return Area(
            id = entity.id,
            name = entity.name,
            parentId = entity.parentId,
            areas = emptyList() // Иерархия восстанавливается отдельно
        )
    }

    /**
     * Рекурсивно сохраняет всю иерархию областей в плоский список
     */
    fun flattenHierarchy(area: Area): List<AreaEntity> {
        val result = mutableListOf<AreaEntity>()
        result.add(toEntity(area))
        area.areas.forEach { childArea ->
            result.addAll(flattenHierarchy(childArea))
        }
        return result
    }

    /**
     * Восстанавливает иерархическую структуру из плоского списка
     */
    fun buildHierarchy(flatList: List<AreaEntity>): List<Area> {
        val areaMap = flatList.associate { it.id to toMutableArea(toDomain(it)) }

        areaMap.values.forEach { area ->
            area.parentId?.let { parentId ->
                areaMap[parentId]?.areas?.add(toImmutableArea(area))
            }
        }

        return areaMap.values
            .filter { it.parentId == null }
            .map { toImmutableArea(it) }
    }

    private fun toMutableArea(area: Area) = MutableArea(area.id, area.name, area.parentId, area.areas.toMutableList())

    private fun toImmutableArea(mutableArea: MutableArea) =
        Area(mutableArea.id, mutableArea.name, mutableArea.parentId, mutableArea.areas.toList())

    private class MutableArea(
        val id: Int,
        val name: String,
        val parentId: Int?,
        val areas: MutableList<Area>
    )
}
