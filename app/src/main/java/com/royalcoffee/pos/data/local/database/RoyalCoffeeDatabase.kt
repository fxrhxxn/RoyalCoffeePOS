package com.royalcoffee.pos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.royalcoffee.pos.data.local.dao.BusinessDao
import com.royalcoffee.pos.data.local.dao.InventoryHistoryDao
import com.royalcoffee.pos.data.local.dao.ProductDao
import com.royalcoffee.pos.data.local.dao.ReceiptDao
import com.royalcoffee.pos.data.local.dao.UserDao
import com.royalcoffee.pos.data.local.entity.BusinessEntity
import com.royalcoffee.pos.data.local.entity.InventoryHistoryEntity
import com.royalcoffee.pos.data.local.entity.ProductEntity
import com.royalcoffee.pos.data.local.entity.ReceiptDetailEntity
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import com.royalcoffee.pos.data.local.entity.UserEntity
import com.royalcoffee.pos.data.local.dao.CategoryDao
import com.royalcoffee.pos.data.local.entity.CategoryEntity

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
        InventoryHistoryEntity::class,
        UserEntity::class,
        BusinessEntity::class,
        CategoryEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class RoyalCoffeeDatabase : RoomDatabase() {

    // Gives access to product-related database operations.
    abstract fun productDao(): ProductDao

    // Gives access to category-related database operations.
    abstract fun CategoryDao(): CategoryDao

    // Gives access to receipt-related database operations.
    abstract fun receiptDao(): ReceiptDao

    // Gives access to inventory history operations.
    abstract fun inventoryHistoryDao(): InventoryHistoryDao

    // Gives access to user operations.
    abstract fun userDao(): UserDao

    // Gives access to business info operations.
    abstract fun businessDao(): BusinessDao



    companion object {
        /** Preserve all historical charges; convert whole rupees to cents exactly once. */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE products SET unit_price = unit_price * 100, cost_price = cost_price * 100")
                db.execSQL("UPDATE receipt_headers SET subtotal = subtotal * 100, discount = discount * 100, tax = tax * 100, total = total * 100, amount_paid = amount_paid * 100, change_given = change_given * 100")
                db.execSQL("UPDATE receipt_details SET unit_price = unit_price * 100, cost_price = cost_price * 100, line_total = line_total * 100")
                db.execSQL("ALTER TABLE receipt_headers ADD COLUMN discount_percent INTEGER")
                db.execSQL("ALTER TABLE receipt_headers ADD COLUMN vat_included INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE receipt_details ADD COLUMN discount_amount INTEGER NOT NULL DEFAULT 0")
            }
        }

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

        /**
         * Migration from version 4 to 5:
         * 1. Adds 'timestamp' column to 'receipt_headers'.
         * 2. Populates it from existing 'receipt_date_time' strings (dd/MM/yyyy HH:mm).
         * 3. Adds index on 'timestamp'.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add the column
                db.execSQL("ALTER TABLE `receipt_headers` ADD COLUMN `timestamp` INTEGER NOT NULL DEFAULT 0")

                // 2. Derive timestamp from receipt_date_time (dd/MM/yyyy HH:mm)
                // SQLite strftime('%s', 'YYYY-MM-DD HH:MM:SS') returns epoch seconds
                db.execSQL(
                    """
                    UPDATE `receipt_headers`
                    SET `timestamp` = strftime('%s', 
                        substr(receipt_date_time, 7, 4) || '-' || 
                        substr(receipt_date_time, 4, 2) || '-' || 
                        substr(receipt_date_time, 1, 2) || ' ' || 
                        substr(receipt_date_time, 12, 2) || ':' || 
                        substr(receipt_date_time, 15, 2) || ':00'
                    ) * 1000
                    WHERE receipt_date_time IS NOT NULL AND length(receipt_date_time) >= 16
                    """.trimIndent()
                )

                // 3. Add index
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_receipt_headers_timestamp` ON `receipt_headers` (`timestamp`)")
            }
        }

        /**
         * Migration from version 5 to 6:
         * 1. Adds 'cost_price' to 'products' (Default 0 = Unknown).
         * 2. Adds 'cost_price' to 'receipt_details' (Nullable = Unknown).
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `products` ADD COLUMN `cost_price` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `receipt_details` ADD COLUMN `cost_price` INTEGER")
            }
        }

        /**
         * Migration from version 8 to 9:
         *
         * Adds the categories table.
         *
         * Existing category names already stored inside products
         * are copied into the categories table automatically.
         *
         * This allows existing installations to keep their product data
         * while introducing proper category management.
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {

            override fun migrate(
                db: SupportSQLiteDatabase
            ) {

                /*
                 * 1. Create categories table.
                 */
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `categories` (
                `category_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `category_name` TEXT NOT NULL
            )
            """.trimIndent()
                )
                /*
                 * 2. Create a unique index.
                 *
                 * Prevents exact duplicate category names.
                 */
                db.execSQL(
                    """
            CREATE UNIQUE INDEX IF NOT EXISTS
            `index_categories_category_name`
            ON `categories` (`category_name`)
            """.trimIndent()
                )


                /*
                 * 3. Copy existing categories from products.
                 *
                 * For example:
                 *
                 * Latte       -> Coffee
                 * Espresso    -> Coffee
                 * Croissant   -> Pastries
                 *
                 * becomes:
                 *
                 * Coffee
                 * Pastries
                 *
                 * inside the categories table.
                 */
                db.execSQL(
                    """
            INSERT OR IGNORE INTO `categories` (`category_name`)
            SELECT DISTINCT TRIM(`category`)
            FROM `products`
            WHERE TRIM(`category`) <> ''
            ORDER BY `category`
            """.trimIndent()
                )
            }
        }
    }
}

