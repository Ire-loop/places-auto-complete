@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.places.autocomplete.ui.screens.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.places.autocomplete.R
import com.example.places.autocomplete.ui.components.DatePickerDocked
import com.example.places.autocomplete.ui.components.TimePickerDocked
import com.example.places.core.domain.model.ShipmentType
import com.example.places.core.domain.model.TruckType

@Composable
fun ShipmentDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: ShipmentDetailsViewModel = hiltViewModel()
) {

    val shipmentDetails by viewModel.shipmentDetails.collectAsState()
    val isBottomSheetOpen by viewModel.isBottomSheetOpen.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState( skipPartiallyExpanded = true )

    var selectedCategory by remember { mutableStateOf<ShipmentType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TopAppBar(
                        title = {
                            Text(text = "Shipment Details")
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    TextButton(
                        onClick = { viewModel.resetShipmentDetails() },
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.small))
                    ) {
                        Text("Clear")
                    }
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { /* Handle save action */ },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Create Shipment",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(dimensionResource(R.dimen.medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.medium))
        ) {

            /**
             * Configure shipment information here
             */

            ElevatedCard(
                onClick = { viewModel.updateBottomSheetState(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = {
                        Text(text = "Shipment Type")
                    },
                    supportingContent = {
                        if (shipmentDetails.shipmentType.isNotEmpty()) {
                            Text(text = shipmentDetails.shipmentType)
                        } else {
                            Text(text = "Select shipment type")
                        }
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.package_2),
                            contentDescription = "Shipment Leading Icon",
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown Icon"
                        )
                    }
                )
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.medium))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.balance),
                                contentDescription = "Weight Icon",
                            )

                            Text("Weight")
                        }
                    },
                    supportingContent = {

                        OutlinedTextField(
                            value = shipmentDetails.loadWeight,
                            onValueChange = { viewModel.updateLoadWeight(it) },
                            label = { Text("Weight") },
                            placeholder = { Text("Weight in (Tonne)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            trailingIcon = {
                                Text("tonne")
                            }
                        )
                    }
                )
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {

                ListItem(
                    headlineContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.medium))) {
                            Icon(
                                painter = painterResource(R.drawable.calendar_add_on),
                                contentDescription = "Choose date and time"
                            )

                            Text("Date and Time")
                        }
                    },
                    supportingContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small))
                        ) {
                            DatePickerDocked(
                                modifier = Modifier.weight(1f),
                                initialDate = shipmentDetails.shipmentDate,
                            ) { viewModel.updateShipmentDate(it) }
                            TimePickerDocked(
                                modifier = Modifier.weight(1f),
                                initialTime = shipmentDetails.shipmentTime
                            ) { viewModel.updateShipmentTime(it) }
                        }
                    }
                )
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.medium))) {
                            Icon(
                                painter = painterResource(R.drawable.local_shipping),
                                contentDescription = "Choose truck type"
                            )

                            Text("Truck type")
                        }
                    },
                    supportingContent = {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small)),
                            maxItemsInEachRow = 2
                        ) {
                            TruckType.entries.forEach { truckType ->
                                OutlinedButton(
                                    onClick = { viewModel.updateTruckType(truckType.title) },
                                    shape = RoundedCornerShape(dimensionResource(R.dimen.xsmall)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = dimensionResource(R.dimen.xsmall))
                                ) {
                                    Text(truckType.title)
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    if (isBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.updateBottomSheetState(false) },
            sheetState = bottomSheetState
        ) {

            Column(
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.medium))
            ) {

                // Title bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Select Shipment Type",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    IconButton(
                        onClick = { viewModel.updateBottomSheetState(false) },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "Close Icon"
                        )
                    }
                }

                // Shipment type options
                ShipmentType.entries.forEach { type ->
                    ElevatedCard(
                        onClick = { selectedCategory = type }
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(type.title)
                            },
                            supportingContent = {
                                Text(
                                    type.description,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingContent = {
                                if (selectedCategory == type) {

                                    IconButton(
                                        onClick = { selectedCategory = null }
                                    ) {

                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Dropdown Icon"
                                        )
                                    }
                                } else {

                                    IconButton(
                                        onClick = { selectedCategory = type }
                                    ) {

                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Dropdown Icon"
                                        )
                                    }
                                }
                            }
                        )

                        if (selectedCategory == type) {
                            type.subTypes.forEach { subType ->
                                ListItem(
                                    headlineContent = {
                                        Text(subType)
                                    },
                                    modifier = Modifier.clickable {
                                        viewModel.updateShipmentType(subType)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}