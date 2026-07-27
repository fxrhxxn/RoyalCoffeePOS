package com.royalcoffee.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ProductEntity represents one row in the products table.
 *
 * Entity = database table model.
 *
 * This table will store products that appear in the Sales screen
 * and Inventory screen.
 */
@Entity(
    tableName = "products"
)
data class ProductEntity(
    /**
     * Unique product ID.
     *
     * autoGenerate = true means Room can automatically create IDs
     * when new products are inserted later.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "product_id")
    val productId: Int = 0,

    /**
     * Product name shown to the cashier.
     *
     * Example:
     * Latte, Espresso, Croissant.
     */
    @ColumnInfo(name = "product_name")
    val productName: String,

    /**
     * Product category.
     *
     * For now this is stored as text.
     * Later, we can create a separate categories table if needed.
     */
    @ColumnInfo(name = "category")
    val category: String,

    /**
     * Selling price in Mauritian Rupees.
     *
     * We store money as Int for now because your prices are whole numbers.
     * Example:
     * 120 = Rs 120
     */
    @ColumnInfo(name = "unit_price")
    val unitPrice: Int,

    /**
     * Current stock quantity.
     */
    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Int,

    /**
     * Minimum stock level before the product is considered low stock.
     */
    @ColumnInfo(name = "reorder_level")
    val reorderLevel: Int,

    /**
     * Controls whether the product should be shown in the app.
     *
     * true = active product
     * false = hidden/disabled product
     */
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)