package ru.practicum.android.diploma.data.remote.mapper

import ru.practicum.android.diploma.data.remote.dto.response.AddressResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.ContactsResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.EmployerResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.EmploymentResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.ExperienceResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.SalaryResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.ScheduleResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.VacancyDetailResponseDto
import ru.practicum.android.diploma.domain.models.Address
import ru.practicum.android.diploma.domain.models.Contacts
import ru.practicum.android.diploma.domain.models.Employer
import ru.practicum.android.diploma.domain.models.Employment
import ru.practicum.android.diploma.domain.models.Experience
import ru.practicum.android.diploma.domain.models.Salary
import ru.practicum.android.diploma.domain.models.Schedule
import ru.practicum.android.diploma.domain.models.Vacancy

class VacancyRemoteMapper(
    private val areaRemoteMapper: AreaRemoteMapper,
    private val industryRemoteMapper: IndustryRemoteMapper
) {
    /**
     * Преобразует детальный ответ вакансии из API в domain модель
     */
    fun mapToDomain(dto: VacancyDetailResponseDto): Vacancy {
        return Vacancy(
            id = dto.id,
            name = dto.name,
            description = dto.description.orEmpty(),
            salary = dto.salary?.toDomain(),
            address = dto.address?.toDomain(),
            experience = dto.experience?.toDomain(),
            schedule = dto.schedule?.toDomain(),
            employment = dto.employment?.toDomain(),
            contacts = dto.contacts?.toDomain(),
            employer = dto.employer.toDomain(),
            area = areaRemoteMapper.mapToDomain(dto.area),
            skills = dto.skills.orEmpty(),
            url = dto.url,
            industry = dto.industry?.let { industryRemoteMapper.mapToDomain(it) }
        )
    }

    private fun SalaryResponseDto.toDomain(): Salary {
        return Salary(
            from = from,
            to = to,
            currency = currency
        )
    }

    private fun AddressResponseDto.toDomain(): Address {
        return Address(
            city = city.orEmpty(),
            street = street.orEmpty(),
            building = building.orEmpty(),
            fullAddress = fullAddress.orEmpty()
        )
    }

    private fun ExperienceResponseDto.toDomain(): Experience {
        return Experience(
            id = id,
            name = name
        )
    }

    private fun ScheduleResponseDto.toDomain(): Schedule {
        return Schedule(
            id = id,
            name = name
        )
    }

    private fun EmploymentResponseDto.toDomain(): Employment {
        return Employment(
            id = id,
            name = name
        )
    }

    private fun ContactsResponseDto.toDomain(): Contacts {
        val phoneNumbers = phone?.map { it.formatted }.orEmpty()

        return Contacts(
            id = id,
            name = name,
            email = email.orEmpty(),
            phone = phoneNumbers
        )
    }

    private fun EmployerResponseDto.toDomain(): Employer {
        return Employer(
            id = id,
            name = name,
            logo = logo.orEmpty()
        )
    }
}
