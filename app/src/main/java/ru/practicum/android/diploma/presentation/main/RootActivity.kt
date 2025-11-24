package ru.practicum.android.diploma.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {

    private val binding: ActivityRootBinding by lazy {
        ActivityRootBinding.inflate(layoutInflater)
    }

    // Список id фрагментов, где BottomNavigationView должен быть виден
    private val bottomNavVisibleDestinations = setOf(
        R.id.searchFragment,
        R.id.favoriteVacancyFragment,
        R.id.teamFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.rootFragmentContainerView
        ) as NavHostFragment
        val navController = navHostFragment.navController

        // Подключаем BottomNavigationView к NavController
        binding.bottomNavigationView.setupWithNavController(navController)

        // Слушаем изменения фрагмента и скрываем/показываем меню
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in bottomNavVisibleDestinations) {
                binding.bottomNavigationView.showNav()
            } else {
                binding.bottomNavigationView.hideNav()
            }
        }

        // Пример использования access token для HeadHunter API
        networkRequestExample(accessToken = BuildConfig.API_ACCESS_TOKEN)
    }

    private fun networkRequestExample(accessToken: String) {
        // ...
    }

    // Функции расширения для показа/скрытия BottomNavigationView
    private fun com.google.android.material.bottomnavigation.BottomNavigationView.showNav() {
        this.visibility = android.view.View.VISIBLE
    }

    private fun com.google.android.material.bottomnavigation.BottomNavigationView.hideNav() {
        this.visibility = android.view.View.GONE
    }
}

