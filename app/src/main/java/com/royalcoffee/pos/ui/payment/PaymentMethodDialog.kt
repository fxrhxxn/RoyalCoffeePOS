package com.royalcoffee.pos.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.model.PaymentMethod

@Composable
fun PaymentMethodDialog(
    total: Int,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select Payment Method")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Amount due: Rs $total")

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onPaymentMethodSelected(PaymentMethod.CASH)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cash")
                }

                Button(
                    onClick = {
                        onPaymentMethodSelected(PaymentMethod.CARD)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Card")
                }

                Button(
                    onClick = {
                        onPaymentMethodSelected(PaymentMethod.MOBILE)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Mobile Payment")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.width(120.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    )
}