package ru.practicum.android.diploma.presentation.vacancy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.remote.dto.response.ApiResponse
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.usecases.GetVacancyDetailsUseCase

class VacancyViewModel(
    private val getVacancyDetailsUseCase: GetVacancyDetailsUseCase
) : ViewModel() {

    private val _vacancyState = MutableLiveData<VacancyState>()
    val vacancyState: LiveData<VacancyState> = _vacancyState

    fun loadVacancyDetails(vacancyId: String) {
        _vacancyState.value = VacancyState.Loading

        viewModelScope.launch {
            when (val response = getVacancyDetailsUseCase(vacancyId)) {
                is ApiResponse.Success -> {
                    _vacancyState.value = VacancyState.Content(response.data)
                }
                is ApiResponse.Error -> {
                    val errorType = when {
                        response.code == 404 -> ErrorType.VACANCY_NOT_FOUND
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

    sealed class VacancyState {
        object Loading : VacancyState()
        data class Content(val vacancy: Vacancy) : VacancyState()
        data class Error(val type: ErrorType) : VacancyState()
    }

    enum class ErrorType {
        VACANCY_NOT_FOUND,
        NETWORK_ERROR,
        SERVER_ERROR
    }
}
