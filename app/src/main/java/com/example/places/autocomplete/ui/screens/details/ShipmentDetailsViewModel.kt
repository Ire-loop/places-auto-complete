package com.example.places.autocomplete.ui.screens.details

import androidx.lifecycle.ViewModel
import com.example.places.core.data.repository.SharedShipmentRepositoryImpl
import com.example.places.core.domain.model.ShipmentDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ShipmentDetailsViewModel @Inject constructor(
    private val sharedShipmentRepositoryImpl: SharedShipmentRepositoryImpl
) : ViewModel() {

    private val _shipmentDetails = MutableStateFlow(ShipmentDetails())
    val shipmentDetails = _shipmentDetails.asStateFlow()

    private val _isBottomSheetOpen = MutableStateFlow(false)
    val isBottomSheetOpen = _isBottomSheetOpen.asStateFlow()

    fun updateBottomSheetState(isOpen: Boolean) {
        _isBottomSheetOpen.value = isOpen
    }

    fun updateShipmentType(shipmentType: String) {
        _shipmentDetails.update {
            it.copy(shipmentType = shipmentType)
        }
        updateBottomSheetState(isOpen = false)
    }

    fun updateLoadWeight(loadWeight: String) {
        _shipmentDetails.update {
            it.copy(loadWeight = loadWeight)
        }
    }

    fun updateShipmentDate(shipmentDate: LocalDate) {
        _shipmentDetails.update {
            it.copy(shipmentDate = shipmentDate)
        }
    }

    fun updateShipmentTime(shipmentTime: LocalTime) {
        _shipmentDetails.update {
            it.copy(shipmentTime = shipmentTime)
        }
    }

    fun updateTruckType(truckType: String) {

    }

    fun resetShipmentDetails() {
        _shipmentDetails.update { ShipmentDetails() }
    }
}