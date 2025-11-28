package ru.practicum.android.diploma.di

import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.data.local.database.AppDatabase
import ru.practicum.android.diploma.data.local.mapper.AreaLocalMapper
import ru.practicum.android.diploma.data.local.mapper.IndustryLocalMapper
import ru.practicum.android.diploma.data.local.mapper.VacancyLocalMapper
import ru.practicum.android.diploma.data.remote.api.ApiService
import ru.practicum.android.diploma.data.remote.mapper.AreaRemoteMapper
import ru.practicum.android.diploma.data.remote.mapper.IndustryRemoteMapper
import ru.practicum.android.diploma.data.remote.mapper.VacancyRemoteMapper
import ru.practicum.android.diploma.data.remote.mapper.VacancyRequestMapper
import ru.practicum.android.diploma.data.repository.AreaRepositoryImpl
import ru.practicum.android.diploma.data.repository.IndustryRepositoryImpl
import ru.practicum.android.diploma.data.repository.VacancyRepositoryImpl
import ru.practicum.android.diploma.domain.repository.IAreaRepository
import ru.practicum.android.diploma.domain.repository.IIndustryRepository
import ru.practicum.android.diploma.domain.repository.IVacancyRepository
import ru.practicum.android.diploma.domain.usecases.GetCachedVacanciesUseCase
import ru.practicum.android.diploma.domain.usecases.GetVacancyDetailsUseCase
import ru.practicum.android.diploma.domain.usecases.SearchVacanciesUseCase
import ru.practicum.android.diploma.presentation.search.SearchViewModel
import java.util.concurrent.TimeUnit

// Константы для таймаутов
private const val CONNECT_TIMEOUT_SECONDS = 30L
private const val READ_TIMEOUT_SECONDS = 30L
private const val WRITE_TIMEOUT_SECONDS = 30L

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

    // Remote Mappers
    single { AreaRemoteMapper() }
    single { IndustryRemoteMapper() }
    single { VacancyRemoteMapper(get(), get()) }
    single { VacancyRequestMapper() }

    // Local Mappers
    single { AreaLocalMapper() }
    single { IndustryLocalMapper() }
    single { VacancyLocalMapper() }

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
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://practicum-diploma-8bc38133faba.herokuapp.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Repositories
    single<IAreaRepository> { AreaRepositoryImpl(get(), get(), get(), get()) }
    single<IIndustryRepository> { IndustryRepositoryImpl(get(), get(), get(), get()) }
    single<IVacancyRepository> { VacancyRepositoryImpl(get(), get(), get(), get(), get()) }

    // Use Cases
    single { SearchVacanciesUseCase(get()) }
    single { GetVacancyDetailsUseCase(get()) }
    single { GetCachedVacanciesUseCase(get()) }

    // ViewModels
    viewModel {
        SearchViewModel(
            searchUseCase = get<SearchVacanciesUseCase>()
        )
    }
}
