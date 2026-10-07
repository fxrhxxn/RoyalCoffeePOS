package com.royalcoffee.pos.notifications

import androidx.room.*
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Entity(tableName = "stock_batches", indices = [Index("productId")])
data class StockBatch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Int,
    val remaining: Int,
    val expiryDay: Long? = null,
    val receivedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_alerts")
data class StockAlert(
    @PrimaryKey val alertKey: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
    val active: Boolean = true,
    val delivered: Boolean = false
)

data class NamedBatch(val id: Long, val productId: Int, val remaining: Int, val expiryDay: Long?, val productName: String)

@Dao
interface StockAlertDao {
    @Insert suspend fun insertBatch(batch: StockBatch)
    @Query("SELECT * FROM stock_batches WHERE productId = :productId AND remaining > 0 ORDER BY expiryDay IS NULL, expiryDay, receivedAt, id")
    suspend fun batches(productId: Int): List<StockBatch>
    @Query("UPDATE stock_batches SET remaining = :remaining WHERE id = :id")
    suspend fun updateRemaining(id: Long, remaining: Int)
    @Query("UPDATE stock_batches SET expiryDay = :day WHERE id = :id")
    suspend fun updateExpiry(id: Long, day: Long?)
    @Query("SELECT b.id,b.productId,b.remaining,b.expiryDay,p.product_name AS productName FROM stock_batches b JOIN products p ON p.product_id=b.productId WHERE b.remaining>0 AND p.is_active=1 ORDER BY b.expiryDay IS NULL,b.expiryDay,b.id")
    fun observeBatches(): Flow<List<NamedBatch>>
    @Query("SELECT * FROM stock_alerts ORDER BY createdAt DESC")
    fun observeAlerts(): Flow<List<StockAlert>>
    @Query("SELECT * FROM stock_alerts WHERE active=1")
    suspend fun activeAlerts(): List<StockAlert>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putAlert(alert: StockAlert)
    @Query("DELETE FROM stock_alerts WHERE alertKey=:key") suspend fun resolve(key: String)
    @Query("UPDATE stock_alerts SET delivered=1 WHERE alertKey=:key") suspend fun delivered(key: String)
    @Query("SELECT * FROM stock_alerts WHERE active=1 AND delivered=0") suspend fun pending(): List<StockAlert>
}

/** Called inside the same Room transaction as the aggregate stock change. */
suspend fun RoyalCoffeeDatabase.consumeBatches(productId: Int, quantity: Int, previousStock: Int) {
    val dao = stockAlertDao()
    val known = dao.batches(productId).sumOf { it.remaining }
    check(known <= previousStock) { "Batch stock exceeds product stock" }
    if (known < previousStock) dao.insertBatch(StockBatch(productId = productId, remaining = previousStock - known))
    var left = quantity
    for (batch in dao.batches(productId)) {
        val used = minOf(left, batch.remaining)
        dao.updateRemaining(batch.id, batch.remaining - used)
        left -= used
        if (left == 0) break
    }
    check(left == 0) { "Insufficient batch stock" }
}

/** Reconciliation is transactional: concurrent UI and scheduled checks cannot duplicate alerts. */
suspend fun RoyalCoffeeDatabase.refreshStockAlerts(today: Long = LocalDate.now().toEpochDay()) = withTransaction {
    val dao = stockAlertDao()
    val desired = linkedMapOf<String, String>()
    for (product in productDao().alertProducts()) {
        if (product.stockQuantity <= product.reorderLevel) {
            desired["low:${product.productId}"] = "${product.productName}: ${product.stockQuantity} units left (reorder level ${product.reorderLevel})."
        }
        for (batch in dao.batches(product.productId)) {
            val expiry = batch.expiryDay ?: continue
            if (expiry <= today + 14) {
                val state = if (expiry < today) "Expired" else "Expiring soon"
                desired["expiry:${batch.id}:$expiry:$state"] = "$state: ${product.productName}, batch ${batch.id}, ${batch.remaining} units, expiry ${LocalDate.ofEpochDay(expiry)}."
            }
        }
    }
    val active = dao.activeAlerts().associateBy { it.alertKey }
    for ((key, message) in desired) {
        val existing = active[key]
        if (existing == null) {
            // A new episode retains its own history entry, including after a restock.
            dao.putAlert(StockAlert(alertKey = key, message = message))
        } else if (existing.message != message) dao.putAlert(existing.copy(message = message))
    }
    for ((key, alert) in active) if (key !in desired) {
        dao.putAlert(alert.copy(alertKey = "$key:history:${alert.createdAt}", active = false))
        dao.resolve(key)
    }
}
