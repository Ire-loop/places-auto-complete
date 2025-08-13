package com.example.places.core.domain.repository

import com.example.places.core.domain.model.LatLngLocation
import com.example.places.core.domain.model.Route

/**
 * Repository interface for handling route-related operations.
 * Provides methods to fetch and manage routes between locations.
 */
interface RoutesRepository {

    /**
     * Fetches available routes between an origin and destination.
     * 
     * @param origin The starting location coordinates
     * @param destination The ending location coordinates
     * @return Result containing a list of possible routes, or an error if the operation fails
     */
    suspend fun getRoute(origin: LatLngLocation, destination: LatLngLocation): Result<List<Route>>
}