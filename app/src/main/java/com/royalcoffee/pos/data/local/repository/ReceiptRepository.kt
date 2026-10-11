package com.royalcoffee.pos.data.local.repository

import com.royalcoffee.pos.notifications.consumeBatches
import androidx.room.withTransaction
import com.royalcoffee.pos.model.Money
import com.royalcoffee.pos.model.DiscountPolicy
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.data.local.dao.CategorySales
import com.royalcoffee.pos.data.local.dao.GrossProfitStats
import com.royalcoffee.pos.data.local.dao.HourlySales
import com.royalcoffee.pos.data.local.dao.InventoryHistoryDao
import com.royalcoffee.pos.data.local.dao.PaymentBreakdown
import com.royalcoffee.pos.data.local.dao.ProfitableProduct
import com.royalcoffee.pos.data.local.dao.ProductDao
import com.royalcoffee.pos.data.local.dao.ProfitTrendPoint
import com.royalcoffee.pos.data.local.dao.ReceiptDao
import com.royalcoffee.pos.data.local.dao.SalesSummary
import com.royalcoffee.pos.data.local.dao.SalesTrendPoint
import com.royalcoffee.pos.data.local.dao.TopProduct
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabase
import com.royalcoffee.pos.data.local.entity.InventoryHistoryEntity
import com.royalcoffee.pos.data.local.entity.InventoryTransactionType
import com.royalcoffee.pos.data.local.entity.ReceiptDetailEntity
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.PaymentMethod
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.model.ReceiptItem
import com.royalcoffee.pos.util.InsufficientStockException
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * ReceiptRepository acts as a clean layer between the UI and ReceiptDao.
 *
 * The Sales screen should not directly create SQL/database records.
 * Instead:
 *
 * UI -> Repository -> DAO -> Room database
 */
