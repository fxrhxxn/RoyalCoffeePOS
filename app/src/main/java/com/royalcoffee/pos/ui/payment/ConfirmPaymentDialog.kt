package com.royalcoffee.pos.ui.payment

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
            Text(
                text = "$message\n\n$amountDueText"
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
