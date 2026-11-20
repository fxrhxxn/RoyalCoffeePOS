package com.royalcoffee.pos.ui.dashboard

import com.royalcoffee.pos.model.Money
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royalcoffee.pos.data.local.dao.TopProduct
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold

/**
 * DashboardScreen displays a professional business overview for Admin users.
 *
 * It features interactive charts for sales trends and payment breakdowns,
 * along with real-time inventory alerts and key performance metrics.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RoyalGold)
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Error loading dashboard", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.error!!, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val horizontalPadding = when {
                    maxWidth < 700.dp -> 12.dp
                    maxWidth < 1000.dp -> 18.dp
                    else -> 24.dp
                }
                val verticalSpacing = if (maxHeight < 600.dp) 16.dp else 24.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding, vertical = verticalSpacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DashboardHeader(
                        selectedRange = state.selectedRange,
                        onRangeSelected = { viewModel.updateRange(it) }
                    )

                    Spacer(modifier = Modifier.height(verticalSpacing))

                    DashboardContent(state = state)
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    selectedRange: DashboardRange,
    onRangeSelected: (DashboardRange) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Business Dashboard",
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Real-time analytics and performance overview",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        RangeSelector(
            selectedRange = selectedRange,
            onRangeSelected = onRangeSelected
        )
    }
}

@Composable
private fun RangeSelector(
    selectedRange: DashboardRange,
    onRangeSelected: (DashboardRange) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(DashboardRange.entries) { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RoyalGold,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun DashboardContent(state: DashboardState) {
    val compLabel = when (state.selectedRange) {
        DashboardRange.TODAY -> "vs yesterday"
        DashboardRange.YESTERDAY -> "vs day before"
        DashboardRange.WEEK -> "vs last week"
        DashboardRange.MONTH -> "vs last month"
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val contentWidth = maxWidth
        val compactLayout = contentWidth < 850.dp
        val veryCompactLayout = contentWidth < 620.dp

        val sectionSpacing = if (compactLayout) 16.dp else 24.dp
        val chartHeight = if (compactLayout) 280.dp else 320.dp

        Column(
            modifier = Modifier.widthIn(max = 1200.dp),
            verticalArrangement = Arrangement.spacedBy(sectionSpacing)
        ) {
            // Data Completeness Info
            if (state.profitDataCompleteness.isNotEmpty()) {
                Text(
                    text = "ℹ ${state.profitDataCompleteness}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // 1. Summary Metrics
            if (veryCompactLayout) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCardWithTrend(
                        title = "Total Revenue",
                        value = "Rs ${Money.format(state.totalRevenue)}",
                        trend = state.revenueTrendPercentage,
                        isPositive = state.isRevenueTrendPositive,
                        comparisonLabel = compLabel,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MetricCardWithTrend(
                        title = "Gross Profit",
                        value = "Rs ${Money.format(state.grossProfit)}",
                        trend = state.profitTrendPercentage,
                        isPositive = state.isProfitTrendPositive,
                        comparisonLabel = compLabel,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MetricCardWithTrend(
                        title = "Orders",
                        value = state.totalOrders.toString(),
                        trend = state.ordersTrendPercentage,
                        isPositive = state.isOrdersTrendPositive,
                        comparisonLabel = compLabel,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MetricCardWithTrend(
                        title = "Avg Order",
                        value = "Rs ${Money.format(state.avgOrderValue)}",
                        trend = "${state.margin}% Margin",
                        isPositive = true,
                        comparisonLabel = "Total Efficiency",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (compactLayout) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCardWithTrend(
                            title = "Total Revenue",
                            value = "Rs ${Money.format(state.totalRevenue)}",
                            trend = state.revenueTrendPercentage,
                            isPositive = state.isRevenueTrendPositive,
                            comparisonLabel = compLabel,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCardWithTrend(
                            title = "Gross Profit",
                            value = "Rs ${Money.format(state.grossProfit)}",
                            trend = state.profitTrendPercentage,
                            isPositive = state.isProfitTrendPositive,
                            comparisonLabel = compLabel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCardWithTrend(
                            title = "Orders",
                            value = state.totalOrders.toString(),
                            trend = state.ordersTrendPercentage,
                            isPositive = state.isOrdersTrendPositive,
                            comparisonLabel = compLabel,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCardWithTrend(
                            title = "Avg Order",
                            value = "Rs ${Money.format(state.avgOrderValue)}",
                            trend = "${state.margin}% Margin",
                            isPositive = true,
                            comparisonLabel = "Total Efficiency",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCardWithTrend(
                        title = "Total Revenue",
                        value = "Rs ${Money.format(state.totalRevenue)}",
                        trend = state.revenueTrendPercentage,
                        isPositive = state.isRevenueTrendPositive,
                        comparisonLabel = compLabel,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCardWithTrend(
                        title = "Gross Profit",
                        value = "Rs ${Money.format(state.grossProfit)}",
                        trend = state.profitTrendPercentage,
                        isPositive = state.isProfitTrendPositive,
                        comparisonLabel = compLabel,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCardWithTrend(
                        title = "Orders",
                        value = state.totalOrders.toString(),
                        trend = state.ordersTrendPercentage,
                        isPositive = state.isOrdersTrendPositive,
                        comparisonLabel = compLabel,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCardWithTrend(
                        title = "Avg Order",
                        value = "Rs ${Money.format(state.avgOrderValue)}",
                        trend = "${state.margin}% Margin",
                        isPositive = true,
                        comparisonLabel = "Total Efficiency",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Primary Insights: Sales Trend & Busiest Hours
            if (compactLayout) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                ) {
                    ChartCard(
                        title = "Revenue & Gross Profit Trend",
                        chartHeight = chartHeight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DashboardLineChart(
                            revenueData = state.salesTrend,
                            profitData = state.profitTrend,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    ChartCard(
                        title = "Period Performance",
                        chartHeight = chartHeight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.hourlySales.isEmpty()) {
                            EmptyStateMessage("No data for this period")
                        } else {
                            DashboardBarChart(
                                labels = state.hourlySales.map { it.hourLabel },
                                revenueValues = state.hourlySales.map { it.totalSales },
                                valuePrefix = "Rs ",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ChartCard(
                        title = "Revenue & Gross Profit Trend",
                        chartHeight = chartHeight,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        DashboardLineChart(
                            revenueData = state.salesTrend,
                            profitData = state.profitTrend,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    ChartCard(
                        title = "Period Performance",
                        chartHeight = chartHeight,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.hourlySales.isEmpty()) {
                            EmptyStateMessage("No data for this period")
                        } else {
                            DashboardBarChart(
                                labels = state.hourlySales.map { it.hourLabel },
                                revenueValues = state.hourlySales.map { it.totalSales },
                                valuePrefix = "Rs ",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 3. Middle Insights: Payments & Category Performance
            if (compactLayout) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                ) {
                    ChartCard(
                        title = "Payment Methods",
                        chartHeight = chartHeight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DashboardDonutChart(
                            data = state.paymentBreakdown,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    ChartCard(
                        title = "Category Performance",
                        chartHeight = chartHeight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.categorySales.isEmpty()) {
                            EmptyStateMessage("No category data yet")
                        } else {
                            DashboardBarChart(
                                labels = state.categorySales.map { it.category },
                                revenueValues = state.categorySales.map { it.totalRevenue },
                                profitValues = state.categorySales.map { it.knownCostRevenue - it.totalCOGS },
                                valuePrefix = "Rs ",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ChartCard(
                        title = "Payment Methods",
                        chartHeight = chartHeight,
                        modifier = Modifier.weight(1f)
                    ) {
                        DashboardDonutChart(
                            data = state.paymentBreakdown,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    ChartCard(
                        title = "Category Performance",
                        chartHeight = chartHeight,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        if (state.categorySales.isEmpty()) {
                            EmptyStateMessage("No category data yet")
                        } else {
                            DashboardBarChart(
                                labels = state.categorySales.map { it.category },
                                revenueValues = state.categorySales.map { it.totalRevenue },
                                profitValues = state.categorySales.map { it.knownCostRevenue - it.totalCOGS },
                                valuePrefix = "Rs ",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 4. Profitability Insights
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                if (compactLayout) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InsightItem("Most Profitable Category", state.mostProfitableCategory, Modifier.fillMaxWidth())
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        InsightItem("Highest Margin Category", state.highestMarginCategory, Modifier.fillMaxWidth())
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        InsightItem("Most Profitable Product", state.mostProfitableProduct, Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        InsightItem("Most Profitable Category", state.mostProfitableCategory, Modifier.weight(1f))
                        InsightItem("Highest Margin Category", state.highestMarginCategory, Modifier.weight(1f))
                        InsightItem("Most Profitable Product", state.mostProfitableProduct, Modifier.weight(1f))
                    }
                }
            }

            // 5. Secondary Insights: Top Products & Inventory
            if (compactLayout) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                ) {
                    TopProductsCard(
                        topProducts = state.topProducts,
                        modifier = Modifier.fillMaxWidth()
                    )
                    InventoryAlertCard(
                        title = "Low Stock Alerts",
                        items = state.lowStockItems.map { "${it.productName} (${it.stockQuantity} left)" },
                        isError = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    InventoryAlertCard(
                        title = "Out of Stock",
                        items = state.outOfStockItems.map { it.productName },
                        isError = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TopProductsCard(
                        topProducts = state.topProducts,
                        modifier = Modifier.weight(1.2f)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InventoryAlertCard(
                            title = "Low Stock Alerts",
                            items = state.lowStockItems.map { "${it.productName} (${it.stockQuantity} left)" },
                            isError = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        InventoryAlertCard(
                            title = "Out of Stock",
                            items = state.outOfStockItems.map { it.productName },
                            isError = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopProductsCard(
    topProducts: List<TopProduct>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Top Selling Products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (topProducts.isEmpty()) {
                EmptyStateMessage("No sales recorded")
            } else {
                val maxCount = topProducts.firstOrNull()?.totalQuantity ?: 1
                topProducts.forEach { product ->
                    TopProductRow(
                        name = product.productName,
                        count = product.totalQuantity,
                        maxCount = maxCount
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightItem(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 320.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier.height(chartHeight),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxSize(), content = content)
        }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun TopProductRow(name: String, count: Int, maxCount: Int) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$count sold",
                style = MaterialTheme.typography.bodySmall,
                color = RoyalGold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val progress = count.toFloat() / maxCount.toFloat()
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = RoyalGold,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun InventoryAlertCard(
    title: String,
    items: List<String>,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError && items.isNotEmpty()) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isError && items.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (items.isEmpty()) {
                Text(text = "All clear", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                items.take(3).forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                if (items.size > 3) {
                    Text(
                        text = "+ ${items.size - 3} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalGold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
