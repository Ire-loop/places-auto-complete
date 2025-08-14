package com.example.places.core.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class ShipmentDetails(
    val loadType: String = "", // Material and load type (e.g., "Steel Pipes", "Aluminium Raw Items", "Iron and Steel wire rolls")
    val loadWeight: String = "", // Weight of the load in tons (e.g., "5", "25", "10")
    val shipmentDate: LocalDate = LocalDate.now(), // Date of shipment (e.g., "2023-10-01")
    val shipmentTime: LocalTime = LocalTime.now(), // Time of shipment (e.g., "10:30")
    val truckType: TruckType = TruckType.OPEN, // Type of truck (e.g., OPEN, CONTAINER)
)
