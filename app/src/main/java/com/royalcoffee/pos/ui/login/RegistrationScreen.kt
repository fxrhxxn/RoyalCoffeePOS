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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onRegistrationComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableStateOf(1) }

    val scrollState = rememberScrollState()

    // Step 1: Business Info
    var businessName by remember { mutableStateOf("Royal Coffee") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var brn by remember { mutableStateOf("") }
    var vat by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Mauritius") } // Default country set to Mauritius
    var email by remember { mutableStateOf("") }

    // Tracks whether the user attempts to continue
    var hasAttemptedNext by remember { mutableStateOf(false) }

    // Recalculate validation results whether the relevant business fields change
    val businessValidation = BusinessRegistrationValidator.validate(
        businessName = businessName,
        brn = brn,
        vat = vat,
        country = country,
        email = email
    )

    // Step 2: Admin Credentials
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    // Admin Account Validation
    val isAdminInformationValid =
        fullName.isNotBlank() &&
                username.isNotBlank() &&
                pin.length >= 4 &&
                pin.all { it.isDigit() } &&
                pin == confirmPin

    // Reset any previous registration state when the screen is open
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    // When successful registration, navigate to the home screen
    LaunchedEffect(uiState) {
        if (uiState is RegistrationUiState.Success) {
            onRegistrationComplete()
        }
    }

    Scaffold(
        // Registration Screen header
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Welcome to the Registration Page",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
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
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 600.dp)
                    .padding(24.dp)
                    .imePadding()
                    .verticalScroll(scrollState),
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

                Spacer(modifier = Modifier.height(48.dp))

                // Registration card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Display either Business information or admin account depending on currentStep
                        if (currentStep == 1) {
                            BusinessInfoStep(
                                businessName = businessName, onBusinessNameChange = { businessName = it },
                                address = address, onAddressChange = { address = it },
                                phone = phone, onPhoneChange = { phone = it },
                                brn = brn, onBrnChange = { brn = it },
                                vat = vat, onVatChange = { vat = it },
                                country = country, onCountryChange = { country = it },
                                email = email, onEmailChange = { email = it },

                                // Show Validation errors
                                validationErrors = if (hasAttemptedNext) {
                                    businessValidation
                                } else {
                                    BusinessValidationErrors()
                                }
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

                        // ViewModel Error Message
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back when on step 2
                            if (currentStep == 2) {
                                OutlinedButton(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    Spacer(Modifier.width(8.dp))
                                    Text("Back")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                onClick = {
                                    if (currentStep == 1) {
                                        // The user attempts to move to the next screen
                                        // The error message becomes visible if there are validation errors
                                        hasAttemptedNext = true
                                        // Move to the next screen if there are no validation errors
                                        if (businessValidation.isValid) {
                                            currentStep = 2
                                        }
                                    } else {
                                        // Validate business information once the user has completed the registration
                                        // The ViewModel performs the validation
                                        if (!businessValidation.isValid) {
                                            hasAttemptedNext = true
                                            // Returns to step 1
                                            currentStep = 1
                                    } else {
                                        // Move to the next screen
                                        viewModel.register(
                                            fullName = fullName.trim(),
                                            username = username.trim(),
                                            pin = pin,
                                            businessName = businessName.trim(),
                                            address = address.trim(),
                                            phone = phone.trim(),
                                            brn = brn.trim().uppercase(),
                                            vat = vat.trim(),
                                            country = country.trim(),
                                            email = email
                                        )
                                            }
                                    }
                                },
                                /*
                                enabled = if (currentStep == 1) {
                                    businessName.isNotBlank() && email.isNotBlank()
                                } else {
                                    fullName.isNotBlank() && username.isNotBlank() && pin.length >= 4 && pin == confirmPin
                                },
                                modifier = Modifier.height(50.dp)
                                */
                                enabled = uiState !is RegistrationUiState.Loading &&
                                        (currentStep == 1 || (currentStep == 2 &&
                                                        fullName.isNotBlank() &&
                                                        username.isNotBlank() &&
                                                        pin.length >=4 &&
                                                        pin == confirmPin &&
                                                        businessValidation.isValid)),
                                modifier = Modifier.height(50.dp)
                            ) {
                                if (uiState is RegistrationUiState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Text(if (currentStep == 1) "Next" else "Complete Setup")
                                    Spacer(Modifier.width(8.dp))
                                    Icon(if (currentStep == 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check, contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


// Display the numbered progress indicator used above registration card
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
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) RoyalGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Registration - Step 1:
// Collect Business information from the user to be saved in Room Database later
@Composable
fun BusinessInfoStep(
    businessName: String, onBusinessNameChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    brn: String, onBrnChange: (String) -> Unit,
    vat: String, onVatChange: (String) -> Unit,
    country: String, onCountryChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    validationErrors: BusinessValidationErrors
) {
    Column {
        Text(
            text = "Business Details",
            style = MaterialTheme.typography.titleLarge,
            color = RoyalGreenDark
        )
        Text(
            text = "Legal info for receipts and reports",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        // Business Name
        // Default Value - Royal Coffee
        // User may edit this field to modify the business name
        OutlinedTextField(
            value = businessName,
            onValueChange = { onBusinessNameChange(it)},
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
            leadingIcon = { Icon(Icons.Default.Business, null) },

            singleLine = true,
            isError = validationErrors.businessName != null,
            supportingText = {
                Text(
                    text = validationErrors.businessName ?: "Example: Royal Coffee - Ebene"
                )
            }
        )

        // Business Address
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { onAddressChange(it)},
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused()
        )
        Spacer(Modifier.height(12.dp))

        // Business Phone Number
        OutlinedTextField(
            value = phone,
            onValueChange = { newValue ->
                // Allow digits and common characters
                if (newValue.all {
                    character ->
                        character.isDigit() ||
                                character == '+' ||
                                character == '-' ||
                                character == ' ' ||
                                character == '(' ||
                                character == ')' }
                    ){ onPhoneChange(newValue)
                }
            },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            )
        )
        Spacer(Modifier.height(12.dp))

        // BRN and VAT
        // Display both next to each other
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = brn,
                onValueChange = { newValue ->
                    if(newValue.length <= 10) {
                        onBrnChange(newValue.uppercase())
                    }
                },
                label = { Text("BRN") },
                modifier = Modifier.weight(1f).keepVisibleWhenFocused(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii
                ),
                isError = validationErrors.brn != null,
                supportingText = { Text(text = validationErrors.brn ?: "Example: C123456789") }
            )
            Spacer(Modifier.width(12.dp))

            // VAT
            OutlinedTextField(
                value = vat,
                onValueChange = { newValue ->
                    // Only allow digits of 8 characters or less
                    if (newValue.length <= 8 && newValue.all { it.isDigit() }) {
                        onVatChange(newValue)
                    }
                },
                label = { Text("VAT") },
                modifier = Modifier.weight(1f).keepVisibleWhenFocused(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = validationErrors.vat != null,
                supportingText = { Text(text = validationErrors.vat ?: "Enter a valid 8 digit VAT number") }
            )
        }
        Spacer(Modifier.height(12.dp))

        // Country
        OutlinedTextField(
            value = country,
            onValueChange = { onCountryChange(it)},
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
            singleLine = true,
            isError = validationErrors.country != null,
            supportingText = {
                validationErrors.country?.let { errorMessage -> Text(text = errorMessage) }
            }
        )
        Spacer(Modifier.height(12.dp))

        // Business Email
        OutlinedTextField(
            value = email,
            onValueChange = {onEmailChange(it)},
            label = { Text("Business Email") },
            modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            isError = validationErrors.email != null,
            supportingText = {
                Text(
                    text = validationErrors.email ?: "Enter valid email: johndoe@example.com"
                )
            }
        )
    }
}



// Create the first admin account of the application
@Composable
fun AdminCredentialsStep(
    fullName: String, onFullNameChange: (String) -> Unit,
    username: String, onUsernameChange: (String) -> Unit,
    pin: String, onPinChange: (String) -> Unit,
    confirmPin: String, onConfirmPinChange: (String) -> Unit
) {
    // Used to determine whether confirm pin field should display an error
    val pinsDoNotMatch = pin.isNotEmpty() && confirmPin.isNotEmpty() && pin != confirmPin


    Column {
        Text("Admin Account", style = MaterialTheme.typography.titleLarge, color = RoyalGreenDark)
        Text("Create first Admin account", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        // Full Name
        OutlinedTextField(
            value = fullName,
            onValueChange = { onFullNameChange(it)},
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
            leadingIcon = { Icon(Icons.Default.Person, null) },
            supportingText = { Text("Example: John Doe") } // { Text("Example: Mungada Christa") }
        )
        Spacer(Modifier.height(12.dp))


        // Username
        OutlinedTextField(
            value = username, onValueChange = onUsernameChange,
            label = { Text("Login Username") },
            modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
            supportingText = { Text("Username used for login in. Example: JohnDoe") } // { Text("This is what you type to log in (e.g., 'admin')") }
        )
        Spacer(Modifier.height(12.dp))

        // Pin
        OutlinedTextField(
            value = pin,
            onValueChange = { newPin ->
                // Pin contains only digits
                if (newPin.all { it.isDigit() }) {
                    onPinChange(newPin)
                }
            },
            label = { Text("Security PIN") },
            modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            supportingText = {
                if (pin.isNotEmpty() && pin.length < 4) {
                    Text( text = "Entered pin must contain 4 digits minimum")
                } else {
                    Text(text = "Pin must contain 4 digits minimum")
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        // Pin confirmation
        OutlinedTextField(
            value = confirmPin,
            onValueChange = { newPin ->
                // Confirm pin contains only digits
                if (newPin.all { it.isDigit() }
                ) {

                onConfirmPinChange(newPin)
                }
            },
            label = { Text("Confirm PIN") },
            modifier = Modifier.fillMaxWidth().keepVisibleWhenFocused(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType =
                    KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            isError = pinsDoNotMatch,

            supportingText = {
                when {
                    pinsDoNotMatch -> {
                        Text(text = "Pins do not match")
                    }

                    confirmPin.isNotEmpty() && pin == confirmPin -> {
                        Text(text = "Pins match")
                    } else -> {
                        Text(text = "Re enter security pin")
                    }
                }
            }
        )
    }
}

/**
 * Keeps a focused input field visible when the software keyboard opens.
 *
 * The field asks its scrollable parent to move it into view whenever
 * it receives focus.
 */
@Composable
private fun Modifier.keepVisibleWhenFocused(): Modifier {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    return this
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                coroutineScope.launch {
                    // Small delay gives the keyboard time to start opening
                    // before calculating the field's visible position.
                    delay(200)
                    bringIntoViewRequester.bringIntoView()
                }
            }
        }
}

