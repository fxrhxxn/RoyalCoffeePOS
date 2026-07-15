package com.royalcoffee.pos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.ui.login.LoginScreen
import com.royalcoffee.pos.ui.sales.SalesScreen

@Composable
fun RoyalCoffeeApp() {
    var loggedInUserRole by remember {
        mutableStateOf<UserRole?>(null)
    }

    if (loggedInUserRole == null) {
        LoginScreen(
            onLoginSuccess = { role ->
                loggedInUserRole = role
            }
        )
    } else {
        SalesScreen(
            userRole = loggedInUserRole!!,
            onLogout = {
                loggedInUserRole = null
            }
        )
    }
}