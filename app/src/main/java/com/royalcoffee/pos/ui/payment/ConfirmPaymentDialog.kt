package com.royalcoffee.pos.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.R
import com.royalcoffee.pos.model.Money
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
        PaymentMethod.CARD -> stringResource(R.string.payment_card_title)
        PaymentMethod.MOBILE -> stringResource(R.string.payment_mobile_title)
        PaymentMethod.CASH -> stringResource(R.string.payment_cash_title)
    }

    val message = when (paymentMethod) {
        PaymentMethod.CARD -> stringResource(R.string.payment_card_waiting)
        PaymentMethod.MOBILE -> stringResource(R.string.payment_mobile_waiting)
        PaymentMethod.CASH -> stringResource(R.string.payment_cash_title)
    }

    val amountDueText = stringResource(R.string.payment_amount_due_format, Money.format(total))

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = title)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Text(
                    text = amountDueText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
