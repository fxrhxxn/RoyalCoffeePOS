package com.royalcoffee.pos.ui.inventory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.CategoryEntity
import com.royalcoffee.pos.data.local.entity.InventoryTransactionType
import com.royalcoffee.pos.data.local.entity.ProductEntity
import com.royalcoffee.pos.data.local.repository.CategoryRepository
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


/**
 * Filters used by the Inventory screen.
 */
enum class StockFilter {
    ALL,
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}


/**
 * ViewModel for the Inventory screen.
 *
 * Handles:
 * - Product filtering
 * - Category management
 * - Product management
 * - Stock management
 * - Inventory summary data
 */
class InventoryViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val userRole: UserRole
) : ViewModel() {


    // ---------------------------------------------------------
    // SEARCH AND FILTER STATE
    // ---------------------------------------------------------

    private val _searchText =
        MutableStateFlow("")

    val searchText: StateFlow<String>
        get() = _searchText


    private val _selectedCategory =
        MutableStateFlow("All")

    val selectedCategory: StateFlow<String>
        get() = _selectedCategory


    private val _stockFilter =
        MutableStateFlow(StockFilter.ALL)

    val stockFilter: StateFlow<StockFilter>
        get() = _stockFilter


    // ---------------------------------------------------------
    // ERROR STATE
    // ---------------------------------------------------------

    var inventoryError by mutableStateOf<String?>(null)
        private set


    // Used for category-specific errors.
    var categoryError by mutableStateOf<String?>(null)
        private set


    // ---------------------------------------------------------
    // INVENTORY SUMMARY
    // ---------------------------------------------------------

    val totalProducts: StateFlow<Int> =
        productRepository.totalProductsCount
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )


    val totalStock: StateFlow<Int> =
        productRepository.totalStockUnits
            .map { it ?: 0 }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )


    val lowStockCount: StateFlow<Int> =
        productRepository.lowStockCount
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )


    val outOfStockCount: StateFlow<Int> =
        productRepository.outOfStockCount
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )


    // ---------------------------------------------------------
    // PRODUCTS
    // ---------------------------------------------------------

    private val _allProducts =
        productRepository.allProducts
            .map { entities ->

                entities.map { entity ->
                    entity.toInventoryProduct()
                }
            }


    // ---------------------------------------------------------
    // CATEGORIES
    // ---------------------------------------------------------

    /**
     * Actual category records stored in Room.
     *
     * Used by the Manage Categories dialog.
     */
    val categoryEntities: StateFlow<List<CategoryEntity>> =
        categoryRepository.allCategories
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )


    /**
     * Category names used by:
     *
     * - Add Product dropdown
     * - Edit Product dropdown
     */
    val productCategories: StateFlow<List<String>> =
        categoryRepository.allCategories
            .map { categories ->

                categories.map { category ->
                    category.categoryName
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )


    /**
     * Inventory filter categories.
     *
     * "All" is a UI-only option and is not stored in Room.
     */
    val filterCategories: StateFlow<List<String>> =
        categoryRepository.allCategories
            .map { categories ->

                listOf("All") +
                        categories.map { category ->
                            category.categoryName
                        }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                listOf("All")
            )


    // ---------------------------------------------------------
    // FILTERED PRODUCTS
    // ---------------------------------------------------------

    val filteredProducts: StateFlow<List<InventoryProduct>> =
        combine(
            _allProducts,
            _searchText,
            _selectedCategory,
            _stockFilter
        ) { products, search, category, filter ->

            products.filter { product ->

                val matchesSearch =
                    product.name.contains(
                        search,
                        ignoreCase = true
                    )


                val matchesCategory =
                    category == "All" ||
                            product.category.equals(
                                category,
                                ignoreCase = true
                            )


                val matchesStock =
                    when (filter) {

                        StockFilter.ALL ->
                            true

                        StockFilter.IN_STOCK ->
                            product.stockQuantity >
                                    product.reorderLevel

                        StockFilter.LOW_STOCK ->
                            product.stockQuantity > 0 &&
                                    product.stockQuantity <=
                                    product.reorderLevel

                        StockFilter.OUT_OF_STOCK ->
                            product.stockQuantity <= 0
                    }


                matchesSearch &&
                        matchesCategory &&
                        matchesStock
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )


    // ---------------------------------------------------------
    // FILTER ACTIONS
    // ---------------------------------------------------------

    fun updateSearch(
        text: String
    ) {
        _searchText.value = text
    }


    fun updateCategory(
        category: String
    ) {
        _selectedCategory.value = category
    }


    fun updateStockFilter(
        filter: StockFilter
    ) {
        _stockFilter.value = filter
    }


    fun clearError() {
        inventoryError = null
    }


    fun clearCategoryError() {
        categoryError = null
    }


    // ---------------------------------------------------------
    // CATEGORY MANAGEMENT
    // ---------------------------------------------------------

    /**
     * Adds a new category.
     *
     * Only Admin users are allowed.
     */
    fun addCategory(
        categoryName: String
    ) {

        if (userRole != UserRole.ADMIN) {
            return
        }


        viewModelScope.launch {

            val result =
                categoryRepository.addCategory(
                    categoryName
                )


            result.onSuccess {

                categoryError = null
            }


            result.onFailure { exception ->

                categoryError =
                    exception.localizedMessage
                        ?: "Unable to add category."
            }
        }
    }


    /**
     * Removes a category.
     *
     * The repository prevents removal when products
     * are still using the category.
     */
    fun removeCategory(
        category: CategoryEntity
    ) {

        if (userRole != UserRole.ADMIN) {
            return
        }


        viewModelScope.launch {

            val result =
                categoryRepository.removeCategory(
                    category
                )


            result.onSuccess {

                categoryError = null

                // Return to All if the removed category was selected.
                if (
                    _selectedCategory.value.equals(
                        category.categoryName,
                        ignoreCase = true
                    )
                ) {

                    _selectedCategory.value = "All"
                }
            }


            result.onFailure { exception ->

                categoryError =
                    exception.localizedMessage
                        ?: "Unable to remove category."
            }
        }
    }


    // ---------------------------------------------------------
    // PRODUCT MANAGEMENT
    // ---------------------------------------------------------

    /**
     * Adds a new product.
     */
    fun addProduct(
        name: String,
        category: String,
        price: Long,
        cost: Long,
        stock: Int,
        reorder: Int
    ) {

        if (userRole != UserRole.ADMIN) {
            return
        }


        viewModelScope.launch {

            try {

                productRepository.addProduct(
                    ProductEntity(
                        productName =
                            name.trim(),

                        category =
                            category.trim(),

                        unitPrice =
                            price,

                        costPrice =
                            cost,

                        stockQuantity =
                            stock,

                        reorderLevel =
                            reorder,

                        isActive =
                            true
                    )
                )

                inventoryError = null

            } catch (exception: Exception) {

                inventoryError =
                    "Unable to add product: " +
                            "${exception.localizedMessage}"
            }
        }
    }


    /**
     * Updates an existing product.
     */
    fun updateProduct(
        productId: Int,
        name: String,
        category: String,
        price: Long,
        cost: Long,
        reorder: Int,
        active: Boolean
    ) {

        if (userRole != UserRole.ADMIN) {
            return
        }


        viewModelScope.launch {

            try {

                /*
                 * Find the current product so that editing details
                 * does not reset the stock quantity.
                 */
                val existingProduct =
                    filteredProducts.value.find {
                        it.productId == productId
                    }


                productRepository.updateProduct(
                    ProductEntity(
                        productId =
                            productId,

                        productName =
                            name.trim(),

                        category =
                            category.trim(),

                        unitPrice =
                            price,

                        costPrice =
                            cost,

                        stockQuantity =
                            existingProduct?.stockQuantity ?: 0,

                        reorderLevel =
                            reorder,

                        isActive =
                            active
                    )
                )

                inventoryError = null

            } catch (exception: Exception) {

                inventoryError =
                    "Unable to update product: " +
                            "${exception.localizedMessage}"
            }
        }
    }


    // ---------------------------------------------------------
    // STOCK MANAGEMENT
    // ---------------------------------------------------------

    /**
     * Adjusts product stock.
     */
    /**
     * Adjusts product stock.
     */
    fun adjustStock(
        productId: Int,
        change: Int,
        type: InventoryTransactionType,
        reason: String,
        expiryDay: Long? = null
    ) {

        if (userRole != UserRole.ADMIN) {
            return
        }

        viewModelScope.launch {

            try {

                productRepository.adjustStock(
                    productId = productId,
                    quantityChange = change,
                    type = type,
                    reason = reason,
                    expiryDay = expiryDay
                )

                inventoryError = null

            } catch (exception: Exception) {

                inventoryError =
                    exception.localizedMessage
                        ?: "Stock update failed."
            }
        }
    }


    /**
     * Deactivates a product.
     */
    var removalCandidate by mutableStateOf<InventoryProduct?>(null)
        private set
    var removalPermanent by mutableStateOf(false)
        private set
    var lifecycleMessage by mutableStateOf<String?>(null)
        private set
    fun clearLifecycleMessage() { lifecycleMessage = null }
    fun cancelRemoval() { removalCandidate = null }
    fun requestRemoval(product: InventoryProduct) {
        if (userRole != UserRole.ADMIN) return
        viewModelScope.launch {
            try {
                removalPermanent = productRepository.canDeleteProduct(product.productId, userRole)
                removalCandidate = product
            } catch (failure: Exception) { inventoryError = failure.localizedMessage }
        }
    }
    fun confirmRemoval() {
        val candidate = removalCandidate ?: return
        val permanent = removalPermanent
        removalCandidate = null
        viewModelScope.launch {
            try {
                val deleted = productRepository.removeProduct(candidate.productId, userRole, permanent)
                lifecycleMessage = if (deleted) "Product permanently deleted." else "Product deactivated. All history is retained."
            } catch (failure: Exception) { inventoryError = failure.localizedMessage }
        }
    }
    fun reactivateProduct(productId: Int) {
        if (userRole != UserRole.ADMIN) return
        viewModelScope.launch {
            try { productRepository.reactivateProduct(productId, userRole) }
            catch (failure: Exception) { inventoryError = failure.localizedMessage }
        }
    }

    // ---------------------------------------------------------
    // ENTITY TO UI MODEL
    // ---------------------------------------------------------

    /**
     * Converts a Room ProductEntity into the UI model.
     */
    private fun ProductEntity.toInventoryProduct() =
        InventoryProduct(
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


/**
 * Factory used because InventoryViewModel has constructor dependencies.
 */
class InventoryViewModelFactory(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val userRole: UserRole
) : ViewModelProvider.Factory {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return InventoryViewModel(
            productRepository = productRepository,
            categoryRepository = categoryRepository,
            userRole = userRole
        ) as T
    }
}
