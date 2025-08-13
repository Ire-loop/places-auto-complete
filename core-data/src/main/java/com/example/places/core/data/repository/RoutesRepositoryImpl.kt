package com.example.places.core.data.repository

import android.util.Log
import com.example.places.core.domain.model.LatLngLocation
import com.example.places.core.domain.model.Location
import com.example.places.core.domain.model.Route
import com.example.places.core.domain.model.RouteModifiers
import com.example.places.core.domain.model.RoutesRequest
import com.example.places.core.domain.model.Waypoint
import com.example.places.core.domain.repository.RoutesRepository
import com.example.places.core.network.api.routes.RoutesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [RoutesRepository] that fetches route data from the Routes API.
 * Handles the conversion of location coordinates to waypoints and manages API communication.
 *
 * @property routesApi The API interface for making route computation requests
 */
@Singleton
class RoutesRepositoryImpl @Inject constructor(
    private val routesApi: RoutesApi // Routes API interface injected via Hilt
) : RoutesRepository {

    companion object {
        private const val TAG = "RoutesRepositoryImpl"
    }

    /**
     * Computes available routes between two geographic locations.
     * Converts the origin and destination coordinates into waypoints,
     * creates a routes request, and calls the Routes API.
     *
     * @param origin Starting location coordinates
     * @param destination Ending location coordinates
     * @return Result containing a list of possible routes or an error
     */
    override suspend fun getRoute(
        origin: LatLngLocation,
        destination: LatLngLocation
    ): Result<List<Route>> {
        Log.d(TAG, "Getting route from $origin to $destination")

        // Convert LatLng coordinates to Waypoint objects for the API request
        val waypointOrigin = Waypoint(location = Location(latLngLocation = origin))
        val waypointDestination = Waypoint(location = Location(latLngLocation = destination))

        // Create route modifiers (can be customized for different route preferences)
        val routeModifiers = RouteModifiers()

        // Build the complete routes request object
        val routesRequest = RoutesRequest(
            origin = waypointOrigin,
            destination = waypointDestination,
            routeModifiers = routeModifiers
        )

        Log.d(TAG, "Routes request created: origin=${waypointOrigin}, dest=${waypointDestination}")

        // Switch to IO dispatcher for network operation
        return withContext(Dispatchers.IO) {
            try {
                // Make the API call to compute routes
                Log.d(TAG, "Making API call to computeRoutes")
                val response = routesApi.computeRoutes(routesRequest)

                // Log response metadata
                Log.d(
                    TAG,
                    "API response received - success: ${response.isSuccessful}, code: ${response.code()}"
                )

                // Handle successful response
                if (response.isSuccessful) {
                    val body = response.body()
                    
                    // Log response body details for debugging
                    Log.d(
                        TAG,
                        "Response body: routes=${body?.routes?.size}, error=${body?.error}"
                    )

                    // Return routes if available, otherwise return empty list
                    body?.let {
                        Log.d(TAG, "✓ Returning ${it.routes.size} routes successfully")
                        Result.success(it.routes)
                    } ?: run {
                        Log.w(TAG, "⚠ Response body is null, returning empty list")
                        Result.success(emptyList())
                    }
                } else {
                    // Handle API error response
                    val errorMsg = "API request failed with code ${response.code()}: ${
                        response.errorBody()?.string()
                    }"
                    Log.e(TAG, "✗ $errorMsg")
                    Result.failure(Exception(errorMsg))
                }

            } catch (error: Exception) {
                // Handle any exceptions during the API call
                Log.e(TAG, "✗ Exception in getRoute: ${error.message}", error)
                Result.failure(error)
            }
        }
    }
}