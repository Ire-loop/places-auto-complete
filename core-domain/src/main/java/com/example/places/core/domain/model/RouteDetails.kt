package com.example.places.core.domain.model

import com.google.android.gms.maps.model.LatLng

data class RouteDetails(
    val distance: Int,
    val duration: String,
    val polyline: List<LatLng>
)