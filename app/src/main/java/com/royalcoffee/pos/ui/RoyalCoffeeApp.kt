package com.royalcoffee.pos.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabaseProvider
import com.royalcoffee.pos.data.local.repository.BusinessRepository
import com.royalcoffee.pos.data.local.repository.UserRepository
import com.royalcoffee.pos.model.AuthenticatedUser
import com.royalcoffee.pos.ui.login.LoginScreen
import com.royalcoffee.pos.ui.login.LoginViewModel
import com.royalcoffee.pos.ui.login.LoginViewModelFactory
import com.royalcoffee.pos.ui.login.RegistrationScreen
import com.royalcoffee.pos.ui.login.RegistrationViewModel
import com.royalcoffee.pos.ui.login.RegistrationViewModelFactory
import com.royalcoffee.pos.ui.login.WelcomeScreen
import com.royalcoffee.pos.ui.sales.SalesScreen

/**
 * Represents the authentication/setup state of the application.
 *
 * CHECKING:
 * The app is checking the local Room database.
 *
 * WELCOME:
 * The app has never been configured before.
 *
 * REGISTRATION:
 * The user is completing first-time setup.
 *
 * LOGIN:
 * Existing users can log into the POS.
 *
 * AUTHENTICATED:
 * A valid user has logged in successfully.
 */
enum class AppAuthState {
    CHECKING,
    WELCOME,
    REGISTRATION,
    LOGIN,
    AUTHENTICATED
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RoyalCoffeeApp() {

    val context = LocalContext.current

    /*
     * Obtain the Room database instance.
     *
     * remember prevents the database instance from being recreated
     * every time Compose recomposes this screen.
     */
    val database = remember(context) {
        RoyalCoffeeDatabaseProvider.getDatabase(context)
    }

    // Repository responsible for users.
    val userRepository = remember(database) {
        UserRepository(
            database.userDao()
        )
    }

    // Repository responsible for business information.
    val businessRepository = remember(database) {
        BusinessRepository(
            database.businessDao()
        )
    }

    // Current application authentication state.
    var authState by remember {
        mutableStateOf(AppAuthState.CHECKING)
    }

    // Stores the identity snapshot of the user who successfully logged in.
    var loggedInUser by remember {
        mutableStateOf<AuthenticatedUser?>(null)
    }

    /*
     * Runs once when the application starts.
     * We check whether any user has already been created.
     *
     * No users:
     *      This is the first launch.
     *      Show the Welcome screen.
     *
     * Existing user:
     *      Setup has already been completed.
     *      Show the Login screen.
     */
    LaunchedEffect(Unit) {
        val userCount = userRepository.getUserCount()
        authState =
            if (userCount == 0) {
                AppAuthState.WELCOME
            } else {
                AppAuthState.LOGIN
            }
    }

    // AnimatedContent allows us to animate between the different authentication/setup screens.
    // The Welcome screen slides left while Registration enters from the right.
    AnimatedContent(
        targetState = authState,
        transitionSpec = {
            // Moving forward into setup.
            if (
                initialState == AppAuthState.WELCOME &&
                targetState == AppAuthState.REGISTRATION
            ) {
                (
                        slideInHorizontally(
                            initialOffsetX = { fullWidth ->
                                fullWidth
                            }
                        ) + fadeIn()
                        ).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth ->
                                -fullWidth / 3
                            }
                        ) + fadeOut()
                    )

            } else {
                // Normal transitions between the other application states.
                fadeIn()
                    .togetherWith(
                        fadeOut()
                    )
            }
        },

        label = "RoyalCoffeeAuthTransition"

    ) { currentState ->

        when (currentState) {
            // The app is checking whether first-time setup has already been completed.
            // This normally lasts only for a very short moment.
            AppAuthState.CHECKING -> {
                // Can later be replaced with a loading screen.
            }
            // True first-launch screen.
            AppAuthState.WELCOME -> {
                WelcomeScreen(
                    onGetStarted = {
                        // The user pressed the Get Started button and
                        // moves to the Registration Screen with a smooth animation
                        authState = AppAuthState.REGISTRATION
                    }
                )
            }
            // First-time registration.
            AppAuthState.REGISTRATION -> {

                val registrationViewModel: RegistrationViewModel =
                    viewModel(
                        factory = RegistrationViewModelFactory(
                            userRepository,
                            businessRepository
                        )
                    )

                RegistrationScreen(
                    viewModel = registrationViewModel,
                    onRegistrationComplete = {
                        /*
                         * Registration successfully created:
                         * - Business profile
                         * - First administrator
                         * Move to Login.
                         */
                        authState = AppAuthState.LOGIN
                    }
                )
            }

            //  Normal login screen after setup has already been completed
            AppAuthState.LOGIN -> {
                val loginViewModel: LoginViewModel =
                    viewModel(
                        factory = LoginViewModelFactory(
                            userRepository
                        )
                    )

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { user ->
                        // Keep the authenticated identity in memory.
                        loggedInUser = user
                        // Move into the main POS application.
                        authState =
                            AppAuthState.AUTHENTICATED
                    }
                )
            }

            /*
             * User is successfully logged in.
             */
            AppAuthState.AUTHENTICATED -> {
                /*
                 * loggedInUser should always exist here because
                 * AUTHENTICATED can only be reached after successful login.
                 */
                val user = loggedInUser
                if (user != null) {
                    AuthenticatedSession(
                        authenticatedUser = user,
                        onLogout = {
                            // Clear the current session and return to login screen
                            loggedInUser = null
                            authState =
                                AppAuthState.LOGIN
                        }
                    )
                }
            }
        }
    }
}


/** Own all authenticated ViewModels for one login and release them on logout. */
@Composable
private fun AuthenticatedSession(authenticatedUser: AuthenticatedUser, onLogout: () -> Unit) {
    val owner = remember(authenticatedUser.sessionId) {
        object : androidx.lifecycle.ViewModelStoreOwner {
            override val viewModelStore = androidx.lifecycle.ViewModelStore()
        }
    }
    androidx.compose.runtime.DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner provides owner
    ) {
        SalesScreen(authenticatedUser = authenticatedUser, onLogout = onLogout)
    }
}
