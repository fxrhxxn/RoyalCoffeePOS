package com.royalcoffee.pos.model

// A cart item represents a product inside the current ticket
data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val lineTotal: Int
        get() = product.price * quantity
}