@file:OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)

package com.example.places.autocomplete.ui.screens.autocomplete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.places.autocomplete.R
import com.example.places.autocomplete.ui.components.LocationSuggestionCard
import com.example.places.autocomplete.ui.components.SelectCurrentLocationCard
import com.example.places.autocomplete.ui.components.SelectLocationCard
import com.example.places.core.domain.model.LocationField
import kotlinx.coroutines.FlowPreview

@Composable
fun SelectLocationScreen(
    viewModel: SelectLocationScreenViewModel = hiltViewModel()
) {
    // State to hold predictions
    val originLocation = viewModel.originLocation.collectAsState()
    val destinationLocation = viewModel.destinationLocation.collectAsState()
    val activeField = viewModel.activeField.collectAsState()

    // Get predictions based on active field
    val predictions = when (activeField.value) {
        LocationField.ORIGIN -> originLocation.value.predictions
        LocationField.DESTINATION -> destinationLocation.value.predictions
        else -> emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(dimensionResource(R.dimen.medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small))
    ) {
        // Card with Origin/Destination text fields
        item(key = "SelectLocationCard") {
            SelectLocationCard(
                origin = originLocation.value,
                destination = destinationLocation.value,
                viewModel = viewModel
            )
        }

        // Show predictions if not empty
        if (predictions.isNotEmpty()) {
            items(count = predictions.size, key = { predictions[it].placeId }) { index ->
                LocationSuggestionCard(
                    prediction = predictions[index],
                    onClick = {
                        // Handle selection based on active field
                        when (activeField.value) {
                            LocationField.ORIGIN -> viewModel.selectOriginLocation(it)
                            LocationField.DESTINATION -> viewModel.selectDestinationLocation(it)
                            else -> {}
                        }
                    }
                )
            }
        } else {
            // Show current location card if no predictions
            item(key = "SelectCurrentLocationCard") {
                SelectCurrentLocationCard()
            }
        }
    }
}