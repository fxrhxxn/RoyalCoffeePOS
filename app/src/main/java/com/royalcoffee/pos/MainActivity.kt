package com.royalcoffee.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.royalcoffee.pos.ui.RoyalCoffeeApp
import com.royalcoffee.pos.ui.theme.RoyalCoffeePOSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoyalCoffeePOSTheme {
                RoyalCoffeeApp()
            }
        }
    }
}