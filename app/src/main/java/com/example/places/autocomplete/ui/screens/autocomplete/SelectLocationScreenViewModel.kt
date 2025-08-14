package com.example.places.autocomplete.ui.screens.autocomplete

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.places.core.domain.model.LatLngLocation
import com.example.places.core.domain.model.LocationField
import com.example.places.core.domain.repository.GeocodingRepository
import com.example.places.core.domain.repository.SharedShipmentRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectLocationScreenViewModel @Inject constructor(
    private val placesClient: PlacesClient,
    private val geocodingRepository: GeocodingRepository,
    val sharedShipmentRepository: SharedShipmentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SelectLocationVM"
    }

    val originLocation = sharedShipmentRepository.originAddress
    val destinationLocation = sharedShipmentRepository.destinationAddress

    private val _activeField = MutableStateFlow(LocationField.ORIGIN)
    val activeField = _activeField.asStateFlow()

    // India location bias
    private val indiaLocationBias: LocationBias = RectangularBounds.newInstance(
        LatLng(8.4, 68.7),   // SW lat, lng (Southern tip of India, Western border)
        LatLng(35.5, 97.25)  // NE lat, lng (Northern Kashmir, Eastern border)
    )

    fun setActiveField(field: LocationField) {
        Log.d(TAG, "Setting active field to: $field")
        _activeField.value = field
    }

    fun updateOriginSearchText(text: String) {
        sharedShipmentRepository.updateOriginAddressSearchText(text)
        _activeField.value = LocationField.ORIGIN
        searchPlaces(text, true)
    }

    fun updateDestinationSearchText(text: String) {
        sharedShipmentRepository.updateDestinationAddressSearchText(text)
        _activeField.value = LocationField.DESTINATION
        searchPlaces(text, false)
    }

    private fun searchPlaces(query: String, isOrigin: Boolean) {
        if (query.isBlank()) {
            Log.d(
                TAG,
                "Search query is blank, clearing predictions for ${if (isOrigin) "origin" else "destination"}"
            )
            if (isOrigin) {
                sharedShipmentRepository.updateOriginAddressPrediction(emptyList())
            } else {
                sharedShipmentRepository.updateDestinationAddressPrediction(emptyList())
            }
            return
        }

        Log.d(TAG, "Searching places for '${query}' (${if (isOrigin) "origin" else "destination"})")

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setLocationBias(indiaLocationBias)
            .setCountries("IN")
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                Log.d(
                    TAG,
                    "Search successful: Found ${predictions.size} predictions for '${query}'"
                )

                if (isOrigin) {
                    sharedShipmentRepository.updateOriginAddressPrediction(predictions)
                } else {
                    sharedShipmentRepository.updateDestinationAddressPrediction(predictions)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Search failed for '${query}': ${exception.message}", exception)

                if (isOrigin) {
                    sharedShipmentRepository.updateOriginAddressPrediction(emptyList())
                } else {
                    sharedShipmentRepository.updateDestinationAddressPrediction(emptyList())
                }
            }
    }

    fun selectOriginLocation(prediction: AutocompletePrediction) {
        val fullText = prediction.getFullText(null).toString()
        Log.d(TAG, "Selecting origin location: ${prediction.getPrimaryText(null)}, Full: $fullText")

        selectPlace(fullText)
    }

    fun selectDestinationLocation(prediction: AutocompletePrediction) {
        val fullText = prediction.getFullText(null).toString()
        Log.d(
            TAG,
            "Selecting destination location: ${prediction.getPrimaryText(null)}, Full: $fullText"
        )

        selectPlace(fullText)
    }

    private fun selectPlace(address: String) {
        viewModelScope.launch {
            geocodingRepository.findCoordinatesFromAddress(address)
                .onSuccess { geocodedLocation ->

                    when (_activeField.value) {

                        LocationField.ORIGIN -> {

                            Log.d(TAG, "Setting origin location: $geocodedLocation")
                            val latLngLocation = LatLngLocation(
                                geocodedLocation.latitude,
                                geocodedLocation.longitude
                            )

                            sharedShipmentRepository.updateOriginAddressSearchText(address)

                            sharedShipmentRepository.updateOriginLatLng(latLngLocation)
                        }

                        LocationField.DESTINATION -> {

                            Log.d(TAG, "Setting destination location: $geocodedLocation")
                            val latLngLocation = LatLngLocation(
                                geocodedLocation.latitude,
                                geocodedLocation.longitude
                            )

                            sharedShipmentRepository.updateDestinationAddressSearchText(address)

                            sharedShipmentRepository.updateDestinationLatLng(latLngLocation)
                        }

                        LocationField.NONE -> {}
                    }


                    Log.d(TAG, "Geocoding successful: $geocodedLocation")
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to select place: ${exception.message}", exception)
                }
        }
    }

    fun resetOriginLocation() {
        sharedShipmentRepository.resetOriginAddress()
    }

    fun resetDestinationLocation() {
        sharedShipmentRepository.resetDestinationAddress()
    }

    fun swapLocations() {
        sharedShipmentRepository.swapOriginAndDestinationAddresses()
    }
}