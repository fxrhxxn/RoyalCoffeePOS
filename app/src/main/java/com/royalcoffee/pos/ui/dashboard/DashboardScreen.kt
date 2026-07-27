package com.royalcoffee.pos.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold

/**
 * DashboardSummary contains the main figures shown at the top of the dashboard.
 *
 * This is temporary sample data for now.
 * Later, these values will be calculated from the local Room database.
 */
data class DashboardSummary(
    val todaySales: Int,
    val todayOrders: Int,
    val averageOrderValue: Int,
    val lowStockCount: Int
)

/**
 * TopSellingProduct represents one product in the top-selling products chart.
 *
 * Later, this will be calculated from receipt/order details stored in Room.
 */
data class TopSellingProduct(
    val productName: String,
    val quantitySold: Int,
    val revenue: Int
)

/**
 * PaymentBreakdownItem represents sales grouped by payment method.
 *
 * Later, this will be calculated from saved payment records.
 */
data class PaymentBreakdownItem(
    val paymentMethod: String,
    val orderCount: Int,
    val amount: Int
)

/**
 * LowStockDashboardItem represents low-stock products shown in the dashboard.
 *
 * Later, this will be calculated from the inventory table.
 */
data class LowStockDashboardItem(
    val productName: String,
    val stockQuantity: Int,
    val reorderLevel: Int
)

/**
 * Identifies which dashboard chart was tapped.
 *
 * This allows the dashboard to open the correct detailed view dialog.
 */
private enum class DashboardChartType {
    TOP_PRODUCTS,
    PAYMENTS,
    LOW_STOCK
}

/**
 * Internal data model used by the reusable horizontal bar chart.
 */
private data class ChartBarItem(
    val label: String,
    val value: Int,
    val supportingText: String
)

/**
 * DashboardScreen shows a simple Admin business overview.
 *
 * Version 1 goal:
 * - Display summary cards.
 * - Display simple Compose-based charts.
 * - Allow the Admin to tap a chart and view structured details.
 *
 * Important:
 * These values are temporary sample data.
 * Later, this screen will be connected to the Room database.
 */
@Composable
fun DashboardScreen(
    dashboardSummary: DashboardSummary,
    topSellingProducts: List<TopSellingProduct>,
    paymentBreakdown: List<PaymentBreakdownItem>,
    lowStockItems: List<LowStockDashboardItem>,
    modifier: Modifier = Modifier
) {
    var selectedChart by remember {
        mutableStateOf<DashboardChartType?>(null)
    }

    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Dashboard",
                fontFamily = CinzelFontFamily,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "View today's sales performance and key business indicators.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier.widthIn(max = 1040.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                DashboardSummarySection(
                    dashboardSummary = dashboardSummary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    HorizontalBarChartCard(
                        title = "Top Products",
                        description = "Best-selling products by revenue.",
                        data = topSellingProducts.map { product ->
                            ChartBarItem(
                                label = product.productName,
                                value = product.revenue,
                                supportingText = "${product.quantitySold} sold"
                            )
                        },
                        valuePrefix = "Rs ",
                        onClick = {
                            selectedChart = DashboardChartType.TOP_PRODUCTS
                        },
                        modifier = Modifier.weight(1f)
                    )

                    HorizontalBarChartCard(
                        title = "Payments",
                        description = "Sales grouped by payment method.",
                        data = paymentBreakdown.map { payment ->
                            ChartBarItem(
                                label = payment.paymentMethod,
                                value = payment.amount,
                                supportingText = "${payment.orderCount} order(s)"
                            )
                        },
                        valuePrefix = "Rs ",
                        onClick = {
                            selectedChart = DashboardChartType.PAYMENTS
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalBarChartCard(
                    title = "Low Stock",
                    description = "Products that reached or fell below reorder level.",
                    data = lowStockItems.map { item ->
                        ChartBarItem(
                            label = item.productName,
                            value = item.stockQuantity,
                            supportingText = "Reorder at ${item.reorderLevel}"
                        )
                    },
                    valueSuffix = " unit(s)",
                    onClick = {
                        selectedChart = DashboardChartType.LOW_STOCK
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isWarning = lowStockItems.isNotEmpty()
                )
            }
        }
    }

    /**
     * Opens a structured detail dialog when the Admin taps a chart.
     */
    when (selectedChart) {
        DashboardChartType.TOP_PRODUCTS -> {
            TopProductsDetailDialog(
                products = topSellingProducts,
                onDismiss = {
                    selectedChart = null
                }
            )
        }

        DashboardChartType.PAYMENTS -> {
            PaymentBreakdownDetailDialog(
                paymentBreakdown = paymentBreakdown,
                onDismiss = {
                    selectedChart = null
                }
            )
        }

        DashboardChartType.LOW_STOCK -> {
            LowStockDetailDialog(
                lowStockItems = lowStockItems,
                onDismiss = {
                    selectedChart = null
                }
            )
        }

        null -> {
            // No chart detail dialog is currently open.
        }
    }
}

