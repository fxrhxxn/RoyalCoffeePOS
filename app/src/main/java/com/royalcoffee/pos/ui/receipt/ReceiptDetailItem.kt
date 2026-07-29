package com.royalcoffee.pos.ui.receipt

/**
 * ReceiptDetailItem is the UI model for one product line
 * inside a saved receipt.
 *
 * Example:
 * 2 x Latte at Rs 120 = Rs 240
 */
data class ReceiptDetailItem(
    val productName: String,
    val quantity: Int,
    val unitPrice: Int,
    val lineTotal: Int
)