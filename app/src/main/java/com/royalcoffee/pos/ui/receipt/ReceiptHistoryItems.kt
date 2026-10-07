package com.royalcoffee.pos.ui.receipt

/**
 * ReceiptHistoryItem is the UI model used by the Receipt History screen.
 *
 * It is separate from ReceiptHeaderEntity because the UI should not depend
 * directly on database table classes.
 */
data class ReceiptHistoryItem(
    val receiptId: Int,
    val receiptNumber: String,
    val receiptDateTime: String,
    val cashierName: String,
    val total: Long,
    val paymentMethod: String
)
