package com.royalcoffee.pos

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import com.royalcoffee.pos.notifications.StockAlertDelivery
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabaseProvider
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.royalcoffee.pos.ui.RoyalCoffeeApp
import com.royalcoffee.pos.ui.theme.RoyalCoffeePOSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StockAlertDelivery.schedule(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val db = RoyalCoffeeDatabaseProvider.getDatabase(applicationContext)
                combine(db.productDao().getAllProducts(), db.stockAlertDao().observeBatches()) { products, batches -> products to batches }
                    .collect { StockAlertDelivery.check(applicationContext) }
            }
        }
        setContent {
            RoyalCoffeePOSTheme {
                RoyalCoffeeApp()
            }
        }
    }
}
