package com.royalcoffee.pos.ui.receipt

import com.royalcoffee.pos.model.Money
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
 * - The receipt paper stays centered on the left/main area.
 * - The action buttons are placed in a fixed column on the right.
 * - The Close button is placed at the top right to reduce accidental closing.
 * - Main receipt actions are large and placed at the bottom right.
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
            /**
             * Gives us full control of dialog width on tablet screens.
             */
            usePlatformDefaultWidth = false,

            /**
             * Prevent accidental closing of the receipt preview.
             */
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 1100.dp)
                .heightIn(min = 560.dp, max = 820.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp)
            ) {
                Text(
                    text = "Receipt Preview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                /**
                 * Main content area.
                 *
                 * Left side:
                 * - Thermal receipt preview.
                 *
                 * Right side:
                 * - Close button at the top.
                 * - Receipt actions at the bottom.
                 */
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val actionPanelWidth = (maxWidth * 0.22f).coerceIn(
                        minimumValue = 200.dp,
                        maximumValue = 250.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(26.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        /**
                         * Receipt preview area.
                         *
                         * The receipt is intentionally narrow to resemble a real
                         * 58mm thermal receipt.
                         */
                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            val paperWidth = (maxWidth * 0.55f).coerceIn(
                                minimumValue = 320.dp,
                                maximumValue = 390.dp
                            )

                            ThermalReceiptPaper(
                                receipt = receipt,
                                modifier = Modifier.width(paperWidth)
                            )
                        }

                        /**
                         * Right-side action panel.
                         */
                        ReceiptSideActionPanel(
                            isHistorical = isHistorical,
                            onClose = onClose,
                            onNextOrder = onNextOrder,

                            /**
                             * The dialog does not decide how printing works.
                             * It simply tells the parent screen that the user clicked Print Receipt.
                             */
                            onShareWhatsApp = { onShareWhatsApp(receipt) },
                            onPrintReceipt = {
                                onPrintReceipt(receipt)
                            },
                            onDownloadPdf = {
                                onDownloadPdf(receipt)
                            },
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

/**
 * Right-side action panel.
 *
 * Layout:
 * - Close button at the top.
 * - Main receipt action buttons at the bottom.
 *
 * This reduces the chance of accidentally closing the receipt preview while
 * trying to print, share, download, or start the next order.
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
    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /**
         * Close is intentionally placed alone at the top.
         */
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

        /**
         * Bottom action group.
         *
         * These buttons are large so they are easy to tap on a tablet POS.
         */
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

        /**
         * Print Receipt is the main receipt action.
         *
         * For now, because no physical printer is connected, it shows a
         * professional printer connection error.
         */
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

        /**
         * Next Order completes the current order and clears the POS screen.
         */
        if (!isHistorical) OutlinedButton(
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

/**
 * Reusable secondary action button for receipt actions that are not yet primary.
 *
 * Used for:
 * - Download Receipt
 * - Share via WhatsApp
 */
@Composable
private fun ReceiptSecondaryActionButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * ThermalReceiptPaper represents the actual printed receipt preview.
 *
 * Design decisions:
 * - Narrow width like a real receipt.
 * - Light paper background.
 * - Monospace text to imitate ESC/POS thermal printer output.
 * - Compact spacing.
 * - Text dividers instead of full app-style dividers.
 */
@Composable
private fun ThermalReceiptPaper(
    receipt: Receipt,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(max = 620.dp),
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
 *
 * Later, company name, address, BRN, VAT, and phone number
 * can come from the Company table in the database.
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
 *
 * Each item is displayed in a compact thermal-printer style:
 * product name and total on the first line,
 * quantity and unit price on the second line.
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
 *
 * The total is larger and bold because it is the most important
 * financial value on the receipt.
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
 *
 * For cash payments, amount paid and change are useful.
 * For card/mobile payments, amount paid usually equals the total.
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
 *
 * Thermal receipts normally use printer-like fixed-width text.
 * FontFamily.Monospace helps imitate that look in the preview.
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
 *
 * ESC/POS receipts often use text-based separators like:
 * --------------------------------
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
 *
 * Example:
 * left = "Subtotal"
 * right = "Rs 120"
 *
 * Result:
 * Subtotal                  Rs 120
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

/**
 * Approximate number of characters per line for a 58mm thermal printer.
 *
 * Many 58mm ESC/POS printers support around 32 characters per line.
 * When we add 80mm printers later, this can become configurable.
 */
private const val THERMAL_LINE_WIDTH = 32
