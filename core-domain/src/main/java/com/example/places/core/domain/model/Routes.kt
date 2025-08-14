package com.example.places.core.domain.model

import com.google.android.libraries.places.api.model.RoutingParameters.RoutingPreference
import com.google.android.libraries.places.api.model.RoutingParameters.TravelMode
import com.google.gson.annotations.SerializedName

data class RoutesRequest(

    @SerializedName("origin") val origin: Waypoint,

    @SerializedName("destination") val destination: Waypoint,

    @SerializedName("travelMode") val travelMode: TravelMode = TravelMode.DRIVE,

    @SerializedName("routingPreference") val routingPreference: RoutingPreference = RoutingPreference.TRAFFIC_AWARE,

    @SerializedName("computeAlternativeRoutes") val computeAlternativeRoutes: Boolean = false,

    @SerializedName("routeModifiers") val routeModifiers: RouteModifiers,

    @SerializedName("languageCode") val languageCode: String = "en-US",

    @SerializedName("units") val units: Units = Units.METRIC,
)

/**
 * Waypoint representation for origin, destination, or intermediate points
 */
data class Waypoint(
    @SerializedName("location")
    val location: Location? = null,

    @SerializedName("placeId")
    val placeId: String? = null,

    @SerializedName("address")
    val address: String? = null
)

/**
 * Location wrapper for lat/lng coordinates
 */
data class Location(
    @SerializedName("latLng")
    val latLngLocation: LatLngLocation
)

/**
 * Latitude and longitude coordinates
 */
data class LatLngLocation(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)

data class RouteModifiers(
    val avoidTolls: Boolean = false,
    val avoidHighways: Boolean = false,
    val avoidFerries: Boolean = false,
    val avoidIndoor: Boolean = false
)

/**
 * Units enumeration
 */
enum class Units {
    @SerializedName("METRIC")
    METRIC,

    @SerializedName("IMPERIAL")
    IMPERIAL
}

/**
 * Response from Routes API
 */
data class RoutesResponse(
    @SerializedName("routes")
    val routes: List<Route> = emptyList(),

    @SerializedName("error")
    val error: ErrorResponse? = null
)

/**
 * Individual route information
 */
data class Route(
    @SerializedName("distanceMeters")
    val distanceMeters: Int? = null,

    @SerializedName("duration")
    val duration: String? = null, // Duration in seconds with 's' suffix (e.g., "600s")

    @SerializedName("polyline")
    val polyline: Polyline? = null,
)

/**
 * Encoded polyline for the route
 */
data class Polyline(
    @SerializedName("encodedPolyline")
    val encodedPolyline: String? = null,

    @SerializedName("geoJsonLinestring")
    val geoJsonLinestring: GeoJsonLinestring? = null
)

/**
 * GeoJSON linestring representation
 */
data class GeoJsonLinestring(
    @SerializedName("type")
    val type: String = "LineString",

    @SerializedName("coordinates")
    val coordinates: List<List<Double>> = emptyList()
)

/**
 * Error response from the API
 */
data class ErrorResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: String? = null
)
