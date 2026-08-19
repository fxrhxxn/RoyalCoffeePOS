package com.royalcoffee.pos.util

/**
 * Thrown when an order cannot be completed due to insufficient stock.
 */
class InsufficientStockException(
    val productName: String,
    val availableStock: Int,
    val requestedQuantity: Int
) : Exception("Insufficient stock for $productName. Requested: $requestedQuantity, Available: $availableStock")
