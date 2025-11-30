package ru.practicum.android.diploma.presentation.vacancy

import android.content.Context
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.Vacancy
import java.util.Locale

object SalaryFormatter {

    fun formatSalary(vacancy: Vacancy, context: Context): String {
        val salary = vacancy.salary ?: return context.getString(R.string.salary_not_specified)

        val currencySymbol = when (salary.currency) {
            "RUR", "RUB" -> "₽"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> salary.currency ?: ""
        }

        return when {
            salary.from != null && salary.to != null ->
                "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} $currencySymbol"
            salary.from != null ->
                "от ${formatNumber(salary.from)} $currencySymbol"
            salary.to != null ->
                "до ${formatNumber(salary.to)} $currencySymbol"
            else ->
                context.getString(R.string.salary_not_specified)
        }
    }

    private fun formatNumber(number: Int): String {
        return String.format(Locale.getDefault(), "%,d", number).replace(',', ' ')
    }
}
