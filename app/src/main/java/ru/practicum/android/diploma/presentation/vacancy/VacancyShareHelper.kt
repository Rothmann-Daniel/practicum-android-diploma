package ru.practicum.android.diploma.presentation.vacancy

import android.content.Context
import android.content.Intent
import ru.practicum.android.diploma.R

class VacancyShareHelper(private val context: Context) {

    fun shareVacancy(url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, context.getString(R.string.share_vacancy))
        )
    }
}
