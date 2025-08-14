package com.example.places.core.data.repository

import android.util.Log
import com.example.places.core.domain.model.LatLngLocation
import com.example.places.core.domain.parser.GoogleMapsHtmlParser
import com.example.places.core.domain.repository.GeocodingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GeocodingRepositoryImpl"

/**
 * Implementation of [GeocodingRepository] that handles geocoding operations.
 * Converts addresses to geographic coordinates using Google Maps HTML parsing.
 *
 * @property googleMapsHtmlParser Parser for extracting coordinates from Google Maps HTML responses
 */
@Singleton
class GeocodingRepositoryImpl @Inject constructor(
    private val googleMapsHtmlParser: GoogleMapsHtmlParser
) : GeocodingRepository {

    /**
     * Converts an address string to geographic coordinates.
     * Performs validation on the resulting coordinates to ensure they are within valid ranges.
     *
     * @param address The address string to geocode
     * @return Result containing [LatLngLocation] if successful, or an error if geocoding fails
     */
    override suspend fun findCoordinatesFromAddress(address: String): Result<LatLngLocation> {
        Log.d(TAG, "Starting geocoding for address: $address")
        Log.v(TAG, "Thread: ${Thread.currentThread().name}")
        
        return withContext(Dispatchers.IO) {
            Log.v(TAG, "Switched to IO dispatcher for geocoding operation")
            
            try {
                // Attempt to geocode the address using robust parsing strategy
                Log.d(TAG, "Calling robustGeocode for address: $address")
                val startTime = System.currentTimeMillis()
                
                googleMapsHtmlParser.robustGeocode(address).let { result ->
                    val elapsedTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "Geocoding completed in ${elapsedTime}ms")
                    
                    when {
                        // Valid coordinates found
                        result != null && isValidCoordinate(result) -> {
                            val latLngLocation = LatLngLocation(result.latitude, result.longitude)
                            Log.d(TAG, "✓ Geocoding successful for address: $address")
                            Log.i(
                                TAG,
                                "Coordinates found: lat=${latLngLocation.latitude}, lng=${latLngLocation.longitude}"
                            )
                            return@withContext Result.success(latLngLocation)
                        }
                        // Coordinates found but invalid
                        result != null -> {
                            Log.w(TAG, "✗ Invalid coordinates found for address: $address")
                            Log.w(TAG, "Invalid values: lat=${result.latitude}, lng=${result.longitude}")
                            return@withContext Result.failure(
                                IllegalArgumentException("Invalid coordinates for address: $address")
                            )
                        }
                        // No coordinates found
                        else -> {
                            Log.w(TAG, "✗ No coordinates found for address: $address")
                            Log.d(TAG, "Parser returned null for address: $address")
                            return@withContext Result.failure(
                                IllegalArgumentException("No coordinates found for address: $address")
                            )
                        }
                    }
                }
            } catch (error: Exception) {
                Log.e(TAG, "✗ Error geocoding address: $address", error)
                Log.e(TAG, "Error type: ${error.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${error.message}")
                return@withContext Result.failure(error)
            }
        }
    }

    /**
     * Creates a Flow that emits geocoding results for the given address.
     * Useful for reactive UI updates based on geocoding operations.
     *
     * @param address The address string to geocode
     * @return Flow emitting Result containing [LatLngLocation] or error
     */
    override fun geocodeAddressFlow(address: String): Flow<Result<LatLngLocation>> = flow {
        Log.d(TAG, "Creating geocoding flow for address: $address")
        Log.v(TAG, "Flow collector thread: ${Thread.currentThread().name}")
        
        // Emit loading state could be added here if needed
        Log.d(TAG, "Emitting geocoding result for flow")
        emit(findCoordinatesFromAddress(address))
        
        Log.d(TAG, "Flow emission completed for address: $address")
    }

    /**
     * Validates that geographic coordinates are within acceptable ranges.
     * Latitude must be between -90 and 90 degrees.
     * Longitude must be between -180 and 180 degrees.
     *
     * @param location The location to validate
     * @return true if coordinates are valid, false otherwise
     */
    private fun isValidCoordinate(location: LatLngLocation): Boolean {
        val isLatitudeValid = location.latitude >= -90 && location.latitude <= 90
        val isLongitudeValid = location.longitude >= -180 && location.longitude <= 180
        val isValid = isLatitudeValid && isLongitudeValid
        
        Log.v(
            TAG,
            "Coordinate validation: lat=${location.latitude}, lng=${location.longitude}, valid=$isValid"
        )
        
        if (!isLatitudeValid) {
            Log.w(TAG, "Invalid latitude: ${location.latitude} (must be between -90 and 90)")
        }
        if (!isLongitudeValid) {
            Log.w(TAG, "Invalid longitude: ${location.longitude} (must be between -180 and 180)")
        }
        
        return isValid
    }
}