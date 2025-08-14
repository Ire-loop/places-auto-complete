@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.places.autocomplete.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.example.places.autocomplete.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DatePickerDocked(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit
) {
    // Initialize with the provided date converted to milliseconds
    val initialMillis = initialDate
        ?.atStartOfDay(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()

    // State to hold the currently selected date in milliseconds
    var selectedDate by remember(initialMillis) { mutableStateOf(initialMillis) }

    // State to control the visibility of the date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }

    // Material 3 date picker state that manages the calendar component
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        selectableDates = object : SelectableDates {
            // Get current date at midnight to compare
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Only allow today and future dates
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= today
            }
        }
    )

    // Convert the selected date to display format or show placeholder
    val displayDate = selectedDate?.let {
        convertMillisToDate(it, "dd/MM/yyyy")
    } ?: "DD/MM/YYYY"

    // Text field that displays the selected date - clickable to open date picker
    OutlinedTextField(
        value = displayDate,
        onValueChange = { /* Read-only field */ },
        label = { Text("Date") },
        readOnly = true,
        maxLines = 1,
        trailingIcon = {
            // Calendar icon button that opens the date picker dialog
            IconButton(
                onClick = { showDatePicker = true },
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            }
        },
        modifier = modifier
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Update selected date and notify parent
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false

                        // Convert millis to LocalDate and pass to callback
                        val dateToPass = selectedDate?.let { millis ->
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        } ?: LocalDate.now()
                        onDateSelected(dateToPass)
                    }
                ) {
                    Text("OK")
                }
            },
            modifier = Modifier.padding(dimensionResource(R.dimen.medium)),
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }
}

/**
 * Utility function to convert milliseconds timestamp to formatted date string
 *
 * @param millis The timestamp in milliseconds
 * @param pattern The desired date format pattern (default: "MM/dd/yyyy")
 * @return Formatted date string
 */
fun convertMillisToDate(millis: Long, pattern: String = "MM/dd/yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(millis))
}