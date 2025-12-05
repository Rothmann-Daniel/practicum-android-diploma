package ru.practicum.android.diploma.domain.models

data class FilterSettings(
    val industry: Industry? = null,
    val salary: Int? = null,
    val onlyWithSalary: Boolean = false
)
