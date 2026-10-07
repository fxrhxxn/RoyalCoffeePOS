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
            productDao.updateProduct(product.copy(stockQuantity = current.stockQuantity, isActive = current.isActive, legacyHistoryUncertain = current.legacyHistoryUncertain))
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
    suspend fun canDeleteProduct(productId: Int, role: com.royalcoffee.pos.model.UserRole): Boolean {
        require(role == com.royalcoffee.pos.model.UserRole.ADMIN) { "Admin access required" }
        return !productDao.hasHistory(productId)
    }

    /** Recheck inside the mutation transaction: a concurrent sale must turn deletion into deactivation. */
    suspend fun removeProduct(productId: Int, role: com.royalcoffee.pos.model.UserRole, allowPermanentDelete: Boolean): Boolean = database.withTransaction {
        require(role == com.royalcoffee.pos.model.UserRole.ADMIN) { "Admin access required" }
        val product = productDao.getProductById(productId) ?: return@withTransaction false
        if (allowPermanentDelete && !productDao.hasHistory(productId)) {
            database.stockAlertDao().deleteEmptyProductBatches(productId)
            productDao.deleteProduct(productId)
            true
        } else {
            productDao.updateProduct(product.copy(isActive = false))
            false
        }
    }

    suspend fun reactivateProduct(productId: Int, role: com.royalcoffee.pos.model.UserRole) = database.withTransaction {
        require(role == com.royalcoffee.pos.model.UserRole.ADMIN) { "Admin access required" }
        val product = productDao.getProductById(productId) ?: return@withTransaction
        productDao.updateProduct(product.copy(isActive = true))
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
     *  Default products already preset in the application.
     */
    private fun defaultProducts(): List<ProductEntity> {
        return listOf(
            // --- Hot Drinks ---
            ProductEntity(
                productName = "Latte",
                category = "Hot Drinks",
                unitPrice = 12000L,
                costPrice = 4500L,
                stockQuantity = 28,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Espresso",
                category = "Hot Drinks",
                unitPrice = 8000L,
                costPrice = 3000L,
                stockQuantity = 40,
                reorderLevel = 15
            ),
            ProductEntity(
                productName = "Cappuccino",
                category = "Hot Drinks",
                unitPrice = 11000L,
                costPrice = 4000L,
                stockQuantity = 30,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Americano",
                category = "Hot Drinks",
                unitPrice = 9000L,
                costPrice = 3500L,
                stockQuantity = 35,
                reorderLevel = 12
            ),
            ProductEntity(
                productName = "Mocha",
                category = "Hot Drinks",
                unitPrice = 13000L,
                costPrice = 5000L,
                stockQuantity = 25,
                reorderLevel = 8
            ),
            ProductEntity(
                productName = "Hot Chocolate",
                category = "Hot Drinks",
                unitPrice = 10000L,
                costPrice = 4000L,
                stockQuantity = 20,
                reorderLevel = 8
            ),

            // --- Cold Drinks ---
            ProductEntity(
                productName = "Iced Americano",
                category = "Cold Drinks",
                unitPrice = 9500L,
                costPrice = 3500L,
                stockQuantity = 30,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Iced Latte",
                category = "Cold Drinks",
                unitPrice = 12500L,
                costPrice = 4500L,
                stockQuantity = 25,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Iced Matcha",
                category = "Cold Drinks",
                unitPrice = 14000L,
                costPrice = 5500L,
                stockQuantity = 20,
                reorderLevel = 8
            ),
            ProductEntity(
                productName = "Cold Brew",
                category = "Cold Drinks",
                unitPrice = 11000L,
                costPrice = 4000L,
                stockQuantity = 25,
                reorderLevel = 10
            ),

            // --- Pastries ---
            ProductEntity(
                productName = "Croissant",
                category = "Pastries",
                unitPrice = 6000L,
                costPrice = 2000L,
                stockQuantity = 15,
                reorderLevel = 5
            ),
            ProductEntity(
                productName = "Donut",
                category = "Pastries",
                unitPrice = 5000L,
                costPrice = 1500L,
                stockQuantity = 20,
                reorderLevel = 6
            ),
            ProductEntity(
                productName = "Cupcake",
                category = "Pastries",
                unitPrice = 5500L,
                costPrice = 1800L,
                stockQuantity = 18,
                reorderLevel = 5
            ),
            ProductEntity(
                productName = "Cheesecake",
                category = "Pastries",
                unitPrice = 15000L,
                costPrice = 6000L,
                stockQuantity = 10,
                reorderLevel = 3
            ),
            ProductEntity(
                productName = "Muffin",
                category = "Pastries",
                unitPrice = 6500L,
                costPrice = 2200L,
                stockQuantity = 16,
                reorderLevel = 5
            ),

            // --- Food ---
            ProductEntity(
                productName = "Panini",
                category = "Food",
                unitPrice = 18000L,
                costPrice = 7500L,
                stockQuantity = 12,
                reorderLevel = 4
            ),
            ProductEntity(
                productName = "Wrap",
                category = "Food",
                unitPrice = 16000L,
                costPrice = 6500L,
                stockQuantity = 15,
                reorderLevel = 5
            ),
            ProductEntity(
                productName = "Salad",
                category = "Food",
                unitPrice = 20000L,
                costPrice = 8000L,
                stockQuantity = 10,
                reorderLevel = 3
            ),
            ProductEntity(
                productName = "Pasta",
                category = "Food",
                unitPrice = 24000L,
                costPrice = 9500L,
                stockQuantity = 8,
                reorderLevel = 2
            )
        )
    }
}
