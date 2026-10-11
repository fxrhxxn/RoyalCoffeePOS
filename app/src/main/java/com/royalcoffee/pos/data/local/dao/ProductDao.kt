package com.royalcoffee.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.royalcoffee.pos.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * ProductDao (Data Access Object) is an interface that contains database operations for the products table.
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
    @Query("SELECT EXISTS(SELECT 1 FROM inventory_history WHERE product_id=:id) OR EXISTS(SELECT 1 FROM receipt_details WHERE product_id=:id) OR EXISTS(SELECT 1 FROM products WHERE product_id=:id AND (legacy_history_uncertain=1 OR stock_quantity>0))")
    suspend fun hasHistory(id: Int): Boolean
    @Query("DELETE FROM products WHERE product_id=:id")
    suspend fun deleteProduct(id: Int)

    @Query("SELECT * FROM products WHERE is_active=1")
    suspend fun alertProducts(): List<ProductEntity>


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
     * Returns the current stock quantity for a specific product.
     */
    @Query("SELECT stock_quantity FROM products WHERE product_id = :productId")
    suspend fun getProductStock(productId: Int): Int?

    /**
     * Deducts a specific quantity from the product's stock only if sufficient stock exists.
     *
     * Returns the number of rows affected.
     * If 1, deduction was successful.
     * If 0, stock was insufficient or product not found.
     */
    @Query(
        """
        UPDATE products 
        SET stock_quantity = stock_quantity - :quantity 
        WHERE product_id = :productId 
        AND stock_quantity >= :quantity
        """
    )
    suspend fun deductProductStock(productId: Int, quantity: Int): Int

    /**
     * Inserts one product.
     *
     * If a product with the same primary key already exists,
     * it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity)

    /**
     * Inserts one product and returns its ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProductAndGetId(product: ProductEntity): Long

    /**
     * Fetches a specific product by ID.
     */
    @Query("SELECT * FROM products WHERE product_id = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?

    /**
     * Updates only the stock quantity.
     */
    @Query("UPDATE products SET stock_quantity = :newStock WHERE product_id = :productId")
    suspend fun updateStockQuantity(productId: Int, newStock: Int)

    /**
     * Inserts multiple products.
     *
     * This will be useful when we seed default sample products
     * the first time the app database is created.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProducts(products: List<ProductEntity>)

    /**
     * Updates an existing product.
     */
    @androidx.room.Update
    suspend fun updateProduct(product: ProductEntity)

    /**
     * Queries for inventory summary statistics.
     */
    @Query("SELECT COUNT(*) FROM products WHERE is_active = 1")
    fun getTotalProductsCount(): Flow<Int>

    @Query("SELECT SUM(stock_quantity) FROM products WHERE is_active = 1")
    fun getTotalStockUnits(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM products WHERE stock_quantity <= reorder_level AND stock_quantity > 0 AND is_active = 1")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE stock_quantity <= 0 AND is_active = 1")
    fun getOutOfStockCount(): Flow<Int>

    /**
     * Deletes all products.
     *
     * This is mainly useful for testing and development.
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    /**
     * Returns the total number of products in the products table.
     *
     * This is useful when we want to check whether the database
     * already contains products before inserting default sample data.
     */
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}

