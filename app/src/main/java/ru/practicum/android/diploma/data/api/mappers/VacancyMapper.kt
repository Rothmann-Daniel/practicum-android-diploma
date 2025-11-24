package ru.practicum.android.diploma.data.api.mappers

import ru.practicum.android.diploma.data.api.response.AddressResponse
import ru.practicum.android.diploma.data.api.response.ContactsResponse
import ru.practicum.android.diploma.data.api.response.EmployerResponse
import ru.practicum.android.diploma.data.api.response.EmploymentResponse
import ru.practicum.android.diploma.data.api.response.ExperienceResponse
import ru.practicum.android.diploma.data.api.response.SalaryResponse
import ru.practicum.android.diploma.data.api.response.ScheduleResponse
import ru.practicum.android.diploma.data.api.response.VacancyDetailResponse
import ru.practicum.android.diploma.data.local.entities.VacancyEntity
import ru.practicum.android.diploma.domain.models.Address
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.models.Contacts
import ru.practicum.android.diploma.domain.models.Employer
import ru.practicum.android.diploma.domain.models.Employment
import ru.practicum.android.diploma.domain.models.Experience
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.models.Salary
import ru.practicum.android.diploma.domain.models.Schedule
import ru.practicum.android.diploma.domain.models.Vacancy

class VacancyMapper(
    private val areaMapper: AreaMapper,
    private val industryMapper: IndustryMapper
) {
    /**
     * Преобразует детальный ответ вакансии из API в domain модель
     */
    fun mapToDomain(response: VacancyDetailResponse): Vacancy {
        return Vacancy(
            id = response.id,
            name = response.name,
            description = response.description.orEmpty(),
            salary = response.salary?.toDomain(),
            address = response.address?.toDomain(),
            experience = response.experience?.toDomain(),
            schedule = response.schedule?.toDomain(),
            employment = response.employment?.toDomain(),
            contacts = response.contacts?.toDomain(),
            employer = response.employer.toDomain(),
            area = areaMapper.mapToDomain(response.area),
            skills = response.skills.orEmpty(),
            url = response.url,
            industry = response.industry?.let { industryMapper.mapToDomain(it) }
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
            employerLogo = domain.employer.logo.takeIf { it.isNotBlank() },
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
     * Разбито на несколько методов для снижения сложности
     */
    fun mapFromDb(entity: VacancyEntity): Vacancy {
        return Vacancy(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            salary = mapSalary(entity),
            address = mapAddress(entity),
            experience = mapExperience(entity),
            schedule = mapSchedule(entity),
            employment = mapEmployment(entity),
            contacts = null, // Контакты не сохраняются в БД
            employer = mapEmployer(entity),
            area = mapArea(entity),
            skills = mapSkills(entity),
            url = entity.url,
            industry = mapIndustry(entity)
        )
    }

    private fun mapSalary(entity: VacancyEntity): Salary? {
        return if (entity.salaryFrom != null || entity.salaryTo != null) {
            Salary(
                from = entity.salaryFrom,
                to = entity.salaryTo,
                currency = entity.salaryCurrency
            )
        } else {
            null
        }
    }

    private fun mapAddress(entity: VacancyEntity): Address? {
        return entity.address?.let {
            Address(
                city = "",
                street = "",
                building = "",
                fullAddress = it
            )
        }
    }

    private fun mapExperience(entity: VacancyEntity): Experience? {
        return entity.experienceId?.let {
            Experience(
                id = it,
                name = entity.experienceName.orEmpty()
            )
        }
    }

    private fun mapSchedule(entity: VacancyEntity): Schedule? {
        return entity.scheduleId?.let {
            Schedule(
                id = it,
                name = entity.scheduleName.orEmpty()
            )
        }
    }

    private fun mapEmployment(entity: VacancyEntity): Employment? {
        return entity.employmentId?.let {
            Employment(
                id = it,
                name = entity.employmentName.orEmpty()
            )
        }
    }

    private fun mapEmployer(entity: VacancyEntity): Employer {
        return Employer(
            id = entity.employerId,
            name = entity.employerName,
            logo = entity.employerLogo.orEmpty()
        )
    }

    private fun mapArea(entity: VacancyEntity): Area {
        return Area(
            id = entity.areaId,
            name = entity.areaName,
            parentId = null,
            areas = emptyList()
        )
    }

    private fun mapSkills(entity: VacancyEntity): List<String> {
        return if (entity.skills.isNotBlank()) {
            entity.skills.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    private fun mapIndustry(entity: VacancyEntity): Industry? {
        return if (entity.industryId != null && entity.industryName != null) {
            Industry(
                id = entity.industryId,
                name = entity.industryName
            )
        } else {
            null
        }
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
            city = city.orEmpty(),
            street = street.orEmpty(),
            building = building.orEmpty(),
            fullAddress = fullAddress.orEmpty()
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
        val phoneNumbers = phone?.map { it.formatted }.orEmpty()

        return Contacts(
            id = id,
            name = name,
            email = email.orEmpty(),
            phone = phoneNumbers
        )
    }

    private fun EmployerResponse.toDomain(): Employer {
        return Employer(
            id = id,
            name = name,
            logo = logo.orEmpty()
        )
    }
}
