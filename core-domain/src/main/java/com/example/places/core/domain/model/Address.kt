package com.example.places.core.domain.model

import com.google.android.libraries.places.api.model.AutocompletePrediction

data class Address(
    val searchText: String = "", // The text entered in the search field
    val predictions: List<AutocompletePrediction> = emptyList(), // List of autocomplete predictions
    val addressLine1: String = "", // First line of the address (optional)
    val addressLine2: String = "", // Second line of the address (optional)
    val area: String = "", // Area or locality of the address
    val city: String = "", // City of the address
    val district: String = "", // District of the address
    val state: String = "", // State of the address
    val pincode: String = "", // Postal code of the address
    val latLngLocation: LatLngLocation = LatLngLocation(0.0, 0.0) // Latitude and longitude of the address
)