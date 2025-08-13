package com.example.places.autocomplete.ui.screens.map

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.places.autocomplete.ui.components.RouteMapComponent
import com.google.android.gms.maps.model.LatLng

@Composable
fun RouteMapScreen(
    routeMapViewModel: RouteMapViewModel = hiltViewModel()
) {

    val isPermanentlyDenied = routeMapViewModel
        .locationRepository.isLocationPermissionPermanentlyDenied.collectAsState().value

    val hasLocationPermission = routeMapViewModel
        .locationRepository.locationPermissionGranted.collectAsState().value

    var showPermissionDialog by remember { mutableStateOf(true) }

    if (showPermissionDialog && isPermanentlyDenied) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Location permission is permanently denied. Please enable it in app settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        routeMapViewModel.locationRepository.openAppSettings()
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val originLocation = routeMapViewModel.originAddress.collectAsState().value
        .let { LatLng(it.latLngLocation.latitude, it.latLngLocation.longitude) }

    val destinationLocation = routeMapViewModel.destinationAddress.collectAsState().value
        .let { LatLng(it.latLngLocation.latitude, it.latLngLocation.longitude) }



    val routeDetails = routeMapViewModel.routeDetails.collectAsState().value

    RouteMapComponent(
        originLocation = originLocation,
        destinationLocation = destinationLocation,
        routeDetails = routeDetails,
        checkAndEnableGps = { routeMapViewModel.locationRepository.checkAndEnableGps() },
        hasLocationPermission = hasLocationPermission
    )
}