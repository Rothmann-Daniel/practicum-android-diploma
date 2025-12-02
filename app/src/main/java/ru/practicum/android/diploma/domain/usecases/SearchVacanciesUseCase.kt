package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.VacancySearchRequest
import ru.practicum.android.diploma.domain.models.VacancySearchResult
import ru.practicum.android.diploma.domain.repository.IVacancyRepository

class SearchVacanciesUseCase(private val repository: IVacancyRepository) {

    suspend operator fun invoke(request: VacancySearchRequest): DomainResult<VacancySearchResult> {
        return repository.getVacancies(request)
    }
}
