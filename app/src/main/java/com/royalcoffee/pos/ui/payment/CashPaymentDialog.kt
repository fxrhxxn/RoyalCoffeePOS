package com.royalcoffee.pos.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.model.PaymentMethod

@Composable
fun CashPaymentDialog(
    total: Int,
    onConfirmPayment: (
        paymentMethod: PaymentMethod,
        amountPaid: Int,
        changeGiven: Int
    ) -> Unit,
    onCancel: () -> Unit
) {
    var amountReceivedText by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val amountReceived = amountReceivedText.toIntOrNull()
    val change = if (amountReceived != null) {
        amountReceived - total
    } else {
        0
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Cash Payment")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Amount due: Rs $total")

                OutlinedTextField(
                    value = amountReceivedText,
                    onValueChange = { newValue ->
                        amountReceivedText = newValue.filter { it.isDigit() }
                        errorMessage = null
                    },
                    label = {
                        Text(text = "Amount received")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (amountReceived != null) {
                    Text(text = "Change: Rs ${change.coerceAtLeast(0)}")
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        amountReceived == null -> {
                            errorMessage = "Enter amount received"
                        }

                        amountReceived < total -> {
                            errorMessage = "Amount received is less than total"
                        }

                        else -> {
                            onConfirmPayment(
                                PaymentMethod.CASH,
                                amountReceived,
                                amountReceived - total
                            )
                        }
                    }
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