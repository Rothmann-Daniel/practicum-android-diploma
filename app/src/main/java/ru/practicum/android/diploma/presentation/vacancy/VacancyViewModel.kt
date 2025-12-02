package ru.practicum.android.diploma.presentation.vacancy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.usecases.AddVacancyToFavoritesUseCase
import ru.practicum.android.diploma.domain.usecases.DeleteVacancyFromFavoritesUseCase
import ru.practicum.android.diploma.domain.usecases.GetFavoriteVacancyByIdUseCase
import ru.practicum.android.diploma.domain.usecases.GetVacancyDetailsUseCase
import ru.practicum.android.diploma.domain.usecases.IsVacancyInFavoritesUseCase

class VacancyViewModel(
    private val getVacancyDetailsUseCase: GetVacancyDetailsUseCase,
    private val isVacancyInFavoritesUseCase: IsVacancyInFavoritesUseCase,
    private val addVacancyToFavoritesUseCase: AddVacancyToFavoritesUseCase,
    private val deleteVacancyFromFavoritesUseCase: DeleteVacancyFromFavoritesUseCase,
    private val getFavoriteVacancyByIdUseCase: GetFavoriteVacancyByIdUseCase
) : ViewModel() {

    private val _vacancyState = MutableLiveData<VacancyState>()
    val vacancyState: LiveData<VacancyState> = _vacancyState

    fun loadVacancyDetails(vacancyId: String) {
        _vacancyState.value = VacancyState.Loading
        viewModelScope.launch {
            val inFavorites = isVacancyInFavoritesUseCase(vacancyId)
            if (inFavorites) {
                loadVacancyDetailsFromLocalStorage(vacancyId, inFavorites)
            } else {
                loadVacancyDetailsFromRemoteStorage(vacancyId, inFavorites)
            }
        }
    }

    private suspend fun loadVacancyDetailsFromLocalStorage(vacancyId: String, inFavorites: Boolean) {
        val vacancy = getFavoriteVacancyByIdUseCase(vacancyId)
        if (vacancy == null) {
            _vacancyState.value = VacancyState.Error(ErrorType.VACANCY_NOT_FOUND)
        } else {
            _vacancyState.value = VacancyState.Content(vacancy, inFavorites)
        }
    }

    private suspend fun loadVacancyDetailsFromRemoteStorage(vacancyId: String, inFavorites: Boolean) {
        when (val response = getVacancyDetailsUseCase(vacancyId)) {
            is DomainResult.Success -> { // Изменено с ApiResponse на DomainResult
                _vacancyState.value = VacancyState.Content(response.data, inFavorites)
            }
            is DomainResult.Error -> { // Изменено с ApiResponse на DomainResult
                val errorType = when (response.type) {
                    DomainResult.ErrorType.NOT_FOUND -> ErrorType.VACANCY_NOT_FOUND
                    DomainResult.ErrorType.NETWORK_ERROR -> ErrorType.NETWORK_ERROR
                    DomainResult.ErrorType.SERVER_ERROR,
                    DomainResult.ErrorType.ACCESS_DENIED,
                    DomainResult.ErrorType.DATABASE_ERROR,
                    DomainResult.ErrorType.UNKNOWN_ERROR -> ErrorType.SERVER_ERROR
                }
                _vacancyState.value = VacancyState.Error(errorType)
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
}
