package com.royalcoffee.pos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.royalcoffee.pos.data.local.dao.ProductDao
import com.royalcoffee.pos.data.local.entity.ProductEntity

/**
 * RoyalCoffeeDatabase is the main Room database for the app.
 *
 * This class lists:
 * - the database tables
 * - the database version
 * - the DAOs used to access data
 *
 * For now, the database contains only one table:
 * - products
 */
@Database(
    entities = [
        ProductEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RoyalCoffeeDatabase : RoomDatabase() {

    /**
     * Gives access to product-related database operations.
     */
    abstract fun productDao(): ProductDao
}