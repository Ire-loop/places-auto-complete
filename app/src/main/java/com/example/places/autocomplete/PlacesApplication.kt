package com.example.places.autocomplete

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

// Application class with Hilt integration and Places API initialization
@HiltAndroidApp
class PlacesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the Places API with the provided API key
        Places.initializeWithNewPlacesApiEnabled(applicationContext, BuildConfig.PLACES_API_KEY)
    }
}