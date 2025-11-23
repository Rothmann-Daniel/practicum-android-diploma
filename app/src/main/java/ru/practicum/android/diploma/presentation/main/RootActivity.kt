package ru.practicum.android.diploma.presentation.main

import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.api.request.VacancyRequest
import ru.practicum.android.diploma.data.api.response.ApiResponse
import ru.practicum.android.diploma.domain.repository.IAreaRepository
import ru.practicum.android.diploma.domain.repository.IIndustryRepository
import ru.practicum.android.diploma.domain.repository.IVacancyRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RootActivity : AppCompatActivity() {
    private val areaRepository: IAreaRepository by inject()
    private val industryRepository: IIndustryRepository by inject()
    private val vacancyRepository: IVacancyRepository by inject()

    private lateinit var logTextView: TextView
    private lateinit var scrollView: ScrollView

    private val logs = StringBuilder()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_test)

        logTextView = findViewById(R.id.logTextView)
        scrollView = findViewById(R.id.scrollView)

        setupButtons()

        log("=== API Testing Started ===")
        log("Время запуска: ${dateFormat.format(Date())}")
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnTestAreas).setOnClickListener {
            testAreas()
        }

        findViewById<Button>(R.id.btnTestIndustries).setOnClickListener {
            testIndustries()
        }

        findViewById<Button>(R.id.btnTestVacancies).setOnClickListener {
            testVacancies()
        }

        findViewById<Button>(R.id.btnTestVacancyDetail).setOnClickListener {
            testVacancyDetail()
        }

        findViewById<Button>(R.id.btnTestLocalData).setOnClickListener {
            testLocalData()
        }

        findViewById<Button>(R.id.btnClearLog).setOnClickListener {
            clearLog()
        }
    }

    private fun testAreas() {
        log("\n--- Тест: Получение регионов ---")
        log("Отправка запроса GET /areas...")

        lifecycleScope.launch {
            try {
                when (val result = areaRepository.getAreas()) {
                    is ApiResponse.Success -> {
                        val areas = result.data
                        log("✓ УСПЕХ: Получено ${areas.size} регионов")

                        // Показываем первые 3 региона
                        areas.take(3).forEach { area ->
                            log("  • ${area.name} (ID: ${area.id})")
                            if (area.areas.isNotEmpty()) {
                                log("    └─ Дочерних областей: ${area.areas.size}")
                            }
                        }

                        // Статистика
                        val totalSubareas = areas.sumOf { countSubareas(it) }
                        log("Статистика:")
                        log("  - Регионов верхнего уровня: ${areas.size}")
                        log("  - Всего областей (включая вложенные): $totalSubareas")

                        // Проверка сохранения в БД
                        val localAreas = areaRepository.getLocalAreas()
                        log("  - Сохранено в БД: ${localAreas.size} регионов")
                    }
                    is ApiResponse.Error -> {
                        log("✗ ОШИБКА: ${result.message}")
                        result.code?.let { log("  Код ошибки: $it") }
                    }
                    ApiResponse.Loading -> {
                        log("⟳ Загрузка...")
                    }
                }
            } catch (e: Exception) {
                log("✗ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun testIndustries() {
        log("\n--- Тест: Получение отраслей ---")
        log("Отправка запроса GET /industries...")

        lifecycleScope.launch {
            try {
                when (val result = industryRepository.getIndustries()) {
                    is ApiResponse.Success -> {
                        val industries = result.data
                        log("✓ УСПЕХ: Получено ${industries.size} отраслей")

                        // Показываем первые 5 отраслей
                        industries.take(5).forEach { industry ->
                            log("  • ${industry.name} (ID: ${industry.id})")
                        }

                        // Проверка сохранения в БД
                        val localIndustries = industryRepository.getLocalIndustries()
                        log("Сохранено в БД: ${localIndustries.size} отраслей")
                    }
                    is ApiResponse.Error -> {
                        log("✗ ОШИБКА: ${result.message}")
                        result.code?.let { log("  Код ошибки: $it") }
                    }
                    ApiResponse.Loading -> {
                        log("⟳ Загрузка...")
                    }
                }
            } catch (e: Exception) {
                log("✗ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun testVacancies() {
        log("\n--- Тест: Поиск вакансий ---")
        log("Параметры: text='Android', page=0")
        log("Отправка запроса GET /vacancies...")

        lifecycleScope.launch {
            try {
                val request = VacancyRequest(
                    text = "Android",
                    page = 0
                )

                when (val result = vacancyRepository.getVacancies(request)) {
                    is ApiResponse.Success -> {
                        val searchResult = result.data
                        log("✓ УСПЕХ!")
                        log("Найдено вакансий: ${searchResult.found}")
                        log("Всего страниц: ${searchResult.pages}")
                        log("Текущая страница: ${searchResult.page}")
                        log("Получено на странице: ${searchResult.vacancies.size}")

                        // Показываем первые 3 вакансии
                        log("\nПервые вакансии:")
                        searchResult.vacancies.take(3).forEach { vacancy ->
                            log("  • ${vacancy.name}")
                            log("    Компания: ${vacancy.employer.name}")
                            log("    Регион: ${vacancy.area.name}")
                            vacancy.salary?.let { salary ->
                                val salaryStr = formatSalary(salary.from, salary.to, salary.currency)
                                log("    Зарплата: $salaryStr")
                            }
                        }
                    }
                    is ApiResponse.Error -> {
                        log("✗ ОШИБКА: ${result.message}")
                        result.code?.let { log("  Код ошибки: $it") }
                    }
                    ApiResponse.Loading -> {
                        log("⟳ Загрузка...")
                    }
                }
            } catch (e: Exception) {
                log("✗ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun testVacancyDetail() {
        log("\n--- Тест: Детали вакансии ---")
        log("Сначала получаем список для ID...")

        lifecycleScope.launch {
            try {
                // Сначала получаем список вакансий
                val request = VacancyRequest(text = "Android", page = 0)
                val searchResult = vacancyRepository.getVacancies(request)

                if (searchResult is ApiResponse.Success && searchResult.data.vacancies.isNotEmpty()) {
                    val vacancyId = searchResult.data.vacancies.first().id
                    log("Получаем детали вакансии ID: $vacancyId")
                    log("Отправка запроса GET /vacancies/$vacancyId...")

                    when (val result = vacancyRepository.getVacancyById(vacancyId)) {
                        is ApiResponse.Success -> {
                            val vacancy = result.data
                            log("✓ УСПЕХ!")
                            log("Название: ${vacancy.name}")
                            log("Компания: ${vacancy.employer.name}")
                            log("Регион: ${vacancy.area.name}")

                            vacancy.salary?.let {
                                log("Зарплата: ${formatSalary(it.from, it.to, it.currency)}")
                            }

                            vacancy.experience?.let {
                                log("Опыт: ${it.name}")
                            }

                            vacancy.schedule?.let {
                                log("График: ${it.name}")
                            }

                            vacancy.employment?.let {
                                log("Занятость: ${it.name}")
                            }

                            if (vacancy.skills.isNotEmpty()) {
                                log("Навыки (${vacancy.skills.size}):")
                                vacancy.skills.take(5).forEach { skill ->
                                    log("  • $skill")
                                }
                            }

                            log("Описание: ${vacancy.description.take(200)}...")
                            log("URL: ${vacancy.url}")
                        }
                        is ApiResponse.Error -> {
                            log("✗ ОШИБКА: ${result.message}")
                            result.code?.let { log("  Код ошибки: $it") }
                        }
                        ApiResponse.Loading -> {
                            log("⟳ Загрузка...")
                        }
                    }
                } else {
                    log("✗ Не удалось получить ID вакансии для теста")
                }
            } catch (e: Exception) {
                log("✗ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun testLocalData() {
        log("\n--- Тест: Локальные данные ---")

        lifecycleScope.launch {
            try {
                val localAreas = areaRepository.getLocalAreas()
                val localIndustries = industryRepository.getLocalIndustries()
                val localVacancies = vacancyRepository.getLocalVacancies()

                log("Данные из локальной БД:")
                log("  • Регионов: ${localAreas.size}")
                log("  • Отраслей: ${localIndustries.size}")
                log("  • Вакансий: ${localVacancies.size}")

                if (localAreas.isNotEmpty()) {
                    log("\nПримеры регионов:")
                    localAreas.take(3).forEach { area ->
                        log("  • ${area.name} (ID: ${area.id})")
                    }
                }

                if (localIndustries.isNotEmpty()) {
                    log("\nПримеры отраслей:")
                    localIndustries.take(3).forEach { industry ->
                        log("  • ${industry.name}")
                    }
                }

                if (localVacancies.isNotEmpty()) {
                    log("\nПримеры вакансий:")
                    localVacancies.take(3).forEach { vacancy ->
                        log("  • ${vacancy.name} - ${vacancy.employer.name}")
                    }
                }

                if (localAreas.isEmpty() && localIndustries.isEmpty() && localVacancies.isEmpty()) {
                    log("⚠ БД пуста. Выполните тесты API для загрузки данных.")
                }
            } catch (e: Exception) {
                log("✗ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun countSubareas(area: ru.practicum.android.diploma.domain.models.Area): Int {
        return 1 + area.areas.sumOf { countSubareas(it) }
    }

    private fun formatSalary(from: Int?, to: Int?, currency: String?): String {
        return when {
            from != null && to != null -> "от $from до $to $currency"
            from != null -> "от $from $currency"
            to != null -> "до $to $currency"
            else -> "не указана"
        }
    }

    private fun log(message: String) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $message\n"

        runOnUiThread {
            logs.append(logEntry)
            logTextView.text = logs.toString()

            // Автоскролл вниз
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }

    private fun clearLog() {
        logs.clear()
        logs.append("=== Лог очищен ===\n")
        logTextView.text = logs.toString()
    }
}
