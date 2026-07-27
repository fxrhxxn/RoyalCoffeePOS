package com.royalcoffee.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.royalcoffee.pos.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * ProductDao contains database operations for the products table.
 *
 * DAO = Data Access Object.
 *
 * This is where we define SQL queries such as:
 * - get all products
 * - insert products
 * - find low-stock products
 */
@Dao
interface ProductDao {

    /**
     * Returns all active products.
     *
     * Flow allows the UI to automatically receive updates
     * when the database changes.
     */
    @Query(
        """
        SELECT *
        FROM products
        WHERE is_active = 1
        ORDER BY product_name ASC
        """
    )
    fun getActiveProducts(): Flow<List<ProductEntity>>

    /**
     * Returns all products, including inactive products.
     *
     * This is useful for the Admin inventory screen.
     */
    @Query(
        """
        SELECT *
        FROM products
        ORDER BY product_name ASC
        """
    )
    fun getAllProducts(): Flow<List<ProductEntity>>

    /**
     * Returns products where stock is at or below the reorder level.
     *
     * This will be useful for:
     * - Inventory screen
     * - Dashboard low-stock chart
     * - Future notifications
     */
    @Query(
        """
        SELECT *
        FROM products
        WHERE stock_quantity <= reorder_level
        AND is_active = 1
        ORDER BY stock_quantity ASC
        """
    )
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    /**
     * Inserts one product.
     *
     * If a product with the same primary key already exists,
     * it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    /**
     * Inserts multiple products.
     *
     * This will be useful when we seed default sample products
     * the first time the app database is created.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    /**
     * Deletes all products.
     *
     * This is mainly useful for testing and development.
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}