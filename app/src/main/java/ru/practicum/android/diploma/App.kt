package ru.practicum.android.diploma

import android.app.Application
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import ru.practicum.android.diploma.di.appModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // ПРОВЕРКА ТОКЕНА
        Log.d("TOKEN_DEBUG", "=== TOKEN CHECK ===")
        Log.d("TOKEN_DEBUG", "Token: ${BuildConfig.API_ACCESS_TOKEN}")
        Log.d("TOKEN_DEBUG", "Token length: ${BuildConfig.API_ACCESS_TOKEN.length}")
        Log.d("TOKEN_DEBUG", "First 10 chars: ${BuildConfig.API_ACCESS_TOKEN.take(10)}...")
        Log.d("TOKEN_DEBUG", "===================")

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}
