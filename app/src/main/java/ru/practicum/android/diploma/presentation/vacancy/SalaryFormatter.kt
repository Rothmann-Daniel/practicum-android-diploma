package ru.practicum.android.diploma.presentation.vacancy

import android.content.Context
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.Salary
import java.util.Locale

object SalaryFormatter {

    fun formatSalary(salary: Salary?, context: Context): String {
        if (salary == null) return context.getString(R.string.salary_not_specified)

        val currencySymbol = getCurrencySymbol(salary.currency ?: "")

        if (currencySymbol.isEmpty()) {
            // Если валюта неизвестна, показываем как есть с кодом валюты
            return formatSalaryWithCode(salary, salary.currency ?: "")
        }

        return when {
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

    private fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            // Основные валюты
            "RUR", "RUB" -> "₽"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CNY" -> "¥"

            // Страны СНГ и бывшего СССР
            "BYN", "BYR" -> "Br" // Белорусский рубль
            "UAH" -> "₴" // Украинская гривна
            "KZT" -> "₸" // Казахстанский тенге
            "AZN" -> "₼" // Азербайджанский манат
            "AMD" -> "֏" // Армянский драм
            "GEL" -> "₾" // Грузинский лари
            "KGS" -> "с" // Киргизский сом
            "UZS" -> "сўм" // Узбекский сум
            "TJS" -> "SM" // Таджикский сомони
            "TMT" -> "TMT" // Туркменский манат

            // Другие популярные валюты
            "CHF" -> "Fr" // Швейцарский франк
            "CAD" -> "C$" // Канадский доллар
            "AUD" -> "A$" // Австралийский доллар
            "NZD" -> "NZ$" // Новозеландский доллар
            "SGD" -> "S$" // Сингапурский доллар
            "HKD" -> "HK$" // Гонконгский доллар
            "SEK" -> "kr" // Шведская крона
            "NOK" -> "kr" // Норвежская крона
            "DKK" -> "kr" // Датская крона
            "PLN" -> "zł" // Польский злотый
            "CZK" -> "Kč" // Чешская крона
            "HUF" -> "Ft" // Венгерский форинт
            "RON" -> "lei" // Румынский лей
            "BGN" -> "лв" // Болгарский лев
            "TRY" -> "₺" // Турецкая лира
            "ILS" -> "₪" // Израильский шекель
            "INR" -> "₹" // Индийская рупия
            "KRW" -> "₩" // Южнокорейская вона
            "THB" -> "฿" // Тайский бат
            "PHP" -> "₱" // Филиппинское песо
            "MYR" -> "RM" // Малайзийский ринггит
            "IDR" -> "Rp" // Индонезийская рупия
            "VND" -> "₫" // Вьетнамский донг
            "BRL" -> "R$" // Бразильский реал
            "MXN" -> "Mex$" // Мексиканское песо
            "ZAR" -> "R" // Южноафриканский рэнд

            else -> "" // Неизвестная валюта - вернем пустую строку
        }
    }

    private fun formatNumber(number: Int): String {
        return String.format(Locale.getDefault(), "%,d", number).replace(',', ' ')
    }
}
