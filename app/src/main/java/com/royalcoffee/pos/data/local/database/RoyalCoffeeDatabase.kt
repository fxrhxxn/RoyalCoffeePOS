package com.royalcoffee.pos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.royalcoffee.pos.data.local.dao.InventoryHistoryDao
import com.royalcoffee.pos.data.local.dao.ProductDao
import com.royalcoffee.pos.data.local.dao.ReceiptDao
import com.royalcoffee.pos.data.local.entity.InventoryHistoryEntity
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
        ReceiptDetailEntity::class,
        InventoryHistoryEntity::class
    ],
    version = 4,
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

    /**
     * Gives access to inventory history operations.
     */
    abstract fun inventoryHistoryDao(): InventoryHistoryDao

    companion object {
        /**
         * Migration from version 3 to 4:
         * Adds the 'inventory_history' table.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `inventory_history` (
                        `history_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `product_id` INTEGER NOT NULL, 
                        `product_name_snapshot` TEXT NOT NULL, 
                        `transaction_type` TEXT NOT NULL, 
                        `quantity_change` INTEGER NOT NULL, 
                        `previous_stock` INTEGER NOT NULL, 
                        `new_stock` INTEGER NOT NULL, 
                        `reason` TEXT, 
                        `timestamp` INTEGER NOT NULL, 
                        FOREIGN KEY(`product_id`) REFERENCES `products`(`product_id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_history_product_id` ON `inventory_history` (`product_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_history_timestamp` ON `inventory_history` (`timestamp`)")
            }
        }
    }
}
