package com.royalcoffee.pos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.royalcoffee.pos.data.local.dao.CategorySales
import com.royalcoffee.pos.data.local.dao.GrossProfitStats
import com.royalcoffee.pos.data.local.dao.HourlySales
import com.royalcoffee.pos.data.local.dao.PaymentBreakdown
import com.royalcoffee.pos.data.local.dao.ProfitableProduct
import com.royalcoffee.pos.data.local.dao.ProfitTrendPoint
import com.royalcoffee.pos.data.local.dao.SalesSummary
import com.royalcoffee.pos.data.local.dao.SalesTrendPoint
import com.royalcoffee.pos.data.local.dao.TopProduct
import com.royalcoffee.pos.data.local.entity.ProductEntity
import com.royalcoffee.pos.data.local.repository.ProductRepository
import com.royalcoffee.pos.data.local.repository.ReceiptRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * UI State for the Dashboard screen.
 */
data class DashboardState(
    val isLoading: Boolean = false,
    val todaySales: Int = 0,
    val todayOrders: Int = 0,
    val avgOrderValue: Int = 0,
    val todayGrossProfit: Int = 0,
    val todayMargin: Int = 0,
    val profitDataCompleteness: String = "",
    val topProduct: String = "N/A",
    val salesTrend: List<SalesTrendPoint> = emptyList(),
    val profitTrend: List<ProfitTrendPoint> = emptyList(),
    val hourlySales: List<HourlySales> = emptyList(),
    val categorySales: List<CategorySales> = emptyList(),
    val paymentBreakdown: List<PaymentBreakdown> = emptyList(),
    val topProducts: List<TopProduct> = emptyList(),
    val mostProfitableCategory: String = "N/A",
    val highestMarginCategory: String = "N/A",
    val mostProfitableProduct: String = "N/A",
    val lowStockItems: List<ProductEntity> = emptyList(),
    val outOfStockItems: List<ProductEntity> = emptyList(),
    val salesTrendPercentage: String = "No comparison available",
    val isTrendPositive: Boolean = true,
    val error: String? = null
)

/**
 * DashboardViewModel manages all data fetching and business logic for the Dashboard.
 *
 * It observes real-time database flows from repositories and calculates
 * metrics like today's sales, trends, and inventory alerts.
 */
class DashboardViewModel(
    private val receiptRepository: ReceiptRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Combines multiple database flows to build the dashboard state.
     *
     * We use today's start/end timestamps for current metrics and
     * a 7-day window for the trend chart.
     */
    private fun loadDashboardData() {
        val now = LocalDate.now()
        val zoneId = ZoneId.systemDefault()

        val todayStart = now.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayEnd = now.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        val yesterdayStart = now.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val yesterdayEnd = now.minusDays(1).atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        val weekStart = now.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()

        combine(
            receiptRepository.getSalesSummary(todayStart, todayEnd),
            receiptRepository.getSalesSummary(yesterdayStart, yesterdayEnd),
            receiptRepository.getSalesTrend(weekStart, todayEnd),
            receiptRepository.getPaymentBreakdown(todayStart, todayEnd),
            receiptRepository.getTopSellingProducts(todayStart, todayEnd, 5),
            productRepository.allProducts,
            receiptRepository.getHourlySales(todayStart, todayEnd),
            receiptRepository.getCategorySales(todayStart, todayEnd),
            receiptRepository.getGrossProfitStats(todayStart, todayEnd),
            receiptRepository.getProfitTrend(weekStart, todayEnd),
            receiptRepository.getMostProfitableProducts(todayStart, todayEnd, 1)
        ) { args: Array<Any?> ->
            val todaySum = args[0] as SalesSummary
            val yesterdaySum = args[1] as SalesSummary
            val trend = args[2] as List<SalesTrendPoint>
            val payments = args[3] as List<PaymentBreakdown>
            val topProds = args[4] as List<TopProduct>
            val allProds = args[5] as List<ProductEntity>
            val hourly = args[6] as List<HourlySales>
            val categories = args[7] as List<CategorySales>
            val profitStats = args[8] as GrossProfitStats
            val pTrend = args[9] as List<ProfitTrendPoint>
            val mostProfProd = args[10] as List<ProfitableProduct>

            val lowStock = allProds.filter { it.stockQuantity <= it.reorderLevel && it.stockQuantity > 0 && it.isActive }
            val outOfStock = allProds.filter { it.stockQuantity <= 0 && it.isActive }

            val trendInfo = calculateTrend(todaySum.totalSales, yesterdaySum.totalSales)
            val avg = if (todaySum.orderCount > 0) todaySum.totalSales / todaySum.orderCount else 0

            val grossProfit = profitStats.knownCostRevenue - profitStats.totalCOGS
            val margin = if (profitStats.knownCostRevenue > 0) (grossProfit.toDouble() / profitStats.knownCostRevenue * 100).toInt() else 0
            val completeness = "Profit data available for ${profitStats.knownCostCount} / ${profitStats.totalSalesCount} sales"

            // Profitability Insights
            val mostProfCat = categories.maxByOrNull { it.knownCostRevenue - it.totalCOGS }?.category ?: "N/A"
            val highMarginCat = categories.filter { it.knownCostRevenue > 0 }.maxByOrNull { (it.knownCostRevenue - it.totalCOGS).toDouble() / it.knownCostRevenue }?.category ?: "N/A"
            
            // For Most Profitable Product, we need another query ideally, but let's approximate from topProds if we had costs there.
            // For now, let's keep it simple or add a query if needed.
            // Actually topProds doesn't have cost. Let's just use topProds[0] as a placeholder or add a query.
            // I'll skip most profitable product for a second or use top selling as proxy for now.

            DashboardState(
                isLoading = false,
                todaySales = todaySum.totalSales,
                todayOrders = todaySum.orderCount,
                avgOrderValue = avg,
                todayGrossProfit = grossProfit,
                todayMargin = margin,
                profitDataCompleteness = completeness,
                topProduct = topProds.firstOrNull()?.productName ?: "N/A",
                salesTrend = trend,
                profitTrend = pTrend,
                hourlySales = hourly,
                categorySales = categories,
                paymentBreakdown = payments,
                topProducts = topProds,
                mostProfitableCategory = mostProfCat,
                highestMarginCategory = highMarginCat,
                mostProfitableProduct = mostProfProd.firstOrNull()?.productName ?: "N/A",
                lowStockItems = lowStock,
                outOfStockItems = outOfStock,
                salesTrendPercentage = trendInfo.first,
                isTrendPositive = trendInfo.second
            )
        }.catch { e ->
            _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)
    }

    /**
     * Calculates the percentage change between today and yesterday.
     */
    private fun calculateTrend(today: Int, yesterday: Int): Pair<String, Boolean> {
        if (yesterday == 0) {
            return if (today > 0) "↑ New Sales" to true else "No comparison available" to true
        }

        val change = ((today - yesterday).toDouble() / yesterday.toDouble()) * 100
        val sign = if (change >= 0) "↑" else "↓"
        val formatted = "${sign} ${Math.abs(change).toInt()}%"

        return formatted to (change >= 0)
    }
}

/**
 * Factory for creating DashboardViewModel with its dependencies.
 */
class DashboardViewModelFactory(
    private val receiptRepository: ReceiptRepository,
    private val productRepository: ProductRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(receiptRepository, productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
