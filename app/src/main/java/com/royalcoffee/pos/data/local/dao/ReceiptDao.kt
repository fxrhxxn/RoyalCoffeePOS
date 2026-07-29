package com.royalcoffee.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.royalcoffee.pos.data.local.entity.ReceiptDetailEntity
import com.royalcoffee.pos.data.local.entity.ReceiptHeaderEntity
import kotlinx.coroutines.flow.Flow

/**
 * ReceiptDao contains database operations for saved receipts.
 *
 * This DAO allows the app to:
 * - save receipt headers
 * - save receipt detail lines
 * - read saved receipts later
 */
@Dao
interface ReceiptDao {

    /**
     * Inserts a receipt header and returns the generated receipt ID.
     *
     * That receipt ID is then used by the receipt detail rows.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiptHeader(
        receiptHeader: ReceiptHeaderEntity
    ): Long

    /**
     * Inserts all detail rows for a receipt.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiptDetails(
        receiptDetails: List<ReceiptDetailEntity>
    )

    /**
     * Saves a full receipt in one database transaction.
     *
     * @Transaction means:
     * - If header and details both insert successfully, save everything.
     * - If something fails, Room rolls back the full operation.
     */
    @Transaction
    suspend fun insertFullReceipt(
        receiptHeader: ReceiptHeaderEntity,
        receiptDetails: List<ReceiptDetailEntity>
    ) {
        val receiptId = insertReceiptHeader(receiptHeader).toInt()

        val detailsWithReceiptId = receiptDetails.map { detail ->
            detail.copy(
                receiptId = receiptId
            )
        }

        insertReceiptDetails(detailsWithReceiptId)
    }

    /**
     * Returns all saved receipt headers.
     *
     * Later, we can use this for:
     * - receipt history
     * - dashboard sales totals
     * - daily reports
     */
    @Query(
        """
        SELECT *
        FROM receipt_headers
        ORDER BY receipt_id DESC
        """
    )
    fun getAllReceiptHeaders(): Flow<List<ReceiptHeaderEntity>>

    /**
     * Returns all detail rows for one receipt.
     */
    @Query(
        """
        SELECT *
        FROM receipt_details
        WHERE receipt_id = :receiptId
        ORDER BY receipt_detail_id ASC
        """
    )
    fun getReceiptDetails(
        receiptId: Int
    ): Flow<List<ReceiptDetailEntity>>

    /**
     * Returns all detail rows for one receipt as a one-time list.
     *
     * This is useful when the Admin taps a receipt in the history screen.
     */
    @Query(
        """
        SELECT *
        FROM receipt_details
        WHERE receipt_id = :receiptId
        ORDER BY receipt_detail_id ASC
        """
    )
    suspend fun getReceiptDetailsOnce(
        receiptId: Int
    ): List<ReceiptDetailEntity>
}
