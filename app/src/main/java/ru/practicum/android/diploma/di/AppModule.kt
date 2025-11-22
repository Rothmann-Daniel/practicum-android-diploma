package ru.practicum.android.diploma.di

import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.data.api.ApiService
import ru.practicum.android.diploma.data.api.mappers.AreaMapper
import ru.practicum.android.diploma.data.api.mappers.IndustryMapper
import ru.practicum.android.diploma.data.api.mappers.VacancyMapper
import ru.practicum.android.diploma.data.local.AppDatabase
import ru.practicum.android.diploma.data.repository.AreaRepositoryImpl
import ru.practicum.android.diploma.data.repository.IndustryRepositoryImpl
import ru.practicum.android.diploma.data.repository.VacancyRepositoryImpl
import ru.practicum.android.diploma.domain.repository.IAreaRepository
import ru.practicum.android.diploma.domain.repository.IIndustryRepository
import ru.practicum.android.diploma.domain.repository.IVacancyRepository
import java.util.concurrent.TimeUnit

val appModule = module {
    // Database - ВАЖНО: добавлен fallbackToDestructiveMigration для пересоздания БД
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Пересоздаёт БД при изменении схемы
            .build()
    }

    // DAOs
    single { get<AppDatabase>().areaDao() }
    single { get<AppDatabase>().industryDao() }
    single { get<AppDatabase>().vacancyDao() }

    // Mappers
    single { AreaMapper() }
    single { IndustryMapper() }
    single { VacancyMapper(get(), get()) }

    // API
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithToken = originalRequest.newBuilder()
                    .header("Authorization", "Bearer ${BuildConfig.API_ACCESS_TOKEN}")
                    .build()
                chain.proceed(requestWithToken)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://practicum-diploma-8bc38133faba.herokuapp.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Repositories
    single<IAreaRepository> { AreaRepositoryImpl(get(), get(), get()) }
    single<IIndustryRepository> { IndustryRepositoryImpl(get(), get(), get()) }
    single<IVacancyRepository> { VacancyRepositoryImpl(get(), get(), get()) }
}
