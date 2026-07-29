package com.royalcoffee.pos.data.local.repository

import com.royalcoffee.pos.data.local.dao.ProductDao
import com.royalcoffee.pos.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * ProductRepository acts as a clean layer between the UI and ProductDao.
 *
 * The UI should not directly depend on SQL queries.
 * Instead, the UI or ViewModel will later talk to this repository.
 */
class ProductRepository(
    private val productDao: ProductDao
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
     * Inserts one product into the database.
     */
    suspend fun insertProduct(product: ProductEntity) {
        productDao.insertProduct(product)
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
                stockQuantity = 28,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Espresso",
                category = "Coffee",
                unitPrice = 95,
                stockQuantity = 35,
                reorderLevel = 12
            ),
            ProductEntity(
                productName = "Croissant",
                category = "Pastries",
                unitPrice = 80,
                stockQuantity = 5,
                reorderLevel = 8
            ),
            ProductEntity(
                productName = "Mocha",
                category = "Coffee",
                unitPrice = 135,
                stockQuantity = 18,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Iced Coffee",
                category = "Cold",
                unitPrice = 140,
                stockQuantity = 22,
                reorderLevel = 10
            ),
            ProductEntity(
                productName = "Muffin",
                category = "Pastries",
                unitPrice = 75,
                stockQuantity = 3,
                reorderLevel = 6
            ),
            ProductEntity(
                productName = "Hot Chocolate",
                category = "Hot",
                unitPrice = 110,
                stockQuantity = 16,
                reorderLevel = 8
            )
        )
    }
}