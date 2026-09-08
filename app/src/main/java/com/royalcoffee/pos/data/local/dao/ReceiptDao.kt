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
 * Data model for dashboard sales summary.
 */
data class SalesSummary(
    val totalSales: Int,
    val orderCount: Int
)

/**
 * Data model for dashboard payment breakdown.
 */
data class PaymentBreakdown(
    val paymentMethod: String,
    val totalAmount: Int,
    val orderCount: Int
)

/**
 * Data model for dashboard top selling products.
 */
data class TopProduct(
    val productName: String,
    val totalQuantity: Int
)

/**
 * Data model for most profitable product.
 */
data class ProfitableProduct(
    val productName: String,
    val totalGrossProfit: Int
)

/**
 * Data model for sales trend line chart.
 */
data class SalesTrendPoint(
    val dateLabel: String,
    val totalSales: Int,
    val orderCount: Int
)

/**
 * Data model for hourly sales breakdown.
 */
data class HourlySales(
    val hourLabel: String, // e.g. "09:00"
    val totalSales: Int,
    val orderCount: Int
)

/**
 * Data model for category-based performance.
 */
data class CategorySales(
    val category: String,
    val totalRevenue: Int,
    val knownCostRevenue: Int,
    val totalCOGS: Int,
    val quantitySold: Int
)

/**
 * Data model for profit statistics.
 */
data class GrossProfitStats(
    val totalRevenue: Int,
    val knownCostRevenue: Int,
    val totalCOGS: Int,
    val knownCostCount: Int,
    val totalSalesCount: Int
)

/**
 * Data model for daily profit trend.
 */
