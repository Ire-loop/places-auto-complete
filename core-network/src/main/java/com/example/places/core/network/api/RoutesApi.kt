package com.example.places.core.network.api

import com.example.places.core.domain.model.RoutesRequest
import com.example.places.core.domain.model.RoutesResponse
import com.example.places.core.network.BuildConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for Google Routes API
 */
interface RoutesApi {

    /**
     * Compute routes between origin and destination
     *
     * @param request The routes request containing origin, destination, and preferences
     * @param apiKey Your Google Maps API key
     * @param fieldMask Comma-separated list of response fields to include
     * @return Response containing computed routes
     */
    @POST("directions/v2:computeRoutes")
    suspend fun computeRoutes(
        @Body request: RoutesRequest,
        @Header("X-Goog-Api-Key") apiKey: String = BuildConfig.MAPS_API_KEY,
        @Header("X-Goog-FieldMask") fieldMask: String = DEFAULT_FIELD_MASK
    ): Response<RoutesResponse>

    companion object {
        // Default field mask to include all necessary fields in the response
        const val DEFAULT_FIELD_MASK = "routes.duration,routes.distanceMeters,routes.polyline"
    }
}