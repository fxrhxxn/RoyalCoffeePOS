package com.royalcoffee.pos.data.local.repository

import androidx.room.withTransaction
import com.royalcoffee.pos.data.local.dao.InventoryHistoryDao
import com.royalcoffee.pos.data.local.dao.ProductDao
import com.royalcoffee.pos.data.local.dao.ReceiptDao
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabase
import com.royalcoffee.pos.data.local.entity.InventoryHistoryEntity
import com.royalcoffee.pos.data.local.entity.InventoryTransactionType
import com.royalcoffee.pos.data.local.entity.ReceiptDetailEntity
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.Receipt
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
    suspend fun saveReceipt(receipt: Receipt, cartItems: List<CartItem>) {
        database.withTransaction {
            /**
             * 1. Perform a final stock check and gather previous stock values
             * for history records.
             */
            val stockSnapshots = cartItems.map { cartItem ->
                val currentStock = productDao.getProductStock(cartItem.product.id) ?: 0
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
                cashierName = receipt.cashierName,
                subtotal = receipt.subtotal,
                discount = receipt.discount,
                tax = receipt.tax,
                total = receipt.total,
                paymentMethod = receipt.paymentMethod.name,
                amountPaid = receipt.amountPaid,
                changeGiven = receipt.changeGiven,
                footerMessage = receipt.footerMessage
            )

            val receiptDetails = receipt.items.map { item ->
                ReceiptDetailEntity(
                    receiptId = 0,
                    productName = item.productName,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    lineTotal = item.lineTotal
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
}
