package com.example.places.core.domain.repository

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    /**
     * Observable state for location permission status
     */
    val locationPermissionGranted: StateFlow<Boolean>

    /**
     * Observable state for whether the permission is permanently denied
     */
    val isLocationPermissionPermanentlyDenied: StateFlow<Boolean>

    fun initialize(activity: ComponentActivity)

    /**
     * Update the current location permission state
     */
    fun updateLocationPermissionState()
    
    /**
     * Check permission status when app resumes (e.g., from settings)
     */
    fun checkPermissionOnResume()

    /**
     * Update the current GPS permission state
     */
    fun checkAndEnableGps(): Boolean

    /**
     * Open the app settings screen for the user to change permissions
     */
    fun openAppSettings()
}