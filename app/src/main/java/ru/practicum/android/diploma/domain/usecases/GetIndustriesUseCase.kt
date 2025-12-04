package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.DomainResult
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.IIndustryRepository

class GetIndustriesUseCase(private val repository: IIndustryRepository) {
    suspend operator fun invoke(): DomainResult<List<Industry>> {
        return repository.getIndustries()
    }
}