@Composable
private fun DashboardSummarySection(
    dashboardSummary: DashboardSummary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DashboardMetricCard(
            title = "Today's Sales",
            value = "Rs ${dashboardSummary.todaySales}",
            modifier = Modifier.weight(1f)
        )

        DashboardMetricCard(
            title = "Orders",
            value = dashboardSummary.todayOrders.toString(),
            modifier = Modifier.weight(1f)
        )

        DashboardMetricCard(
            title = "Average Order",
            value = "Rs ${dashboardSummary.averageOrderValue}",
            modifier = Modifier.weight(1f)
        )

        DashboardMetricCard(
            title = "Low Stock",
            value = dashboardSummary.lowStockCount.toString(),
            modifier = Modifier.weight(1f),
            isWarning = dashboardSummary.lowStockCount > 0
        )
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isWarning) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
            } else {
                RoyalGold.copy(alpha = 0.35f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HorizontalBarChartCard(
    title: String,
    description: String,
    data: List<ChartBarItem>,
    valuePrefix: String = "",
    valueSuffix: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    val maxValue = data.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = modifier
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isWarning) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isWarning) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Tap chart to view details",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Text(
                    text = "No data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    data.forEach { item ->
                        HorizontalBarRow(
                            item = item,
                            maxValue = maxValue,
                            valuePrefix = valuePrefix,
                            valueSuffix = valueSuffix,
                            isWarning = isWarning
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalBarRow(
    item: ChartBarItem,
    maxValue: Int,
    valuePrefix: String,
    valueSuffix: String,
    isWarning: Boolean
) {
    val barFraction = (item.value.toFloat() / maxValue.toFloat()).coerceIn(
        minimumValue = 0.05f,
        maximumValue = 1f
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = item.supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = "$valuePrefix${item.value}$valueSuffix",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(50)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barFraction)
                    .height(12.dp)
                    .background(
                        color = if (isWarning) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        },
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
private fun TopProductsDetailDialog(
    products: List<TopSellingProduct>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Top Products Details")
        },
        text = {
            Column(
                modifier = Modifier.widthIn(max = 620.dp)
            ) {
                DetailHeaderRow(
                    first = "Product",
                    second = "Qty Sold",
                    third = "Revenue"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                products.forEach { product ->
                    DetailDataRow(
                        first = product.productName,
                        second = product.quantitySold.toString(),
                        third = "Rs ${product.revenue}"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "Close")
            }
        }
    )
}

@Composable
private fun PaymentBreakdownDetailDialog(
    paymentBreakdown: List<PaymentBreakdownItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Payment Details")
        },
        text = {
            Column(
                modifier = Modifier.widthIn(max = 620.dp)
            ) {
                DetailHeaderRow(
                    first = "Method",
                    second = "Orders",
                    third = "Amount"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                paymentBreakdown.forEach { payment ->
                    DetailDataRow(
                        first = payment.paymentMethod,
                        second = payment.orderCount.toString(),
                        third = "Rs ${payment.amount}"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "Close")
            }
        }
    )
}

@Composable
private fun LowStockDetailDialog(
    lowStockItems: List<LowStockDashboardItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Low Stock Details")
        },
        text = {
            Column(
                modifier = Modifier.widthIn(max = 620.dp)
            ) {
                if (lowStockItems.isEmpty()) {
                    Text(
                        text = "No low-stock products at the moment.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    DetailHeaderRow(
                        first = "Product",
                        second = "Stock",
                        third = "Reorder"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    lowStockItems.forEach { item ->
                        DetailDataRow(
                            first = item.productName,
                            second = item.stockQuantity.toString(),
                            third = item.reorderLevel.toString()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "Close")
            }
        }
    )
}

@Composable
private fun DetailHeaderRow(
    first: String,
    second: String,
    third: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = first,
            modifier = Modifier.weight(1.4f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = second,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = third,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DetailDataRow(
    first: String,
    second: String,
    third: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = first,
            modifier = Modifier.weight(1.4f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = second,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = third,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}