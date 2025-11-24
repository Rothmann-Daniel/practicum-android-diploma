package ru.practicum.android.diploma.di

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.data.api.ApiService

val appModule = module {
    // Database

    // DAOs

    // Mappers

    // API
    single {
        // Создаем OkHttpClient с интерсептором для авторизации
        println("DEBUG: Token = ${BuildConfig.API_ACCESS_TOKEN}") // Проверка в логах сборки
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithToken = originalRequest.newBuilder()
                    .header("Authorization", "Bearer ${BuildConfig.API_ACCESS_TOKEN}")
                    .build()
                chain.proceed(requestWithToken)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        Retrofit.Builder()
            .baseUrl("https://practicum-diploma-8bc38133faba.herokuapp.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Repositories
}