class ReceiptRepository(
    private val database: RoyalCoffeeDatabase,
    private val receiptDao: ReceiptDao,
    private val productDao: ProductDao,
    private val inventoryHistoryDao: InventoryHistoryDao
) {
    /**
     * Returns all saved receipt headers.
     *
     * This will be used by the Receipt History screen.
     */
    val allReceiptHeaders: Flow<List<ReceiptHeaderEntity>> =
        receiptDao.getAllReceiptHeaders()

    /**
     * Returns receipt headers for a specific date range.
     */
    fun getReceiptHeadersByDateRange(start: Long, end: Long): Flow<List<ReceiptHeaderEntity>> =
        receiptDao.getReceiptHeadersByDateRange(start, end)

    /**
     * Generates the next sequential receipt number for the current year.
     *
     * Logic:
     * 1. Check database for the latest receipt number starting with "RC-YYYY-".
     * 2. If found, increment its sequence.
     * 3. If not found, start with 1.
     *
     * Example:
     * Last receipt = "RC-2026-000005"
     * Next receipt = "RC-2026-000006"
     */
    suspend fun generateNextReceiptNumber(): String {
        val now = LocalDateTime.now()
        val year = now.year
        val yearPrefix = "RC-$year-"

        val lastReceiptNumber = receiptDao.getLastReceiptNumber(yearPrefix)

        val nextSequence = if (lastReceiptNumber != null) {
            /**
             * Extract the number after the last hyphen.
             * RC-2026-000005 -> 000005
             */
            val sequencePart = lastReceiptNumber.substringAfterLast("-")
            sequencePart.toIntOrNull()?.plus(1) ?: 1
        } else {
            1
        }

        /**
         * Pad with zeros to 6 digits.
         */
        val paddedNumber = nextSequence.toString().padStart(6, '0')

        return "$yearPrefix$paddedNumber"
    }

    /**
     * Saves a complete receipt, deducts inventory, and records sale history
     * in a single atomic transaction.
     *
     * If any part of this process fails (e.g. insufficient stock),
     * the entire transaction is rolled back.
     */
    suspend fun saveReceipt(receipt: Receipt, cartItems: List<CartItem>, userRole: UserRole) {
        val percent = requireNotNull(receipt.discountPercent)
        DiscountPolicy.requireAllowed(userRole, percent)
        val subtotal = Money.sum(cartItems.map { it.lineTotal })
        require(cartItems.isNotEmpty() && receipt.items.size == cartItems.size)
        require(receipt.subtotal == subtotal && receipt.discount == Money.discount(subtotal, percent))
        require(receipt.vatIncluded && receipt.tax == 0L && receipt.total == subtotal - receipt.discount)
        receipt.items.zip(cartItems).forEach { (item, cart) ->
            require(item.productName == cart.product.name && item.quantity == cart.quantity &&
                item.unitPrice == cart.product.price && item.costPrice == cart.product.costPrice)
        }
        require(receipt.amountPaid != null && receipt.amountPaid >= receipt.total &&
            receipt.changeGiven == receipt.amountPaid - receipt.total)
        val allocations = Money.allocateDiscount(receipt.items.map { it.lineTotal }, receipt.discount)
        database.withTransaction {
            /**
             * 1. Perform a final stock check and gather previous stock values
             * for history records.
             */
            val stockSnapshots = cartItems.map { cartItem ->
                val currentProduct = productDao.getProductById(cartItem.product.id)
                check(currentProduct != null && currentProduct.isActive) { "${cartItem.product.name} is no longer available for sale." }
                val currentStock = currentProduct.stockQuantity
                if (currentStock < cartItem.quantity) {
                    throw InsufficientStockException(
                        productName = cartItem.product.name,
                        availableStock = currentStock,
                        requestedQuantity = cartItem.quantity
                    )
                }
                currentStock
            }

            /**
             * 2. Deduct stock and insert history for each item.
             */
            cartItems.forEachIndexed { index, cartItem ->
                val previousStock = stockSnapshots[index]
                val affectedRows = productDao.deductProductStock(
                    productId = cartItem.product.id,
                    quantity = cartItem.quantity
                )

                if (affectedRows == 0) {
                    throw InsufficientStockException(
                        productName = cartItem.product.name,
                        availableStock = previousStock,
                        requestedQuantity = cartItem.quantity
                    )
                }

                database.consumeBatches(cartItem.product.id, cartItem.quantity, previousStock)
                val newStock = previousStock - cartItem.quantity
                inventoryHistoryDao.insertHistory(
                    InventoryHistoryEntity(
                        productId = cartItem.product.id,
                        productNameSnapshot = cartItem.product.name,
                        transactionType = InventoryTransactionType.SALE,
                        quantityChange = -cartItem.quantity,
                        previousStock = previousStock,
                        newStock = newStock,
                        reason = "POS Sale: ${receipt.receiptNumber}"
                    )
                )
            }

            /**
             * 3. Save receipt header and details.
             */
            val receiptHeader = ReceiptHeaderEntity(
                receiptNumber = receipt.receiptNumber,
                receiptDateTime = receipt.receiptDateTime,
                timestamp = System.currentTimeMillis(),
                cashierName = receipt.cashierName,
                subtotal = receipt.subtotal,
                discount = receipt.discount,
                tax = receipt.tax,
                total = receipt.total,
                paymentMethod = receipt.paymentMethod.name,
                amountPaid = receipt.amountPaid,
                changeGiven = receipt.changeGiven,
                footerMessage = receipt.footerMessage,
                discountPercent = receipt.discountPercent,
                vatIncluded = receipt.vatIncluded
            )

            val receiptDetails = receipt.items.mapIndexed { index, item ->
                ReceiptDetailEntity(
                    receiptId = 0,
                    productId = cartItems[index].product.id,
                    productName = item.productName,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    costPrice = item.costPrice,
                    lineTotal = item.lineTotal,
                    discountAmount = allocations[index]
                )
            }

            receiptDao.insertFullReceipt(
                receiptHeader = receiptHeader,
                receiptDetails = receiptDetails
            )
        }
    }

    /**
     * Returns receipt detail rows for one saved receipt.
     *
     * Used when Admin taps a receipt from Receipt History.
     */
    suspend fun getReceiptDetailsOnce(
        receiptId: Int
    ): List<ReceiptDetailEntity> {
        return receiptDao.getReceiptDetailsOnce(
            receiptId = receiptId
        )
    }

    /**
     * Returns a full Receipt model for a specific header ID.
     */
    suspend fun getFullReceipt(receiptId: Int): Receipt? {
        val header = receiptDao.getReceiptHeaderById(receiptId) ?: return null
        val details = receiptDao.getReceiptDetailsOnce(receiptId)

        return Receipt(
            receiptNumber = header.receiptNumber,
            receiptDateTime = header.receiptDateTime,
            storeName = "Royal Coffee",
            storeAddressLines = listOf("Phoenix", "Mauritius"),
            storePhone = "+230 0000 0000",
            cashierName = header.cashierName,
            items = details.map { detail ->
                ReceiptItem(
                    productName = detail.productName,
                    quantity = detail.quantity,
                    unitPrice = detail.unitPrice,
                    costPrice = detail.costPrice
                )
            },
            subtotal = header.subtotal,
            discount = header.discount,
            tax = header.tax,
            total = header.total,
            paymentMethod = PaymentMethod.valueOf(header.paymentMethod),
            amountPaid = header.amountPaid,
            changeGiven = header.changeGiven,
            footerMessage = header.footerMessage,
            discountPercent = header.discountPercent,
            vatIncluded = header.vatIncluded
        )
    }

    /**
     * Dashboard specific data flows.
     */
    fun getSalesSummary(start: Long, end: Long): Flow<SalesSummary> =
        receiptDao.getSalesSummary(start, end)

    fun getPaymentBreakdown(start: Long, end: Long): Flow<List<PaymentBreakdown>> =
        receiptDao.getPaymentBreakdown(start, end)

    fun getTopSellingProducts(start: Long, end: Long, limit: Int = 5): Flow<List<TopProduct>> =
        receiptDao.getTopSellingProducts(start, end, limit)

    fun getSalesTrend(start: Long, end: Long): Flow<List<SalesTrendPoint>> =
        receiptDao.getSalesTrend(start, end)

    fun getHourlySales(start: Long, end: Long): Flow<List<HourlySales>> =
        receiptDao.getHourlySales(start, end)

    fun getCategorySales(start: Long, end: Long): Flow<List<CategorySales>> =
        receiptDao.getCategorySales(start, end)

    fun getGrossProfitStats(start: Long, end: Long): Flow<GrossProfitStats> =
        receiptDao.getGrossProfitStats(start, end)

    fun getProfitTrend(start: Long, end: Long): Flow<List<ProfitTrendPoint>> =
        receiptDao.getProfitTrend(start, end)

    fun getMostProfitableProducts(start: Long, end: Long, limit: Int = 1): Flow<List<ProfitableProduct>> =
        receiptDao.getMostProfitableProducts(start, end, limit)
}

