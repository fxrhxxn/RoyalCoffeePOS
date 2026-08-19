package com.royalcoffee.pos.ui.sales

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.repository.ReceiptRepository
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.PaymentMethod
import com.royalcoffee.pos.model.Product
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.model.ReceiptItem
import com.royalcoffee.pos.util.InsufficientStockException
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SalesViewModel handles the logic for creating and saving receipts,
 * as well as managing the active cart and product filtering.
 *
 * Flow:
 * SalesScreen -> SalesViewModel -> ReceiptRepository -> ReceiptDao -> Room
 */
class SalesViewModel(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    /**
     * Holds the currently generated receipt for the active order.
     */
    var currentReceipt by mutableStateOf<Receipt?>(null)
        private set

    /**
     * Controls the visibility of the receipt preview dialog.
     */
    var showReceiptPreviewDialog by mutableStateOf(false)
        private set

    /**
     * Stores the latest sales-related error (e.g. insufficient stock).
     */
    var salesError by mutableStateOf<String?>(null)
        private set

    /**
     * Live cart for the current order.
     */
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

    /**
     * Basic order calculations.
     */
    val subtotal: Int get() = _cartItems.sumOf { it.lineTotal }
    val discount: Int get() = 0 // Fixed at 0 for Step 1
    val tax: Int get() = (subtotal * 0.15).toInt()
    val total: Int get() = subtotal - discount + tax

    /**
     * Updates the search text and resets category if needed (optional).
     */
    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    /**
     * Updates the selected category.
     */
    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    /**
     * Clears any active sales error.
     */
    fun clearSalesError() {
        salesError = null
    }

    /**
     * Filters the provided products based on current search and category.
     */
    fun filterProducts(products: List<Product>): List<Product> {
        return products.filter { product ->
            val matchesCategory = _selectedCategory.value == "All" || product.category == _selectedCategory.value
            val matchesSearch = product.name.contains(_searchText.value, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    /**
     * Adds a product to the cart if stock allows.
     */
    fun addProductToCart(product: Product) {
        if (product.stockQuantity <= 0) return

        val existingItemIndex = _cartItems.indexOfFirst {
            it.product.id == product.id
        }

        if (existingItemIndex >= 0) {
            val existingItem = _cartItems[existingItemIndex]
            if (existingItem.quantity < product.stockQuantity) {
                _cartItems[existingItemIndex] = existingItem.copy(
                    quantity = existingItem.quantity + 1
                )
            }
        } else {
            _cartItems.add(
                CartItem(
                    product = product,
                    quantity = 1
                )
            )
        }
    }

    /**
     * Increases the quantity of a cart item if stock allows.
     */
    fun increaseQuantity(product: Product) {
        val itemIndex = _cartItems.indexOfFirst {
            it.product.id == product.id
        }

        if (itemIndex >= 0) {
            val item = _cartItems[itemIndex]
            if (item.quantity < product.stockQuantity) {
                _cartItems[itemIndex] = item.copy(
                    quantity = item.quantity + 1
                )
            }
        }
    }

    /**
     * Decreases the quantity of a cart item.
     * If quantity reaches 0, the item is removed.
     */
    fun decreaseQuantity(product: Product) {
        val itemIndex = _cartItems.indexOfFirst {
            it.product.id == product.id
        }

        if (itemIndex >= 0) {
            val item = _cartItems[itemIndex]
            if (item.quantity > 1) {
                _cartItems[itemIndex] = item.copy(
                    quantity = item.quantity - 1
                )
            } else {
                _cartItems.removeAt(itemIndex)
            }
        }
    }

    /**
     * Removes an item from the cart regardless of quantity.
     */
    fun removeItem(product: Product) {
        _cartItems.removeAll { it.product.id == product.id }
    }

    /**
     * Clears the cart/ticket.
     */
    fun clearCart() {
        _cartItems.clear()
    }

    /**
     * Generates a new receipt, saves it to Room, and opens the preview.
     *
     * Following the required flow:
     * Payment successful -> Generate number -> Save to Room -> Display receipt
     */
    fun processCompletedOrder(
        cartItems: List<CartItem>,
        subtotal: Int,
        discount: Int,
        tax: Int,
        total: Int,
        paymentMethod: PaymentMethod,
        amountPaid: Int?,
        changeGiven: Int?,
        cashierName: String,
        footerMessage: String
    ) {
        /**
         * If the receipt is already generated for this order, just show it.
         */
        if (currentReceipt != null) {
            showReceiptPreviewDialog = true
            return
        }

        viewModelScope.launch {
            try {
                /**
                 * 1. Get latest sequence and generate number from Room.
                 */
                val nextReceiptNumber = receiptRepository.generateNextReceiptNumber()
                val now = LocalDateTime.now()

                val receiptItems = cartItems.map { cartItem ->
                    ReceiptItem(
                        productName = cartItem.product.name,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.product.price
                    )
                }

                /**
                 * 2. Create the Receipt model.
                 */
                val receipt = Receipt(
                    receiptNumber = nextReceiptNumber,
                    receiptDateTime = formatReceiptDateTime(now),
                    storeName = "Royal Coffee",
                    storeAddressLines = listOf("Phoenix", "Mauritius"),
                    storePhone = "+230 0000 0000",
                    cashierName = cashierName,
                    items = receiptItems,
                    subtotal = subtotal,
                    discount = discount,
                    tax = tax,
                    total = total,
                    paymentMethod = paymentMethod,
                    amountPaid = amountPaid,
                    changeGiven = changeGiven,
                    footerMessage = footerMessage
                )

                /**
                 * 3. Save receipt to Room immediately and deduct stock.
                 *
                 * This happens in a single atomic transaction.
                 */
                receiptRepository.saveReceipt(
                    receipt = receipt,
                    cartItems = cartItems
                )

                /**
                 * 4. Display the receipt.
                 */
                currentReceipt = receipt
                showReceiptPreviewDialog = true
                salesError = null

            } catch (stockException: InsufficientStockException) {
                // If stock is insufficient, notify the user and abort.
                salesError = stockException.message
            } catch (exception: Exception) {
                // Generic error handling
                salesError = "Unable to complete sale: ${exception.localizedMessage}"
            }
        }
    }

    /**
     * Resets the receipt state for the next order.
     */
    fun clearReceiptState() {
        currentReceipt = null
        showReceiptPreviewDialog = false
    }

    /**
     * Closes the receipt preview dialog without clearing the receipt.
     */
    fun closeReceiptPreview() {
        showReceiptPreviewDialog = false
    }

    private fun formatReceiptDateTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return dateTime.format(formatter)
    }
}

/**
 * Factory for creating SalesViewModel with its dependencies.
 */
class SalesViewModelFactory(
    private val receiptRepository: ReceiptRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            return SalesViewModel(receiptRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
