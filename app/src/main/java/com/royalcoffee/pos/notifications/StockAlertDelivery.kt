package com.royalcoffee.pos.notifications

import android.app.*
import android.app.job.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.royalcoffee.pos.MainActivity
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabaseProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object StockAlertDelivery {
    private val mutex = Mutex()
    suspend fun check(context: Context) = mutex.withLock {
        val db = RoyalCoffeeDatabaseProvider.getDatabase(context)
        db.refreshStockAlerts()
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("stock", "Stock and expiry", NotificationManager.IMPORTANCE_DEFAULT))
        if (!manager.areNotificationsEnabled()) return@withLock
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@withLock
        for (alert in db.stockAlertDao().pending()) {
            val intent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val notification = NotificationCompat.Builder(context, "stock")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Royal Coffee stock alert")
                .setContentText(alert.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
                .setContentIntent(intent).setAutoCancel(true).setOnlyAlertOnce(true).build()
            try {
                manager.notify(alert.alertKey, 0, notification)
                db.stockAlertDao().delivered(alert.alertKey)
            } catch (_: SecurityException) { return@withLock }
        }
    }
    fun schedule(context: Context) {
        val scheduler = context.getSystemService(JobScheduler::class.java)
        val job = JobInfo.Builder(7301, ComponentName(context, StockAlertJob::class.java))
            .setPeriodic(6 * 60 * 60 * 1000L).setPersisted(true).build()
        scheduler.schedule(job)
    }
}

class StockAlertJob : JobService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    override fun onStartJob(params: JobParameters): Boolean {
        job = scope.launch {
            try {
                StockAlertDelivery.check(applicationContext)
                jobFinished(params, false)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { jobFinished(params, true) }
        }
        return true
    }
    override fun onStopJob(params: JobParameters): Boolean { job?.cancel(); return true }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
