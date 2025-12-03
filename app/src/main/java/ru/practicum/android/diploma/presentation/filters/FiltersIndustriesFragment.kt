package ru.practicum.android.diploma.presentation.filters

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFiltersIndustriesBinding

class FiltersIndustriesFragment : Fragment() {

    private var _binding: FragmentFiltersIndustriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FilterIndustriesViewModel by viewModel()
    private lateinit var adapter: FilterIndustriesAdapter

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
        adapter = FilterIndustriesAdapter { industry ->
            viewModel.selectIndustry(industry)
        }

        binding.recyclerViewIndustries.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIndustries.adapter = adapter
        binding.buttonContainer.isVisible = false
    }

    private fun setupSearch() {
        binding.searchQueryIndustries.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
                updateClearButton(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClear.setOnClickListener {
            binding.searchQueryIndustries.text?.clear()
            viewModel.setSearchQuery("")
        }
    }

    private fun updateClearButton(query: String) {
        binding.btnClear.setImageResource(
            if (query.isNotEmpty()) R.drawable.ic_close else R.drawable.ic_search
        )
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.selectButton.setOnClickListener {
            viewModel.saveSelectedIndustry()

            // ПОКАЗЫВАЕМ TOAST СООБЩЕНИЕ
            viewModel.selectedIndustry.value?.let { selectedIndustry ->
                showSuccessMessage(selectedIndustry.name)
            }

            parentFragmentManager.popBackStack()
        }
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

            // После обновления списка устанавливаем выбранную отрасль в адаптере
            viewModel.selectedIndustryId.value?.let { industryId ->
                adapter.setSelectedIndustryId(industryId)
            }
        }

        viewModel.selectedIndustryId.observe(viewLifecycleOwner) { industryId ->
            // Когда меняется выбранная отрасль, обновляем адаптер
            adapter.setSelectedIndustryId(industryId)
        }

        viewModel.showSelectButton.observe(viewLifecycleOwner) { show ->
            binding.buttonContainer.isVisible = show
        }

        viewModel.selectedIndustry.observe(viewLifecycleOwner) { industry ->
            // Дополнительная логика если нужно
        }
    }

    private fun handleState(state: FilterIndustriesViewModel.IndustriesState) {
        when (state) {
            FilterIndustriesViewModel.IndustriesState.Loading -> showLoading()
            FilterIndustriesViewModel.IndustriesState.Content -> showContent()
            FilterIndustriesViewModel.IndustriesState.Empty -> showEmpty()
            FilterIndustriesViewModel.IndustriesState.Error -> showError()
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerViewIndustries.isVisible = false
        binding.errorNoInternetConnection.isVisible = false
        binding.serverErrorLayout.isVisible = false
        binding.buttonContainer.isVisible = false
    }

    private fun showContent() {
        binding.progressBar.isVisible = false
        binding.recyclerViewIndustries.isVisible = true
        binding.errorNoInternetConnection.isVisible = false
        binding.serverErrorLayout.isVisible = false
    }

    private fun showEmpty() {
        binding.progressBar.isVisible = false
        binding.recyclerViewIndustries.isVisible = false
        binding.errorNoInternetConnection.isVisible = false
        binding.serverErrorLayout.isVisible = true
        binding.buttonContainer.isVisible = false
    }

    private fun showError() {
        binding.progressBar.isVisible = false
        binding.recyclerViewIndustries.isVisible = false
        binding.errorNoInternetConnection.isVisible = true
        binding.serverErrorLayout.isVisible = false
        binding.buttonContainer.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
