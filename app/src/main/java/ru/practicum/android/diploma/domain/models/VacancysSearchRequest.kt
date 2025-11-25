package ru.practicum.android.diploma.domain.models

data class VacancySearchRequest(
    val text: String? = null,
    val area: Int? = null,
    val industry: Int? = null,
    val salary: Int? = null,
    val page: Int = 0,
    val onlyWithSalary: Boolean? = null )
