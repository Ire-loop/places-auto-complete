package com.example.places.autocomplete.ui.navigation

import kotlinx.serialization.Serializable

enum class Routes(val title: String) {

    @Serializable SELECT_LOCATION("Select Location"),
    @Serializable SHIPMENT_DETAILS("Shipment Details"),
    @Serializable ROUTE_MAP("Route"),
}