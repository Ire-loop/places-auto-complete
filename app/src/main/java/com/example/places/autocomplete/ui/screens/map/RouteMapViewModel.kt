package com.example.places.autocomplete.ui.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.places.core.domain.PolylineDecoder
import com.example.places.core.domain.model.LatLngLocation
import com.example.places.core.domain.model.RouteDetails
import com.example.places.core.domain.repository.LocationRepository
import com.example.places.core.domain.repository.RoutesRepository
import com.example.places.core.domain.repository.SharedShipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RouteMapViewModel"

@HiltViewModel
class RouteMapViewModel @Inject constructor(
    private val routesRepository: RoutesRepository,
    val locationRepository: LocationRepository,
    private val sharedShipmentRepository: SharedShipmentRepository
) : ViewModel() {

    val originAddress = sharedShipmentRepository.originAddress
    val destinationAddress = sharedShipmentRepository.destinationAddress

    private val _routeDetails = MutableStateFlow<List<RouteDetails>>(emptyList())
    val routeDetails = _routeDetails.asStateFlow()

    init {
        locationRepository.updateLocationPermissionState()
        fetchRoute(originAddress.value.latLngLocation, destinationAddress.value.latLngLocation)
        Log.d(TAG, "Initialized with origin: ${originAddress.value}, destination: ${destinationAddress.value}")
    }

    fun fetchRoute(origin: LatLngLocation, destination: LatLngLocation) {

        viewModelScope.launch {

            try {

                routesRepository.getRoute(origin, destination)
                    .fold(
                        onSuccess = { routes ->

                            val routeDetails = routes.map { route ->

                                Log.d(TAG, "Routes fetched successfully: ${routes.size} route(s)")

                                val decodedPolyline = PolylineDecoder.decode(route.polyline?.encodedPolyline)

                                val simplifiedPolyline = PolylineDecoder.simplifyPolyline(decodedPolyline)

                                RouteDetails(
                                    distance = route.distanceMeters ?: 0,
                                    duration = route.duration ?: "0s",
                                    polyline = simplifiedPolyline
                                )
                            }

                            _routeDetails.value = routeDetails

                            Log.d(TAG, "Current _routeDetails value: ${_routeDetails.value.size} routes")
                        },
                        onFailure = { error ->

                            Log.e(TAG, "Error fetching route: ${error.message}", error)

                            _routeDetails.value = emptyList()

                        }
                    )
            } catch (error: Exception) {

                Log.e(TAG, "Exception in fetchRoute: ${error.message}", error)

                _routeDetails.value = emptyList()
            }
        }
    }
}