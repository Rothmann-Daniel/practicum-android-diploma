package ru.practicum.android.diploma.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.practicum.android.diploma.data.api.response.AreaResponse
import ru.practicum.android.diploma.data.api.response.FilterIndustryResponse
import ru.practicum.android.diploma.data.api.response.VacancyDetailResponse
import ru.practicum.android.diploma.data.api.response.VacancyResponse

interface ApiService {
    @GET("areas")
    suspend fun getAreas(): List<AreaResponse>

    @GET("industries")
    suspend fun getIndustries(): List<FilterIndustryResponse>

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
    suspend fun getVacancyById(@Path("id") id: String): VacancyDetailResponse
}
