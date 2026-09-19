package com.royalcoffee.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Transaction types for inventory changes.
 */
enum class InventoryTransactionType {
    SALE,
    RESTOCK,
    ADJUSTMENT,
    WASTE,
    INITIAL_STOCK
}

/**
 * InventoryHistoryEntity tracks every stock-changing event for auditing and reporting.
 */
@Entity(
    tableName = "inventory_history",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["product_id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["timestamp"])
    ]
)
data class InventoryHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "history_id")
    val historyId: Int = 0,

    @ColumnInfo(name = "product_id")
    val productId: Int,

    /**
     * Snapshotted name at the time of the transaction for historical display.
     */
    @ColumnInfo(name = "product_name_snapshot")
    val productNameSnapshot: String,

    @ColumnInfo(name = "transaction_type")
    val transactionType: InventoryTransactionType,

    /**
     * The amount the stock changed by (e.g., -2 for a sale, +10 for a restock).
     */
    @ColumnInfo(name = "quantity_change")
    val quantityChange: Int,

    @ColumnInfo(name = "previous_stock")
    val previousStock: Int,

    @ColumnInfo(name = "new_stock")
    val newStock: Int,

    /**
     * Optional reason for adjustments or waste.
     */
    @ColumnInfo(name = "reason")
    val reason: String?,

    /**
     * Milliseconds since epoch.
     */
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
