package com.royalcoffee.pos.ui.receipt

import com.royalcoffee.pos.model.Money
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.model.ReceiptItem
import com.royalcoffee.pos.ui.theme.CinzelFontFamily

/**
 * ReceiptPreviewDialog displays a print-ready thermal receipt preview.
 *
 * Updated layout:
 * - Responsive layout for both wide landscape tablets and compact/multi-window views.
 * - Thermal receipt paper stays centered.
 * - Action buttons adapt between side panel (wide) and bottom panel (compact).
 * - Close button is explicitly placed to avoid accidental dismiss.
 */
@Composable
fun ReceiptPreviewDialog(
    receipt: Receipt,
    onClose: () -> Unit,
    onNextOrder: () -> Unit,
    onPrintReceipt: (Receipt) -> Unit,
    onDownloadPdf: (Receipt) -> Unit,
    onShareWhatsApp: (Receipt) -> Unit,
    isHistorical: Boolean = false
) {
    Dialog(
        onDismissRequest = {
            /**
             * We intentionally do not close the dialog when the user taps outside
             * or presses back. The user should close it using the Close button.
             */
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val availableHeight = maxHeight
            val dialogHeight = (availableHeight * 0.92f).coerceAtMost(820.dp)

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 1100.dp)
                    .height(dialogHeight),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    val dialogContentWidth = maxWidth
                    val compactLayout = dialogContentWidth < 760.dp

                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Receipt Preview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (compactLayout) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    val paperWidth = (maxWidth * 0.90f).coerceIn(280.dp, 380.dp)
                                    ThermalReceiptPaper(
                                        receipt = receipt,
                                        modifier = Modifier.width(paperWidth)
                                    )
                                }

                                CompactActionPanel(
                                    isHistorical = isHistorical,
                                    onClose = onClose,
                                    onNextOrder = onNextOrder,
                                    onShareWhatsApp = { onShareWhatsApp(receipt) },
                                    onPrintReceipt = { onPrintReceipt(receipt) },
                                    onDownloadPdf = { onDownloadPdf(receipt) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val actionPanelWidth = (dialogContentWidth * 0.22f).coerceIn(
                                    minimumValue = 200.dp,
                                    maximumValue = 250.dp
                                )

                                BoxWithConstraints(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    val paperWidth = (maxWidth * 0.55f).coerceIn(
                                        minimumValue = 300.dp,
                                        maximumValue = 390.dp
                                    )

                                    ThermalReceiptPaper(
                                        receipt = receipt,
                                        modifier = Modifier.width(paperWidth)
                                    )
                                }

                                ReceiptSideActionPanel(
                                    isHistorical = isHistorical,
                                    onClose = onClose,
                                    onNextOrder = onNextOrder,
                                    onShareWhatsApp = { onShareWhatsApp(receipt) },
                                    onPrintReceipt = { onPrintReceipt(receipt) },
                                    onDownloadPdf = { onDownloadPdf(receipt) },
                                    modifier = Modifier
                                        .width(actionPanelWidth)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact action panel used when horizontal width is restricted.
 */
@Composable
private fun CompactActionPanel(
    onClose: () -> Unit,
    onNextOrder: () -> Unit,
    onPrintReceipt: () -> Unit,
    onDownloadPdf: () -> Unit,
    onShareWhatsApp: () -> Unit,
    isHistorical: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReceiptSecondaryActionButton(
                text = "Download Receipt",
                onClick = onDownloadPdf,
                modifier = Modifier.weight(1f)
            )
            ReceiptSecondaryActionButton(
                text = "Share via WhatsApp",
                onClick = onShareWhatsApp,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier
                    .weight(0.8f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Close",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPrintReceipt,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isHistorical) "Reprint Receipt" else "Print Receipt",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!isHistorical) {
                OutlinedButton(
                    onClick = onNextOrder,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Next Order",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Right-side action panel for wide layout.
 */
@Composable
private fun ReceiptSideActionPanel(
    onClose: () -> Unit,
    onNextOrder: () -> Unit,
    onPrintReceipt: () -> Unit,
    onDownloadPdf: () -> Unit,
    onShareWhatsApp: () -> Unit,
    isHistorical: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Close")
        }

        Spacer(modifier = Modifier.weight(1f))

        ReceiptSecondaryActionButton(
            text = "Download Receipt",
            onClick = onDownloadPdf
        )

        Spacer(modifier = Modifier.height(12.dp))

        ReceiptSecondaryActionButton(
            text = "Share via WhatsApp",
            onClick = onShareWhatsApp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onPrintReceipt,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isHistorical) "Reprint Receipt" else "Print Receipt",
                style = MaterialTheme.typography.titleSmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!isHistorical) {
            OutlinedButton(
                onClick = onNextOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Next Order",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

/**
 * Reusable secondary action button for receipt actions.
 */
@Composable
private fun ReceiptSecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * ThermalReceiptPaper represents the actual printed receipt preview.
 */
@Composable
private fun ThermalReceiptPaper(
    receipt: Receipt,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFEF8)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.Black.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ThermalReceiptHeader(receipt = receipt)

            ThermalDivider()

            ThermalReceiptItems(items = receipt.items)

            ThermalDivider()

            ThermalReceiptSummary(receipt = receipt)

            ThermalDivider()

            ThermalPaymentSection(receipt = receipt)

            ThermalDivider()

            Spacer(modifier = Modifier.height(4.dp))

            ReceiptMonoText(
                text = receipt.footerMessage.uppercase(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Header section of the printed receipt.
 */
@Composable
private fun ThermalReceiptHeader(
    receipt: Receipt
) {
    Text(
        text = receipt.storeName,
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        color = Color.Black,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(4.dp))

    receipt.storeAddressLines.forEach { addressLine ->
        ReceiptMonoText(
            text = addressLine,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }

    ReceiptMonoText(
        text = "Tel: ${receipt.storePhone}",
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    ReceiptMonoText(
        text = formatReceiptLine(
            left = "Receipt:",
            right = receipt.receiptNumber
        )
    )

    ReceiptMonoText(
        text = formatReceiptLine(
            left = "Date:",
            right = receipt.receiptDateTime
        )
    )

    ReceiptMonoText(
        text = formatReceiptLine(
            left = "Cashier:",
            right = receipt.cashierName
        )
    )
}

/**
 * Item section.
 */
@Composable
private fun ThermalReceiptItems(
    items: List<ReceiptItem>
) {
    ReceiptMonoText(
        text = formatReceiptLine(
            left = "ITEM",
            right = "TOTAL"
        ),
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(4.dp))

    items.forEach { item ->
        ReceiptMonoText(
            text = formatReceiptLine(
                left = item.productName,
                right = "Rs ${Money.format(item.lineTotal)}"
            ),
            fontWeight = FontWeight.Medium
        )

        ReceiptMonoText(
            text = "  ${item.quantity} x Rs ${Money.format(item.unitPrice)}",
            color = Color.Black.copy(alpha = 0.68f),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

/**
 * Shows subtotal, discount, tax, and final total.
 */
@Composable
private fun ThermalReceiptSummary(
    receipt: Receipt
) {
    ReceiptMonoText(
        text = formatReceiptLine(
            left = "Subtotal",
            right = "Rs ${Money.format(receipt.subtotal)}"
        )
    )

    ReceiptMonoText(
        text = formatReceiptLine(
            left = receipt.discountPercent?.let { "Discount ($it%)" } ?: "Discount",
            right = "Rs ${Money.format(receipt.discount)}"
        )
    )

    ReceiptMonoText(
        text = formatReceiptLine(
            left = if (receipt.vatIncluded) "VAT Included" else "Tax",
            right = if (receipt.vatIncluded) "" else "Rs ${Money.format(receipt.tax)}"
        )
    )

    Spacer(modifier = Modifier.height(6.dp))

    ReceiptMonoText(
        text = formatReceiptLine(
            left = "TOTAL",
            right = "Rs ${Money.format(receipt.total)}"
        ),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
}

/**
 * Shows payment details.
 */
@Composable
private fun ThermalPaymentSection(
    receipt: Receipt
) {
    ReceiptMonoText(
        text = "PAYMENT",
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    ReceiptMonoText(
        text = formatReceiptLine(
            left = "Method",
            right = receipt.paymentMethod.name
        )
    )

    if (receipt.amountPaid != null) {
        ReceiptMonoText(
            text = formatReceiptLine(
                left = "Amount Paid",
                right = "Rs ${Money.format(receipt.amountPaid)}"
            )
        )
    }

    if (receipt.hasChange) {
        ReceiptMonoText(
            text = formatReceiptLine(
                left = "Change",
                right = "Rs ${Money.format(requireNotNull(receipt.changeGiven))}"
            )
        )
    }
}

/**
 * Reusable monospace text used inside the thermal receipt.
 */
@Composable
private fun ReceiptMonoText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit = 13.sp,
    color: Color = Color.Black.copy(alpha = 0.88f)
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        textAlign = textAlign,
        fontFamily = FontFamily.Monospace,
        fontSize = fontSize,
        lineHeight = 18.sp,
        fontWeight = fontWeight,
        color = color
    )
}

/**
 * Thermal receipt divider.
 */
@Composable
private fun ThermalDivider() {
    Spacer(modifier = Modifier.height(10.dp))

    ReceiptMonoText(
        text = "-".repeat(THERMAL_LINE_WIDTH),
        textAlign = TextAlign.Center,
        color = Color.Black.copy(alpha = 0.65f)
    )

    Spacer(modifier = Modifier.height(10.dp))
}

/**
 * Formats a left/right receipt row for a fixed-width thermal receipt.
 */
private fun formatReceiptLine(
    left: String,
    right: String
): String {
    val cleanLeft = left.trim()
    val cleanRight = right.trim()

    val availableLeftSpace = THERMAL_LINE_WIDTH - cleanRight.length

    if (availableLeftSpace <= 1) {
        return cleanRight.take(THERMAL_LINE_WIDTH)
    }

    val trimmedLeft = cleanLeft.take(availableLeftSpace - 1)

    return trimmedLeft.padEnd(availableLeftSpace, ' ') + cleanRight
}

private const val THERMAL_LINE_WIDTH = 32
