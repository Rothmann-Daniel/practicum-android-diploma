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
import ru.practicum.android.diploma.domain.models.Industry

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

        parentFragmentManager.setFragmentResultListener(
            "industry_selected",
            viewLifecycleOwner
        ) { _, bundle ->
            val industry = bundle.getParcelable<Industry>("industry")
            viewModel.updateIndustryDraft(industry)
            sendDraftToSearchFragment() // !!! CHANGE !!!
        }

        setupObservers()
    }

    private fun setupObservers() {
        // подписка на черновик
        viewModel.draftFilters.observe(viewLifecycleOwner) { draft ->
            updateUI(draft)
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
            val currentDraft = viewModel.draftFilters.value
            if (currentDraft?.industry != null) {
                // очищаем отрасль в черновике
                viewModel.updateIndustryDraft(null)
                sendDraftToSearchFragment()
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
                val salary = edit.text?.toString()?.toIntOrNull()
                viewModel.updateSalaryDraft(salary)
                sendDraftToSearchFragment()
            }
        }
    }

    private fun setupSalaryTextWatcher() {
        val edit = binding.salarySum
        val clearIcon = binding.salaryClearIcon
        val label = binding.salaryLabel

        edit.doOnTextChanged { text, _, _, _ ->
            val salary = text?.toString()?.toIntOrNull()
            viewModel.updateSalaryDraft(salary)

            val hasFocus = edit.isFocused
            clearIcon.visibility = if (!text.isNullOrEmpty() && hasFocus) {
                View.VISIBLE
            } else {
                View.GONE
            }
            label.setTextColor(
                if (hasFocus) {
                    requireContext().getColor(R.color.blue)
                } else {
                    requireContext().getColor(R.color.gray)
                }
            )

            val currentDraft = viewModel.draftFilters.value
            if (currentDraft != null) {
                updateButtonsState(currentDraft.copy(salary = salary))
            }
            sendDraftToSearchFragment()
        }
    }

    private fun setupSalaryClearButton() {
        binding.salaryClearIcon.setOnClickListener {
            binding.salarySum.setText("")
            hideKeyboard()
            viewModel.updateSalaryDraft(null)
            sendDraftToSearchFragment()
        }
    }

    private fun setupSalaryCheckbox() {
        binding.checkBoxIcon.setOnClickListener {
            val current = viewModel.draftFilters.value?.onlyWithSalary ?: false
            viewModel.updateOnlyWithSalaryDraft(!current)
            sendDraftToSearchFragment()
        }
    }

    private fun setupApplyButton() {
        binding.btnApply.setOnClickListener {
            val salaryText = binding.salarySum.text?.toString()
            val salary = salaryText?.toIntOrNull()
            viewModel.updateSalaryDraft(salary)

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.applyFilters()
                val filters = viewModel.appliedFilters.value
                if (filters != null) {
                    parentFragmentManager.setFragmentResult(
                        "filters_applied",
                        Bundle().apply { putParcelable("filters", filters) }
                    )
                }
                findNavController().popBackStack()
            }
        }
    }

    // При выборе отрасли, зарплаты, чекбокса — draft, не запускаем поиск
    private fun sendDraftToSearchFragment(isReset: Boolean = false) {
        val draft = viewModel.draftFilters.value ?: FilterSettings()
        parentFragmentManager.setFragmentResult(
            "filters_draft",
            Bundle().apply {
                putParcelable("filters", draft)
                putBoolean("isReset", isReset)
            }
        )
    }

    // При нажатии Reset — отправляем пустой draft с флагом isReset
    private fun setupResetButton() {
        binding.btnResert.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.clearAllFilters() // очищаем локально
                parentFragmentManager.setFragmentResult(
                    "filters_applied",
                    Bundle().apply {
                        putParcelable("filters", FilterSettings())
                        putBoolean("isReset", true)
                    }
                )
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
