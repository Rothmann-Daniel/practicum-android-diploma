package ru.practicum.android.diploma.data.api.mappers

import ru.practicum.android.diploma.data.api.response.*
import ru.practicum.android.diploma.data.local.entities.VacancyEntity
import ru.practicum.android.diploma.domain.models.*

class VacancyMapper(
    private val areaMapper: AreaMapper,
    private val industryMapper: IndustryMapper
) {
    /**
     * Преобразует детальный ответ вакансии из API в domain модель
     */
    fun toDomain(response: VacancyDetailResponse): Vacancy {
        return Vacancy(
            id = response.id,
            name = response.name,
            description = response.description ?: "",
            salary = response.salary?.toDomain(),
            address = response.address?.toDomain(),
            experience = response.experience?.toDomain(),
            schedule = response.schedule?.toDomain(),
            employment = response.employment?.toDomain(),
            contacts = response.contacts?.toDomain(),
            employer = response.employer.toDomain(),
            area = areaMapper.toDomain(response.area),
            skills = response.skills ?: emptyList(),
            url = response.url,
            industry = response.industry?.let { industryMapper.toDomain(it) }
        )
    }

    /**
     * Преобразует domain модель вакансии в entity для БД
     */
    fun toEntity(domain: Vacancy): VacancyEntity {
        return VacancyEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            salaryFrom = domain.salary?.from,
            salaryTo = domain.salary?.to,
            salaryCurrency = domain.salary?.currency,
            address = domain.address?.fullAddress,
            experienceId = domain.experience?.id,
            experienceName = domain.experience?.name,
            scheduleId = domain.schedule?.id,
            scheduleName = domain.schedule?.name,
            employmentId = domain.employment?.id,
            employmentName = domain.employment?.name,
            employerId = domain.employer.id,
            employerName = domain.employer.name,
            employerLogo = domain.employer.logo.takeIf { it.isNotBlank() }, // nullable
            areaId = domain.area.id,
            areaName = domain.area.name,
            skills = domain.skills.joinToString(","),
            url = domain.url,
            industryId = domain.industry?.id,
            industryName = domain.industry?.name
        )
    }

    /**
     * Преобразует entity из БД в domain модель вакансии
     */
    fun toDomain(entity: VacancyEntity): Vacancy {
        return Vacancy(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            salary = if (entity.salaryFrom != null || entity.salaryTo != null) {
                Salary(
                    from = entity.salaryFrom,
                    to = entity.salaryTo,
                    currency = entity.salaryCurrency
                )
            } else null,
            address = entity.address?.let {
                Address(
                    city = "",
                    street = "",
                    building = "",
                    fullAddress = it
                )
            },
            experience = entity.experienceId?.let {
                Experience(
                    id = it,
                    name = entity.experienceName ?: ""
                )
            },
            schedule = entity.scheduleId?.let {
                Schedule(
                    id = it,
                    name = entity.scheduleName ?: ""
                )
            },
            employment = entity.employmentId?.let {
                Employment(
                    id = it,
                    name = entity.employmentName ?: ""
                )
            },
            contacts = null, // Контакты не сохраняются в БД
            employer = Employer(
                id = entity.employerId,
                name = entity.employerName,
                logo = entity.employerLogo ?: ""
            ),
            area = Area(
                id = entity.areaId,
                name = entity.areaName,
                parentId = null,
                areas = emptyList()
            ),
            skills = if (entity.skills.isNotBlank()) {
                entity.skills.split(",").filter { it.isNotBlank() }
            } else {
                emptyList()
            },
            url = entity.url,
            industry = if (entity.industryId != null && entity.industryName != null) {
                Industry(
                    id = entity.industryId,
                    name = entity.industryName
                )
            } else null
        )
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
            city = city ?: "",
            street = street ?: "",
            building = building ?: "",
            fullAddress = fullAddress ?: ""
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
        // Преобразуем массив объектов PhoneResponse в список строк
        val phoneNumbers = phone?.map { it.formatted } ?: emptyList()

        return Contacts(
            id = id,
            name = name,
            email = email ?: "",
            phone = phoneNumbers
        )
    }

    private fun EmployerResponse.toDomain(): Employer {
        return Employer(
            id = id,
            name = name,
            logo = logo ?: ""
        )
    }
}
