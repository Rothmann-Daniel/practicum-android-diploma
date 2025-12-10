package ru.practicum.android.diploma.presentation.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFiltersIndustriesBinding
import ru.practicum.android.diploma.presentation.filters.FiltersFragment.Companion.INDUSTRY_SELECTED_KEY

class FiltersIndustriesFragment : Fragment() {

    private var _binding: FragmentFiltersIndustriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FilterIndustriesViewModel by viewModel()

    // Ленивая инициализация адаптера с передачей функции скрытия клавиатуры
    private val adapter: FilterIndustriesAdapter by lazy {
        FilterIndustriesAdapter(
            onItemClick = { industry ->
                viewModel.selectIndustry(industry)
            },
            hideKeyboard = {
                hideKeyboard()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFiltersIndustriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupSearch()
        setupClickListeners()
        observeViewModel()

        viewModel.loadIndustries()
    }

    private fun setupUI() {
        binding.recyclerViewIndustries.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIndustries.adapter = adapter
        binding.buttonContainer.isVisible = false

        // Добавляем обработчик клика по RecyclerView для скрытия клавиатуры
        binding.recyclerViewIndustries.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
    }

    private fun setupSearch() {
        binding.searchQueryIndustries.doOnTextChanged { text, _, _, _ ->
            val query = text.toString()
            viewModel.setSearchQuery(query)
            updateClearButton(query)
        }

        binding.btnClear.setOnClickListener {
            binding.searchQueryIndustries.text?.clear()
            // сразу сбрасываем поиск без задержки
            viewModel.clearSearch()
            hideKeyboard() // Скрываем клавиатуру при очистке поиска
        }
    }

    private fun updateClearButton(query: String) {
        binding.btnClear.setImageResource(
            if (query.isNotEmpty()) R.drawable.ic_close else R.drawable.ic_search
        )
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            hideKeyboard()
            parentFragmentManager.popBackStack()
        }

        binding.selectButton.setOnClickListener {
            hideKeyboard()
            viewModel.saveSelectedIndustry()

            viewModel.selectedIndustry.value?.let { selectedIndustry ->
                showSuccessMessage(selectedIndustry.name)
            }

            viewModel.selectedIndustry.value?.let { selectedIndustry ->
                // Передаём выбранную отрасль обратно в FiltersFragment
                parentFragmentManager.setFragmentResult(
                    INDUSTRY_SELECTED_KEY,
                    Bundle().apply { putParcelable("industry", selectedIndustry) }
                )
            }

            parentFragmentManager.popBackStack()
        }
    }

    // Функция для скрытия клавиатуры
    private fun hideKeyboard() {
        val inputMethodManager = ContextCompat.getSystemService(
            requireContext(),
            InputMethodManager::class.java
        )
        inputMethodManager?.hideSoftInputFromWindow(
            binding.searchQueryIndustries.windowToken,
            0
        )
    }

    private fun showSuccessMessage(industryName: String) {
        val message = "Выбрана отрасль: $industryName"
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            handleState(state)
        }

        viewModel.filteredIndustries.observe(viewLifecycleOwner) { industries ->
            adapter.submitList(industries)

            viewModel.selectedIndustryId.value?.let { industryId ->
                adapter.setSelectedIndustryId(industryId)
            }
        }

        viewModel.selectedIndustryId.observe(viewLifecycleOwner) { industryId ->
            adapter.setSelectedIndustryId(industryId)
        }

        viewModel.showSelectButton.observe(viewLifecycleOwner) { show ->
            binding.buttonContainer.isVisible = show
        }
    }

    private fun handleState(state: FilterIndustriesViewModel.IndustriesState) {
        when (state) {
            FilterIndustriesViewModel.IndustriesState.Loading -> showLoading()
            FilterIndustriesViewModel.IndustriesState.Content -> showContent()
            FilterIndustriesViewModel.IndustriesState.NoResults -> showNoResults()
            FilterIndustriesViewModel.IndustriesState.Empty -> showEmpty()
            FilterIndustriesViewModel.IndustriesState.Error -> showError()
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerViewIndustries.isVisible = false
        binding.errorNoInternetConnection.isVisible = false
        binding.serverErrorLayout.isVisible = false
        binding.errorNoSuchIndustry.isVisible = false
        binding.buttonContainer.isVisible = false
    }

    private fun showContent() {
        binding.progressBar.isVisible = false
        binding.recyclerViewIndustries.isVisible = true
        binding.errorNoInternetConnection.isVisible = false
        binding.serverErrorLayout.isVisible = false
        binding.errorNoSuchIndustry.isVisible = false
        // Кнопка видна только если есть выбранная отрасль
    }

    private fun showNoResults() {
        // используем плейсхолдер для "нет такой отрасли"
        binding.progressBar.isVisible = false
        binding.recyclerViewIndustries.isVisible = false
        binding.errorNoInternetConnection.isVisible = false
        binding.serverErrorLayout.isVisible = false
        binding.errorNoSuchIndustry.isVisible = true
        // скрываем кнопку при отображении ошибки "нет такой отрасли"
        binding.buttonContainer.isVisible = false
    }

    private fun showEmpty() {
        binding.progressBar.isVisible = false
        binding.recyclerViewIndustries.isVisible = false
        binding.errorNoInternetConnection.isVisible = false
        binding.serverErrorLayout.isVisible = true
        binding.errorNoSuchIndustry.isVisible = false
        binding.buttonContainer.isVisible = false
    }

    private fun showError() {
        binding.progressBar.isVisible = false
        binding.recyclerViewIndustries.isVisible = false
        binding.errorNoInternetConnection.isVisible = true
        binding.serverErrorLayout.isVisible = false
        binding.errorNoSuchIndustry.isVisible = false
        binding.buttonContainer.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
