package com.royalcoffee.pos.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.ui.theme.RoyalGold
import com.royalcoffee.pos.ui.theme.RoyalGreenDark

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit
) {
    var username by remember {
        mutableStateOf("")
    }

    var pin by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    fun attemptLogin() {
        val cleanUsername = username.trim().lowercase()

        when {
            cleanUsername == "admin" && pin == "1234" -> {
                onLoginSuccess(UserRole.ADMIN)
            }

            cleanUsername == "barista" && pin == "1234" -> {
                onLoginSuccess(UserRole.BARISTA)
            }

            else -> {
                errorMessage = "Invalid username or PIN"
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(920.dp)
                    .height(520.dp),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 12.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = RoyalGold.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LoginBrandPanel(
                        modifier = Modifier
                            .weight(0.95f)
                            .fillMaxHeight()
                    )

                    LoginFormPanel(
                        username = username,
                        pin = pin,
                        errorMessage = errorMessage,
                        onUsernameChange = { newValue ->
                            username = newValue
                            errorMessage = null
                        },
                        onPinChange = { newValue ->
                            pin = newValue.filter { it.isDigit() }.take(6)
                            errorMessage = null
                        },
                        onLoginClick = {
                            attemptLogin()
                        },
                        modifier = Modifier
                            .weight(1.05f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginBrandPanel(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RoyalGreenDark,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = RoyalGold.copy(alpha = 0.16f),
                border = BorderStroke(
                    width = 1.dp,
                    color = RoyalGold.copy(alpha = 0.8f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalCafe,
                        contentDescription = "Coffee cup icon",
                        tint = RoyalGold,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Royal Coffee POS",
                style = MaterialTheme.typography.headlineLarge,
                color = RoyalGold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Café Point-of-Sale system",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun LoginFormPanel(
    username: String,
    pin: String,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 56.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in with your username and PIN to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = {
                Text(text = "Username")
            },
            singleLine = true,
            isError = errorMessage != null,
            modifier = Modifier.width(340.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = onPinChange,
            label = {
                Text(text = "PIN")
            },
            singleLine = true,
            isError = errorMessage != null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            modifier = Modifier.width(340.dp)
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onLoginClick,
            enabled = username.isNotBlank() && pin.isNotBlank(),
            modifier = Modifier
                .width(340.dp)
                .height(48.dp)
        ) {
            Text(text = "Log in")
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Demo users: admin / 1234 or barista / 1234",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
    }
}