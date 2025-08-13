package com.example.places.autocomplete

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.places.autocomplete.ui.navigation.AppNavigation
import com.example.places.autocomplete.ui.theme.PlacesAutocompleteDemoTheme
import com.example.places.core.domain.repository.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationRepository: LocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationRepository.initialize(this)

        enableEdgeToEdge()
        setContent {
            PlacesAutocompleteDemoTheme {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permission status when app resumes (e.g., returning from settings)
        locationRepository.checkPermissionOnResume()
    }
}