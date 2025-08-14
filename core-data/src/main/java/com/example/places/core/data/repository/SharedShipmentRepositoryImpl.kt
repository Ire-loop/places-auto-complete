package com.example.places.core.data.repository

import android.util.Log
import com.example.places.core.domain.model.Address
import com.example.places.core.domain.model.LatLngLocation
import com.example.places.core.domain.model.ShipmentDetails
import com.example.places.core.domain.model.TruckType
import com.example.places.core.domain.repository.SharedShipmentRepository
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SharedShipmentRepository that manages shipment addresses.
 *
 * This repository acts as a shared state holder for origin and destination addresses
 * during the shipment creation flow. It provides reactive state management using Kotlin Flow,
 * allowing UI components to observe address changes in real-time.
 *
 * The repository is scoped as @Singleton to ensure a single source of truth across
 * the entire application lifecycle for shipment address data.
 *
 * @constructor Creates an instance using Hilt dependency injection
 */
@Singleton
class SharedShipmentRepositoryImpl @Inject constructor() : SharedShipmentRepository {

    companion object {
        private const val TAG = "SharedShipmentRepo"
    }

    /**
     * Private mutable state flow for the origin address.
     * Initialized with an empty Address object as default.
     */
    private val _originAddress = MutableStateFlow(Address())

    /**
     * Public read-only state flow exposing the current origin address.
     * UI components can collect this flow to react to origin address changes.
     */
    override val originAddress = _originAddress.asStateFlow()

    /**
     * Private mutable state flow for the destination address.
     * Initialized with an empty Address object as default.
     */
    private val _destinationAddress = MutableStateFlow(Address())

    /**
     * Public read-only state flow exposing the current destination address.
     * UI components can collect this flow to react to destination address changes.
     */
    override val destinationAddress = _destinationAddress.asStateFlow()

    /**
     * Private mutable state flow for shipment details.
     * Contains load type, weight, date, time, and truck type information.
     */
    private val _shipmentDetails = MutableStateFlow(ShipmentDetails())
    
    /**
     * Public read-only state flow exposing the current shipment details.
     * UI components can collect this flow to react to shipment detail changes.
     */
    override val shipmentDetails = _shipmentDetails.asStateFlow()

    /**
     * Resets the origin address to its default state.
     * Clears all fields including search text, predictions, and coordinates.
     * Useful when starting a new shipment or clearing the origin selection.
     */
    override fun resetOriginAddress() {
        _originAddress.value = Address()
        Log.d(TAG, "Origin address reset to default state")
    }

    /**
     * Resets the destination address to its default state.
     * Clears all fields including search text, predictions, and coordinates.
     * Useful when starting a new shipment or clearing the destination selection.
     */
    override fun resetDestinationAddress() {
        _destinationAddress.value = Address()
        Log.d(TAG, "Destination address reset to default state")
    }

    /**
     * Swaps the origin and destination addresses.
     *
     * This function exchanges the complete address information between origin and destination,
     * including all fields (address lines, city, state, pincode, coordinates, search text, and predictions).
     * Useful for reversing a trip route or when the user wants to switch pickup and delivery locations.
     *
     * The swap operation is atomic - both addresses are updated simultaneously to ensure
     * consistency for any observers collecting the state flows.
     */
    override fun swapOriginAndDestinationAddresses() {
        Log.d(TAG, "Swapping origin and destination addresses")

        // Store current values before swapping
        val currentOrigin = _originAddress.value
        val currentDestination = _destinationAddress.value

        Log.v(
            TAG,
            "Before swap - Origin: ${currentOrigin.city}, Destination: ${currentDestination.city}"
        )

        // Perform the swap
        _originAddress.value = currentDestination
        _destinationAddress.value = currentOrigin

        Log.v(
            TAG,
            "After swap - Origin: ${_originAddress.value.city}, Destination: ${_destinationAddress.value.city}"
        )
        Log.i(TAG, "Successfully swapped origin and destination addresses")
    }

    /**
     * Updates the search text for the origin address field.
     * Triggers when user types in the origin search field.
     * Uses immutable update pattern to ensure proper Flow emission.
     */
    override fun updateOriginAddressSearchText(searchText: String) {
        _originAddress.update { it.copy(searchText = searchText) }
    }

    /**
     * Updates the search text for the destination address field.
     * Triggers when user types in the destination search field.
     * Uses immutable update pattern to ensure proper Flow emission.
     */
    override fun updateDestinationAddressSearchText(searchText: String) {
        _destinationAddress.update { it.copy(searchText = searchText) }
    }

    /**
     * Updates the autocomplete predictions for the origin address.
     * Called after receiving suggestions from Google Places API.
     * Predictions are displayed as a dropdown list for user selection.
     */
    override fun updateOriginAddressPrediction(predictions: List<AutocompletePrediction>) {
        _originAddress.update { it.copy(predictions = predictions) }
    }

