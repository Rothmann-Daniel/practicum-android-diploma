package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.ApiResponse
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.IVacancyRepository

class GetVacancyDetailsUseCase(private val repository: IVacancyRepository) {

    suspend operator fun invoke(id: String): ApiResponse<Vacancy> {
        return repository.getVacancyById(id)
    }
}
