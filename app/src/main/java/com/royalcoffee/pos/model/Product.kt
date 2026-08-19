package com.royalcoffee.pos.model

// A dataclass is used to represent structured data
data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Int,
    val stockQuantity: Int,
    val reorderLevel: Int
)