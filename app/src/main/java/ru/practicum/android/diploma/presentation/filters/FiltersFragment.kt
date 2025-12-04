package ru.practicum.android.diploma.presentation.filters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

    private var selectedIndustry: Industry? = null
    private var salary: Int? = null
    private var salaryChecked = false

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

        viewModel.loadFiltersFromPrefs()
        setupBackButton()
        setupIndustryBlock()
        setupSalaryInput()
        setupSalaryCheckbox()
        setupApplyButton()
        setupResetButton()
        observeFilters()

        // Слушаем выбранную отрасль из IndustriesFragment
        parentFragmentManager.setFragmentResultListener("selectedIndustry", viewLifecycleOwner) { _, bundle ->
            val industry = bundle.getParcelable<Industry>("industry")
            selectedIndustry = industry
            binding.industry.text = industry?.name ?: ""
            binding.industry.visibility = if (industry != null) View.VISIBLE else View.GONE
            updateButtonsState()
            Log.d("FiltersFragment", "Selected industry from result: $industry")
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeFilters() {
        // Один раз подписываемся на LiveData
        viewModel.filterSettings.observe(viewLifecycleOwner) { settings ->
            Log.d("FiltersFragment", "observeFilters: $settings")
            updateUI(settings)
        }
    }

    private fun saveFilters(settings: FilterSettings) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveFilters(settings)
        }
    }

    private fun updateUI(settings: FilterSettings) {
        if (selectedIndustry == null) {
            binding.industry.text = settings.industry?.name ?: ""
            binding.industry.visibility = if (settings.industry != null) View.VISIBLE else View.GONE
            selectedIndustry = settings.industry
        }

        if (binding.salarySum.text.toString().isEmpty()) {
            binding.salarySum.setText(settings.salary?.toString() ?: "")
            salary = settings.salary
        }

        salaryChecked = settings.onlyWithSalary
        updateSalaryCheckboxIcon()

        updateButtonsState()
    }

    private fun setupIndustryBlock() {
        binding.industryForwardIcon.setOnClickListener {
            findNavController().navigate(R.id.action_filters_to_industries)
        }
        // Текст отрасли скрыт пока не выбран фильтр
        binding.industry.visibility = View.GONE
    }

    private fun setupSalaryInput() {
        val edit = binding.salarySum
        val clearIcon = binding.salaryClearIcon
        val label = binding.salaryLabel

        edit.doOnTextChanged { text, _, _, _ ->
            salary = text?.toString()?.toIntOrNull()

            // показать / скрыть иконку очистки
            clearIcon.visibility = if (text.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }

            // менять цвет лейбла
            label.setTextColor(
                if (text.isNullOrEmpty()) {
                    requireContext().getColor(R.color.gray)
                } else {
                    requireContext().getColor(R.color.blue)
                }
            )

            updateButtonsState()
        }

        // очистка поля
        clearIcon.setOnClickListener {
            edit.setText("")
            hideKeyboard()
        }
    }

    private fun setupSalaryCheckbox() {
        binding.checkBoxIcon.setOnClickListener {
            salaryChecked = !salaryChecked
            updateSalaryCheckboxIcon()
            updateButtonsState()
        }
    }

    private fun updateSalaryCheckboxIcon() {
        val iconRes = if (salaryChecked) {
            R.drawable.ic_check_box_on
        } else {
            R.drawable.ic_check_box_off
        }

        binding.checkBoxIcon.setImageResource(iconRes)
    }

    private fun setupApplyButton() {
        binding.btnApply.setOnClickListener {
            val settings = FilterSettings(
                industry = selectedIndustry, // берём уже выбранный объект
                salary = salary, // берём число, введённое пользователем
                onlyWithSalary = salaryChecked // берём актуальное значение чекбокса
            )

            saveFilters(settings) // сохраняем все фильтры
            findNavController().popBackStack()
        }
    }

    private fun setupResetButton() {
        binding.btnResert.setOnClickListener {
            viewModel.clearAllFilters()
            resetFilters()
        }
    }

    private fun updateButtonsState(settings: FilterSettings? = null) {
        val currentSettings = settings ?: FilterSettings(
            industry = selectedIndustry,
            salary = salary,
            onlyWithSalary = salaryChecked
        )
        val anyFilterSet = currentSettings.salary != null ||
            currentSettings.industry != null ||
            currentSettings.onlyWithSalary

        val visibility = if (anyFilterSet) View.VISIBLE else View.GONE
        binding.btnApply.visibility = visibility
        binding.btnResert.visibility = visibility
    }

    private fun resetFilters() {
        salary = null
        selectedIndustry = null
        salaryChecked = false

        binding.salarySum.setText("")
        binding.industry.visibility = View.GONE

        updateButtonsState()
        updateSalaryCheckboxIcon()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.salarySum.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
