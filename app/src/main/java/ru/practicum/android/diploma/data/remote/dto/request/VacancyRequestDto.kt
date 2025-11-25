package ru.practicum.android.diploma.data.remote.dto.request

data class VacancyRequestDto(
    val area: Int? = null,
    val industry: Int? = null,
    val text: String? = null,
    val salary: Int? = null,
    val page: Int? = null,
    val onlyWithSalary: Boolean? = null

)
