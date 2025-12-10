package ru.practicum.android.diploma.presentation.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.databinding.FragmentFavoriteVacancyBinding
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.presentation.search.VacanciesAdapter

class FavoriteVacancyFragment : Fragment() {

    private var _binding: FragmentFavoriteVacancyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoriteVacancyViewModel by viewModel()

    private var adapter: VacanciesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteVacancyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.showFavorites()
        setupAdapter()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupAdapter() {
        adapter = VacanciesAdapter(useTopPadding = false) { vacancy ->
            val action = FavoriteVacancyFragmentDirections.actionFavoriteVacancyToVacancy(
                vacancyId = vacancy.id
            )
            findNavController().navigate(action)
        }
        binding.recyclerViewFavouriteVacancy.adapter = adapter
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavouriteVacancy.layoutManager = layoutManager
        binding.recyclerViewFavouriteVacancy.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoriteVacancyViewModel.FavoritesUiState.Loading -> showLoadingState()
                is FavoriteVacancyViewModel.FavoritesUiState.EmptyFavorites -> showEmptyState()
                is FavoriteVacancyViewModel.FavoritesUiState.Error -> showErrorState()
                is FavoriteVacancyViewModel.FavoritesUiState.Content -> showVacanciesList(state.vacancies)
            }
        }
    }

    // Метод для показа состояния загрузки
    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewFavouriteVacancy.visibility = View.GONE
        binding.emptyListFavouritesVacancy.visibility = View.GONE
        binding.errorGetListFavouriteVacancy.visibility = View.GONE
    }

    // Метод для показа состояния с данными
    private fun showContentState(vacancyList: List<Vacancy>) {
        binding.progressBar.visibility = View.GONE

        if (vacancyList.isEmpty()) {
            // Показываем плейсхолдер для пустого списка
            showEmptyState()
        } else {
            // Показываем список вакансий
            showVacanciesList(vacancyList)
            // Устанавливаем данные в адаптер - удалить
            // adapter.submitList(vacancyList)
        }
    }

    // Метод для показа пустого списка
    private fun showEmptyState() {
        binding.emptyListFavouritesVacancy.visibility = View.VISIBLE
        binding.recyclerViewFavouriteVacancy.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.errorGetListFavouriteVacancy.visibility = View.GONE
    }

    // Метод для показа списка вакансий
    private fun showVacanciesList(vacancies: List<Vacancy>) {
        binding.recyclerViewFavouriteVacancy.visibility = View.VISIBLE
        binding.emptyListFavouritesVacancy.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.errorGetListFavouriteVacancy.visibility = View.GONE
        adapter?.submitList(vacancies)
    }

    // Метод для показа состояния ошибки
    private fun showErrorState() {
        binding.errorGetListFavouriteVacancy.visibility = View.VISIBLE
        binding.recyclerViewFavouriteVacancy.visibility = View.GONE
        binding.emptyListFavouritesVacancy.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    // Комбинированный метод для удобного управления состояниями
    fun setState(state: FavoriteState, vacancyList: List<Vacancy>? = null) {
        when (state) {
            FavoriteState.LOADING -> showLoadingState()
            FavoriteState.CONTENT -> showContentState(vacancyList ?: emptyList())
            FavoriteState.EMPTY -> showEmptyState()
            FavoriteState.ERROR -> showErrorState()
        }
    }

    // Простые методы для вызова
    fun showLoading() = setState(FavoriteState.LOADING)
    fun showContent(list: List<Vacancy>) = setState(FavoriteState.CONTENT, list)
    fun showError() = setState(FavoriteState.ERROR)
    fun showEmpty() = setState(FavoriteState.EMPTY)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Enum для состояний экрана
    enum class FavoriteState {
        LOADING, CONTENT, EMPTY, ERROR
    }
}
