package com.royalcoffee.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ReceiptDetailEntity represents one product line inside a receipt.
 *
 * One receipt can have many receipt detail rows.
 *
 * Example:
 * Receipt RC-2026-000001:
 * - 2 x Latte
 * - 1 x Croissant
 */
@Entity(
    tableName = "receipt_details",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptHeaderEntity::class,
            parentColumns = ["receipt_id"],
            childColumns = ["receipt_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["receipt_id"])
    ]
)
data class ReceiptDetailEntity(
    /**
     * Local auto-generated detail ID.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "receipt_detail_id")
    val receiptDetailId: Int = 0,

    /**
     * Links this detail row to a receipt header.
     */
    @ColumnInfo(name = "receipt_id")
    val receiptId: Int,

    /**
     * Product name at the time of sale.
     *
     * We store the name here so old receipts remain accurate even if
     * the product name changes later.
     */
    @ColumnInfo(name = "product_name")
    val productName: String,

    /**
     * Quantity sold.
     */
    @ColumnInfo(name = "quantity")
    val quantity: Int,

    /**
     * Unit price at the time of sale.
     *
     * We store this here so old receipts remain accurate even if
     * the product price changes later.
     */
    @ColumnInfo(name = "unit_price")
    val unitPrice: Int,

    /**
     * Total for this receipt line.
     *
     * Example:
     * quantity = 2
     * unitPrice = 120
     * lineTotal = 240
     */
    @ColumnInfo(name = "line_total")
    val lineTotal: Int
)