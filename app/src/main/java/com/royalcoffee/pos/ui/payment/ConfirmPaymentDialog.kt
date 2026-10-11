package com.royalcoffee.pos.ui.payment

import com.royalcoffee.pos.model.Money
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.royalcoffee.pos.model.PaymentMethod

@Composable
fun ConfirmPaymentDialog(
    paymentMethod: PaymentMethod,
    total: Long,
    onConfirmPayment: (
        paymentMethod: PaymentMethod,
        amountPaid: Long,
        changeGiven: Long
    ) -> Unit,
    onCancel: () -> Unit
) {
    val title = when (paymentMethod) {
        PaymentMethod.CARD -> "Card Payment"
        PaymentMethod.MOBILE -> "Mobile Payment / Juice"
        PaymentMethod.CASH -> "Cash Payment"
    }

    val message = when (paymentMethod) {
        PaymentMethod.CARD -> {
            "Waiting for Card Payment Confirmation."
        }

        PaymentMethod.MOBILE -> {
            "Waiting for Mobile Payment Confirmation"
        }

        PaymentMethod.CASH -> {
            "Confirm cash payment."
        }
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = title)
        },
        text = {
            Text(
                text = "$message\n\nAmount due: Rs ${Money.format(total)}"
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmPayment(
                        paymentMethod,
                        total,
                        0
                    )
                }
            ) {
                Text(text = "Confirm Payment")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel
            ) {
                Text(text = "Cancel")
            }
        }
    )
}
