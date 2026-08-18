package com.royalcoffee.pos.data.local.repository

import com.royalcoffee.pos.data.local.dao.ReceiptDao
import com.royalcoffee.pos.data.local.entity.ReceiptDetailEntity
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import com.royalcoffee.pos.model.Receipt
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
    private val receiptDao: ReceiptDao
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
     * Saves a complete receipt to the local Room database.
     *
     * This includes:
     * - one receipt header row
     * - many receipt detail rows
     */
    suspend fun saveReceipt(receipt: Receipt) {
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

        /**
         * receiptId is set to 0 temporarily.
         *
         * ReceiptDao.insertFullReceipt() will insert the header first,
         * get the generated receipt ID, then attach that ID to each detail row.
         */
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
