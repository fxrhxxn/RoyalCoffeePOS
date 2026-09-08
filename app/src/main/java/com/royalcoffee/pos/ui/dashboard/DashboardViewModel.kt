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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Supported date ranges for the dashboard analysis.
 */
enum class DashboardRange(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    WEEK("Last 7 Days"),
    MONTH("Last 30 Days")
}

/**
 * UI State for the Dashboard screen.
 */
data class DashboardState(
    val isLoading: Boolean = false,
    val selectedRange: DashboardRange = DashboardRange.TODAY,
    val totalRevenue: Int = 0,
    val totalOrders: Int = 0,
    val avgOrderValue: Int = 0,
    val grossProfit: Int = 0,
    val margin: Int = 0,
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
    
    // Comparative Trend Data
    val revenueTrendPercentage: String = "No comparison",
    val isRevenueTrendPositive: Boolean = true,
    val ordersTrendPercentage: String = "No comparison",
    val isOrdersTrendPositive: Boolean = true,
    val profitTrendPercentage: String = "No comparison",
    val isProfitTrendPositive: Boolean = true,
    
    val error: String? = null
)

/**
 * DashboardViewModel manages all data fetching and business logic for the Dashboard.
 *
 * It observes real-time database flows and allows dynamic switching between
 * different analysis periods (Today, Week, Month).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val receiptRepository: ReceiptRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(DashboardRange.TODAY)
    val selectedRange = _selectedRange.asStateFlow()

    private val _state = MutableStateFlow(DashboardState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        // Observe range changes and trigger data reload
        _selectedRange
            .onEach { _state.value = _state.value.copy(isLoading = true, selectedRange = it) }
            .flatMapLatest { range ->
                getDashboardDataFlow(range)
            }
            .onEach { newState ->
                _state.value = newState
            }
            .catch { e ->
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Updates the active dashboard range.
     */
    fun updateRange(range: DashboardRange) {
        _selectedRange.value = range
    }

    /**
     * Builds the combined data flow for a specific range.
     */
    private fun getDashboardDataFlow(range: DashboardRange): Flow<DashboardState> {
        val (currentStart, currentEnd) = getRangeTimestamps(range)
        val (prevStart, prevEnd) = getComparisonRangeTimestamps(range)

        // For trend charts, we always show at least the current period.
        // If range is TODAY or YESTERDAY, we show a 7-day window anyway for context.
        val trendStart = if (range == DashboardRange.MONTH) currentStart else {
            val now = LocalDate.now()
            now.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        return combine(
            receiptRepository.getSalesSummary(currentStart, currentEnd),
            receiptRepository.getSalesSummary(prevStart, prevEnd),
            receiptRepository.getSalesTrend(trendStart, currentEnd),
            receiptRepository.getPaymentBreakdown(currentStart, currentEnd),
            receiptRepository.getTopSellingProducts(currentStart, currentEnd, 5),
            productRepository.allProducts,
            receiptRepository.getHourlySales(currentStart, currentEnd),
            receiptRepository.getCategorySales(currentStart, currentEnd),
            receiptRepository.getGrossProfitStats(currentStart, currentEnd),
            receiptRepository.getGrossProfitStats(prevStart, prevEnd),
            receiptRepository.getProfitTrend(trendStart, currentEnd),
            receiptRepository.getMostProfitableProducts(currentStart, currentEnd, 1)
        ) { args: Array<Any?> ->
            val currentSum = args[0] as SalesSummary
            val prevSum = args[1] as SalesSummary
            val trend = args[2] as List<SalesTrendPoint>
            val payments = args[3] as List<PaymentBreakdown>
            val topProds = args[4] as List<TopProduct>
            val allProds = args[5] as List<ProductEntity>
            val hourly = args[6] as List<HourlySales>
            val categories = args[7] as List<CategorySales>
            val currentProfit = args[8] as GrossProfitStats
            val prevProfit = args[9] as GrossProfitStats
            val pTrend = args[10] as List<ProfitTrendPoint>
            val mostProfProd = args[11] as List<ProfitableProduct>

            val lowStock = allProds.filter { it.stockQuantity <= it.reorderLevel && it.stockQuantity > 0 && it.isActive }
            val outOfStock = allProds.filter { it.stockQuantity <= 0 && it.isActive }

            val revTrend = calculateTrend(currentSum.totalSales, prevSum.totalSales)
            val ordTrend = calculateTrend(currentSum.orderCount, prevSum.orderCount)
            
            val currentGP = currentProfit.knownCostRevenue - currentProfit.totalCOGS
            val prevGP = prevProfit.knownCostRevenue - prevProfit.totalCOGS
            val gpTrend = calculateTrend(currentGP, prevGP)

            val avg = if (currentSum.orderCount > 0) currentSum.totalSales / currentSum.orderCount else 0
            val margin = if (currentProfit.knownCostRevenue > 0) (currentGP.toDouble() / currentProfit.knownCostRevenue * 100).toInt() else 0
            val completeness = "Profit data available for ${currentProfit.knownCostCount} / ${currentProfit.totalSalesCount} sales"

            val mostProfCat = categories.maxByOrNull { it.knownCostRevenue - it.totalCOGS }?.category ?: "N/A"
            val highMarginCat = categories.filter { it.knownCostRevenue > 0 }.maxByOrNull { (it.knownCostRevenue - it.totalCOGS).toDouble() / it.knownCostRevenue }?.category ?: "N/A"

            DashboardState(
                isLoading = false,
                selectedRange = range,
                totalRevenue = currentSum.totalSales,
                totalOrders = currentSum.orderCount,
                avgOrderValue = avg,
                grossProfit = currentGP,
                margin = margin,
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
                revenueTrendPercentage = revTrend.first,
                isRevenueTrendPositive = revTrend.second,
                ordersTrendPercentage = ordTrend.first,
                isOrdersTrendPositive = ordTrend.second,
                profitTrendPercentage = gpTrend.first,
                isProfitTrendPositive = gpTrend.second
            )
        }
    }

    private fun calculateTrend(current: Int, previous: Int): Pair<String, Boolean> {
        if (previous == 0) {
            return if (current > 0) "↑ New" to true else "No comparison" to true
        }

        val change = ((current - previous).toDouble() / previous.toDouble()) * 100
        val sign = if (change >= 0) "↑" else "↓"
        val formatted = "${sign} ${Math.abs(change).toInt()}%"

        return formatted to (change >= 0)
    }

    private fun getRangeTimestamps(range: DashboardRange): Pair<Long, Long> {
        val now = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        
        return when (range) {
            DashboardRange.TODAY -> {
                val start = now.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                start to end
            }
            DashboardRange.YESTERDAY -> {
                val start = now.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.minusDays(1).atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                start to end
            }
            DashboardRange.WEEK -> {
                val start = now.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                start to end
            }
            DashboardRange.MONTH -> {
                val start = now.minusDays(29).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                start to end
            }
        }
    }

    private fun getComparisonRangeTimestamps(range: DashboardRange): Pair<Long, Long> {
        val now = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        
        return when (range) {
            DashboardRange.TODAY -> {
                val start = now.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.minusDays(1).atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                start to end
            }
            DashboardRange.YESTERDAY -> {
                val start = now.minusDays(2).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.minusDays(2).atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                start to end
            }
            DashboardRange.WEEK -> {
                val start = now.minusDays(13).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.minusDays(7).atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                start to end
            }
            DashboardRange.MONTH -> {
                val start = now.minusDays(59).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.minusDays(30).atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                start to end
            }
        }
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
