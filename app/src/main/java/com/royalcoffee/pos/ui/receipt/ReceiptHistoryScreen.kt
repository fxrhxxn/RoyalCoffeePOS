package com.royalcoffee.pos.ui.receipt

import com.royalcoffee.pos.model.Money
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ReceiptHistoryScreen displays saved receipts from the Room database.
 *
 * Version 2:
 * - Shows receipt headers.
 * - Allows Admin to tap a receipt and view product details.
 * - Added date filtering.
 */
@Composable
fun ReceiptHistoryScreen(
    receipts: List<ReceiptHistoryItem>,
    selectedReceipt: ReceiptHistoryItem?,
    selectedReceiptDetails: List<ReceiptDetailItem>,
    isDetailDialogVisible: Boolean,
    currentFilter: ReceiptDateFilter,
    customDate: LocalDate,
    onReceiptClick: (ReceiptHistoryItem) -> Unit,
    onCloseDetailDialog: () -> Unit,
    onDownloadReceipt: (Int) -> Unit,
    onFilterChange: (ReceiptDateFilter) -> Unit,
    onCustomDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight

            val horizontalPadding = when {
                availableWidth < 700.dp -> 12.dp
                availableWidth < 1000.dp -> 18.dp
                else -> 28.dp
            }
            val verticalPadding = if (availableHeight < 600.dp) 12.dp else 24.dp
            val sectionSpacing = if (availableHeight < 600.dp) 14.dp else 24.dp

            val compactLayout = availableWidth < 650.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Receipt History",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "View completed transactions saved locally on this device.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                // Filter Chips
                ReceiptFilterBar(
                    currentFilter = currentFilter,
                    customDate = customDate,
                    onFilterClick = { filter ->
                        if (filter == ReceiptDateFilter.CUSTOM) {
                            showDatePicker = true
                        } else {
                            onFilterChange(filter)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                Column(
                    modifier = Modifier
                        .widthIn(max = 960.dp)
                        .weight(1f)
                ) {
                    if (receipts.isEmpty()) {
                        EmptyReceiptHistoryCard(
                            isFiltered = currentFilter != ReceiptDateFilter.ALL,
                            compactLayout = compactLayout
                        )
                    } else {
                        ReceiptHistoryListCard(
                            receipts = receipts,
                            compactLayout = compactLayout,
                            onReceiptClick = onReceiptClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onCustomDateChange(selectedDate)
                        }
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (isDetailDialogVisible && selectedReceipt != null) {
        ReceiptDetailDialog(
            receipt = selectedReceipt,
            details = selectedReceiptDetails,
            onDismiss = onCloseDetailDialog,
            onDownload = {
                onDownloadReceipt(selectedReceipt.receiptId)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptFilterBar(
    currentFilter: ReceiptDateFilter,
    customDate: LocalDate,
    onFilterClick: (ReceiptDateFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(ReceiptDateFilter.entries.toTypedArray()) { filter ->
            val isSelected = currentFilter == filter
            val label = if (filter == ReceiptDateFilter.CUSTOM && isSelected) {
                "Date: ${customDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
            } else {
                filter.label
            }

            FilterChip(
                selected = isSelected,
                onClick = { onFilterClick(filter) },
                label = { Text(label) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                } else if (filter == ReceiptDateFilter.CUSTOM) {
                    {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun EmptyReceiptHistoryCard(
    isFiltered: Boolean,
    compactLayout: Boolean
) {
    val cardPadding = if (compactLayout) 16.dp else 28.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = RoyalGold.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isFiltered) "No receipts found for this date" else "No saved receipts yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isFiltered)
                    "Try selecting a different date range or filter."
                else
                    "Complete an order and tap Next Order to save a receipt.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReceiptHistoryListCard(
    receipts: List<ReceiptHistoryItem>,
    compactLayout: Boolean,
    onReceiptClick: (ReceiptHistoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardPadding = if (compactLayout) 14.dp else 22.dp

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(cardPadding)
        ) {
            Text(
                text = "Saved Receipts",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${receipts.size} receipt(s) saved locally. Tap a receipt to view details.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(receipts) { receipt ->
                    ReceiptHistoryRow(
                        receipt = receipt,
                        compactLayout = compactLayout,
                        onClick = {
                            onReceiptClick(receipt)
                        }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptHistoryRow(
    receipt: ReceiptHistoryItem,
    compactLayout: Boolean,
    onClick: () -> Unit
) {
    if (compactLayout) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = receipt.receiptNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rs ${Money.format(receipt.total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = receipt.receiptDateTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cashier: ${receipt.cashierName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Payment: ${receipt.paymentMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1.5f)
            ) {
                Text(
                    text = receipt.receiptNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = receipt.receiptDateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = receipt.cashierName,
                modifier = Modifier.weight(0.9f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = receipt.paymentMethod,
                modifier = Modifier.weight(0.9f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Rs ${Money.format(receipt.total)}",
                modifier = Modifier.weight(0.9f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun ReceiptDetailDialog(
    receipt: ReceiptHistoryItem,
    details: List<ReceiptDetailItem>,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            BoxWithConstraints {
                val isCompactDialog = maxWidth < 450.dp
                if (isCompactDialog) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = receipt.receiptNumber,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = onDownload,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Download PDF")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = receipt.receiptNumber,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onDownload,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Download PDF")
                        }
                    }
                }
            }
        },
        text = {
            BoxWithConstraints(modifier = Modifier.widthIn(max = 680.dp)) {
                val isCompactDialog = maxWidth < 450.dp

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${receipt.receiptDateTime} • ${receipt.paymentMethod} • Rs ${Money.format(receipt.total)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isCompactDialog) {
                        DetailHeaderRow(
                            first = "Product",
                            second = "Qty",
                            third = "Line Total"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    if (details.isEmpty()) {
                        Text(
                            text = "No receipt line details found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(details) { detail ->
                                DetailDataRow(
                                    detail = detail,
                                    isCompact = isCompactDialog
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = "Close")
            }
        }
    )
}

@Composable
private fun DetailHeaderRow(
    first: String,
    second: String,
    third: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = first,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = second,
            modifier = Modifier.weight(0.5f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = third,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DetailDataRow(
    detail: ReceiptDetailItem,
    isCompact: Boolean
) {
    if (isCompact) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = detail.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rs ${Money.format(detail.unitPrice)} each × ${detail.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Rs ${Money.format(detail.lineTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${detail.productName}\nRs ${Money.format(detail.unitPrice)} each",
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = detail.quantity.toString(),
                modifier = Modifier.weight(0.5f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Rs ${Money.format(detail.lineTotal)}",
                modifier = Modifier.weight(0.8f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End
            )
        }
    }
}
