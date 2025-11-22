package ru.practicum.android.diploma.data.api.response

data class VacancyResponse(
    val found: Int,
    val pages: Int,
    val page: Int,
    val vacancies: List<VacancyDetailResponse>
)

data class VacancyDetailResponse(
    val id: String,
    val name: String,
    val description: String,
    val salary: SalaryResponse?,
    val address: AddressResponse?,
    val experience: ExperienceResponse?,
    val schedule: ScheduleResponse?,
    val employment: EmploymentResponse?,
    val contacts: ContactsResponse?,
    val employer: EmployerResponse,
    val area: FilterAreaResponse,
    val skills: List<String>,
    val url: String,
    val industry: FilterIndustryResponse
)

data class SalaryResponse(
    val from: Int?,
    val to: Int?,
    val currency: String?
)

data class AddressResponse(
    val city: String,
    val street: String,
    val building: String,
    val fullAddress: String
)

data class ExperienceResponse(
    val id: String,
    val name: String
)

data class ScheduleResponse(
    val id: String,
    val name: String
)

data class EmploymentResponse(
    val id: String,
    val name: String
)

data class ContactsResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: List<String>
)

data class EmployerResponse(
    val id: String,
    val name: String,
    val logo: String
)
