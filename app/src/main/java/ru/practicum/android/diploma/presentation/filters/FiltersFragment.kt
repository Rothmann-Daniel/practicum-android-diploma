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
        loadSavedFilters()
    }

    private fun loadSavedFilters() {
        viewModel.filterSettings.observe(viewLifecycleOwner) { settings ->

            // Загружаем отрасль
            if (settings.industry != null) {
                binding.industryTitle.textSize = 12f
                binding.industryTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.industry.text = settings.industry.name
                binding.industry.visibility = View.VISIBLE
                binding.industryForwardIcon.setImageResource(R.drawable.ic_close)
            } else {
                binding.industry.text = ""
                binding.industry.visibility = View.GONE
                binding.industryTitle.textSize = 16f
                binding.industryTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                binding.industryForwardIcon.setImageResource(R.drawable.ic_arrow_forward)
            }

            // Загружаем зарплату
            binding.salarySum.setText(settings.salary?.toString() ?: "")

            // Загружаем чекбокс

            binding.checkBoxIcon.setImageResource(
                if (settings.onlyWithSalary) R.drawable.ic_check_box_on
                else R.drawable.ic_check_box_off
            )

            updateButtonsState()
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupIndustryBlock() {
        binding.industryForwardIcon.setOnClickListener {
            viewModel.filterSettings.observe(viewLifecycleOwner) { settings ->
                if (settings.industry != null) {
                    // Очистка отрасли
                    viewModel.saveIndustry(null)
                    binding.industry.text = ""
                    binding.industry.visibility = View.GONE
                    binding.industryTitle.textSize = 16f
                    binding.industryTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                    binding.industryForwardIcon.setImageResource(R.drawable.ic_arrow_forward)
                } else {
                    // Навигация на выбор отрасли
                    findNavController().navigate(R.id.action_filters_to_industries)
                }
                updateButtonsState()
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
            clearIcon.visibility = if (textNotEmpty && hasFocus) View.VISIBLE else View.GONE
            label.setTextColor(
                if (textNotEmpty && hasFocus) requireContext().getColor(R.color.blue)
                else requireContext().getColor(R.color.gray)
            )
        }

        edit.doOnTextChanged { text, _, _, _ ->
            val salary = text?.toString()?.toIntOrNull()
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.saveSalary(salary)
            }

            // иконка и цвет зависят от текста и фокуса
            val hasFocus = edit.isFocused
            clearIcon.visibility = if (!text.isNullOrEmpty() && hasFocus) View.VISIBLE else View.GONE
            label.setTextColor(
                if (!text.isNullOrEmpty() && hasFocus) requireContext().getColor(R.color.blue)
                else requireContext().getColor(R.color.gray)
            )

            updateButtonsState()
        }
        clearIcon.setOnClickListener {
            edit.setText("")
            hideKeyboard()
        }
    }

    private fun setupSalaryCheckbox() {
        binding.checkBoxIcon.setOnClickListener {
            viewModel.filterSettings.observe(viewLifecycleOwner) { settings ->
                val current = settings.onlyWithSalary
                val newValue = !current
                viewModel.saveOnlyWithSalary(newValue)
                binding.checkBoxIcon.setImageResource(
                    if (newValue) R.drawable.ic_check_box_on
                    else R.drawable.ic_check_box_off
                )
                updateButtonsState()
            }
        }
    }

    private fun setupApplyButton() {
        binding.btnApply.setOnClickListener {
            //добавить логику по применению фильтров в поиске
            findNavController().popBackStack()
        }
    }

    private fun setupResetButton() {
        binding.btnResert.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.clearAllFilters()
                loadSavedFilters()
            }
        }
    }

    private fun updateButtonsState() {
        viewModel.filterSettings.observe(viewLifecycleOwner) { settings ->
            val industry = settings.industry
            val salary = settings.salary
            val onlyWithSalary = settings.onlyWithSalary

            val anyFilterSet = industry != null || salary != null || onlyWithSalary
            val visibility = if (anyFilterSet) View.VISIBLE else View.GONE
            binding.btnApply.visibility = visibility
            binding.btnResert.visibility = visibility
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
}
