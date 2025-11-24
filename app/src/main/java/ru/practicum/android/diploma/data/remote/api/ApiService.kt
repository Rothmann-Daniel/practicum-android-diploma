package ru.practicum.android.diploma.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.practicum.android.diploma.data.remote.dto.response.AreaResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.FilterIndustryResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.VacancyDetailResponseDto
import ru.practicum.android.diploma.data.remote.dto.response.VacancyResponse

interface ApiService {
    @GET("areas")
    suspend fun getAreas(): List<AreaResponseDto>

    @GET("industries")
    suspend fun getIndustries(): List<FilterIndustryResponseDto>

    @GET("vacancies")
    suspend fun getVacancies(
        @Query("area") area: Int? = null,
        @Query("industry") industry: Int? = null,
        @Query("text") text: String? = null,
        @Query("salary") salary: Int? = null,
        @Query("page") page: Int? = null,
        @Query("only_with_salary") onlyWithSalary: Boolean? = null
    ): VacancyResponse

    @GET("vacancies/{id}")
    suspend fun getVacancyById(@Path("id") id: String): VacancyDetailResponseDto
}
