package com.royalcoffee.pos.ui.receipt

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.model.ReceiptItem
import com.royalcoffee.pos.ui.theme.RoyalGold

/**
 * ReceiptPreviewDialog displays the completed receipt.
 *
 * Important change in Step 12.2:
 * Instead of receiving many separate values like subtotal, tax, total, payment method, etc.,
 * this dialog now receives one Receipt object.
 *
 * This is better because the same Receipt object can later be used for:
 * - Thermal printer formatting
 * - PDF generation
 * - WhatsApp sharing
 * - Database storage
 */
@Composable
fun ReceiptPreviewDialog(
    receipt: Receipt,
    onClose: () -> Unit,
    onNextOrder: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            /**
             * We disable the platform default width because Android dialogs
             * are normally quite narrow.
             *
             * This gives us better control on tablets.
             */
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 900.dp)
                .heightIn(max = 760.dp),
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
                 * The receipt content is scrollable.
                 * The action buttons at the bottom remain fixed.
                 */
                ReceiptContent(
                    receipt = receipt,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                ReceiptBottomActionBar(
                    onClose = onClose,
                    onNextOrder = onNextOrder
                )
            }
        }
    }
}

/**
 * Main receipt body.
 *
 * This function now receives the whole Receipt object.
 * That makes the UI easier to maintain because all receipt data lives in one place.
 */
@Composable
private fun ReceiptContent(
    receipt: Receipt,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(
            width = 1.dp,
            color = RoyalGold.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            ReceiptHeader(receipt = receipt)

            ReceiptSectionDivider()

            ReceiptItemsSection(items = receipt.items)

            ReceiptSectionDivider()

            ReceiptSummarySection(receipt = receipt)

            Spacer(modifier = Modifier.height(18.dp))

            PaymentSection(receipt = receipt)

            Spacer(modifier = Modifier.height(18.dp))

            ReceiptFooter(footerMessage = receipt.footerMessage)
        }
    }
}

/**
 * Fixed bottom action bar.
 *
 * Print Receipt is the main action.
 * For now, it shows a professional error message because no real printer is connected yet.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReceiptBottomActionBar(
    onClose: () -> Unit,
    onNextOrder: () -> Unit
) {
    val context = LocalContext.current

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 10.dp,
            alignment = Alignment.End
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = {
                /**
                 * Temporary printer feedback.
                 *
                 * We do not say "simulated printer" because this should behave
                 * like a real POS system when the printer is unavailable.
                 */
                Toast.makeText(
                    context,
                    "Printer not available. Check connection.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.height(46.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Print Receipt")
        }

        OutlinedButton(
            onClick = {
                Toast.makeText(
                    context,
                    "WhatsApp sharing is not available yet.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.height(46.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Share WhatsApp")
        }

        OutlinedButton(
            onClick = {
                Toast.makeText(
                    context,
                    "Receipt download is not available yet.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.height(46.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Download Receipt")
        }

        Button(
            onClick = onNextOrder,
            modifier = Modifier.height(46.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Next Order")
        }

        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.height(46.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Close")
        }
    }
}

/**
 * Header section shown at the top of the receipt.
 *
 * The store details now come from the Receipt object.
 */
@Composable
private fun ReceiptHeader(
    receipt: Receipt
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            border = BorderStroke(
                width = 1.dp,
                color = RoyalGold.copy(alpha = 0.7f)
            )
        ) {
            Text(
                text = "RC",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = receipt.storeName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        /**
         * Store address can have multiple lines.
         * Example:
         * Royal Road
         * Phoenix
         * Mauritius
         */
        receipt.storeAddressLines.forEach { addressLine ->
            Text(
                text = addressLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Tel: ${receipt.storePhone}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Receipt No: ${receipt.receiptNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Date/Time: ${receipt.receiptDateTime}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Cashier: ${receipt.cashierName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Displays all finalized receipt items.
 */
@Composable
private fun ReceiptItemsSection(
    items: List<ReceiptItem>
) {
    Text(
        text = "Items",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    items.forEach { item ->
        ReceiptItemRow(receiptItem = item)
    }
}

/**
 * Displays one product line on the receipt.
 */
@Composable
private fun ReceiptItemRow(
    receiptItem: ReceiptItem
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = receiptItem.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Qty ${receiptItem.quantity} x Rs ${receiptItem.unitPrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Text(
                text = "Rs ${receiptItem.lineTotal}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Displays the receipt calculation section.
 */
@Composable
private fun ReceiptSummarySection(
    receipt: Receipt
) {
    Text(
        text = "Order Summary",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    ReceiptSummaryRow(
        label = "Subtotal",
        value = "Rs ${receipt.subtotal}"
    )

    ReceiptSummaryRow(
        label = "Discount",
        value = "Rs ${receipt.discount}"
    )

    ReceiptSummaryRow(
        label = "Tax",
        value = "Rs ${receipt.tax}"
    )

    Spacer(modifier = Modifier.height(8.dp))

    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    )

    Spacer(modifier = Modifier.height(10.dp))

    ReceiptTotalRow(total = receipt.total)
}

/**
 * Reusable two-column row.
 */
@Composable
private fun ReceiptSummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Displays the final total prominently.
 */
@Composable
private fun ReceiptTotalRow(
    total: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Total",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Rs $total",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Displays payment information.
 */
@Composable
private fun PaymentSection(
    receipt: Receipt
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = "Payment",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        ReceiptSummaryRow(
            label = "Method",
            value = receipt.paymentMethod.name
        )

        if (receipt.amountPaid != null) {
            ReceiptSummaryRow(
                label = "Amount Paid",
                value = "Rs ${receipt.amountPaid}"
            )
        }

        if (receipt.hasChange) {
            ReceiptSummaryRow(
                label = "Change",
                value = "Rs ${receipt.changeGiven}"
            )
        }
    }
}

/**
 * Small footer at the bottom of the receipt.
 */
@Composable
private fun ReceiptFooter(
    footerMessage: String
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = footerMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Reusable divider for separating receipt sections.
 */
@Composable
private fun ReceiptSectionDivider() {
    Spacer(modifier = Modifier.height(16.dp))

    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    )

    Spacer(modifier = Modifier.height(16.dp))
}