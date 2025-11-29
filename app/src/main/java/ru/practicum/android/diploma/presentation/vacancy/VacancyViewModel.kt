package ru.practicum.android.diploma.presentation.vacancy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.usecases.AddVacancyToFavoritesUseCase
import ru.practicum.android.diploma.domain.usecases.DeleteVacancyFromFavoritesUseCase
import ru.practicum.android.diploma.domain.usecases.GetVacancyDetailsUseCase
import ru.practicum.android.diploma.domain.usecases.IsVacancyInFavoritesUseCase

class VacancyViewModel(
    private val getVacancyDetailsUseCase: GetVacancyDetailsUseCase,
    private val isVacancyInFavoritesUseCase: IsVacancyInFavoritesUseCase,
    private val addVacancyToFavoritesUseCase: AddVacancyToFavoritesUseCase,
    private val deleteVacancyFromFavoritesUseCase: DeleteVacancyFromFavoritesUseCase
    ) : ViewModel() {

    private val _vacancyState = MutableLiveData<VacancyState>()
    val vacancyState: LiveData<VacancyState> = _vacancyState

    fun loadVacancyDetails(vacancyId: String) {
        _vacancyState.value = VacancyState.Loading

        viewModelScope.launch {
            when (val response = getVacancyDetailsUseCase(vacancyId)) {
                is ApiResponse.Success -> {
                    val inFavorites = isVacancyInFavoritesUseCase(vacancyId)
                    _vacancyState.value = VacancyState.Content(response.data, inFavorites)
                }
                is ApiResponse.Error -> {
                    val errorType = when {
                        response.code == HTTP_NOT_FOUND -> ErrorType.VACANCY_NOT_FOUND
                        response.message.contains("Ошибка сети") ||
                            response.message.contains("Превышено время") -> ErrorType.NETWORK_ERROR
                        else -> ErrorType.SERVER_ERROR
                    }
                    _vacancyState.value = VacancyState.Error(errorType)
                }
                is ApiResponse.Loading -> {
                    _vacancyState.value = VacancyState.Loading
                }
            }
        }
    }

    fun addToOrRemoveFromFavorites() {
        if (_vacancyState.value is VacancyState.Content) {
            val vacancy = (_vacancyState.value as VacancyState.Content).vacancy
            val inFavorites = (_vacancyState.value as VacancyState.Content).inFavorites
            _vacancyState.value = VacancyState.Content(vacancy, !inFavorites)

            viewModelScope.launch {
                if (inFavorites) {
                    deleteVacancyFromFavoritesUseCase(vacancy.id)
                } else {
                    addVacancyToFavoritesUseCase(vacancy)
                }
            }
        }
    }

    sealed class VacancyState {
        object Loading : VacancyState()
        data class Content(val vacancy: Vacancy, val inFavorites: Boolean) : VacancyState()
        data class Error(val type: ErrorType) : VacancyState()
    }

    enum class ErrorType {
        VACANCY_NOT_FOUND,
        NETWORK_ERROR,
        SERVER_ERROR
    }

    companion object {
        private const val HTTP_NOT_FOUND = 404
    }
}
