package com.example.places.autocomplete.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.example.places.autocomplete.R
import com.example.places.autocomplete.ui.screens.autocomplete.SelectLocationScreenViewModel
import com.example.places.core.domain.model.Address
import com.example.places.core.domain.model.LocationField

@Composable
fun SelectLocationCard(
    origin: Address,
    destination: Address,
    viewModel: SelectLocationScreenViewModel
) {

    val originFocusRequester = remember { FocusRequester() }
    val destinationFocusRequester = remember { FocusRequester() }
    val activeField by viewModel.activeField.collectAsState()

    // Sync focus with active field changes
    LaunchedEffect(activeField) {
        when (activeField) {

            LocationField.ORIGIN -> {
                originFocusRequester.requestFocus()
            }

            LocationField.DESTINATION -> {
                destinationFocusRequester.requestFocus()
            }

            LocationField.NONE -> {
                // Clear focus from both fields
                originFocusRequester.freeFocus()
                destinationFocusRequester.freeFocus()
            }
        }
    }

    val smallPadding = dimensionResource(R.dimen.small)
    val mediumPadding = dimensionResource(R.dimen.medium)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(mediumPadding),
        elevation = CardDefaults.cardElevation(defaultElevation = smallPadding)
    ) {
        Column(
            modifier = Modifier.padding(mediumPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Origin Location
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    OutlinedTextField(
                        value = origin.searchText,
                        onValueChange = { viewModel.updateOriginSearchText(it) },
                        label = { Text("Origin") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.trip_origin),
                                contentDescription = "Origin",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (origin.searchText.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        viewModel.resetOriginLocation()
                                    }
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(originFocusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    viewModel.setActiveField(LocationField.ORIGIN)
                                }
                            },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(smallPadding))

                    // Destination Location
                    OutlinedTextField(
                        value = destination.searchText,
                        onValueChange = { viewModel.updateDestinationSearchText(it) },
                        label = { Text("To") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.location_pin),
                                contentDescription = "Destination",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        trailingIcon = {
                            if (destination.searchText.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.resetDestinationLocation() }
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(destinationFocusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    viewModel.setActiveField(LocationField.DESTINATION)
                                }
                            },
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.width(smallPadding))

                IconButton(
                    onClick = { viewModel.swapLocations() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.swap_vert),
                        contentDescription = "Swap",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}