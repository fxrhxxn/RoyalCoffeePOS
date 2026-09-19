package com.royalcoffee.pos.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabaseProvider
import com.royalcoffee.pos.data.local.repository.BusinessRepository
import com.royalcoffee.pos.data.local.repository.UserRepository
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.ui.login.LoginScreen
import com.royalcoffee.pos.ui.login.LoginViewModel
import com.royalcoffee.pos.ui.login.LoginViewModelFactory
import com.royalcoffee.pos.ui.login.RegistrationScreen
import com.royalcoffee.pos.ui.login.RegistrationViewModel
import com.royalcoffee.pos.ui.login.RegistrationViewModelFactory
import com.royalcoffee.pos.ui.sales.SalesScreen

enum class AppAuthState {
    CHECKING,
    REGISTRATION,
    LOGIN,
    AUTHENTICATED
}

@Composable
fun RoyalCoffeeApp() {
    val context = LocalContext.current
    
    val database = remember(context) {
        RoyalCoffeeDatabaseProvider.getDatabase(context)
    }
    
    val userRepository = remember(database) {
        UserRepository(database.userDao())
    }
    
    val businessRepository = remember(database) {
        BusinessRepository(database.businessDao())
    }

    var authState by remember { mutableStateOf(AppAuthState.CHECKING) }
    var loggedInUserRole by remember { mutableStateOf<UserRole?>(null) }

    // Check if any admin exists on startup
    LaunchedEffect(Unit) {
        val userCount = userRepository.getUserCount()
        if (userCount == 0) {
            authState = AppAuthState.REGISTRATION
        } else {
            authState = AppAuthState.LOGIN
        }
    }

    when (authState) {
        AppAuthState.CHECKING -> {
            // Splash or Loading
        }
        AppAuthState.REGISTRATION -> {
            val registrationViewModel: RegistrationViewModel = viewModel(
                factory = RegistrationViewModelFactory(userRepository, businessRepository)
            )
            RegistrationScreen(
                viewModel = registrationViewModel,
                onRegistrationComplete = {
                    authState = AppAuthState.LOGIN
                }
            )
        }
        AppAuthState.LOGIN -> {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(userRepository)
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { role ->
                    loggedInUserRole = role
                    authState = AppAuthState.AUTHENTICATED
                }
            )
        }
        AppAuthState.AUTHENTICATED -> {
            SalesScreen(
                userRole = loggedInUserRole!!,
                onLogout = {
                    loggedInUserRole = null
                    authState = AppAuthState.LOGIN
                }
            )
        }
    }
}
