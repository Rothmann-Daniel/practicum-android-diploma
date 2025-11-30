package ru.practicum.android.diploma.data.local.mapper

import ru.practicum.android.diploma.data.local.entities.VacancyInFavoritesEntity
import ru.practicum.android.diploma.domain.models.Address
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.models.Employer
import ru.practicum.android.diploma.domain.models.Employment
import ru.practicum.android.diploma.domain.models.Experience
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.models.Salary
import ru.practicum.android.diploma.domain.models.Schedule
import ru.practicum.android.diploma.domain.models.Vacancy

class FavoritesLocalMapper {
    /**
     * Преобразует domain модель вакансии в entity для БД
     */
    fun toEntity(domain: Vacancy): VacancyInFavoritesEntity {
        return VacancyInFavoritesEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            salaryFrom = domain.salary?.from,
            salaryTo = domain.salary?.to,
            salaryCurrency = domain.salary?.currency,
            address = domain.address?.fullAddress,
            city = domain.address?.city,
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
     */
    fun mapFromDb(entity: VacancyInFavoritesEntity): Vacancy {
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

    private fun mapSalary(entity: VacancyInFavoritesEntity): Salary? {
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

    private fun mapAddress(entity: VacancyInFavoritesEntity): Address? {
        if (entity.address == null && entity.city == null) return null
        return Address(
            city = entity.city ?: "",
            street = "",
            building = "",
            fullAddress = entity.address ?: ""
        )
    }

    private fun mapExperience(entity: VacancyInFavoritesEntity): Experience? {
        return entity.experienceId?.let {
            Experience(
                id = it,
                name = entity.experienceName.orEmpty()
            )
        }
    }

    private fun mapSchedule(entity: VacancyInFavoritesEntity): Schedule? {
        return entity.scheduleId?.let {
            Schedule(
                id = it,
                name = entity.scheduleName.orEmpty()
            )
        }
    }

    private fun mapEmployment(entity: VacancyInFavoritesEntity): Employment? {
        return entity.employmentId?.let {
            Employment(
                id = it,
                name = entity.employmentName.orEmpty()
            )
        }
    }

    private fun mapEmployer(entity: VacancyInFavoritesEntity): Employer {
        return Employer(
            id = entity.employerId,
            name = entity.employerName,
            logo = entity.employerLogo.orEmpty()
        )
    }

    private fun mapArea(entity: VacancyInFavoritesEntity): Area {
        return Area(
            id = entity.areaId,
            name = entity.areaName,
            parentId = null,
            areas = emptyList()
        )
    }

    private fun mapSkills(entity: VacancyInFavoritesEntity): List<String> {
        return if (entity.skills.isNotBlank()) {
            entity.skills.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    private fun mapIndustry(entity: VacancyInFavoritesEntity): Industry? {
        return if (entity.industryId != null && entity.industryName != null) {
            Industry(
                id = entity.industryId,
                name = entity.industryName
            )
        } else {
            null
        }
    }
}
