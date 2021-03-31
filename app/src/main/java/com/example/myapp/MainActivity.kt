package com.example.myapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.myapp.repository.AppListRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appListRepository: AppListRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            Timber.i("Main Activity is created.")
            Timber.i((savedInstanceState?.get("REVIEW_OF_ORIGINAL_INDEX1") as String?) ?: "")
            if (appListRepository.getLatestAppCardList() == null) {
                val navController =findNavController(R.id.myNavHostFragment)
                val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
                navController.graph = navGraph.apply {
                    startDestination = R.id.app_navigation
                }
            }
        }
        setContentView(R.layout.activity_main)
    }
}
