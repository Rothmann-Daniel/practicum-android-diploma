package ru.practicum.android.diploma.data.remote.mapper

import ru.practicum.android.diploma.data.remote.dto.request.VacancyRequestDto
import ru.practicum.android.diploma.domain.models.VacancySearchRequest

class VacancyRequestMapper {
    fun toDto(request: VacancySearchRequest): VacancyRequestDto {
        return VacancyRequestDto(
            area = request.area,
            industry = request.industry,
            text = request.text,
            salary = request.salary,
            page = request.page,
            onlyWithSalary = request.onlyWithSalary
        )
    }
}
