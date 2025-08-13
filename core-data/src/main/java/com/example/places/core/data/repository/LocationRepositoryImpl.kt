package com.example.places.core.data.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.places.core.domain.repository.LocationRepository
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LocationRepository that handles location permissions and GPS settings.
 *
 * This repository manages:
 * - Location permission requests (fine and coarse)
 * - GPS enable/disable status checks
 * - Permission state monitoring (granted, denied, permanently denied)
 * - Navigation to app settings for manual permission management
 *
 * @property context Application context for permission checks
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : LocationRepository {

    companion object {
        private const val TAG = "LocationRepository"
    }

    // Activity reference required for permission requests and GPS settings
    private lateinit var activity: ComponentActivity

    // Google Play Services settings client for checking GPS status
    private lateinit var settingsClient: SettingsClient

    // Activity result launcher for requesting location permissions
    private lateinit var locationPermissionsLauncher: ActivityResultLauncher<Array<String>>

    // Activity result launcher for GPS enable resolution
    private lateinit var gpsResolutionLauncher: ActivityResultLauncher<IntentSenderRequest>

    // State flow tracking whether location permission is currently granted
    private val _locationPermissionGranted = MutableStateFlow(hasLocationPermission())
    override val locationPermissionGranted = _locationPermissionGranted.asStateFlow()

    // State flow tracking whether location permission has been permanently denied
    // (user selected "Don't ask again" in permission dialog)
    private val _isLocationPermissionPermanentlyDenied = MutableStateFlow(false)
    override val isLocationPermissionPermanentlyDenied =
        _isLocationPermissionPermanentlyDenied.asStateFlow()

    /**
     * Initializes the repository with the activity context needed for permission requests.
     * Must be called before any other repository methods.
     *
     * @param activity The activity that will handle permission requests and GPS settings
     */
    override fun initialize(activity: ComponentActivity) {
        Log.d(
            TAG,
            "Initializing LocationRepository with activity: ${activity.javaClass.simpleName}"
        )
        this.activity = activity

        // Register permission request launcher
        locationPermissionsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            Log.d(TAG, "Permission request result received: $permissions")

            val fineLocationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            Log.d(TAG, "Fine location permission granted: $fineLocationGranted")

            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            Log.d(TAG, "Coarse location permission granted: $coarseLocationGranted")

            _locationPermissionGranted.value = fineLocationGranted || coarseLocationGranted
            Log.i(TAG, "Location permission status updated: ${_locationPermissionGranted.value}")
        }
        // Register GPS resolution launcher
        gpsResolutionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            Log.d(TAG, "GPS resolution result: ${result.resultCode}")

            if (result.resultCode == ComponentActivity.RESULT_OK) {
                // GPS was successfully enabled
                Log.i(TAG, "GPS enabled successfully")
                _locationPermissionGranted.value = hasLocationPermission()
            } else {
                // Handle case where user did not enable GPS
                Log.w(TAG, "User declined to enable GPS")
                _locationPermissionGranted.value = false
            }
        }

        settingsClient = LocationServices.getSettingsClient(activity)
        Log.d(TAG, "LocationRepository initialization complete")
    }

    /**
     * Updates the location permission state and requests permission if not granted.
     * Also checks if permission has been permanently denied.
     */
    override fun updateLocationPermissionState() {
        Log.d(TAG, "Updating location permission state")

        // Update the state of whether the permission is permanently denied
        isLocationPermissionPermanentlyDenied()

        // Note: shouldShowRequestPermissionRationale returns false both when:
        // 1. Permission has never been requested
        // 2. User selected "Don't ask again"
        // We distinguish these cases based on current permission state
        _isLocationPermissionPermanentlyDenied.value = ActivityCompat
            .shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        Log.d(TAG, "Permission permanently denied: ${_isLocationPermissionPermanentlyDenied.value}")

        if (!_locationPermissionGranted.value) {
            // If permission is not granted, request it
            Log.i(TAG, "Requesting location permissions")
            locationPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            // If permission is granted, update the state accordingly
            Log.d(TAG, "Location permission already granted")
            _locationPermissionGranted.value = hasLocationPermission()
        }
    }

    /**
     * Checks if the app has location permission (either fine or coarse).
     *
     * @return true if either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION is granted
     */
    private fun hasLocationPermission(): Boolean {
        // Check if either fine or coarse location permission is granted
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Check if coarse location permission is granted
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasPermission = fineLocationPermission || coarseLocationPermission
        Log.d(
            TAG,
            "hasLocationPermission: fine=$fineLocationPermission, coarse=$coarseLocationPermission, result=$hasPermission"
        )
        return hasPermission
    }

    /**
     * Checks if location permission has been permanently denied.
     * This happens when user selects "Don't ask again" in the permission dialog.
     */
    private fun isLocationPermissionPermanentlyDenied() {
        // Check if the permission is permanently denied
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        _isLocationPermissionPermanentlyDenied.update {
            !shouldShowRationale
        }

        Log.d(
            TAG,
            "Permission permanently denied check: shouldShowRationale=$shouldShowRationale, permanentlyDenied=${!shouldShowRationale}"
        )
    }

    /**
     * Checks if GPS is enabled and prompts the user to enable it if not.
     * Uses Google Play Services to show a system dialog for enabling GPS.
     *
     * @return true if GPS is enabled or resolution is attempted, false otherwise
     */
    override fun checkAndEnableGps(): Boolean {
        Log.d(TAG, "Checking GPS status")

        var gpsStatus = false

        // Create a location request with low power priority
        // This is just for checking GPS status, not for actual location updates
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .setMaxUpdates(1)
            .build()
        Log.d(TAG, "Created location request for GPS check")

        // Build settings request to check if GPS is enabled
        // setAlwaysShow(true) ensures dialog is shown even if GPS is already on
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        // Check location settings asynchronously
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                Log.i(TAG, "GPS is enabled")
                gpsStatus = true
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "GPS check failed: ${exception.message}")

                if (exception is ResolvableApiException) {
                    // GPS can be enabled via Google Play Services dialog
                    Log.i(TAG, "Launching GPS resolution dialog")

                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()

                    gpsResolutionLauncher.launch(intentSenderRequest)

                    gpsStatus = true
                } else {
                    // GPS cannot be enabled via dialog, open system settings
                    Log.w(TAG, "GPS cannot be resolved via dialog, opening system settings")

                    activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

                    gpsStatus = false
                }
            }

        Log.d(TAG, "GPS check complete, status: $gpsStatus")
        return gpsStatus
    }

    /**
     * Checks and updates permission state when the app resumes.
     * Should be called in onResume() to detect permission changes made in settings.
     */
    override fun checkPermissionOnResume() {
        Log.d(TAG, "Checking permission on resume")

        // Check current permission state
        val currentPermissionGranted = hasLocationPermission()

        // Update the permission granted state
        _locationPermissionGranted.value = currentPermissionGranted
        Log.i(TAG, "Permission state on resume: granted=$currentPermissionGranted")

        // If permission is now granted, reset permanently denied flag
        if (currentPermissionGranted) {
            Log.d(TAG, "Permission granted, resetting permanently denied flag")
            _isLocationPermissionPermanentlyDenied.value = false
        } else {
            // Re-check if it's permanently denied
            Log.d(TAG, "Permission not granted, checking if permanently denied")
            isLocationPermissionPermanentlyDenied()
        }
    }

    /**
     * Opens the app's settings page where users can manually grant permissions.
     * Useful when permission has been permanently denied.
     */
    override fun openAppSettings() {
        Log.i(TAG, "Opening app settings for package: ${activity.packageName}")

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            this.setData(Uri.fromParts("package", activity.packageName, null))
        }

        try {
            activity.startActivity(intent)
            Log.d(TAG, "Successfully launched app settings")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app settings", e)
        }
    }
}