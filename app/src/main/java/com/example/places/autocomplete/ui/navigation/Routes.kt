package com.example.places.autocomplete.ui.navigation

import kotlinx.serialization.Serializable

enum class Routes(val title: String) {

    @Serializable SELECT_ORIGIN_ADDRESS("Select Origin Address"),
    @Serializable SELECT_DESTINATION_ADDRESS("Select Destination Address"),
    @Serializable SHIPMENT_DETAILS("Shipment Details"),
}