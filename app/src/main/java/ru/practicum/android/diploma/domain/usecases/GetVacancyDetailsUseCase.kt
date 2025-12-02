package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.IVacancyRepository

class GetVacancyDetailsUseCase(private val repository: IVacancyRepository) {

    suspend operator fun invoke(id: String): DomainResult<Vacancy> {
        return repository.getVacancyById(id)  // Теперь возвращает DomainResult
    }
}
