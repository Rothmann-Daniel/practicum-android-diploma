package ru.practicum.android.diploma.data.api.response

import com.google.gson.annotations.SerializedName

// ИСПРАВЛЕНО: API возвращает "items", а не "vacancies"
data class VacancyResponse(
    val found: Int,
    val pages: Int,
    val page: Int,
    @SerializedName("items") // Аннотация для правильного маппинга
    val vacancies: List<VacancyDetailResponse>
)

data class VacancyDetailResponse(
    val id: String,
    val name: String,
    val description: String?,
    val salary: SalaryResponse?,
    val address: AddressResponse?,
    val experience: ExperienceResponse?,
    val schedule: ScheduleResponse?,
    val employment: EmploymentResponse?,
    val contacts: ContactsResponse?,
    val employer: EmployerResponse,
    val area: AreaResponse,
    val skills: List<String>?,
    val url: String,
    val industry: FilterIndustryResponse?
)

data class SalaryResponse(
    val from: Int?,
    val to: Int?,
    val currency: String?
)

data class AddressResponse(
    @SerializedName("raw") // API возвращает "raw" для полного адреса
    val fullAddress: String?,
    val city: String?,
    val street: String?,
    val building: String?
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
    val email: String?,
    @SerializedName("phones") // API возвращает массив объектов
    val phone: List<PhoneResponse>?
)

// Новый класс для телефонов
data class PhoneResponse(
    val formatted: String,
    val comment: String?
)

data class EmployerResponse(
    val id: String,
    val name: String,
    val logo: String?
)
