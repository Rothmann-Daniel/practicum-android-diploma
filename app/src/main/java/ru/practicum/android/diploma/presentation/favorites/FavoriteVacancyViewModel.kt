package ru.practicum.android.diploma.presentation.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.usecases.GetFavoriteVacanciesUseCase

class FavoriteVacancyViewModel(
    private val getFavoriteVacanciesUseCase: GetFavoriteVacanciesUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: LiveData<FavoritesUiState> = _uiState

    fun showFavorites() {
        viewModelScope.launch {
            try {
                val vacancies = getFavoriteVacanciesUseCase()
                _uiState.value = if (vacancies.isEmpty()) {
                    FavoritesUiState.EmptyFavorites
                } else {
                    FavoritesUiState.Content(vacancies)
                }
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState.Error
            }
        }
    }

    sealed class FavoritesUiState {
        object Loading : FavoritesUiState()
        object EmptyFavorites : FavoritesUiState()
        object Error : FavoritesUiState()
        data class Content(
            val vacancies: List<Vacancy>
        ) : FavoritesUiState()
    }
}
