package com.royalcoffee.pos.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun StockAlertsScreen(database: RoyalCoffeeDatabase, isAdmin: Boolean, modifier: Modifier = Modifier) {
    val alerts by database.stockAlertDao().observeAlerts().collectAsStateWithLifecycle(emptyList())
    val batches by database.stockAlertDao().observeBatches().collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<NamedBatch?>(null) }
    var date by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    LazyColumn(modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Notifications", style = MaterialTheme.typography.headlineMedium) }
        item { Text("Expiry warnings start 14 days before expiry. Expired stock is marked for review; sales remain available.") }
        if (alerts.isEmpty()) item { Text("No stock alerts yet.") }
        items(alerts, key = { it.alertKey }) { alert ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text(alert.message)
                Text(if (alert.active) "Active" else "Resolved", style = MaterialTheme.typography.labelMedium)
                Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(alert.createdAt)), style = MaterialTheme.typography.labelSmall)
            } }
        }
        item { Text("Stock batches", style = MaterialTheme.typography.titleLarge) }
        items(batches, key = { it.id }) { batch ->
            val expiry = batch.expiryDay?.let(LocalDate::ofEpochDay)
            Column {
                Text("${batch.productName} · batch ${batch.id} · ${batch.remaining} units")
                Text(if (expiry == null) "Expiry unknown" else "Expiry $expiry" + if (expiry.isBefore(LocalDate.now())) " · EXPIRED" else "")
                if (isAdmin) TextButton(onClick = { editing = batch; date = expiry?.toString().orEmpty(); error = null }) { Text("Edit expiry") }
            }
        }
    }
    editing?.let { batch ->
        AlertDialog(onDismissRequest = { editing = null }, title = { Text("Batch ${batch.id} expiry") }, text = {
            Column {
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("YYYY-MM-DD (optional)") }, singleLine = true)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }, confirmButton = { TextButton(onClick = {
            val day = if (date.isBlank()) null else runCatching { LocalDate.parse(date.trim()).toEpochDay() }.getOrNull()
            if (date.isNotBlank() && day == null) error = "Enter a date such as 2026-12-31."
            else scope.launch { database.stockAlertDao().updateExpiry(batch.id, day); editing = null }
        }) { Text("Save") } }, dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } })
    }
}
