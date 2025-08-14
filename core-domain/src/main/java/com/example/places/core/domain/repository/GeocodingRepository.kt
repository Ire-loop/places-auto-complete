package com.example.places.core.domain.repository

import com.example.places.core.domain.model.LatLngLocation
import kotlinx.coroutines.flow.Flow

interface GeocodingRepository {

    suspend fun findCoordinatesFromAddress(address: String): Result<LatLngLocation>
    fun geocodeAddressFlow(address: String): Flow<Result<LatLngLocation>>
}