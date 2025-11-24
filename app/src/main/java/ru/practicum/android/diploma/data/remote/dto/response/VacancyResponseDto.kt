package ru.practicum.android.diploma.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// ИСПРАВЛЕНО: API возвращает "items", а не "vacancies"
data class VacancyResponse(
    val found: Int,
    val pages: Int,
    val page: Int,
    @SerializedName("items") // Аннотация для правильного маппинга
    val vacancies: List<VacancyDetailResponseDto>
)

data class VacancyDetailResponseDto(
    val id: String,
    val name: String,
    val description: String?,
    val salary: SalaryResponseDto?,
    val address: AddressResponseDto?,
    val experience: ExperienceResponseDto?,
    val schedule: ScheduleResponseDto?,
    val employment: EmploymentResponseDto?,
    val contacts: ContactsResponseDto?,
    val employer: EmployerResponseDto,
    val area: AreaResponseDto,
    val skills: List<String>?,
    val url: String,
    val industry: FilterIndustryResponseDto?
)

data class SalaryResponseDto(
    val from: Int?,
    val to: Int?,
    val currency: String?
)

data class AddressResponseDto(
    @SerializedName("raw") // API возвращает "raw" для полного адреса
    val fullAddress: String?,
    val city: String?,
    val street: String?,
    val building: String?
)

data class ExperienceResponseDto(
    val id: String,
    val name: String
)

data class ScheduleResponseDto(
    val id: String,
    val name: String
)

data class EmploymentResponseDto(
    val id: String,
    val name: String
)

data class ContactsResponseDto(
    val id: String,
    val name: String,
    val email: String?,
    @SerializedName("phones") // API возвращает массив объектов
    val phone: List<PhoneResponseDto>?
)

data class PhoneResponseDto(
    val formatted: String,
    val comment: String?
)

data class EmployerResponseDto(
    val id: String,
    val name: String,
    val logo: String?
)
