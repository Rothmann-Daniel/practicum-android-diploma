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
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFiltersBinding
import ru.practicum.android.diploma.domain.models.FilterSettings

class FiltersFragment : Fragment() {

    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FiltersViewModel by viewModel()

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

        // Загружаем сохраненные фильтры из SharedPreferences
        setupObservers()
    }

    private fun setupObservers() {
        // ОДИН общий observer для всего UI
        viewModel.filterSettings.observe(viewLifecycleOwner) { settings ->
            updateUI(settings)
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

        // 2. Обновляем зарплату всегда при обновлении из LiveData
        // Убираем проверку на фокус для случая сброса
        val currentText = binding.salarySum.text?.toString() ?: ""
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
        val industry = settings.industry
        val salary = settings.salary
        val onlyWithSalary = settings.onlyWithSalary

        val anyFilterSet = industry != null || salary != null || onlyWithSalary
        val visibility = if (anyFilterSet) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.btnApply.visibility = visibility
        binding.btnResert.visibility = visibility
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupIndustryBlock() {
        binding.industryForwardIcon.setOnClickListener {
            val currentSettings = viewModel.filterSettings.value

            if (currentSettings?.industry != null) {
                // Очистка отрасли
                viewModel.saveIndustry(null)
                // UI обновится автоматически через observer
            } else {
                // Навигация на выбор отрасли
                findNavController().navigate(R.id.action_filters_to_industries)
            }
        }
    }

    private fun setupSalaryInput() {
        val edit = binding.salarySum
        val clearIcon = binding.salaryClearIcon
        val label = binding.salaryLabel

        // Фокус-слушатель
        edit.setOnFocusChangeListener { _, hasFocus ->
            val textNotEmpty = !edit.text.isNullOrEmpty()
            clearIcon.visibility = if (textNotEmpty && hasFocus) {
                View.VISIBLE
            } else {
                View.GONE
            }
            label.setTextColor(
                if (textNotEmpty && hasFocus) {
                    requireContext().getColor(R.color.blue)
                } else {
                    requireContext().getColor(R.color.gray)
                }
            )

            // Когда теряем фокус, сохраняем последнее значение
            if (!hasFocus) {
                val salary = edit.text?.toString()?.toIntOrNull()
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.saveSalary(salary)
                }
            }
        }

        edit.doOnTextChanged { text, _, _, _ ->
            val salary = text?.toString()?.toIntOrNull()

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.saveSalary(salary)
            }

            // Локальное обновление иконки и цвета
            val hasFocus = edit.isFocused
            clearIcon.visibility = if (!text.isNullOrEmpty() && hasFocus) {
                View.VISIBLE
            } else {
                View.GONE
            }
            label.setTextColor(
                if (!text.isNullOrEmpty() && hasFocus) {
                    requireContext().getColor(R.color.blue)
                } else {
                    requireContext().getColor(R.color.gray)
                }
            )

            // Обновляем кнопки на основе текущих данных
            val currentSettings = viewModel.filterSettings.value
            if (currentSettings != null) {
                updateButtonsState(currentSettings.copy(salary = salary))
            }
        }

        clearIcon.setOnClickListener {
            edit.setText("")
            hideKeyboard()
            // Зарплата обновится через doOnTextChanged
        }
    }

    private fun setupSalaryCheckbox() {
        binding.checkBoxIcon.setOnClickListener {
            val current = viewModel.filterSettings.value?.onlyWithSalary ?: false
            val newValue = !current

            viewModel.saveOnlyWithSalary(newValue)
            // UI обновится автоматически через observer
        }
    }

    private fun setupApplyButton() {
        binding.btnApply.setOnClickListener {
            // Сохраняем текущую зарплату перед уходом
            val salaryText = binding.salarySum.text?.toString()
            val salary = salaryText?.toIntOrNull()

            viewLifecycleOwner.lifecycleScope.launch {
                // Гарантируем сохранение последних данных
                viewModel.saveSalary(salary)

                // Применяем фильтры (здесь должна быть логика применения фильтров к поиску)

                // Возвращаемся назад
                findNavController().popBackStack()
            }
        }
    }

    private fun setupResetButton() {
        binding.btnResert.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.clearAllFilters()
                // UI обновится автоматически через observer
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.salarySum.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val INDUSTRY_TITLE_TEXT_SIZE_SMALL = 12f
        private const val INDUSTRY_TITLE_TEXT_SIZE_LARGE = 16f
    }
}
