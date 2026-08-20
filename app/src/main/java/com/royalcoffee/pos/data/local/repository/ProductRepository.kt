package com.royalcoffee.pos.data.local.repository

import androidx.room.withTransaction
import com.royalcoffee.pos.data.local.dao.InventoryHistoryDao
import com.royalcoffee.pos.data.local.dao.ProductDao
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabase
import com.royalcoffee.pos.data.local.entity.InventoryHistoryEntity
import com.royalcoffee.pos.data.local.entity.InventoryTransactionType
import com.royalcoffee.pos.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * ProductRepository acts as a clean layer between the UI and ProductDao.
 *
 * The UI should not directly depend on SQL queries.
 * Instead, the UI or ViewModel will later talk to this repository.
 */
class ProductRepository(
    private val database: RoyalCoffeeDatabase,
    private val productDao: ProductDao,
    private val inventoryHistoryDao: InventoryHistoryDao
) {
    /**
     * Active products are the products that should appear in the Sales screen.
     */
    val activeProducts: Flow<List<ProductEntity>> = productDao.getActiveProducts()

    /**
     * All products are useful for Admin Inventory management.
     */
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    /**
     * Low-stock products are useful for dashboard alerts and inventory warnings.
     */
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    /**
     * Inventory Summary Totals.
     */
    val totalProductsCount: Flow<Int> = productDao.getTotalProductsCount()
    val totalStockUnits: Flow<Int?> = productDao.getTotalStockUnits()
    val lowStockCount: Flow<Int> = productDao.getLowStockCount()
    val outOfStockCount: Flow<Int> = productDao.getOutOfStockCount()

    /**
     * Inserts one product into the database and records initial stock if > 0.
     */
    suspend fun addProduct(product: ProductEntity) {
        database.withTransaction {
            val productId = productDao.insertProductAndGetId(product).toInt()
            
            if (product.stockQuantity > 0) {
                inventoryHistoryDao.insertHistory(
                    InventoryHistoryEntity(
                        productId = productId,
                        productNameSnapshot = product.productName,
                        transactionType = InventoryTransactionType.INITIAL_STOCK,
                        quantityChange = product.stockQuantity,
                        previousStock = 0,
                        newStock = product.stockQuantity,
                        reason = "Product creation"
                    )
                )
            }
        }
    }

    /**
     * Updates an existing product's information (excludes manual stock change).
     */
    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    /**
     * Atomically adjusts stock and logs to history.
     * type: RESTOCK, ADJUSTMENT, or WASTE.
     */
    suspend fun adjustStock(
        productId: Int,
        quantityChange: Int,
        type: InventoryTransactionType,
        reason: String?
    ) {
        database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction
            val previousStock = product.stockQuantity
            val newStock = previousStock + quantityChange
            
            if (newStock < 0) throw IllegalArgumentException("Resulting stock cannot be negative")

            // Atomic update check
            val rowsAffected = if (quantityChange >= 0) {
                // Restock
                productDao.updateStockQuantity(productId, newStock)
                1
            } else {
                // Reduction (Adjustment/Waste)
                productDao.deductProductStock(productId, -quantityChange)
            }

            if (rowsAffected > 0) {
                inventoryHistoryDao.insertHistory(
                    InventoryHistoryEntity(
                        productId = productId,
                        productNameSnapshot = product.productName,
                        transactionType = type,
                        quantityChange = quantityChange,
                        previousStock = previousStock,
                        newStock = newStock,
                        reason = reason
                    )
                )
            } else {
                throw IllegalStateException("Stock update failed (Insufficient stock or product deleted)")
            }
        }
    }

    /**
     * Deactivates a product.
     */
    suspend fun deactivateProduct(productId: Int) {
        val product = productDao.getProductById(productId) ?: return
        productDao.updateProduct(product.copy(isActive = false))
    }

    /**
     * Inserts multiple products into the database.
     */
    suspend fun insertProducts(products: List<ProductEntity>) {
        productDao.insertProducts(products)
    }

    /**
     * Seeds default products only if the products table is empty.
     *
     * This prevents duplicate sample products from being inserted every time
     * the app starts.
     */
    suspend fun seedDefaultProductsIfNeeded() {
        val productCount = productDao.getProductCount()

        if (productCount == 0) {
            productDao.insertProducts(
                defaultProducts()
            )
        }
    }

    /**
     * Temporary default products for first database setup.
     *
     * Later, Admin users will be able to add/edit products from the app.
     */
    private fun defaultProducts(): List<ProductEntity> {
        return listOf(
            ProductEntity(
                productName = "Latte",
                category = "Coffee",
                unitPrice = 120,
                costPrice = 45,
                stockQuantity = 28,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Espresso",
                category = "Coffee",
                unitPrice = 95,
                costPrice = 30,
                stockQuantity = 35,
                reorderLevel = 12
            ),
            ProductEntity(
                productName = "Croissant",
                category = "Pastries",
                unitPrice = 80,
                costPrice = 35,
                stockQuantity = 5,
                reorderLevel = 8
            ),
            ProductEntity(
                productName = "Mocha",
                category = "Coffee",
                unitPrice = 135,
                costPrice = 55,
                stockQuantity = 18,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Iced Coffee",
                category = "Cold",
                unitPrice = 140,
                costPrice = 50,
                stockQuantity = 22,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Muffin",
                category = "Pastries",
                unitPrice = 75,
                costPrice = 25,
                stockQuantity = 3,
                reorderLevel = 6
            ),
            ProductEntity(
                productName = "Hot Chocolate",
                category = "Hot",
                unitPrice = 110,
                costPrice = 40,
                stockQuantity = 16,
                reorderLevel = 8
            )
        )
    }
}