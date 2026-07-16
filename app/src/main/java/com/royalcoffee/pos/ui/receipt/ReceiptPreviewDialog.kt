package com.royalcoffee.pos.ui.receipt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.PaymentMethod
import com.royalcoffee.pos.ui.theme.RoyalGold

@Composable
fun ReceiptPreviewDialog(
    receiptNumber: String,
    receiptDateTime: String,
    cartItems: List<CartItem>,
    subtotal: Int,
    discount: Int,
    tax: Int,
    total: Int,
    paymentMethod: PaymentMethod,
    amountPaid: Int?,
    changeGiven: Int?,
    onClose: () -> Unit,
    onNextOrder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                text = "Receipt Preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            ReceiptContent(
                receiptNumber = receiptNumber,
                receiptDateTime = receiptDateTime,
                cartItems = cartItems,
                subtotal = subtotal,
                discount = discount,
                tax = tax,
                total = total,
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                changeGiven = changeGiven
            )
        },
        confirmButton = {
            Button(
                onClick = onNextOrder
            ) {
                Text(text = "Next Order")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onClose
            ) {
                Text(text = "Close")
            }
        }
    )
}

@Composable
private fun ReceiptContent(
    receiptNumber: String,
    receiptDateTime: String,
    cartItems: List<CartItem>,
    subtotal: Int,
    discount: Int,
    tax: Int,
    total: Int,
    paymentMethod: PaymentMethod,
    amountPaid: Int?,
    changeGiven: Int?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 520.dp),
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
            ReceiptHeader(
                receiptNumber = receiptNumber,
                receiptDateTime = receiptDateTime
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Items",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            cartItems.forEach { item ->
                ReceiptItemRow(cartItem = item)
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReceiptSummaryRow(
                label = "Subtotal",
                value = "Rs $subtotal"
            )

            ReceiptSummaryRow(
                label = "Discount",
                value = "Rs $discount"
            )

            ReceiptSummaryRow(
                label = "Tax",
                value = "Rs $tax"
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ReceiptTotalRow(total = total)

            Spacer(modifier = Modifier.height(18.dp))

            PaymentSection(
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                changeGiven = changeGiven
            )

            Spacer(modifier = Modifier.height(18.dp))

            ReceiptFooter()
        }
    }
}

@Composable
private fun ReceiptHeader(
    receiptNumber: String,
    receiptDateTime: String
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
            text = "Royal Coffee POS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Receipt No: $receiptNumber",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Date/Time: $receiptDateTime",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReceiptItemRow(
    cartItem: CartItem
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
                    text = cartItem.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Qty ${cartItem.quantity} x Rs ${cartItem.product.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Text(
                text = "Rs ${cartItem.lineTotal}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

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

@Composable
private fun PaymentSection(
    paymentMethod: PaymentMethod,
    amountPaid: Int?,
    changeGiven: Int?
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
            value = paymentMethod.name
        )

        if (amountPaid != null) {
            ReceiptSummaryRow(
                label = "Amount Paid",
                value = "Rs $amountPaid"
            )
        }

        if (changeGiven != null && changeGiven > 0) {
            ReceiptSummaryRow(
                label = "Change",
                value = "Rs $changeGiven"
            )
        }
    }
}

@Composable
private fun ReceiptFooter() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Thank you for your purchase.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
    }
}