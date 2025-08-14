@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.places.autocomplete.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.places.autocomplete.R

@Composable
fun TimePickerDocked(
    modifier: Modifier = Modifier,
    initialTime: LocalTime? = null,
    onTimeSelected: (LocalTime) -> Unit
) {
    val label = "Time"
    val timeFormat = "HH:mm"

    // State to hold the currently selected time, initialized with provided time
    var selectedTime by remember(initialTime) { mutableStateOf(initialTime) }

    // State to control the visibility of the time picker dialog
    var showTimePicker by remember { mutableStateOf(false) }

    // Convert the selected time to display format or show placeholder
    val displayTime = selectedTime?.let {
        val formatter = DateTimeFormatter.ofPattern(timeFormat)
        it.format(formatter)
    } ?: "hh:mm"

    // Main container for the time picker component
    Box(
        modifier = modifier
    ) {
        // Text field that displays the selected time and triggers the picker
        OutlinedTextField(
            value = displayTime,
            onValueChange = { },
            label = { Text(label) },
            maxLines = 1,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showTimePicker = !showTimePicker }) {
                    Icon(
                        painter = painterResource(R.drawable.schedule),
                        contentDescription = "Select time"
                    )
                }
            },
            modifier = modifier
        )

        // Conditionally show the time picker dialog
        if (showTimePicker) {
            DialWithDialog(
                initialTime = initialTime,
                onConfirm = { timePickerState ->
                    // Create LocalTime from the picker state
                    val newTime = LocalTime.of(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    // Update local state
                    selectedTime = newTime
                    // Notify parent component
                    onTimeSelected(newTime)
                    // Close the dialog
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }
    }
}

/**
 * Helper composable that creates a time picker dialog with a digital input interface.
 * Initializes the picker with current system time and uses 24-hour format.
 *
 * @param onConfirm Callback when user confirms time selection, passes TimePickerState
 * @param onDismiss Callback when user dismisses the dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialWithDialog(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
    initialTime: LocalTime? = null
) {
    // Get current system time or use provided initial time
    val defaultTime = initialTime ?: LocalTime.now()

    // Create time picker state with initial time
    val timePickerState = rememberTimePickerState(
        initialHour = defaultTime.hour,
        initialMinute = defaultTime.minute,
        is24Hour = true,
    )

    // Display the time picker in a dialog
    TimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(timePickerState) }
    ) {
        TimePicker(
            state = timePickerState,
            layoutType = TimePickerLayoutType.Vertical
        )
    }
}

/**
 * A reusable dialog wrapper for the time picker component.
 * Uses AlertDialog to provide a consistent dialog experience with confirm/dismiss buttons.
 *
 * @param onDismiss Callback when dialog is dismissed (Cancel button or outside click)
 * @param onConfirm Callback when user confirms the selection (OK button)
 * @param content The content to display inside the dialog (typically TimePicker)
 */
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Handle clicking outside dialog
        dismissButton = {
            // Cancel button - closes dialog without saving
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        confirmButton = {
            // OK button - confirms the time selection
            TextButton(onClick = { onConfirm() }) {
                Text("OK")
            }
        },
        text = { content() },
        modifier = Modifier.padding(dimensionResource(R.dimen.medium))// Display the time picker content
    )
}