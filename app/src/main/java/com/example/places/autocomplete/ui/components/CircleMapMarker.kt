package com.example.places.autocomplete.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import com.example.places.autocomplete.R
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState

/**
 * A custom circular map marker component for Google Maps Compose.
 * Displays a filled circle with a border stroke at the specified location.
 *
 * @param markerState The state object containing the marker's position and other properties
 * @param title The title text to display when the marker is tapped
 */
@Composable
fun CircleMapMarker(markerState: MarkerState, title: String) {
    // Stroke width for the circle border, retrieved from dimension resources
    val strokeWidth = dimensionResource(R.dimen.xxxsmall)

    // Wrap the custom drawing in a MarkerComposable to integrate with Google Maps
    MarkerComposable(
        state = markerState,
        title = title,
        anchor = Offset(0.0f, 0.0f) // Anchor point at top-left corner
    ) {
        // Custom canvas drawing for the circular marker
        Canvas(modifier = Modifier.size(dimensionResource(R.dimen.large))) {
            // Draw the filled circle (inner circle)
            drawCircle(
                color = Color.Gray, // Fill color (currently gray, comment mentions Google green)
                radius = size.minDimension / 2
            )
            // Draw the border stroke (outer circle)
            drawCircle(
                color = Color.Black, // Stroke color
                radius = size.minDimension / 2 - strokeWidth.toPx(), // Adjust radius to account for stroke width
                style = Stroke(width = strokeWidth.toPx())
            )
        }
    }
}