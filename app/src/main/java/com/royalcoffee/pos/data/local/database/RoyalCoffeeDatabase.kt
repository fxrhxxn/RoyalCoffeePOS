package com.royalcoffee.pos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.royalcoffee.pos.data.local.dao.ProductDao
import com.royalcoffee.pos.data.local.dao.ReceiptDao
import com.royalcoffee.pos.data.local.entity.ProductEntity
import com.royalcoffee.pos.data.local.entity.ReceiptDetailEntity
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity

/**
 * RoyalCoffeeDatabase is the main Room database for the app.
 *
 * This class lists:
 * - the database tables
 * - the database version
 * - the DAOs used to access data
 */
@Database(
    entities = [
        ProductEntity::class,
        ReceiptHeaderEntity::class,
        ReceiptDetailEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RoyalCoffeeDatabase : RoomDatabase() {

    /**
     * Gives access to product-related database operations.
     */
    abstract fun productDao(): ProductDao

    /**
     * Gives access to receipt-related database operations.
     */
    abstract fun receiptDao(): ReceiptDao
}