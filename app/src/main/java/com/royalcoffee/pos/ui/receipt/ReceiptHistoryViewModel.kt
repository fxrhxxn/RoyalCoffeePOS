package com.royalcoffee.pos.ui.receipt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.ReceiptDetailEntity
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import com.royalcoffee.pos.data.local.repository.ReceiptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ReceiptHistoryViewModel exposes saved receipts from Room to the UI.
 *
 * Flow:
 * UI -> ViewModel -> Repository -> DAO -> Room database
 */
class ReceiptHistoryViewModel(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    /**
     * Saved receipt headers shown in the Receipt History screen.
     */
    val receiptHistory: StateFlow<List<ReceiptHistoryItem>> =
        receiptRepository.allReceiptHeaders
            .map { receiptHeaders ->
                receiptHeaders.map { receiptHeader ->
                    receiptHeader.toReceiptHistoryItem()
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * The receipt selected by the Admin.
     */
    var selectedReceipt by mutableStateOf<ReceiptHistoryItem?>(null)
        private set

    /**
     * Detail rows belonging to the selected receipt.
     */
    var selectedReceiptDetails by mutableStateOf<List<ReceiptDetailItem>>(emptyList())
        private set

    /**
     * Controls whether the receipt detail dialog is visible.
     */
    var isReceiptDetailDialogVisible by mutableStateOf(false)
        private set

    /**
     * Called when the Admin taps a saved receipt row.
     *
     * This loads the receipt detail rows from Room and opens the dialog.
     */
    fun openReceiptDetails(receipt: ReceiptHistoryItem) {
        selectedReceipt = receipt

        viewModelScope.launch {
            selectedReceiptDetails = receiptRepository
                .getReceiptDetailsOnce(receipt.receiptId)
                .map { receiptDetailEntity ->
                    receiptDetailEntity.toReceiptDetailItem()
                }

            isReceiptDetailDialogVisible = true
        }
    }

    /**
     * Closes the receipt detail dialog.
     */
    fun closeReceiptDetails() {
        isReceiptDetailDialogVisible = false
        selectedReceipt = null
        selectedReceiptDetails = emptyList()
    }
}

/**
 * Converts a database ReceiptHeaderEntity into a UI-friendly ReceiptHistoryItem.
 */
private fun ReceiptHeaderEntity.toReceiptHistoryItem(): ReceiptHistoryItem {
    return ReceiptHistoryItem(
        receiptId = receiptId,
        receiptNumber = receiptNumber,
        receiptDateTime = receiptDateTime,
        cashierName = cashierName,
        total = total,
        paymentMethod = paymentMethod
    )
}

/**
 * Converts a database ReceiptDetailEntity into a UI-friendly ReceiptDetailItem.
 */
private fun ReceiptDetailEntity.toReceiptDetailItem(): ReceiptDetailItem {
    return ReceiptDetailItem(
        productName = productName,
        quantity = quantity,
        unitPrice = unitPrice,
        lineTotal = lineTotal
    )
}

/**
 * Factory used to create ReceiptHistoryViewModel manually.
 *
 * We need this because the ViewModel requires a ReceiptRepository parameter.
 */
class ReceiptHistoryViewModelFactory(
    private val receiptRepository: ReceiptRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ReceiptHistoryViewModel::class.java)) {
            return ReceiptHistoryViewModel(
                receiptRepository = receiptRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}