package ru.practicum.android.diploma.presentation.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.practicum.android.diploma.databinding.FragmentFavoriteVacancyBinding

class FavoriteVacancyFragment : Fragment() {

    private var _binding: FragmentFavoriteVacancyBinding? = null
    private val binding get() = _binding!!

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

        // Инициализация при запуске - показываем загрузку или сразу проверяем данные
        showLoadingState()

        // Здесь будет загрузка данных из базы/репозитория
        loadFavorites()
    }

    // Метод для загрузки избранных вакансий
    private fun loadFavorites() {
        // TODO: Заменить на реальную загрузку данных
        // Например:
        // viewModel.favorites.observe(viewLifecycleOwner) { vacancies ->
        //     if (vacancies.isEmpty()) {
        //         showEmptyState()
        //     } else {
        //         showVacanciesList()
        //         // установить данные в адаптер
        //     }
        // }

        // Временная заглушка для тестирования:
        // Для теста пустого состояния раскомментируйте следующую строку:
        showEmptyState()

        // Для теста с данными раскомментируйте:
        // showVacanciesList()
    }

    // Метод для показа состояния загрузки
    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewFavouriteVacancy.visibility = View.GONE
        binding.emptyListFavouritesVacancy.visibility = View.GONE
        binding.errorGetListFavouriteVacancy.visibility = View.GONE
    }

    // Метод для показа состояния с данными
    private fun showContentState(vacancyList: List<Any>) {
        binding.progressBar.visibility = View.GONE

        if (vacancyList.isEmpty()) {
            // Показываем плейсхолдер для пустого списка
            showEmptyState()
        } else {
            // Показываем список вакансий
            showVacanciesList()
            // Устанавливаем данные в адаптер
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
    private fun showVacanciesList() {
        binding.recyclerViewFavouriteVacancy.visibility = View.VISIBLE
        binding.emptyListFavouritesVacancy.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.errorGetListFavouriteVacancy.visibility = View.GONE
    }

    // Метод для показа состояния ошибки
    private fun showErrorState() {
        binding.errorGetListFavouriteVacancy.visibility = View.VISIBLE
        binding.recyclerViewFavouriteVacancy.visibility = View.GONE
        binding.emptyListFavouritesVacancy.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    // Комбинированный метод для удобного управления состояниями
    fun setState(state: FavoriteState, vacancyList: List<Any>? = null) {
        when (state) {
            FavoriteState.LOADING -> showLoadingState()
            FavoriteState.CONTENT -> showContentState(vacancyList ?: emptyList())
            FavoriteState.EMPTY -> showEmptyState()
            FavoriteState.ERROR -> showErrorState()
        }
    }

    // Простые методы для вызова
    fun showLoading() = setState(FavoriteState.LOADING)
    fun showContent(list: List<Any>) = setState(FavoriteState.CONTENT, list)
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
