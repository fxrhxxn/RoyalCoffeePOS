package com.royalcoffee.pos.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import com.royalcoffee.pos.data.local.repository.ReceiptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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