    /**
     * Updates the autocomplete predictions for the destination address.
     * Called after receiving suggestions from Google Places API.
     * Predictions are displayed as a dropdown list for user selection.
     */
    override fun updateDestinationAddressPrediction(predictions: List<AutocompletePrediction>) {
        _destinationAddress.update { it.copy(predictions = predictions) }
    }

    /**
     * Updates only the latitude and longitude of the origin address.
     *
     * This method is useful when updating location coordinates from a map selection
     * or GPS location without modifying other address fields.
     *
     * @param latLngLocation The location object containing latitude and longitude coordinates
     */
    override fun updateOriginLatLng(latLngLocation: LatLngLocation) {

        _originAddress.update { it.copy(latLngLocation = latLngLocation) }
        Log.i(TAG, "Origin location updated successfully")
    }

    /**
     * Updates only the latitude and longitude of the destination address.
     *
     * This method is useful when updating location coordinates from a map selection
     * or GPS location without modifying other address fields.
     *
     * @param latLngLocation The location object containing latitude and longitude coordinates
     */
    override fun updateDestinationLatLng(latLngLocation: LatLngLocation) {

        _destinationAddress.update { it.copy(latLngLocation = latLngLocation) }
        Log.i(TAG, "Destination location updated successfully")
    }

    /**
     * Updates the complete origin address with new address data.
     *
     * Creates a new instance of the current destination address with updated fields
     * from the provided address parameter. This ensures immutability and triggers
     * flow collectors to receive the updated state.
     *
     * @param address The new origin address containing all address fields including
     *                coordinates (latitude/longitude) and location details
     */
    override fun setOriginAddress(address: Address) {

        _originAddress.update {
            it.copy(
                addressLine1 = address.addressLine1,
                addressLine2 = address.addressLine2,
                area = address.area,
                city = address.city,
                state = address.state,
                pincode = address.pincode,
                latLngLocation = address.latLngLocation,
            )
        }

        Log.i(TAG, "Origin address set successfully: ${address.addressLine1}, ${address.city}")
    }

    /**
     * Updates the complete destination address with new address data.
     *
     * Creates a new instance of the current destination address with updated fields
     * from the provided address parameter. This ensures immutability and triggers
     * flow collectors to receive the updated state.
     *
     * @param address The new destination address containing all address fields including
     *                coordinates (latitude/longitude) and location details
     */
    override fun setDestinationAddress(address: Address) {

        _destinationAddress.update {
            val updatedAddress = it.copy(
                addressLine1 = address.addressLine1,
                addressLine2 = address.addressLine2,
                area = address.area,
                city = address.city,
                state = address.state,
                pincode = address.pincode,
                latLngLocation = address.latLngLocation
            )
            Log.v(TAG, "Destination address after update: $updatedAddress")
            updatedAddress
        }

        Log.i(TAG, "Destination address set successfully: ${address.addressLine1}, ${address.city}")
    }

    /**
     * Updates the type of material/load being shipped.
     * Examples: "Steel Pipes", "Aluminum Raw Items", "Iron and Steel wire rolls"
     * 
     * @param loadType Description of the material being transported
     */
    override fun setLoadType(loadType: String) {
        _shipmentDetails.update { it.copy(loadType = loadType) }
        Log.i(TAG, "Load type set successfully: $loadType")
    }

    /**
     * Updates the weight of the shipment load.
     * Stored as string to preserve user input format (e.g., "5", "10.5", "25 tons")
     * 
     * @param loadWeight Weight of the load, typically in tons
     */
    override fun setLoadWeight(loadWeight: String) {
        _shipmentDetails.update { it.copy(loadWeight = loadWeight) }
        Log.i(TAG, "Load weight set successfully: $loadWeight")
    }

    /**
     * Updates the date when the shipment will be loaded.
     * Used for scheduling and planning purposes.
     * 
     * @param date The loading date using Java 8 Time API
     */
    override fun setShipmentDate(date: LocalDate) {
        _shipmentDetails.update { it.copy(shipmentDate = date) }
        Log.i(TAG, "Shipment date set successfully: $date")
    }

    /**
     * Updates the time when the shipment will be loaded.
     * Used for precise scheduling and coordination.
     * 
     * @param time The loading time using Java 8 Time API
     */
    override fun setShipmentTime(time: LocalTime) {
        _shipmentDetails.update { it.copy(shipmentTime = time) }
        Log.i(TAG, "Shipment time set successfully: $time")
    }

    /**
     * Updates the type of truck required for the shipment.
     * Options are OPEN (for general cargo) or CONTAINER (for containerized goods).
     * 
     * @param truckType The selected truck type enum value
     */
    override fun setTruckType(truckType: TruckType) {
        _shipmentDetails.update { it.copy(truckType = truckType) }
        Log.i(TAG, "Truck type set successfully: $truckType")
    }
}