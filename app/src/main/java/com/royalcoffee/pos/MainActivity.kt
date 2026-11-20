package com.royalcoffee.pos

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabaseProvider
import com.royalcoffee.pos.notifications.StockAlertDelivery
import com.royalcoffee.pos.ui.RoyalCoffeeApp
import com.royalcoffee.pos.ui.theme.RoyalCoffeePOSTheme
import com.royalcoffee.pos.util.LanguageHelper
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applySavedLanguage(this)
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
