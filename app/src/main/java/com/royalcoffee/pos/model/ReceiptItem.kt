package com.royalcoffee.pos.model

/**
 * ReceiptItem represents one product line on a receipt.
 *
 * Example:
 * Latte
 * Qty 2 x Rs 120
 * Line total = Rs 240
 *
 * This model is separate from CartItem because:
 * - CartItem represents the live order before payment.
 * - ReceiptItem represents the finalized item printed on a receipt.
 */
data class ReceiptItem(
    val productName: String,
    val quantity: Int,
    val unitPrice: Int,
    val costPrice: Int? = null
) {
    /**
     * Calculates the total price for this item line.
     *
     * Example:
     * quantity = 2
     * unitPrice = 120
     * lineTotal = 240
     */
    val lineTotal: Int
        get() = quantity * unitPrice
}