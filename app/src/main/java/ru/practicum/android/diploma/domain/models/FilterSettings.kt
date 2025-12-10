package ru.practicum.android.diploma.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilterSettings(
    val industry: Industry? = null,
    val salary: Int? = null,
    val onlyWithSalary: Boolean = false
) : Parcelable

