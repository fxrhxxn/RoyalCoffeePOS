package com.royalcoffee.pos.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.ProductEntity
import com.royalcoffee.pos.data.local.repository.ProductRepository
import com.royalcoffee.pos.model.InventoryProduct
import com.royalcoffee.pos.model.Product
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ProductViewModel connects the UI layer to product data from Room.
 *
 * The UI should not directly query the database.
 * Instead:
 * UI -> ViewModel -> Repository -> DAO -> Room database
 */
class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    init {
        /**
         * Insert default products only if the products table is empty.
         *
         * This means the app will automatically have starter products
         * the first time the database is created.
         */
        viewModelScope.launch {
            productRepository.seedDefaultProductsIfNeeded()
        }
    }

    /**
     * Products used by the Sales screen.
     *
     * These are active products only.
     */
    val products: StateFlow<List<Product>> = productRepository.activeProducts
        .map { productEntities ->
            productEntities.map { productEntity ->
                productEntity.toProduct()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Products used by the Inventory screen.
     *
     * These include stock quantity and reorder level.
     */
    val inventoryProducts: StateFlow<List<InventoryProduct>> = productRepository.allProducts
        .map { productEntities ->
            productEntities.map { productEntity ->
                productEntity.toInventoryProduct()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}

/**
 * Converts a database ProductEntity into the Product model used by the Sales screen.
 */
private fun ProductEntity.toProduct(): Product {
    return Product(
        id = productId,
        name = productName,
        category = category,
        price = unitPrice,
        stockQuantity = stockQuantity,
        reorderLevel = reorderLevel
    )
}

/**
 * Converts a database ProductEntity into the InventoryProduct model used by the Inventory screen.
 */
private fun ProductEntity.toInventoryProduct(): InventoryProduct {
    return InventoryProduct(
        productId = productId,
        name = productName,
        category = category,
        unitPrice = unitPrice,
        stockQuantity = stockQuantity,
        reorderLevel = reorderLevel,
        isActive = isActive
    )
}

/**
 * ProductViewModelFactory allows us to create ProductViewModel manually.
 *
 * We need this because ProductViewModel requires a ProductRepository parameter.
 */
class ProductViewModelFactory(
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ProductViewModel(
                productRepository = productRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}