package com.royalcoffee.pos.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DashboardHeader()

                Spacer(modifier = Modifier.height(32.dp))

                DashboardContent(state = state)
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
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
    }
}

@Composable
private fun DashboardContent(state: DashboardState) {
    Column(
        modifier = Modifier.widthIn(max = 1200.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCardWithTrend(
                title = "Today's Revenue",
                value = "Rs ${state.todaySales}",
                trend = state.salesTrendPercentage,
                isPositive = state.isTrendPositive,
                modifier = Modifier.weight(1f)
            )
            MetricCardWithTrend(
                title = "Gross Profit",
                value = "Rs ${state.todayGrossProfit}",
                trend = "${state.todayMargin}% Margin",
                isPositive = true,
                modifier = Modifier.weight(1f)
            )
            MetricCardWithTrend(
                title = "Orders",
                value = state.todayOrders.toString(),
                trend = "Total",
                isPositive = true,
                modifier = Modifier.weight(1f)
            )
            MetricCardWithTrend(
                title = "Avg Order",
                value = "Rs ${state.avgOrderValue}",
                trend = "Per ticket",
                isPositive = true,
                modifier = Modifier.weight(1f)
            )
        }

        // 2. Primary Insights: Sales Trend & Busiest Hours
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ChartCard(
                title = "Revenue & Gross Profit Trend",
                modifier = Modifier.weight(1.5f)
            ) {
                DashboardLineChart(
                    revenueData = state.salesTrend,
                    profitData = state.profitTrend,
                    modifier = Modifier.fillMaxSize()
                )
            }

            ChartCard(
                title = "Today's Busiest Hours",
                modifier = Modifier.weight(1f)
            ) {
                if (state.hourlySales.isEmpty()) {
                    EmptyStateMessage("No hourly data yet")
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

        // 3. Middle Insights: Payments & Category Performance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ChartCard(
                title = "Payment Methods",
                modifier = Modifier.weight(1f)
            ) {
                DashboardDonutChart(
                    data = state.paymentBreakdown,
                    modifier = Modifier.fillMaxSize()
                )
            }

            ChartCard(
                title = "Category Performance",
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

        // 4. Profitability Insights
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                InsightItem("Most Profitable Category", state.mostProfitableCategory, Modifier.weight(1f))
                InsightItem("Highest Margin Category", state.highestMarginCategory, Modifier.weight(1f))
                InsightItem("Most Profitable Product", state.mostProfitableProduct, Modifier.weight(1f))
            }
        }

        // 5. Secondary Insights: Top Products & Inventory
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.weight(1.2f),
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
                    if (state.topProducts.isEmpty()) {
                        EmptyStateMessage("No sales recorded")
                    } else {
                        state.topProducts.forEach { product ->
                            TopProductRow(
                                name = product.productName,
                                count = product.totalQuantity,
                                maxCount = state.topProducts.firstOrNull()?.totalQuantity ?: 1
                            )
                        }
                    }
                }
            }

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
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier.height(320.dp),
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = "$count sold", style = MaterialTheme.typography.bodySmall, color = RoyalGold)
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
