package com.royalcoffee.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ReceiptHeaderEntity represents the main receipt record.
 *
 * One receipt header = one completed customer transaction.
 *
 * Example:
 * Receipt RC-2026-000001 has:
 * - date/time
 * - cashier
 * - totals
 * - payment method
 */
@Entity(
    tableName = "receipt_headers"
)
data class ReceiptHeaderEntity(
    /**
     * Local auto-generated database ID.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "receipt_id")
    val receiptId: Int = 0,

    /**
     * Human-readable receipt number shown to the customer.
     *
     * Example:
     * RC-2026-000001
     */
    @ColumnInfo(name = "receipt_number")
    val receiptNumber: String,

    /**
     * Date and time shown on the receipt.
     *
     * For now, we store it as formatted text.
     * Later, we can store ISO timestamps for better filtering.
     */
    @ColumnInfo(name = "receipt_date_time")
    val receiptDateTime: String,

    /**
     * Cashier name shown on the receipt.
     */
    @ColumnInfo(name = "cashier_name")
    val cashierName: String,

    /**
     * Subtotal before discount and tax.
     */
    @ColumnInfo(name = "subtotal")
    val subtotal: Int,

    /**
     * Discount applied to the order.
     */
    @ColumnInfo(name = "discount")
    val discount: Int,

    /**
     * Tax amount.
     */
    @ColumnInfo(name = "tax")
    val tax: Int,

    /**
     * Final total paid by the customer.
     */
    @ColumnInfo(name = "total")
    val total: Int,

    /**
     * Payment method used.
     *
     * Example:
     * CASH, CARD, MOBILE
     */
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,

    /**
     * Amount paid by the customer.
     *
     * Mainly useful for cash payments.
     */
    @ColumnInfo(name = "amount_paid")
    val amountPaid: Int?,

    /**
     * Change given back to the customer.
     *
     * Mainly useful for cash payments.
     */
    @ColumnInfo(name = "change_given")
    val changeGiven: Int?,

    /**
     * Footer message used on the receipt.
     */
    @ColumnInfo(name = "footer_message")
    val footerMessage: String
)