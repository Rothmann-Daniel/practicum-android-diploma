package ru.practicum.android.diploma.presentation.vacancy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentVacancyBinding
import ru.practicum.android.diploma.domain.models.Vacancy

class VacancyFragment : Fragment() {

    private var _binding: FragmentVacancyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VacancyViewModel by viewModel()
    private val args: VacancyFragmentArgs by navArgs()

    private var vacancyBinder: VacancyDataBinder? = null
    private var vacancyShareHelper: VacancyShareHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVacancyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vacancyBinder = VacancyDataBinder(binding, requireContext())
        vacancyShareHelper = VacancyShareHelper(requireContext())

        setupClickListeners()
        observeViewModel()
        viewModel.loadVacancyDetails(args.vacancyId)
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.shareButton.setOnClickListener {
            shareVacancy()
        }

        binding.favoriteButton.setOnClickListener {
            viewModel.addToOrRemoveFromFavorites()
        }
    }

    private fun observeViewModel() {
        viewModel.vacancyState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VacancyViewModel.VacancyState.Loading -> showLoading()
                is VacancyViewModel.VacancyState.Content -> showContent(state.vacancy, state.inFavorites)
                is VacancyViewModel.VacancyState.Error -> showError(state.type)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.contentScrollview.isVisible = false
        binding.errorVacancyNotFound.isVisible = false
        binding.serverErrorLayout.isVisible = false
    }

    private fun showContent(vacancy: Vacancy, inFavorites: Boolean) {
        binding.progressBar.isVisible = false
        binding.contentScrollview.isVisible = true
        binding.errorVacancyNotFound.isVisible = false
        binding.serverErrorLayout.isVisible = false

        if (inFavorites) {
            binding.favoriteButton.setImageResource(R.drawable.ic_favourites_fill)
        } else {
            binding.favoriteButton.setImageResource(R.drawable.ic_favourites)
        }

        vacancyBinder?.bindVacancyData(vacancy)
    }

    private fun showError(errorType: VacancyViewModel.ErrorType) {
        binding.progressBar.isVisible = false
        binding.contentScrollview.isVisible = false

        when (errorType) {
            VacancyViewModel.ErrorType.VACANCY_NOT_FOUND -> {
                binding.errorVacancyNotFound.isVisible = true
                binding.serverErrorLayout.isVisible = false
            }
            VacancyViewModel.ErrorType.NETWORK_ERROR,
            VacancyViewModel.ErrorType.SERVER_ERROR -> {
                binding.errorVacancyNotFound.isVisible = false
                binding.serverErrorLayout.isVisible = true
            }
        }
    }

    private fun shareVacancy() {
        viewModel.vacancyState.value?.let { state ->
            if (state is VacancyViewModel.VacancyState.Content) {
                vacancyShareHelper?.shareVacancy(state.vacancy.url)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vacancyBinder = null
        vacancyShareHelper = null
        _binding = null
    }
}
