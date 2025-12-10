package ru.practicum.android.diploma.presentation.vacancy

import android.content.Context
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.Salary
import java.util.Locale

object SalaryFormatter {

    fun formatSalary(salary: Salary?, context: Context): String {
        val result = if (salary == null) {
            context.getString(R.string.salary_not_specified)
        } else {
            val currencySymbol = getCurrencySymbol(salary.currency ?: "")
            if (currencySymbol.isEmpty()) {
                formatSalaryWithCode(salary, salary.currency ?: "")
            } else {
                when {
                    salary.from != null && salary.to != null ->
                        context.getString(
                            R.string.salary_from_to,
                            formatNumber(salary.from),
                            formatNumber(salary.to),
                            currencySymbol
                        )

                    salary.from != null ->
                        context.getString(R.string.salary_from, formatNumber(salary.from), currencySymbol)

                    salary.to != null ->
                        context.getString(R.string.salary_to, formatNumber(salary.to), currencySymbol)

                    else ->
                        context.getString(R.string.salary_not_specified)
                }
            }
        }
        return result
    }


    private fun formatSalaryWithCode(salary: Salary, currencyCode: String): String {
        return when {
            salary.from != null && salary.to != null ->
                "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} $currencyCode"

            salary.from != null ->
                "от ${formatNumber(salary.from)} $currencyCode"

            salary.to != null ->
                "до ${formatNumber(salary.to)} $currencyCode"

            else ->
                ""
        }
    }

    private val currencyMap = mapOf(
        "RUR" to "₽", "RUB" to "₽", "USD" to "$", "EUR" to "€", "GBP" to "£",
        "JPY" to "¥", "CNY" to "¥",
        "BYN" to "Br", "BYR" to "Br", "UAH" to "₴", "KZT" to "₸",
        "AZN" to "₼", "AMD" to "֏", "GEL" to "₾", "KGS" to "с", "UZS" to "сўм",
        "TJS" to "SM", "TMT" to "TMT",
        "CHF" to "Fr", "CAD" to "C$", "AUD" to "A$", "NZD" to "NZ$", "SGD" to "S$",
        "HKD" to "HK$", "SEK" to "kr", "NOK" to "kr", "DKK" to "kr", "PLN" to "zł",
        "CZK" to "Kč", "HUF" to "Ft", "RON" to "lei", "BGN" to "лв", "TRY" to "₺",
        "ILS" to "₪", "INR" to "₹", "KRW" to "₩", "THB" to "฿", "PHP" to "₱",
        "MYR" to "RM", "IDR" to "Rp", "VND" to "₫", "BRL" to "R$", "MXN" to "Mex$",
        "ZAR" to "R"
    )

    private fun getCurrencySymbol(currencyCode: String) = currencyMap[currencyCode.uppercase()] ?: ""

    private fun formatNumber(number: Int): String {
        return String.format(Locale.getDefault(), "%,d", number).replace(',', ' ')
    }
}
