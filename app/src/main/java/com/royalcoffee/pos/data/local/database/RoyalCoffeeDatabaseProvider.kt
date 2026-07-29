package com.royalcoffee.pos.data.local.database

import android.content.Context
import androidx.room.Room

/**
 * RoyalCoffeeDatabaseProvider creates and provides access to the Room database.
 *
 * We use a singleton pattern here because the app should normally use
 * one shared database instance instead of opening many database instances.
 */
object RoyalCoffeeDatabaseProvider {

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
                /**
                 * Development-only migration shortcut.
                 *
                 * Because we are still building the app and changing tables,
                 * this allows Room to recreate the database when the version changes.
                 *
                 * Later, for a production app, we should replace this with real migrations.
                 */
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

            INSTANCE = database
            database
        }
    }
}