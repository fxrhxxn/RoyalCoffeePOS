package com.royalcoffee.pos.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold
import com.royalcoffee.pos.ui.theme.RoyalGreenDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onRegistrationComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableStateOf(1) }

    // Step 1: Business Info
    var businessName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var brn by remember { mutableStateOf("") }
    var vat by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Step 2: Admin Credentials
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState) {
        if (uiState is RegistrationUiState.Success) {
            onRegistrationComplete()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Initial Setup",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalGreenDark,
                    titleContentColor = RoyalGold
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(RoyalGreenDark.copy(alpha = 0.05f), MaterialTheme.colorScheme.background)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(if (Modifier.fillMaxSize().let { true }) 0.9f else 0.9f) // responsive width would be better
                    .widthIn(max = 600.dp)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepIndicator(step = 1, active = currentStep >= 1, label = "Business")
                    HorizontalDivider(modifier = Modifier.width(40.dp).padding(horizontal = 8.dp), color = if (currentStep > 1) RoyalGold else MaterialTheme.colorScheme.outlineVariant)
                    StepIndicator(step = 2, active = currentStep >= 2, label = "Admin")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        if (currentStep == 1) {
                            BusinessInfoStep(
                                businessName = businessName, onBusinessNameChange = { businessName = it },
                                address = address, onAddressChange = { address = it },
                                phone = phone, onPhoneChange = { phone = it },
                                brn = brn, onBrnChange = { brn = it },
                                vat = vat, onVatChange = { vat = it },
                                country = country, onCountryChange = { country = it },
                                email = email, onEmailChange = { email = it }
                            )
                        } else {
                            AdminCredentialsStep(
                                fullName = fullName, onFullNameChange = { fullName = it },
                                username = username, onUsernameChange = { username = it },
                                pin = pin, onPinChange = { pin = it },
                                confirmPin = confirmPin, onConfirmPinChange = { confirmPin = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        if (uiState is RegistrationUiState.Error) {
                            Text(
                                text = (uiState as RegistrationUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentStep == 2) {
                                OutlinedButton(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Back")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                onClick = {
                                    if (currentStep == 1) {
                                        currentStep = 2
                                    } else {
                                        viewModel.register(
                                            fullName, username, pin, businessName,
                                            address, phone, brn, vat, country, email
                                        )
                                    }
                                },
                                enabled = if (currentStep == 1) {
                                    businessName.isNotBlank() && email.isNotBlank()
                                } else {
                                    fullName.isNotBlank() && username.isNotBlank() && pin.length >= 4 && pin == confirmPin
                                },
                                modifier = Modifier.height(50.dp)
                            ) {
                                if (uiState is RegistrationUiState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text(if (currentStep == 1) "Next" else "Complete Setup")
                                    Spacer(Modifier.width(8.dp))
                                    Icon(if (currentStep == 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(step: Int, active: Boolean, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(20.dp),
            color = if (active) RoyalGreenDark else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (active) RoyalGold else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(step.toString(), fontWeight = FontWeight.Bold)
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) RoyalGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun BusinessInfoStep(
    businessName: String, onBusinessNameChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    brn: String, onBrnChange: (String) -> Unit,
    vat: String, onVatChange: (String) -> Unit,
    country: String, onCountryChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit
) {
    Column {
        Text("Business Details", style = MaterialTheme.typography.titleLarge, color = RoyalGreenDark)
        Text("Legal info for receipts and reports", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = businessName, onValueChange = onBusinessNameChange,
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Business, null) }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = address, onValueChange = onAddressChange,
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = phone, onValueChange = onPhoneChange,
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = brn, onValueChange = onBrnChange,
                label = { Text("BRN") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            OutlinedTextField(
                value = vat, onValueChange = onVatChange,
                label = { Text("VAT") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = country, onValueChange = onCountryChange,
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email, onValueChange = onEmailChange,
            label = { Text("Business Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
    }
}

@Composable
fun AdminCredentialsStep(
    fullName: String, onFullNameChange: (String) -> Unit,
    username: String, onUsernameChange: (String) -> Unit,
    pin: String, onPinChange: (String) -> Unit,
    confirmPin: String, onConfirmPinChange: (String) -> Unit
) {
    Column {
        Text("Admin Account", style = MaterialTheme.typography.titleLarge, color = RoyalGreenDark)
        Text("Create the account you will use to log in.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName, onValueChange = onFullNameChange,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) },
            supportingText = { Text("Example: Mungada Christa") }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = username, onValueChange = onUsernameChange,
            label = { Text("Login Username") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("This is what you type to log in (e.g., 'admin')") }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = pin, onValueChange = onPinChange,
            label = { Text("Security PIN") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            supportingText = { Text("Minimum 4 digits (e.g., '0102')") }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPin, onValueChange = onConfirmPinChange,
            label = { Text("Confirm PIN") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = pin.isNotEmpty() && confirmPin.isNotEmpty() && pin != confirmPin
        )
    }
}
