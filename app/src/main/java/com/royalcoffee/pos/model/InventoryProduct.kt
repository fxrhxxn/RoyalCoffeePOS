package com.royalcoffee.pos.model

/**
 * InventoryProduct represents a product shown in the Inventory screen.
 *
 * For now, this is temporary in-memory data.
 * Later, this model will be replaced or connected to a Room database entity.
 */
data class InventoryProduct(
    /**
     * Unique product ID.
     *
     * Later, this will come from the Products table.
     */
    val productId: Int,

    /**
     * Product name shown to the Admin.
     *
     * Example:
     * Latte, Espresso, Croissant.
     */
    val name: String,

    /**
     * Product category.
     *
     * Example:
     * Coffee, Pastries, Cold, Hot.
     */
    val category: String,

    /**
     * Selling price of the product.
     */
    val unitPrice: Int,

    /**
     * Cost price of the product.
     */
    val costPrice: Int,

    /**
     * Current stock quantity available.
     */
    val stockQuantity: Int,

    /**
     * Minimum stock level before the item is considered low stock.
     */
    val reorderLevel: Int,

    /**
     * Whether this product is active.
     *
     * Inactive products can later be hidden from the Sales screen.
     */
    val isActive: Boolean = true
) {
    /**
     * Returns true when the product stock is at or below the reorder level.
     */
    val isLowStock: Boolean
        get() = stockQuantity <= reorderLevel

    /**
     * User-friendly stock status shown in the Inventory screen.
     */
    val stockStatus: String
        get() = if (isLowStock) "Low Stock" else "In Stock"
}