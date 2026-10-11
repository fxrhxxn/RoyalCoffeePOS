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

/**
 * ProductViewModel connects the UI layer to product data from Room.
 *
 * The UI should not directly query the database.
 * Instead:
 * UI -> ViewModel -> Repository -> DAO -> Room database
 */


// Defining the class ProductViewModel that extends ViewModel
class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    // Active products used by the Sales screen.
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

     // Products used by the Inventory screen
     // These include stock quantity and reorder level.
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

// Converts a database ProductEntity into the Product model used by the Sales screen.
// Inherits from the Product data class.
private fun ProductEntity.toProduct(): Product {
    return Product(
        id = productId,
        name = productName,
        category = category,
        price = unitPrice,
        costPrice = costPrice,
        stockQuantity = stockQuantity,
        reorderLevel = reorderLevel
    )
}

// Converts a database ProductEntity into the InventoryProduct model used by the Inventory screen.
private fun ProductEntity.toInventoryProduct(): InventoryProduct {
    return InventoryProduct(
        productId = productId,
        name = productName,
        category = category,
        unitPrice = unitPrice,
        costPrice = costPrice,
        stockQuantity = stockQuantity,
        reorderLevel = reorderLevel,
        isActive = isActive
    )
}


// The ProductViewModelFactory allows us to create ProductViewModel manually, because
// ProductViewModel needs a ProductRepository parameter.
class ProductViewModelFactory(
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {

    // @Supress is used to suppress the warning that says "UNCHECKED_CAST"
    // Meaning that the cast is safe as long as the class is a subclass of ViewModel
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