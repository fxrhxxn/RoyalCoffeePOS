package com.royalcoffee.pos.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabase
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NotificationFilterType(val label: String) {
    ALL("All"),
    LOW_STOCK("Low Stock"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED_STOCK("Expired Stock")
}

enum class NotificationCategory(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector
) {
    LOW_STOCK(
        label = "Low Stock",
        containerColor = Color(0xFFFFF3E0),
        contentColor = Color(0xFFE65100),
        icon = Icons.AutoMirrored.Outlined.TrendingDown
    ),
    EXPIRING_SOON(
        label = "Expiring Soon",
        containerColor = Color(0xFFFFF8E1),
        contentColor = Color(0xFFF57F17),
        icon = Icons.Outlined.Schedule
    ),
    EXPIRED_STOCK(
        label = "Expired Stock",
        containerColor = Color(0xFFFFEBEE),
        contentColor = Color(0xFFC62828),
        icon = Icons.Outlined.EventBusy
    )
}

fun StockAlert.getCategory(): NotificationCategory {
    return when {
        alertKey.startsWith("low:") -> NotificationCategory.LOW_STOCK
        alertKey.contains("Expired") || message.startsWith("Expired:") -> NotificationCategory.EXPIRED_STOCK
        alertKey.contains("Expiring soon") || message.startsWith("Expiring soon:") -> NotificationCategory.EXPIRING_SOON
        else -> NotificationCategory.LOW_STOCK
    }
}

@Composable
fun StockAlertsScreen(
    database: RoyalCoffeeDatabase,
    isAdmin: Boolean = true,
    modifier: Modifier = Modifier
) {
    val alerts by database.stockAlertDao().observeAlerts().collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf(NotificationFilterType.ALL) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val filteredAlerts = remember(alerts, selectedFilter) {
        when (selectedFilter) {
            NotificationFilterType.ALL -> alerts
            NotificationFilterType.LOW_STOCK -> alerts.filter { it.getCategory() == NotificationCategory.LOW_STOCK }
            NotificationFilterType.EXPIRING_SOON -> alerts.filter { it.getCategory() == NotificationCategory.EXPIRING_SOON }
            NotificationFilterType.EXPIRED_STOCK -> alerts.filter { it.getCategory() == NotificationCategory.EXPIRED_STOCK }
        }
    }

    val lowStockCount = remember(alerts) { alerts.count { it.getCategory() == NotificationCategory.LOW_STOCK } }
    val expiringSoonCount = remember(alerts) { alerts.count { it.getCategory() == NotificationCategory.EXPIRING_SOON } }
    val expiredCount = remember(alerts) { alerts.count { it.getCategory() == NotificationCategory.EXPIRED_STOCK } }

    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight

            val horizontalPadding = when {
                availableWidth < 700.dp -> 12.dp
                availableWidth < 1000.dp -> 18.dp
                else -> 28.dp
            }
            val verticalPadding = if (availableHeight < 600.dp) 12.dp else 24.dp
            val sectionSpacing = if (availableHeight < 600.dp) 12.dp else 16.dp

            val compactLayout = availableWidth < 650.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            ) {
                // Header Row / Column
                if (compactLayout) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Notifications",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Stock alerts and expiration history",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        if (alerts.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showClearAllDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear All")
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Notifications",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Stock alerts and expiration history",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        if (alerts.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showClearAllDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear All")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))

                NotificationPermission()

                Spacer(modifier = Modifier.height(sectionSpacing))

                // Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(NotificationFilterType.entries.toTypedArray()) { filter ->
                        val count = when (filter) {
                            NotificationFilterType.ALL -> alerts.size
                            NotificationFilterType.LOW_STOCK -> lowStockCount
                            NotificationFilterType.EXPIRING_SOON -> expiringSoonCount
                            NotificationFilterType.EXPIRED_STOCK -> expiredCount
                        }
                        val isSelected = selectedFilter == filter

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text("${filter.label} ($count)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))

                // Notification List or Empty State
                if (filteredAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (alerts.isEmpty()) Icons.Outlined.CheckCircleOutline else Icons.Outlined.NotificationsOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (alerts.isEmpty()) "No notifications" else "No matching notifications",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (alerts.isEmpty())
                                    "Your stock status is healthy and up to date."
                                else
                                    "There are no notifications in the ${selectedFilter.label} category.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredAlerts, key = { it.alertKey }) { alert ->
                            NotificationCard(
                                alert = alert,
                                compactLayout = compactLayout,
                                onDelete = {
                                    scope.launch {
                                        database.stockAlertDao().deleteAlert(alert.alertKey)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Text(text = "Clear All Notifications?")
            },
            text = {
                Text(text = "Are you sure you want to delete all notification history? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            database.stockAlertDao().clearAllAlerts()
                        }
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = "Clear All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearAllDialog = false }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
private fun NotificationCard(
    alert: StockAlert,
    compactLayout: Boolean,
    onDelete: () -> Unit
) {
    val category = alert.getCategory()
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()) }
    val formattedTime = remember(alert.createdAt) { dateFormat.format(Date(alert.createdAt)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        if (compactLayout) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Top row: Icon, Category & Active/Resolved badges, Delete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = category.containerColor
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.label,
                                tint = category.contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = category.containerColor
                        ) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = category.contentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (alert.active)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (alert.active) "Active" else "Resolved",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (alert.active)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete notification",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Alert Message (untruncated, natural wrapping)
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Timestamp
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Badge
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = category.containerColor
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.label,
                            tint = category.contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = category.containerColor
                        ) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = category.contentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (alert.active)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (alert.active) "Active" else "Resolved",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (alert.active)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Delete Action
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete notification",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
