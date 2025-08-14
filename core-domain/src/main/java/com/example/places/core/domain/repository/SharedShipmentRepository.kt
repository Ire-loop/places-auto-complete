package com.example.places.core.domain.repository

import com.example.places.core.domain.model.Address
import com.example.places.core.domain.model.LatLngLocation
import com.example.places.core.domain.model.ShipmentDetails
import com.example.places.core.domain.model.TruckType
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime

/**
 * Repository interface for managing shared shipment data across the application.
 * Provides a centralized state management system for shipment-related information
 * including origin/destination addresses and shipment details.
 */
interface SharedShipmentRepository {

    /**
     * Observable state for the origin address including search text, predictions, and coordinates.
     */
    val originAddress: StateFlow<Address>
    
    /**
     * Observable state for the destination address including search text, predictions, and coordinates.
     */
    val destinationAddress: StateFlow<Address>

    /**
     * Observable state for shipment details including load type, weight, date, time, and truck type.
     */
    val shipmentDetails: StateFlow<ShipmentDetails>

    /**
     * Resets the origin address to default values, clearing search text, predictions, and coordinates.
     */
    fun resetOriginAddress()
    
    /**
     * Resets the destination address to default values, clearing search text, predictions, and coordinates.
     */
    fun resetDestinationAddress()

    /**
     * Swaps the origin and destination addresses, useful for return trips or route reversal.
     */
    fun swapOriginAndDestinationAddresses()

    /**
     * Updates the search text for the origin address field.
     * Typically triggered by user input in the search field.
     * 
     * @param searchText The new search text entered by the user
     */
    fun updateOriginAddressSearchText(searchText: String)
    
    /**
     * Updates the search text for the destination address field.
     * Typically triggered by user input in the search field.
     * 
     * @param searchText The new search text entered by the user
     */
    fun updateDestinationAddressSearchText(searchText: String)

    /**
     * Updates the autocomplete predictions for the origin address.
     * Called after receiving predictions from the Places API.
     * 
     * @param predictions List of autocomplete predictions from Places API
     */
    fun updateOriginAddressPrediction(predictions: List<AutocompletePrediction>)
    
    /**
     * Updates the autocomplete predictions for the destination address.
     * Called after receiving predictions from the Places API.
     * 
     * @param predictions List of autocomplete predictions from Places API
     */
    fun updateDestinationAddressPrediction(predictions: List<AutocompletePrediction>)

    /**
     * Updates the geographic coordinates for the origin address.
     * Called after successful geocoding of the selected address.
     * 
     * @param latLngLocation The latitude and longitude coordinates
     */
    fun updateOriginLatLng(latLngLocation: LatLngLocation)
    
    /**
     * Updates the geographic coordinates for the destination address.
     * Called after successful geocoding of the selected address.
     * 
     * @param latLngLocation The latitude and longitude coordinates
     */
    fun updateDestinationLatLng(latLngLocation: LatLngLocation)

    /**
     * Sets the complete origin address with all properties.
     * 
     * @param address The complete address object with search text, predictions, and coordinates
     */
    fun setOriginAddress(address: Address)
    
    /**
     * Sets the complete destination address with all properties.
     * 
     * @param address The complete address object with search text, predictions, and coordinates
     */
    fun setDestinationAddress(address: Address)

    /**
     * Updates the type of load/material being shipped.
     * 
     * @param loadType Description of the material (e.g., "Steel Pipes", "Raw Materials")
     */
    fun setLoadType(loadType: String)
    
    /**
     * Updates the weight of the shipment.
     * 
     * @param loadWeight Weight in tons as a string (e.g., "5", "10.5")
     */
    fun setLoadWeight(loadWeight: String)
    
    /**
     * Updates the date when the shipment will be loaded.
     * 
     * @param date The loading date
     */
    fun setShipmentDate(date: LocalDate)
    
    /**
     * Updates the time when the shipment will be loaded.
     * 
     * @param time The loading time
     */
    fun setShipmentTime(time: LocalTime)
    
    /**
     * Updates the type of truck required for the shipment.
     * 
     * @param truckType Either OPEN or CONTAINER truck type
     */
    fun setTruckType(truckType: TruckType)
}