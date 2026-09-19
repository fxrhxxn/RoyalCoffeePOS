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
import com.royalcoffee.pos.util.DateFilterUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Filter options for receipt history.
 */
enum class ReceiptDateFilter(val label: String) {
    ALL("All Dates"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom Date")
}

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
     * Currently selected date filter.
     */
    var currentFilter by mutableStateOf(ReceiptDateFilter.ALL)
        private set

    /**
     * Selected custom date (only used when filter is CUSTOM).
     */
    var customDate by mutableStateOf(LocalDate.now())
        private set

    /**
     * State flow for the current filter and custom date to trigger list updates.
     */
    private val filterState = MutableStateFlow(Pair(currentFilter, customDate))

    /**
     * Saved receipt headers shown in the Receipt History screen.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val receiptHistory: StateFlow<List<ReceiptHistoryItem>> =
        filterState.flatMapLatest { (filter, date) ->
            when (filter) {
                ReceiptDateFilter.ALL -> receiptRepository.allReceiptHeaders
                ReceiptDateFilter.TODAY -> {
                    val range = DateFilterUtils.getTodayRange()
                    receiptRepository.getReceiptHeadersByDateRange(range.start, range.end)
                }
                ReceiptDateFilter.YESTERDAY -> {
                    val range = DateFilterUtils.getYesterdayRange()
                    receiptRepository.getReceiptHeadersByDateRange(range.start, range.end)
                }
                ReceiptDateFilter.THIS_WEEK -> {
                    val range = DateFilterUtils.getThisWeekRange()
                    receiptRepository.getReceiptHeadersByDateRange(range.start, range.end)
                }
                ReceiptDateFilter.THIS_MONTH -> {
                    val range = DateFilterUtils.getThisMonthRange()
                    receiptRepository.getReceiptHeadersByDateRange(range.start, range.end)
                }
                ReceiptDateFilter.CUSTOM -> {
                    val range = DateFilterUtils.getCustomDateRange(date)
                    receiptRepository.getReceiptHeadersByDateRange(range.start, range.end)
                }
            }
        }.map { receiptHeaders ->
            receiptHeaders.map { receiptHeader ->
                receiptHeader.toReceiptHistoryItem()
            }
        }.stateIn(
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

    /**
     * Updates the date filter.
     */
    fun updateFilter(filter: ReceiptDateFilter) {
        currentFilter = filter
        filterState.value = Pair(currentFilter, customDate)
    }

    /**
     * Updates the custom date and sets filter to CUSTOM.
     */
    fun updateCustomDate(date: LocalDate) {
        customDate = date
        currentFilter = ReceiptDateFilter.CUSTOM
        filterState.value = Pair(currentFilter, customDate)
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
