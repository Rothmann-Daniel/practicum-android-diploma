package ru.practicum.android.diploma.data.repository

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.FilterRepository

class FilterRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : FilterRepository {

    companion object {
        private const val KEY_INDUSTRY_ID = "filter_industry_id"
        private const val KEY_INDUSTRY_NAME = "filter_industry_name"
        private const val KEY_SALARY = "filter_salary"
        private const val KEY_ONLY_WITH_SALARY = "filter_only_with_salary"
    }

    override suspend fun saveIndustry(industry: Industry?) {
        withContext(Dispatchers.IO) {
            if (industry == null) {
                sharedPreferences.edit()
                    .remove(KEY_INDUSTRY_ID)
                    .remove(KEY_INDUSTRY_NAME)
                    .apply()
            } else {
                sharedPreferences.edit()
                    .putInt(KEY_INDUSTRY_ID, industry.id)
                    .putString(KEY_INDUSTRY_NAME, industry.name)
                    .apply()
            }
        }
    }

    override suspend fun getSavedIndustry(): Industry? {
        return withContext(Dispatchers.IO) {
            val id = sharedPreferences.getInt(KEY_INDUSTRY_ID, -1)
            val name = sharedPreferences.getString(KEY_INDUSTRY_NAME, null)

            if (id != -1 && name != null) {
                Industry(id, name)
            } else {
                null
            }
        }
    }

    override suspend fun saveSalary(salary: Int?) {
        withContext(Dispatchers.IO) {
            if (salary == null) {
                sharedPreferences.edit().remove(KEY_SALARY).apply()
            } else {
                sharedPreferences.edit().putInt(KEY_SALARY, salary).apply()
            }
        }
    }

    override suspend fun getSavedSalary(): Int? {
        return withContext(Dispatchers.IO) {
            if (sharedPreferences.contains(KEY_SALARY)) {
                sharedPreferences.getInt(KEY_SALARY, 0)
            } else {
                null
            }
        }
    }

    override suspend fun saveOnlyWithSalary(onlyWithSalary: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putBoolean(KEY_ONLY_WITH_SALARY, onlyWithSalary).apply()
        }
    }

    override suspend fun getOnlyWithSalary(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(KEY_ONLY_WITH_SALARY, false)
        }
    }

    override suspend fun clearAllFilters() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .remove(KEY_INDUSTRY_ID)
                .remove(KEY_INDUSTRY_NAME)
                .remove(KEY_SALARY)
                .remove(KEY_ONLY_WITH_SALARY)
                .apply()
        }
    }

    override suspend fun getFilterSettings(): FilterSettings {
        return withContext(Dispatchers.IO) {
            FilterSettings(
                industry = getSavedIndustry(),
                salary = getSavedSalary(),
                onlyWithSalary = getOnlyWithSalary()
            )
        }
    }
}
