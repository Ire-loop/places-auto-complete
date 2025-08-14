package com.example.places.autocomplete.ui.screens.details

import androidx.lifecycle.ViewModel
import com.example.places.core.data.repository.SharedShipmentRepositoryImpl
import com.example.places.core.domain.model.TruckType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel for the ShipmentDetailsScreen that manages shipment information.
 * 
 * This ViewModel acts as a bridge between the UI and the SharedShipmentRepository,
 * providing methods to update various shipment details such as load type, weight,
 * date, time, and truck type. It delegates all state management to the repository
 * to ensure consistency across the app.
 *
 * @property sharedShipmentRepositoryImpl Repository for managing shared shipment state
 */
@HiltViewModel
class ShipmentDetailsViewModel @Inject constructor(
    private val sharedShipmentRepositoryImpl: SharedShipmentRepositoryImpl
) : ViewModel() {

    /**
     * Observable state flow containing current shipment details.
     * UI can collect this flow to react to changes in shipment information.
     */
    val shipmentDetails = sharedShipmentRepositoryImpl.shipmentDetails

    /**
     * Updates the type of material being shipped.
     * 
     * @param loadType Description of the material (e.g., "Steel Pipes", "Raw Materials")
     */
    fun setLoadType(loadType: String) {
        sharedShipmentRepositoryImpl.setLoadType(loadType)
    }

    /**
     * Updates the weight of the shipment.
     * 
     * @param loadWeight Weight in tons as a string (e.g., "5", "10.5")
     */
    fun setLoadWeight(loadWeight: String) {
        sharedShipmentRepositoryImpl.setLoadWeight(loadWeight)
    }

    /**
     * Updates the date when the shipment will be loaded.
     * 
     * @param date The loading date
     */
    fun setShipmentDate(date: LocalDate) {
        sharedShipmentRepositoryImpl.setShipmentDate(date)
    }

    /**
     * Updates the time when the shipment will be loaded.
     * 
     * @param time The loading time
     */
    fun setShipmentTime(time: LocalTime) {
        sharedShipmentRepositoryImpl.setShipmentTime(time)
    }

    /**
     * Updates the type of truck required for the shipment.
     * 
     * @param truckType Either OPEN or CONTAINER truck type
     */
    fun setTruckType(truckType: TruckType) {
        sharedShipmentRepositoryImpl.setTruckType(truckType)
    }
}