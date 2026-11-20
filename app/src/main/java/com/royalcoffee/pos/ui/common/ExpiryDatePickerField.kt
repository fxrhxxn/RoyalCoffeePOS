package com.royalcoffee.pos.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Reusable Material 3 Date Picker field for expiry dates.
 *
 * Displays epoch day (Long?) as a formatted date string (e.g. 20 Nov 2026).
 * Safely converts between DatePicker UTC milliseconds and LocalDate epoch days.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryDatePickerField(
    expiryDay: Long?,
    onExpiryChanged: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Expiry Date (optional)",
    allowClear: Boolean = true,
    restrictPastDates: Boolean = true
) {
    var showPicker by remember { mutableStateOf(false) }

    val formattedDate = remember(expiryDay) {
        if (expiryDay != null) {
            val date = LocalDate.ofEpochDay(expiryDay)
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
            date.format(formatter)
        } else {
            ""
        }
    }

    Box(
        modifier = modifier.clickable { showPicker = true }
    ) {
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            placeholder = { Text("Select expiry date") },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (allowClear && expiryDay != null) {
                        IconButton(onClick = { onExpiryChanged(null) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear expiry")
                        }
                    }
                    IconButton(onClick = { showPicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select expiry date")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showPicker) {
        val initialMillis = remember(expiryDay) {
            if (expiryDay != null) {
                LocalDate.ofEpochDay(expiryDay)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            } else {
                LocalDate.now(ZoneOffset.UTC)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            }
        }

        val selectableDates = remember(restrictPastDates) {
            if (restrictPastDates) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val todayUtcMillis = LocalDate.now()
                            .atStartOfDay(ZoneOffset.UTC)
                            .toInstant()
                            .toEpochMilli()
                        return utcTimeMillis >= todayUtcMillis
                    }

                    override fun isSelectableYear(year: Int): Boolean {
                        return year >= LocalDate.now().year
                    }
                }
            } else {
                DatePickerDefaults.AllDates
            }
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = selectableDates
        )

        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onExpiryChanged(selectedLocalDate.toEpochDay())
                        }
                        showPicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Row {
                    if (allowClear && expiryDay != null) {
                        TextButton(
                            onClick = {
                                onExpiryChanged(null)
                                showPicker = false
                            }
                        ) {
                            Text("Remove Expiry")
                        }
                    } else if (allowClear) {
                        TextButton(
                            onClick = {
                                onExpiryChanged(null)
                                showPicker = false
                            }
                        ) {
                            Text("No Expiry")
                        }
                    }
                    TextButton(onClick = { showPicker = false }) {
                        Text("Cancel")
                    }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
