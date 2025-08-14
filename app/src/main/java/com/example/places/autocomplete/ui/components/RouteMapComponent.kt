package com.example.places.autocomplete.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.places.core.domain.model.RouteDetails
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun RouteMapComponent(
    originLocation: LatLng,
    destinationLocation: LatLng,
    routeDetails: List<RouteDetails>,
    checkAndEnableGps: () -> Boolean,
    hasLocationPermission: Boolean
) {

    // Remember marker states to avoid recreation during recomposition
    val originMarkerState = remember(originLocation) {
        MarkerState(position = originLocation)
    }
    val destinationMarkerState = remember(destinationLocation) {
        MarkerState(position = destinationLocation)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(originLocation.latitude, originLocation.longitude),
            12f
        )
    }

    // Update camera position when route details change
    LaunchedEffect(routeDetails) {
        if (routeDetails.isNotEmpty() && routeDetails[0].polyline.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()

            // Add all points from the polyline to the bounds
            routeDetails[0].polyline.forEach { point ->
                boundsBuilder.include(point)
            }

            // Also include origin and destination to ensure they're visible
            boundsBuilder.include(originLocation)
            boundsBuilder.include(destinationLocation)

            val bounds = boundsBuilder.build()

            // Animate camera to show the entire route with padding
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 100), // 100px padding
                durationMs = 1000 // 1 second animation
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = hasLocationPermission
        ),
        onMyLocationButtonClick = checkAndEnableGps
    ) {

        // Add origin marker
        CircleMapMarker(markerState = originMarkerState, title = "Origin")

        // Add destination marker
        Marker(state = destinationMarkerState, title = "Destination", snippet = "End point")

        // Draw route polyline if available
        if (routeDetails.isNotEmpty()) {
            // Draw border polyline
            Polyline(
                points = routeDetails[0].polyline,
                color = Color(0xFF1976D2), // Darker blue for border
                width = 16f, // Wider for border effect
                geodesic = true,
                zIndex = 1f, // Lower z-index for border
                startCap = RoundCap(), // Rounded start cap
                endCap = RoundCap(), // Rounded end cap
                jointType = JointType.ROUND // Int value for smooth corners
            )

            // Draw the main route polyline on top
            Polyline(
                points = routeDetails[0].polyline,
                color = Color(0xFF4285F4), // Google Maps signature blue
                width = 12f, // Standard route width
                geodesic = true,
                clickable = true,
                zIndex = 2f, // Higher z-index to appear on top
                startCap = RoundCap(), // Rounded start cap
                endCap = RoundCap(), // Rounded end cap
                jointType = JointType.ROUND // Int value for smooth corners
            )
        }
    }
}