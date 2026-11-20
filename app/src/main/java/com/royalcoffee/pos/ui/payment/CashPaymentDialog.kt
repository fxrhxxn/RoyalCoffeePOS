package com.royalcoffee.pos.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.R
import com.royalcoffee.pos.model.Money
import com.royalcoffee.pos.model.PaymentMethod

@Composable
fun CashPaymentDialog(
    total: Long,
    onConfirmPayment: (
        paymentMethod: PaymentMethod,
        amountPaid: Long,
        changeGiven: Long
    ) -> Unit,
    onCancel: () -> Unit
) {
    var amountReceivedText by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val amountReceived = Money.parse(amountReceivedText)
    val change = if (amountReceived != null) {
        amountReceived - total
    } else {
        0
    }

    val errEnterAmount = stringResource(R.string.payment_error_enter_amount)
    val errInsufficient = stringResource(R.string.payment_error_insufficient)

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = stringResource(R.string.payment_cash_title))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.payment_amount_due_format, Money.format(total)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = amountReceivedText,
                    onValueChange = { newValue ->
                        amountReceivedText = newValue
                        errorMessage = null
                    },
                    label = {
                        Text(text = stringResource(R.string.payment_amount_received))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (amountReceived != null) {
                    Text(
                        text = stringResource(R.string.payment_change_format, Money.format(change.coerceAtLeast(0))),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        amountReceived == null -> {
                            errorMessage = errEnterAmount
                        }

                        amountReceived < total -> {
                            errorMessage = errInsufficient
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
                Text(text = stringResource(R.string.payment_confirm))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel
            ) {
                Text(text = stringResource(R.string.common_cancel))
            }
        }
    )
}
