package com.example.places.autocomplete.ui.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.places.autocomplete.R
import com.example.places.autocomplete.ui.components.DatePickerDocked
import com.example.places.autocomplete.ui.components.TimePickerDocked
import com.example.places.core.domain.model.TruckType

@Composable
fun ShipmentDetailsScreen(viewModel: ShipmentDetailsViewModel = hiltViewModel()) {

    val shipmentDetails by viewModel.shipmentDetails.collectAsState()

    val outlinedFieldModifier = Modifier.padding(
        vertical = dimensionResource(R.dimen.medium)
    )

    Column(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.medium))
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        // Material type (Editable)
        OutlinedTextField(
            value = shipmentDetails.loadType,
            onValueChange = { viewModel.setLoadType(it) },
            label = { Text("Material type") },
            placeholder = { Text("Material Type") },
            modifier = outlinedFieldModifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
        )

        // Weight (Editable)
        OutlinedTextField(
            value = shipmentDetails.loadWeight,
            onValueChange = { viewModel.setLoadWeight(it) },
            label = { Text("Weight") },
            placeholder = { Text("Load Weight (tons)") },
            trailingIcon = { Text(text = "TON", fontWeight = FontWeight.W600) },
            modifier = outlinedFieldModifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(R.dimen.medium)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.medium))
        ) {
            DatePickerDocked(
                modifier = Modifier.weight(0.5f),
                initialDate = shipmentDetails.shipmentDate,
                onDateSelected = { viewModel.setShipmentDate(it) }
            )

            TimePickerDocked(
                modifier = Modifier.weight(0.5f),
                initialTime = shipmentDetails.shipmentTime,
                onTimeSelected = { viewModel.setShipmentTime(it) }
            )
        }


        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small))
        ) {

            Text(text = "Select different truck type", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Truck type")

            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.small))) {

                OutlinedButton(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.small)),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = TruckType.OPEN == shipmentDetails.truckType),
                    onClick = { viewModel.setTruckType(TruckType.OPEN) }
                ) {
                    Text(
                        text = TruckType.OPEN.title,
                        modifier = Modifier.padding(dimensionResource(R.dimen.small))
                    )
                }

                OutlinedButton(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.small)),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = TruckType.CONTAINER == shipmentDetails.truckType),
                    onClick = { viewModel.setTruckType(TruckType.CONTAINER) }
                ) {
                    Text(
                        text = TruckType.CONTAINER.title,
                        modifier = Modifier.padding(dimensionResource(R.dimen.small))
                    )
                }
            }
        }
    }
}