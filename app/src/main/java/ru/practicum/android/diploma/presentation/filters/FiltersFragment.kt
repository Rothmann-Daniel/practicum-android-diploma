package ru.practicum.android.diploma.presentation.filters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFiltersBinding

class FiltersFragment : Fragment() {

    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!

    private var selectedIndustry: String? = null
    private var salary: String? = null
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

        setupBackButton()
        setupIndustryBlock()
        setupSalaryInput()
        setupSalaryCheckbox()
        setupApplyAndResetButtons()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
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
            salary = text?.toString()

            // показать / скрыть иконку очистки
            clearIcon.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE

            // менять цвет лейбла
            label.setTextColor(
                if (text.isNullOrEmpty())
                    requireContext().getColor(R.color.gray)
                else
                    requireContext().getColor(R.color.blue)
            )

            updateButtonsState()
            updateSalaryCheckboxIcon()
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
        }
    }

    private fun updateSalaryCheckboxIcon() {
        val hasSalary = !salary.isNullOrEmpty()

        val iconRes = if (hasSalary && salaryChecked) {
            R.drawable.ic_check_box_on
        } else {
            R.drawable.ic_check_box_off
        }

        binding.checkBoxIcon.setImageResource(iconRes)
    }

    private fun setupApplyAndResetButtons() {
        updateButtonsState()

        binding.btnApply.setOnClickListener {
            // логика применения фильтров
        }

        binding.btnResert.setOnClickListener {
            resetFilters()
        }
    }

    private fun updateButtonsState() {
        val anyFilterSet =
            !salary.isNullOrEmpty() || selectedIndustry != null || salaryChecked

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