data class ProfitTrendPoint(
    val dateLabel: String,
    val totalRevenue: Int,
    val totalGrossProfit: Int
)

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
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReceiptHeader(
        receiptHeader: ReceiptHeaderEntity
    ): Long

    /**
     * Inserts all detail rows for a receipt.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
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
     * Returns a specific receipt header by its ID.
     */
    @Query("SELECT * FROM receipt_headers WHERE receipt_id = :receiptId")
    suspend fun getReceiptHeaderById(receiptId: Int): ReceiptHeaderEntity?

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

    /**
     * Returns the latest receipt number for a specific year.
     *
     * Example:
     * yearPrefix = "RC-2026-"
     * returns "RC-2026-000005"
     */
    @Query(
        """
        SELECT receipt_number
        FROM receipt_headers
        WHERE receipt_number LIKE :yearPrefix || '%'
        ORDER BY receipt_number DESC
        LIMIT 1
        """
    )
    suspend fun getLastReceiptNumber(yearPrefix: String): String?

    /**
     * Returns a summary of sales for a specific date range.
     */
    @Query(
        """
        SELECT IFNULL(SUM(total), 0) as totalSales, COUNT(receipt_id) as orderCount 
        FROM receipt_headers 
        WHERE timestamp >= :start AND timestamp <= :end
        """
    )
    fun getSalesSummary(start: Long, end: Long): Flow<SalesSummary>

    /**
     * Returns sales grouped by payment method for a specific date range.
     */
    @Query(
        """
        SELECT payment_method as paymentMethod, SUM(total) as totalAmount, COUNT(receipt_id) as orderCount 
        FROM receipt_headers 
        WHERE timestamp >= :start AND timestamp <= :end 
        GROUP BY payment_method
        """
    )
    fun getPaymentBreakdown(start: Long, end: Long): Flow<List<PaymentBreakdown>>

    /**
     * Returns top selling products for a specific date range.
     */
    @Query(
        """
        SELECT product_name as productName, SUM(quantity) as totalQuantity 
        FROM receipt_details 
        JOIN receipt_headers ON receipt_details.receipt_id = receipt_headers.receipt_id 
        WHERE timestamp >= :start AND timestamp <= :end 
        GROUP BY product_name 
        ORDER BY totalQuantity DESC 
        LIMIT :limit
        """
    )
    fun getTopSellingProducts(start: Long, end: Long, limit: Int): Flow<List<TopProduct>>

    /**
     * Returns sales trend points grouped by day.
     */
    @Query(
        """
        SELECT strftime('%d/%m', timestamp / 1000, 'unixepoch') as dateLabel, 
               SUM(total) as totalSales, 
               COUNT(receipt_id) as orderCount 
        FROM receipt_headers 
        WHERE timestamp >= :start AND timestamp <= :end 
        GROUP BY dateLabel 
        ORDER BY MIN(timestamp) ASC
        """
    )
    fun getSalesTrend(start: Long, end: Long): Flow<List<SalesTrendPoint>>

    /**
     * Returns sales grouped by hour for a specific period.
     */
    @Query(
        """
        SELECT strftime('%H:00', timestamp / 1000, 'unixepoch') as hourLabel, 
               SUM(total) as totalSales, 
               COUNT(receipt_id) as orderCount 
        FROM receipt_headers 
        WHERE timestamp >= :start AND timestamp <= :end 
        GROUP BY hourLabel 
        ORDER BY hourLabel ASC
        """
    )
    fun getHourlySales(start: Long, end: Long): Flow<List<HourlySales>>

    /**
     * Returns sales grouped by product category.
     */
    @Query(
        """
        SELECT products.category as category, 
               SUM(receipt_details.line_total) as totalRevenue, 
               SUM(CASE WHEN receipt_details.cost_price IS NOT NULL THEN receipt_details.line_total ELSE 0 END) as knownCostRevenue,
               SUM(CASE WHEN receipt_details.cost_price IS NOT NULL THEN receipt_details.quantity * receipt_details.cost_price ELSE 0 END) as totalCOGS,
               SUM(receipt_details.quantity) as quantitySold 
        FROM receipt_details 
        JOIN receipt_headers ON receipt_details.receipt_id = receipt_headers.receipt_id 
        JOIN products ON receipt_details.product_name = products.product_name 
        WHERE receipt_headers.timestamp >= :start AND receipt_headers.timestamp <= :end 
        GROUP BY category 
        ORDER BY totalRevenue DESC
        """
    )
    fun getCategorySales(start: Long, end: Long): Flow<List<CategorySales>>

    /**
     * Returns the most profitable products.
     */
    @Query(
        """
        SELECT product_name as productName, 
               SUM(quantity * (unit_price - IFNULL(cost_price, unit_price))) as totalGrossProfit
        FROM receipt_details
        JOIN receipt_headers ON receipt_details.receipt_id = receipt_headers.receipt_id
        WHERE receipt_headers.timestamp >= :start AND receipt_headers.timestamp <= :end
        GROUP BY productName
        ORDER BY totalGrossProfit DESC
        LIMIT :limit
        """
    )
    fun getMostProfitableProducts(start: Long, end: Long, limit: Int): Flow<List<ProfitableProduct>>

    /**
     * Returns Gross Profit statistics.
     */
    @Query(
        """
        SELECT 
            IFNULL((SELECT SUM(total) FROM receipt_headers WHERE timestamp >= :start AND timestamp <= :end), 0) as totalRevenue,
            SUM(CASE WHEN d.cost_price IS NOT NULL THEN d.line_total ELSE 0 END) as knownCostRevenue,
            SUM(d.quantity * d.cost_price) as totalCOGS,
            COUNT(DISTINCT CASE WHEN d.cost_price IS NOT NULL THEN h.receipt_id END) as knownCostCount,
            COUNT(DISTINCT h.receipt_id) as totalSalesCount
        FROM receipt_headers h
        LEFT JOIN receipt_details d ON h.receipt_id = d.receipt_id
        WHERE h.timestamp >= :start AND h.timestamp <= :end
        """
    )
    fun getGrossProfitStats(start: Long, end: Long): Flow<GrossProfitStats>

    /**
     * Returns dual trend points for Revenue and Gross Profit.
     */
    @Query(
        """
        SELECT 
            dateLabel,
            SUM(receiptTotal) as totalRevenue,
            SUM(receiptGrossProfit) as totalGrossProfit
        FROM (
            SELECT 
                strftime('%d/%m', h.timestamp / 1000, 'unixepoch') as dateLabel,
                h.receipt_id,
                MAX(h.total) as receiptTotal,
                SUM(d.quantity * (d.unit_price - IFNULL(d.cost_price, d.unit_price))) as receiptGrossProfit,
                MIN(h.timestamp) as minTimestamp
            FROM receipt_headers h
            JOIN receipt_details d ON h.receipt_id = d.receipt_id
            WHERE h.timestamp >= :start AND h.timestamp <= :end
            GROUP BY h.receipt_id
        )
        GROUP BY dateLabel
        ORDER BY MIN(minTimestamp) ASC
        """
    )
    fun getProfitTrend(start: Long, end: Long): Flow<List<ProfitTrendPoint>>
}
