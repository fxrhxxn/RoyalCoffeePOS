package com.royalcoffee.pos.model

/**
 * Receipt represents a completed paid order.
 *
 * This model will become the main source of receipt data for:
 * - Receipt preview
 * - Thermal printer output
 * - PDF download
 * - WhatsApp sharing
 * - Future database storage
 */
data class Receipt(
    val receiptNumber: String,
    val receiptDateTime: String,

    /**
     * Store information shown at the top of the receipt.
     *
     * Later, these values can come from the Company table in the database.
     * For now, they can be filled manually when creating the receipt.
     */
    val storeName: String,
    val storeAddressLines: List<String>,
    val storePhone: String,

    /**
     * Cashier name or username.
     *
     * For now, this can be something simple like:
     * "Admin" or "Barista".
     *
     * Later, this will come from the logged-in user.
     */
    val cashierName: String,

    /**
     * Finalized list of receipt items.
     *
     * These are created from the cart items at the moment
     * the receipt is generated.
     */
    val items: List<ReceiptItem>,

    /**
     * Order calculation values.
     *
     * We store these directly instead of recalculating them later because
     * a receipt should represent the transaction exactly as it happened.
     */
    val subtotal: Long,
    val discount: Long,
    val tax: Long,
    val total: Long,

    /**
     * Payment information.
     *
     * PaymentMethod is already used by the app for Cash, Card, and Mobile Payment.
     */
    val paymentMethod: PaymentMethod,
    val amountPaid: Long?,
    val changeGiven: Long?,

    /**
     * Footer message printed at the bottom of the receipt.
     *
     * Later, Admin will be able to change this from Settings.
     */
    val footerMessage: String = "Thank you for your purchase.",
    val discountPercent: Int? = null,
    val vatIncluded: Boolean = false
) {
    /**
     * Returns the total quantity of items sold in this receipt.
     *
     * Example:
     * Latte x 2
     * Muffin x 1
     * totalItemCount = 3
     */
    val totalItemCount: Int
        get() = items.sumOf { it.quantity }

    /**
     * Returns true when this receipt has at least one item.
     *
     * Useful before allowing printing, PDF generation, or saving.
     */
    val hasItems: Boolean
        get() = items.isNotEmpty()

    /**
     * Returns true when the receipt has been change to give back.
     *
     * Usually applies to cash payments.
     */
    val hasChange: Boolean
        get() = changeGiven != null && changeGiven > 0
}
