package ru.practicum.android.diploma.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.databinding.ActivityRootBinding
import ru.practicum.android.diploma.R

class RootActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.rootFragmentContainerView
        ) as NavHostFragment
        val navController = navHostFragment.navController

        // Пример использования access token для HeadHunter API
        networkRequestExample(accessToken = BuildConfig.API_ACCESS_TOKEN)
    }

    private fun networkRequestExample(accessToken: String) {
        // ...
    }
// init epic0_Add_Icon
}
