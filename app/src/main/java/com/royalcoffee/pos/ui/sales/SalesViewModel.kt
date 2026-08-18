package com.royalcoffee.pos.ui.sales

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.repository.ReceiptRepository
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.PaymentMethod
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.model.ReceiptItem
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * SalesViewModel handles the logic for creating and saving receipts.
 *
 * Flow:
 * SalesScreen -> SalesViewModel -> ReceiptRepository -> ReceiptDao -> Room
 */
class SalesViewModel(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

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
     * Generates a new receipt with a persistent sequence number from Room.
     */
    fun prepareReceipt(
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
        if (currentReceipt != null) {
            showReceiptPreviewDialog = true
            return
        }

        viewModelScope.launch {
            val nextReceiptNumber = receiptRepository.generateNextReceiptNumber()
            val now = LocalDateTime.now()

            val receiptItems = cartItems.map { cartItem ->
                ReceiptItem(
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.product.price
                )
            }

            currentReceipt = Receipt(
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

            showReceiptPreviewDialog = true
        }
    }

    /**
     * Saves the receipt to the database and clears the current receipt state.
     */
    suspend fun finalizeReceipt(receipt: Receipt) {
        receiptRepository.saveReceipt(receipt)
        clearReceiptState()
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
