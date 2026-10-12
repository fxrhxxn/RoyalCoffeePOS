package com.royalcoffee.pos.ui.sales

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.repository.BusinessRepository
import com.royalcoffee.pos.data.local.repository.ReceiptRepository
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.DiscountPolicy
import com.royalcoffee.pos.model.Money
import com.royalcoffee.pos.model.PaymentMethod
import com.royalcoffee.pos.model.Product
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.model.ReceiptItem
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.util.InsufficientStockException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * SalesViewModel handles the logic for creating and saving receipts,
 * as well as managing the active cart and product filtering.
 *
 * The BusinessRepository supplies the registered business details
 * used in the receipt header.
 *
 * Flow:
 * SalesScreen -> SalesViewModel -> repositories -> Room database
 */
class SalesViewModel(
    private val receiptRepository: ReceiptRepository,
    private val businessRepository: BusinessRepository,
    private val userRole: UserRole
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Holds the currently generated receipt for the active order.
    var currentReceipt by mutableStateOf<Receipt?>(null)
        private set

    // Controls the visibility of the receipt preview dialog.
    var showReceiptPreviewDialog by mutableStateOf(false)
        private set

    // Stores the latest sales-related error, such as insufficient stock.
    var salesError by mutableStateOf<String?>(null)
        private set

    // Live cart for the current order.
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem>
        get() = _cartItems

    // Basic order calculations.
    val subtotal: Long
        get() = Money.sum(_cartItems.map { it.lineTotal })

    var discountPercent by mutableStateOf(0)
        private set

    val discountOptions
        get() = DiscountPolicy.options(userRole)

    fun selectDiscount(percent: Int) {
        if (currentReceipt != null || isSaving) return

        DiscountPolicy.requireAllowed(userRole, percent)
        discountPercent = percent
    }

    var isSaving by mutableStateOf(false)
        private set

    val discount: Long
        get() = Money.discount(subtotal, discountPercent)

    // Selling prices already include VAT.
    val tax: Long
        get() = 0L

    val total: Long
        get() = subtotal - discount + tax

    // Updates the search text.
    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    // Updates the selected product category.
    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    // Clears any active sales error.
    fun clearSalesError() {
        salesError = null
    }

    // Filters products based on the current search text and category.
    fun filterProducts(products: List<Product>): List<Product> {
        return products.filter { product ->
            val matchesCategory =
                _selectedCategory.value == "All" ||
                        product.category == _selectedCategory.value

            val matchesSearch =
                product.name.contains(
                    _searchText.value,
                    ignoreCase = true
                )

            matchesCategory && matchesSearch
        }
    }

    // Stores the name of the product when the cart reaches the available stock limit.
    var stockLimitProduct by mutableStateOf<String?>(null)
        private set

    fun acknowledgeStockLimit() {
        stockLimitProduct = null
    }

    // Adds one unit of a product to the cart if stock allows it.
    fun addProductToCart(product: Product) {
        if (isSaving || currentReceipt != null) return

        val index = _cartItems.indexOfFirst {
            it.product.id == product.id
        }

        val quantity =
            if (index < 0) 0
            else _cartItems[index].quantity

        // Do not add more than the available stock.
        if (quantity >= product.stockQuantity) return

        val nextQuantity = quantity + 1
        val updatedItem = CartItem(product, nextQuantity)

        if (index < 0) {
            _cartItems.add(updatedItem)
        } else {
            _cartItems[index] = updatedItem
        }

        // Warn when the cart reaches the available stock limit.
        if (nextQuantity == product.stockQuantity) {
            stockLimitProduct = product.name
        }
    }

    fun increaseQuantity(product: Product) {
        addProductToCart(product)
    }

    // Decreases the quantity of a cart item and removed when quantity is 0
    fun decreaseQuantity(product: Product) {
        if (isSaving || currentReceipt != null) return

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

    // Removes a product from the cart regardless of its quantity.
    fun removeItem(product: Product) {
        if (isSaving || currentReceipt != null) return

        _cartItems.removeAll {
            it.product.id == product.id
        }
    }

    // Clears the cart/ticket.
    fun clearCart() {
        if (isSaving) return

        _cartItems.clear()
        stockLimitProduct = null
        discountPercent = 0
    }

    /**
     * Generates a receipt, saves it to Room, and opens the preview.
     *
     * Flow:
     * Payment successful -> Generate receipt number -> Save to Room
     * -> Display receipt
     */
    fun processCompletedOrder(
        cartItems: List<CartItem>,
        paymentMethod: PaymentMethod,
        amountPaid: Long?,
        changeGiven: Long?,
        cashierName: String,
        footerMessage: String
    ) {
        // If a receipt was already generated for this order, show it again.
        if (currentReceipt != null) {
            showReceiptPreviewDialog = true
            return
        }

        if (isSaving) return

        // Capture the order before suspending so the totals and stock
        // refer to the same cart contents.
        val orderItems = cartItems.toList()
        val percent = discountPercent
        val orderSubtotal = Money.sum(orderItems.map { it.lineTotal })
        val orderDiscount = Money.discount(orderSubtotal, percent)
        val orderTotal = orderSubtotal - orderDiscount

        if (
            orderItems.isEmpty() ||
            amountPaid == null ||
            amountPaid < orderTotal ||
            changeGiven != amountPaid - orderTotal
        ) {
            salesError = "Payment does not match the order total."
            return
        }

        isSaving = true

        viewModelScope.launch {
            try {
                DiscountPolicy.requireAllowed(userRole, percent)
                /*
                 * Load the business profile saved during registration.
                 * This replaces the old hardcoded receipt header values.
                 */
                val businessInfo =
                    businessRepository.getBusinessInfoSync()
                        ?: throw IllegalStateException(
                            "Business details are unavailable. Please complete registration."
                        )

                /*
                 * Generate the next receipt number using the local database.
                 */
                val nextReceiptNumber =
                    receiptRepository.generateNextReceiptNumber()

                val now = LocalDateTime.now()

                val receiptItems = orderItems.map { cartItem ->
                    ReceiptItem(
                        productName = cartItem.product.name,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.product.price,
                        costPrice = cartItem.product.costPrice
                    )
                }

                /*
                 * Include the registered address and country as separate
                 * receipt lines. Empty values are left out.
                 */
                val businessAddressLines = listOf(
                    businessInfo.address,
                    businessInfo.country
                ).filter { it.isNotBlank() }

                /*
                 * Create the Receipt model.
                 *
                 * Receipt preview, PDF generation, and printer formatting
                 * read these values from this model.
                 */
                val receipt = Receipt(
                    receiptNumber = nextReceiptNumber,
                    receiptDateTime = formatReceiptDateTime(now),
                    // These now come from the registration record.
                    storeName = businessInfo.businessName,
                    storeAddressLines = businessAddressLines,
                    storePhone = businessInfo.phoneNumber,

                    cashierName = cashierName,
                    items = receiptItems,
                    subtotal = orderSubtotal,
                    discount = orderDiscount,
                    tax = tax,
                    total = orderTotal,
                    paymentMethod = paymentMethod,
                    amountPaid = amountPaid,
                    changeGiven = changeGiven,
                    footerMessage = footerMessage,
                    discountPercent = percent,
                    vatIncluded = true
                )

                /*
                 * Save the receipt and deduct stock in one transaction.
                 */
                receiptRepository.saveReceipt(
                    receipt = receipt,
                    cartItems = orderItems,
                    userRole = userRole
                )

                /*
                 * Display the receipt preview.
                 */
                currentReceipt = receipt
                showReceiptPreviewDialog = true
                salesError = null

            } catch (stockException: InsufficientStockException) {
                // If stock is insufficient, notify the user and abort.
                salesError = stockException.message
            } catch (exception: Exception) {
                // Show a readable error if receipt generation fails.
                salesError =
                    "Unable to complete sale: " +
                            (exception.localizedMessage ?: "Unknown error")
            } finally {
                isSaving = false
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
     * Closes the receipt preview without clearing the receipt.
     */
    fun closeReceiptPreview() {
        showReceiptPreviewDialog = false
    }

    private fun formatReceiptDateTime(dateTime: LocalDateTime): String {
        val formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

        return dateTime.format(formatter)
    }
}

/**
 * Factory for creating SalesViewModel with its dependencies.
 */
class SalesViewModelFactory(
    private val receiptRepository: ReceiptRepository,
    private val businessRepository: BusinessRepository,
    private val userRole: UserRole
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            return SalesViewModel(
                receiptRepository = receiptRepository,
                businessRepository = businessRepository,
                userRole = userRole
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}