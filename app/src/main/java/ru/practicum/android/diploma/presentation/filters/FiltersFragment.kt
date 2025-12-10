package ru.practicum.android.diploma.presentation.filters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFiltersBinding
import ru.practicum.android.diploma.domain.models.FilterSettings
import ru.practicum.android.diploma.domain.models.Industry

class FiltersFragment : Fragment() {

    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FiltersViewModel by viewModel()

    // Таймер для дебаунса при вводе зарплаты
    private var salaryDebounceJob: kotlinx.coroutines.Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFiltersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupIndustryBlock()
        setupSalaryInput()
        setupSalaryCheckbox()
        setupApplyButton()
        setupResetButton()

        parentFragmentManager.setFragmentResultListener(
            INDUSTRY_SELECTED_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val industry = bundle.getParcelable<Industry>("industry")
            viewModel.updateIndustry(industry)
            // При выборе отрасли отправляем обновление для подсветки
            sendFilterUpdateForHighlight(viewModel.getCurrentSettings() ?: FilterSettings())
        }

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.filterSettings.observe(viewLifecycleOwner) { settings ->
            updateUI(settings)
            // При любом изменении фильтров отправляем обновление для подсветки кнопки
            sendFilterUpdateForHighlight(settings)
        }
    }

    private fun updateUI(settings: FilterSettings) {
        // 1. Обновляем отрасль
        if (settings.industry != null) {
            binding.industryTitle.textSize = INDUSTRY_TITLE_TEXT_SIZE_SMALL
            binding.industryTitle.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
            binding.industry.text = settings.industry.name
            binding.industry.visibility = View.VISIBLE
            binding.industryForwardIcon.setImageResource(R.drawable.ic_close)
        } else {
            binding.industry.text = ""
            binding.industry.visibility = View.GONE
            binding.industryTitle.textSize = INDUSTRY_TITLE_TEXT_SIZE_LARGE
            binding.industryTitle.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.gray)
            )
            binding.industryForwardIcon.setImageResource(R.drawable.ic_arrow_forward)
        }

        // 2. Обновляем зарплату
        val currentText = binding.salarySum.text?.toString()?.trim() ?: ""
        val savedSalary = settings.salary?.toString() ?: ""

        if (currentText != savedSalary) {
            binding.salarySum.setText(savedSalary)
        }

        // 3. Обновляем чекбокс
        binding.checkBoxIcon.setImageResource(
            if (settings.onlyWithSalary) {
                R.drawable.ic_check_box_on
            } else {
                R.drawable.ic_check_box_off
            }
        )

        // 4. Обновляем состояние кнопок
        updateButtonsState(settings)
    }

    private fun updateButtonsState(settings: FilterSettings) {
        val anyFilterSet = hasAnyFilters(settings)
        val visibility = if (anyFilterSet) View.VISIBLE else View.GONE
        binding.btnApply.visibility = visibility
        binding.btnResert.visibility = visibility
    }

    private fun hasAnyFilters(settings: FilterSettings): Boolean {
        return settings.industry != null ||
            settings.salary != null ||
            settings.onlyWithSalary
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            // Перед выходом сохраняем текущую зарплату
            saveCurrentSalaryIfNeeded()
            findNavController().popBackStack()
        }
    }

    private fun setupIndustryBlock() {
        binding.industryForwardIcon.setOnClickListener {
            val currentSettings = viewModel.filterSettings.value
            if (currentSettings?.industry != null) {
                // Очищаем отрасль
                viewModel.updateIndustry(null)
            } else {
                // Переходим к выбору отрасли
                findNavController().navigate(R.id.action_filters_to_industries)
            }
        }

        binding.filerIndustry.setOnClickListener {
            findNavController().navigate(R.id.action_filters_to_industries)
        }
    }

    private fun setupSalaryInput() {
        setupSalaryFocusListener()
        setupSalaryTextWatcher()
        setupSalaryClearButton()
    }

    private fun setupSalaryFocusListener() {
        val edit = binding.salarySum
        val clearIcon = binding.salaryClearIcon
        val label = binding.salaryLabel

        edit.setOnFocusChangeListener { _, hasFocus ->
            val textNotEmpty = !edit.text.isNullOrEmpty()
            clearIcon.visibility = if (textNotEmpty && hasFocus) View.VISIBLE else View.GONE
            label.setTextColor(
                if (hasFocus) {
                    requireContext().getColor(R.color.blue)
                } else {
                    requireContext().getColor(R.color.gray)
                }
            )

            if (!hasFocus) {
                // При потере фокуса сохраняем зарплату
                saveCurrentSalary()
            }
        }
    }

    private fun setupSalaryTextWatcher() {
        val edit = binding.salarySum
        val clearIcon = binding.salaryClearIcon
        val label = binding.salaryLabel

        edit.doOnTextChanged { text, _, _, _ ->
            val hasFocus = edit.isFocused
            val textNotEmpty = !text.isNullOrEmpty()

            clearIcon.visibility = if (textNotEmpty && hasFocus) View.VISIBLE else View.GONE
            label.setTextColor(
                if (hasFocus) {
                    requireContext().getColor(R.color.blue)
                } else {
                    requireContext().getColor(R.color.gray)
                }
            )

            // Дебаунс для сохранения зарплаты
            salaryDebounceJob?.cancel()
            salaryDebounceJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(SALARY_DEBOUNCE_DELAY_MS)
                saveCurrentSalary()
            }

            // Обновляем UI кнопок
            val currentSettings = viewModel.filterSettings.value
            val salary = text?.toString()?.trim()?.toIntOrNull()
            if (currentSettings != null) {
                updateButtonsState(currentSettings.copy(salary = salary))
            }
        }
    }

    private fun saveCurrentSalary() {
        val salaryText = binding.salarySum.text?.toString()?.trim()
        val salary = salaryText?.toIntOrNull()
        viewModel.updateSalary(salary)
    }

    private fun saveCurrentSalaryIfNeeded() {
        val salaryText = binding.salarySum.text?.toString()?.trim()
        val currentSalary = viewModel.filterSettings.value?.salary

        val newSalary = salaryText?.toIntOrNull()

        if (newSalary != currentSalary) {
            viewModel.updateSalary(newSalary)
        }
    }

    private fun setupSalaryClearButton() {
        binding.salaryClearIcon.setOnClickListener {
            // Очищаем зарплату
            binding.salarySum.setText("")
            hideKeyboard()
            viewModel.updateSalary(null)
        }
    }

    private fun setupSalaryCheckbox() {
        binding.checkBoxIcon.setOnClickListener {
            // Изменение чекбокса
            val current = viewModel.filterSettings.value?.onlyWithSalary ?: false
            viewModel.updateOnlyWithSalary(!current)
        }
    }

    private fun setupApplyButton() {
        binding.btnApply.setOnClickListener {
            // Перед применением сохраняем текущую зарплату
            saveCurrentSalaryIfNeeded()

            // Отправляем фильтры с флагом "применить"
            val currentSettings = viewModel.getCurrentSettings()
            sendFilterApplyToSearch(currentSettings ?: FilterSettings())
            findNavController().popBackStack()
        }
    }

    private fun setupResetButton() {
        binding.btnResert.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                // Сброс всех фильтров
                viewModel.clearAllFilters()

                // Отправляем пустые фильтры с флагом "сброс"
                sendResetToSearch()

                // Закрываем экран фильтров
                findNavController().popBackStack()
            }
        }
    }

    private fun sendFilterUpdateForHighlight(settings: FilterSettings) {
        // Только для обновления подсветки кнопки (isApply = false)
        parentFragmentManager.setFragmentResult(
            FILTERS_UPDATE_KEY,
            Bundle().apply {
                putParcelable(FILTERS_KEY, settings)
                putBoolean(IS_APPLY_KEY, false) // НЕ применяем
            }
        )
    }

    private fun sendFilterApplyToSearch(settings: FilterSettings) {
        // Для применения фильтров (isApply = true)
        parentFragmentManager.setFragmentResult(
            FILTERS_UPDATE_KEY,
            Bundle().apply {
                putParcelable(FILTERS_KEY, settings)
                putBoolean(IS_APPLY_KEY, true) // Применяем
            }
        )
    }

    private fun sendResetToSearch() {
        // Для сброса фильтров (isApply = true, isReset = true)
        parentFragmentManager.setFragmentResult(
            FILTERS_UPDATE_KEY,
            Bundle().apply {
                putParcelable(FILTERS_KEY, FilterSettings()) // Пустые фильтры
                putBoolean(IS_APPLY_KEY, true) // Применяем сброс
                putBoolean(IS_RESET_KEY, true) // Флаг сброса
            }
        )
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.salarySum.windowToken, 0)
    }

    override fun onPause() {
        super.onPause()
        saveCurrentSalaryIfNeeded()
    }

    override fun onDestroyView() {
        salaryDebounceJob?.cancel()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val INDUSTRY_SELECTED_KEY = "industry_selected"
        const val FILTERS_UPDATE_KEY = "filters_update"
        const val FILTERS_KEY = "filters"
        const val IS_APPLY_KEY = "is_apply"
        const val IS_RESET_KEY = "is_reset"
        private const val INDUSTRY_TITLE_TEXT_SIZE_SMALL = 12f
        private const val INDUSTRY_TITLE_TEXT_SIZE_LARGE = 16f
        private const val SALARY_DEBOUNCE_DELAY_MS = 500L
    }
}
