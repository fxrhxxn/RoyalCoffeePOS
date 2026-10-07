package com.royalcoffee.pos.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * RoyalCoffeeDatabaseProvider creates and provides access to the Room database.
 *
 * We use a singleton pattern here because the app should normally use
 * one shared database instance instead of opening many database instances.
 */
object RoyalCoffeeDatabaseProvider {

    /**
     * Migration from version 2 to 3.
     *
     * This migration adds a unique index to the receipt_number column
     * in the receipt_headers table.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            /**
             * Fix existing receipt numbers by re-sequencing them within their year.
             * This ensures uniqueness while maintaining the RC-YYYY-XXXXXX format
             * and preserving the order of transactions using receipt_id.
             *
             * This correlated subquery counts how many receipts exist for the same year
             * with an ID less than or equal to the current one, then formats the result.
             */
            db.execSQL(
                """
                UPDATE receipt_headers 
                SET receipt_number = 'RC-' || substr(receipt_number, 4, 4) || '-' || printf('%06d', (
                    SELECT COUNT(*) 
                    FROM receipt_headers AS rh2 
                    WHERE rh2.receipt_id <= receipt_headers.receipt_id 
                    AND substr(rh2.receipt_number, 4, 4) = substr(receipt_headers.receipt_number, 4, 4)
                ))
                """.trimIndent()
            )

            /**
             * Create a unique index on the receipt_number column now that duplicates are resolved.
             */
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_receipt_headers_receipt_number` " +
                        "ON `receipt_headers` (`receipt_number`)"
            )
        }
    }

    /**
     * Volatile makes sure changes to INSTANCE are visible across threads.
     */
    @Volatile
    private var INSTANCE: RoyalCoffeeDatabase? = null

    /**
     * Returns the existing database instance or creates one if it does not exist yet.
     */
    fun getDatabase(context: Context): RoyalCoffeeDatabase {
        return INSTANCE ?: synchronized(this) {
            val database = Room.databaseBuilder(
                context.applicationContext,
                RoyalCoffeeDatabase::class.java,
                "royal_coffee_pos_database"
            )
                .addMigrations(MIGRATION_2_3)
                /**
                 * Development-only migration shortcut.
                 *
                 * Because we are still building the app and changing tables,
                 * this allows Room to recreate the database when the version changes.
                 *
                 * Later, for a production app, we should replace this with real migrations.
                 */
                .addMigrations(RoyalCoffeeDatabase.MIGRATION_3_4)
                .addMigrations(RoyalCoffeeDatabase.MIGRATION_4_5)
                .addMigrations(RoyalCoffeeDatabase.MIGRATION_5_6)
                .addMigrations(RoyalCoffeeDatabase.MIGRATION_8_9)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

            INSTANCE = database
            database
        }
    }
}
