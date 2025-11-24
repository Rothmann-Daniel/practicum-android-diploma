package ru.practicum.android.diploma.data.api.mappers

import ru.practicum.android.diploma.data.api.response.AddressResponse
import ru.practicum.android.diploma.data.api.response.ContactsResponse
import ru.practicum.android.diploma.data.api.response.EmployerResponse
import ru.practicum.android.diploma.data.api.response.EmploymentResponse
import ru.practicum.android.diploma.data.api.response.ExperienceResponse
import ru.practicum.android.diploma.data.api.response.SalaryResponse
import ru.practicum.android.diploma.data.api.response.ScheduleResponse
import ru.practicum.android.diploma.data.api.response.VacancyDetailResponse
import ru.practicum.android.diploma.domain.models.Address
import ru.practicum.android.diploma.domain.models.Contacts
import ru.practicum.android.diploma.domain.models.Employer
import ru.practicum.android.diploma.domain.models.Employment
import ru.practicum.android.diploma.domain.models.Experience
import ru.practicum.android.diploma.domain.models.Salary
import ru.practicum.android.diploma.domain.models.Schedule
import ru.practicum.android.diploma.domain.models.Vacancy

class VacancyMapper(
    private val areaMapper: AreaMapper,
    private val industryMapper: IndustryMapper
) {
    fun toDomain(response: VacancyDetailResponse): Vacancy {
        return Vacancy(
            id = response.id,
            name = response.name,
            description = response.description,
            salary = response.salary?.toDomain(),
            address = response.address?.toDomain(),
            experience = response.experience?.toDomain(),
            schedule = response.schedule?.toDomain(),
            employment = response.employment?.toDomain(),
            contacts = response.contacts?.toDomain(),
            employer = response.employer.toDomain(),
            area = areaMapper.toDomain(response.area),
            skills = response.skills,
            url = response.url,
            industry = industryMapper.toDomain(response.industry)
        )
    }

    fun toEntity() {
        // fun toEntity(domain: Vacancy): VacancyEntity
    }

    fun toDomain() {
        // fun toDomain(entity: VacancyEntity): Vacancy
    }

    private fun SalaryResponse.toDomain(): Salary {
        return Salary(
            from = from,
            to = to,
            currency = currency
        )
    }

    private fun AddressResponse.toDomain(): Address {
        return Address(
            city = city,
            street = street,
            building = building,
            fullAddress = fullAddress
        )
    }

    private fun ExperienceResponse.toDomain(): Experience {
        return Experience(
            id = id,
            name = name
        )
    }

    private fun ScheduleResponse.toDomain(): Schedule {
        return Schedule(
            id = id,
            name = name
        )
    }

    private fun EmploymentResponse.toDomain(): Employment {
        return Employment(
            id = id,
            name = name
        )
    }

    private fun ContactsResponse.toDomain(): Contacts {
        return Contacts(
            id = id,
            name = name,
            email = email,
            phone = phone
        )
    }

    private fun EmployerResponse.toDomain(): Employer {
        return Employer(
            id = id,
            name = name,
            logo = logo
        )
    }
}
