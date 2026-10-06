package com.royalcoffee.pos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
// * CategoryEntity represents one product category stored in the database
 *
 * Examples:
 * - Coffee
 * - Tea
 * - Pastries
 * - Cold Drinks
 *
 * Categories are managed separately from products so that
 * Admin users can create and remove available categories.
 */
@Entity(
    tableName = "categories",

    /*
     Prevent duplicate category names at database level. For example, two exact "Coffee" records cannot exist.
     */
    indices = [
        Index(
            value = ["category_name"],
            unique = true
        )
    ]
)
data class CategoryEntity(

    /**
     * Unique identifier generated automatically by Room.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    val categoryId: Int = 0,

    /**
     * Name shown in:
     *
     * - Add Product dropdown
     * - Edit Product dropdown
     * - Inventory filters
     * - Category management screen
     */
    @ColumnInfo(name = "category_name")
    val categoryName: String
)