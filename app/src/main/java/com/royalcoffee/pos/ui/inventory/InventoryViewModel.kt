package com.royalcoffee.pos.ui.inventory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.InventoryTransactionType
import com.royalcoffee.pos.data.local.entity.ProductEntity
import com.royalcoffee.pos.data.local.repository.ProductRepository
import com.royalcoffee.pos.model.InventoryProduct
import com.royalcoffee.pos.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StockFilter {
    ALL, IN_STOCK, LOW_STOCK, OUT_OF_STOCK
}

class InventoryViewModel(
    private val productRepository: ProductRepository,
    private val userRole: UserRole
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> get() = _searchText

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> get() = _selectedCategory

    private val _stockFilter = MutableStateFlow(StockFilter.ALL)
    val stockFilter: StateFlow<StockFilter> get() = _stockFilter

    var inventoryError by mutableStateOf<String?>(null)
        private set

    val totalProducts: StateFlow<Int> = productRepository.totalProductsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStock: StateFlow<Int> = productRepository.totalStockUnits
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowStockCount: StateFlow<Int> = productRepository.lowStockCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val outOfStockCount: StateFlow<Int> = productRepository.outOfStockCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _allProducts = productRepository.allProducts.map { entities ->
        entities.map { it.toInventoryProduct() }
    }

    val categories: StateFlow<List<String>> = _allProducts
        .map { products -> listOf("All") + products.map { it.category }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val filteredProducts: StateFlow<List<InventoryProduct>> = combine(
        _allProducts,
        _searchText,
        _selectedCategory,
        _stockFilter
    ) { products, search, category, filter ->
        products.filter { p ->
            val matchesSearch = p.name.contains(search, ignoreCase = true)
            val matchesCategory = category == "All" || p.category == category
            val matchesStock = when (filter) {
                StockFilter.ALL -> true
                StockFilter.IN_STOCK -> p.stockQuantity > p.reorderLevel
                StockFilter.LOW_STOCK -> p.stockQuantity > 0 && p.stockQuantity <= p.reorderLevel
                StockFilter.OUT_OF_STOCK -> p.stockQuantity <= 0
            }
            matchesSearch && matchesCategory && matchesStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearch(text: String) { _searchText.value = text }
    fun updateCategory(category: String) { _selectedCategory.value = category }
    fun updateStockFilter(filter: StockFilter) { _stockFilter.value = filter }
    fun clearError() { inventoryError = null }

    fun addProduct(name: String, category: String, price: Int, cost: Int, stock: Int, reorder: Int) {
        if (userRole != UserRole.ADMIN) return
        viewModelScope.launch {
            try {
                productRepository.addProduct(
                    ProductEntity(
                        productName = name,
                        category = category,
                        unitPrice = price,
                        costPrice = cost,
                        stockQuantity = stock,
                        reorderLevel = reorder,
                        isActive = true
                    )
                )
            } catch (e: Exception) {
                inventoryError = "Unable to add product: ${e.localizedMessage}"
            }
        }
    }

    fun updateProduct(productId: Int, name: String, category: String, price: Int, cost: Int, reorder: Int, active: Boolean) {
        if (userRole != UserRole.ADMIN) return
        viewModelScope.launch {
            try {
                val existing = filteredProducts.value.find { it.productId == productId }
                productRepository.updateProduct(
                    ProductEntity(
                        productId = productId,
                        productName = name,
                        category = category,
                        unitPrice = price,
                        costPrice = cost,
                        stockQuantity = existing?.stockQuantity ?: 0,
                        reorderLevel = reorder,
                        isActive = active
                    )
                )
            } catch (e: Exception) {
                inventoryError = "Unable to update product: ${e.localizedMessage}"
            }
        }
    }

    fun adjustStock(productId: Int, change: Int, type: InventoryTransactionType, reason: String) {
        if (userRole != UserRole.ADMIN) return
        viewModelScope.launch {
            try {
                productRepository.adjustStock(productId, change, type, reason)
                inventoryError = null
            } catch (e: Exception) {
                inventoryError = e.localizedMessage ?: "Stock update failed"
            }
        }
    }

    fun deactivateProduct(productId: Int) {
        if (userRole != UserRole.ADMIN) return
        viewModelScope.launch {
            try {
                productRepository.deactivateProduct(productId)
            } catch (e: Exception) {
                inventoryError = "Unable to deactivate product: ${e.localizedMessage}"
            }
        }
    }

    private fun ProductEntity.toInventoryProduct() = InventoryProduct(
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

class InventoryViewModelFactory(
    private val productRepository: ProductRepository,
    private val userRole: UserRole
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InventoryViewModel(productRepository, userRole) as T
    }
}
