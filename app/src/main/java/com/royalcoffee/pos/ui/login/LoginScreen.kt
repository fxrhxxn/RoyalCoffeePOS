package com.royalcoffee.pos.ui.login

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royalcoffee.pos.data.local.entity.UserEntity
import com.royalcoffee.pos.model.AuthenticatedUser
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold
import com.royalcoffee.pos.ui.theme.RoyalGreenDark

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (AuthenticatedUser) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("royal_coffee_prefs", Context.MODE_PRIVATE)
    }
    var selectedLanguage by remember {
        mutableStateOf(prefs.getString("selected_language", "English") ?: "English")
    }

    val onLanguageChange: (String) -> Unit = { newLang ->
        selectedLanguage = newLang
        prefs.edit().putString("selected_language", newLang).apply()
    }

    val uiState by viewModel.uiState.collectAsState()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()

    var username by remember {
        mutableStateOf("")
    }
    var pin by remember {
        mutableStateOf("")
    }
    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> onLoginSuccess((uiState as LoginUiState.Success).user)
            is LoginUiState.Error -> errorMessage = (uiState as LoginUiState.Error).message
            else -> {}
        }
    }

    fun attemptLogin() {
        viewModel.login(username, pin)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val outerPadding = when {
                maxWidth < 900.dp -> 24.dp
                maxWidth < 1100.dp -> 36.dp
                else -> 48.dp
            }

            val availableHeight = (maxHeight - outerPadding * 2f).coerceAtLeast(360.dp)

            val cardHeight = when {
                availableHeight < 520.dp -> availableHeight
                else -> 520.dp
            }

            val cardWidthFraction = when {
                maxWidth < 900.dp -> 0.96f
                maxWidth < 1200.dp -> 0.92f
                else -> 0.86f
            }

            val useTwoPaneLayout = maxWidth >= 760.dp

            Card(
                modifier = Modifier
                    .padding(outerPadding)
                    .fillMaxWidth(cardWidthFraction)
                    .widthIn(max = 980.dp)
                    .height(cardHeight),
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
                if (useTwoPaneLayout) {
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
                            users = users,
                            selectedLanguage = selectedLanguage,
                            onLanguageChange = onLanguageChange,
                            errorMessage = errorMessage,
                            uiState = uiState,
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
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LoginBrandPanel(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.8f)
                        )

                        LoginFormPanel(
                            username = username,
                            pin = pin,
                            users = users,
                            selectedLanguage = selectedLanguage,
                            onLanguageChange = onLanguageChange,
                            errorMessage = errorMessage,
                            uiState = uiState,
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
                                .fillMaxWidth()
                                .weight(1.2f)
                        )
                    }
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
                text = "Royal Coffee",
                style = MaterialTheme.typography.headlineLarge,
                color = RoyalGold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Café Point-of-Sale system",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoginFormPanel(
    username: String,
    pin: String,
    users: List<UserEntity>,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    errorMessage: String?,
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome",
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your staff username and PIN to log in.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        UsernameDropdownField(
            username = username,
            users = users,
            isError = errorMessage != null,
            onUsernameChange = onUsernameChange,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
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
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()

                    if (username.isNotBlank() && pin.isNotBlank()) {
                        onLoginClick()
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                onLoginClick()
            },
            enabled = username.isNotBlank() && pin.isNotBlank() && uiState !is LoginUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .height(48.dp)
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Log in")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LanguageSelectorRow(
            selectedLanguage = selectedLanguage,
            onLanguageChange = onLanguageChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsernameDropdownField(
    username: String,
    users: List<UserEntity>,
    isError: Boolean,
    onUsernameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }

    val filteredUsers = remember(users, username) {
        if (username.isBlank()) {
            users
        } else {
            users.filter {
                it.username.contains(username, ignoreCase = true) ||
                it.fullName.contains(username, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { newValue ->
                onUsernameChange(newValue)
                expanded = true
            },
            label = { Text(text = "Username") },
            singleLine = true,
            isError = isError,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    expanded = false
                    focusManager.moveFocus(FocusDirection.Down)
                }
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                .fillMaxWidth()
        )

        if (filteredUsers.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredUsers.forEach { user ->
                    DropdownMenuItem(
                        text = { Text(text = "${user.fullName} — ${user.username}") },
                        onClick = {
                            onUsernameChange(user.username)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSelectorRow(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val languages = listOf("English", "Français")
        languages.forEach { lang ->
            val isSelected = selectedLanguage == lang
            if (isSelected) {
                Button(
                    onClick = { onLanguageChange(lang) },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = lang)
                }
            } else {
                OutlinedButton(
                    onClick = { onLanguageChange(lang) },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = lang)
                }
            }
        }
    }
}
