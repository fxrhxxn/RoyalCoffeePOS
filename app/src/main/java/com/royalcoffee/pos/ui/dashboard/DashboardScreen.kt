package com.royalcoffee.pos.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * Later, these values will be calculated from the local database.
 */
data class DashboardSummary(
    val todaySales: Int,
    val todayOrders: Int,
    val averageOrderValue: Int,
    val lowStockCount: Int
)

/**
 * TopSellingProduct represents one product in the top-selling products list.
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
 * DashboardScreen shows a simple Admin business overview.
 *
 * Version 1 goal:
 * - Display useful business summary information.
 * - Use temporary sample data.
 * - Prepare the screen structure for future database-driven analytics.
 */
@Composable
fun DashboardScreen(
    dashboardSummary: DashboardSummary,
    topSellingProducts: List<TopSellingProduct>,
    paymentBreakdown: List<PaymentBreakdownItem>,
    modifier: Modifier = Modifier
) {
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
                modifier = Modifier.widthIn(max = 980.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                DashboardSummarySection(
                    dashboardSummary = dashboardSummary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    TopSellingProductsCard(
                        products = topSellingProducts,
                        modifier = Modifier.weight(1f)
                    )

                    PaymentBreakdownCard(
                        paymentBreakdown = paymentBreakdown,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSummarySection(
    dashboardSummary: DashboardSummary
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

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
private fun TopSellingProductsCard(
    products: List<TopSellingProduct>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "Top Products",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Best-selling products for today.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            products.forEach { product ->
                TopSellingProductRow(
                    product = product
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@Composable
private fun TopSellingProductRow(
    product: TopSellingProduct
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.productName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${product.quantitySold} sold",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }

        Text(
            text = "Rs ${product.revenue}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PaymentBreakdownCard(
    paymentBreakdown: List<PaymentBreakdownItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "Payments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Sales grouped by payment method.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            paymentBreakdown.forEach { payment ->
                PaymentBreakdownRow(
                    payment = payment
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@Composable
private fun PaymentBreakdownRow(
    payment: PaymentBreakdownItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = payment.paymentMethod,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${payment.orderCount} order(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }

        Text(
            text = "Rs ${payment.amount}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}