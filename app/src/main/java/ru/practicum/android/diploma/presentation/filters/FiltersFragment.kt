package ru.practicum.android.diploma.presentation.filters

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFiltersBinding
import ru.practicum.android.diploma.domain.models.FilterSettings

class FiltersFragment : Fragment() {

    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FiltersViewModel by activityViewModel()

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
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.filterSettings.observe(viewLifecycleOwner) { settings ->
            updateUI(settings)
        }
    }

    private fun updateUI(settings: FilterSettings) {
        updateIndustryUI(settings.industry?.name)
        updateSalaryUI(settings.salary)
        updateCheckboxUI(settings.onlyWithSalary)
        updateButtonsState(settings)
    }

    private fun updateIndustryUI(industryName: String?) {
        if (industryName != null) {
            binding.industryTitle.textSize = INDUSTRY_TITLE_TEXT_SIZE_SMALL
            binding.industryTitle.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
            binding.industry.text = industryName
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
    }

    private fun updateSalaryUI(salary: Int?) {
        if (!binding.salarySum.isFocused) {
            val currentText = binding.salarySum.text?.toString() ?: ""
            val savedSalary = salary?.toString() ?: ""
            if (currentText != savedSalary) {
                binding.salarySum.setText(savedSalary)
            }
        }
    }

    private fun updateCheckboxUI(onlyWithSalary: Boolean) {
        binding.checkBoxIcon.setImageResource(
            if (onlyWithSalary) {
                R.drawable.ic_check_box_on
            } else {
                R.drawable.ic_check_box_off
            }
        )
    }

    private fun updateButtonsState(settings: FilterSettings) {
        val anyFilterSet = settings.industry != null ||
            settings.salary != null ||
            settings.onlyWithSalary
        val visibility = if (anyFilterSet) View.VISIBLE else View.GONE
        binding.btnApply.visibility = visibility
        binding.btnResert.visibility = visibility
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            viewModel.cancelChanges()
            findNavController().popBackStack()
        }
    }

    private fun setupIndustryBlock() {
        binding.industryForwardIcon.setOnClickListener {
            val currentSettings = viewModel.filterSettings.value
            if (currentSettings?.industry != null) {
                viewModel.updateIndustry(null)
            } else {
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
        binding.salarySum.setOnFocusChangeListener { _, hasFocus ->
            val textNotEmpty = !binding.salarySum.text.isNullOrEmpty()
            binding.salaryClearIcon.visibility =
                if (textNotEmpty && hasFocus) View.VISIBLE else View.GONE

            binding.salaryLabel.setTextColor(
                if (hasFocus) {
                    requireContext().getColor(R.color.blue)
                } else {
                    requireContext().getColor(R.color.gray)
                }
            )
        }
    }

    private fun setupSalaryTextWatcher() {
        binding.salarySum.doOnTextChanged { text, _, _, _ ->
            val salary = text?.toString()?.toIntOrNull()
            viewModel.updateSalary(salary)

            val hasFocus = binding.salarySum.isFocused
            binding.salaryClearIcon.visibility =
                if (!text.isNullOrEmpty() && hasFocus) View.VISIBLE else View.GONE
        }
    }

    private fun setupSalaryClearButton() {
        binding.salaryClearIcon.setOnClickListener {
            binding.salarySum.setText("")
            hideKeyboard()
        }
    }

    private fun setupSalaryCheckbox() {
        binding.checkBoxIcon.setOnClickListener {
            val current = viewModel.filterSettings.value?.onlyWithSalary ?: false
            viewModel.updateOnlyWithSalary(!current)
        }
    }

    private fun setupApplyButton() {
        binding.btnApply.setOnClickListener {
            // Применяем фильтры в корутине
            viewLifecycleOwner.lifecycleScope.launch {
                Log.d("FiltersFragment", "Apply button clicked")

                // Дожидаемся завершения сохранения
                val saved = viewModel.applyFilters()

                Log.d("FiltersFragment", "Filters saved: $saved")

                // Небольшая задержка для гарантии записи в SharedPreferences
                kotlinx.coroutines.delay(100)

                // Теперь устанавливаем флаг
                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(KEY_FILTERS_APPLIED, true)

                Log.d("FiltersFragment", "Flag set, navigating back")

                // Возвращаемся назад
                findNavController().popBackStack()
            }
        }
    }

    private fun setupResetButton() {
        binding.btnResert.setOnClickListener {
            viewModel.clearAllFilters()
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
        const val KEY_FILTERS_APPLIED = "filters_applied"
    }
}
