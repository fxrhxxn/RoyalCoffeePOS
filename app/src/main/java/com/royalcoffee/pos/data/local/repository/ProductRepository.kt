package com.royalcoffee.pos.data.local.repository

import com.royalcoffee.pos.notifications.*
import androidx.room.withTransaction
import com.royalcoffee.pos.model.Money
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
    private fun validateMoney(product: ProductEntity) {
        require(product.unitPrice in 0..Money.MAX_INPUT_CENTS && product.costPrice in 0..Money.MAX_INPUT_CENTS) {
            "Prices must be non-negative and within the supported range."
        }
    }

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
        validateMoney(product)
        database.withTransaction {
            val productId = productDao.insertProductAndGetId(product).toInt()
            
            if (product.stockQuantity > 0) {
                database.stockAlertDao().insertBatch(StockBatch(productId = productId, remaining = product.stockQuantity))
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
        validateMoney(product)
        database.withTransaction {
            val current = productDao.getProductById(product.productId) ?: return@withTransaction
            productDao.updateProduct(product.copy(stockQuantity = current.stockQuantity))
        }
    }

    /**
     * Atomically adjusts stock and logs to history.
     * type: RESTOCK, ADJUSTMENT, or WASTE.
     */
    suspend fun adjustStock(
        productId: Int,
        quantityChange: Int,
        type: InventoryTransactionType,
        reason: String?,
        expiryDay: Long? = null
    ) {
        database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction
            val previousStock = product.stockQuantity
            val newStock = Math.addExact(previousStock, quantityChange)
            
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
                if (quantityChange > 0) database.stockAlertDao().insertBatch(StockBatch(productId = productId, remaining = quantityChange, expiryDay = expiryDay))
                if (quantityChange < 0) database.consumeBatches(productId, -quantityChange, previousStock)
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
        products.forEach { addProduct(it) }
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
            insertProducts(defaultProducts())
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
                unitPrice = 12000L,
                costPrice = 4500L,
                stockQuantity = 28,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Espresso",
                category = "Coffee",
                unitPrice = 9500L,
                costPrice = 3000L,
                stockQuantity = 35,
                reorderLevel = 12
            ),

            ProductEntity(
                productName = "Black Coffee - small",
                category = "Coffee",
                unitPrice = 10000L,
                stockQuantity = 20,
                reorderLevel = 5
            ),

            ProductEntity(
                productName = "Croissant",
                category = "Pastries",
                unitPrice = 8000L,
                costPrice = 3500L,
                stockQuantity = 5,
                reorderLevel = 8
            ),
            ProductEntity(
                productName = "Mocha",
                category = "Coffee",
                unitPrice = 13500L,
                costPrice = 5500L,
                stockQuantity = 18,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Iced Coffee",
                category = "Cold",
                unitPrice = 14000L,
                costPrice = 5000L,
                stockQuantity = 22,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Muffin",
                category = "Pastries",
                unitPrice = 7500L,
                costPrice = 2500L,
                stockQuantity = 3,
                reorderLevel = 6
            ),
            ProductEntity(
                productName = "Hot Chocolate",
                category = "Hot",
                unitPrice = 11000L,
                costPrice = 4000L,
                stockQuantity = 16,
                reorderLevel = 8
            ),

            ProductEntity(
                productName = "Cappuccino",
                category = "Coffee",
                unitPrice = 12500L,
                stockQuantity = 20,
                reorderLevel = 8
            ),

            ProductEntity(
                productName = "Royal Coffee Special",
                category = "Coffee",
                unitPrice = 12500L,
                stockQuantity = 20,
                reorderLevel = 8
            ),

            ProductEntity(
                productName = "Eclaire",
                category = "Pastries",
                unitPrice = 7500L,
                stockQuantity = 20,
                reorderLevel = 8
            ),



        )
    }
}
