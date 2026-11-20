package com.royalcoffee.pos.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.royalcoffee.pos.R
import com.royalcoffee.pos.data.local.entity.BusinessEntity
import com.royalcoffee.pos.data.local.entity.UserEntity
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.ui.SettingsViewModel
import com.royalcoffee.pos.ui.theme.CinzelFontFamily

@Composable
fun SettingsScreen(
    userRole: UserRole,
    viewModel: SettingsViewModel,
    selectedLanguage: String,
    receiptFooterText: String,
    onLanguageChange: (String) -> Unit,
    onReceiptFooterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val businessInfo by viewModel.businessInfo.collectAsStateWithLifecycle()
    var showAddUserDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight

            val horizontalPadding = when {
                availableWidth < 700.dp -> 12.dp
                availableWidth < 1000.dp -> 18.dp
                else -> 28.dp
            }
            val verticalPadding = if (availableHeight < 600.dp) 12.dp else 24.dp
            val headerSpacing = if (availableHeight < 600.dp) 16.dp else 28.dp
            val sectionSpacing = if (availableHeight < 600.dp) 14.dp else 20.dp

            val compactLayout = availableWidth < 650.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Settings",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(headerSpacing))

                Column(
                    modifier = Modifier.widthIn(max = 800.dp),
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                ) {
                    LanguageSettingsCard(
                        selectedLanguage = selectedLanguage,
                        onLanguageChange = onLanguageChange,
                        compactLayout = compactLayout
                    )

                    if (userRole == UserRole.ADMIN) {
                        BusinessProfileCard(
                            businessInfo = businessInfo,
                            onUpdate = { viewModel.updateBusinessInfo(it) },
                            compactLayout = compactLayout
                        )

                        StaffManagementCard(
                            users = users,
                            onAddUserClick = { showAddUserDialog = true },
                            onDeleteUser = { viewModel.deleteUser(it) },
                            compactLayout = compactLayout
                        )

                        ReceiptFooterSettingsCard(
                            receiptFooterText = receiptFooterText,
                            onReceiptFooterChange = onReceiptFooterChange,
                            compactLayout = compactLayout
                        )
                    } else {
                        BaristaAccessInfoCard(
                            compactLayout = compactLayout
                        )
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { name, username, pin, role ->
                viewModel.createUser(name, username, pin, role)
                showAddUserDialog = false
            }
        )
    }
}

@Composable
private fun StaffManagementCard(
    users: List<UserEntity>,
    onAddUserClick: () -> Unit,
    onDeleteUser: (UserEntity) -> Unit,
    compactLayout: Boolean
) {
    val cardPadding = if (compactLayout) 14.dp else 22.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            if (compactLayout) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Staff Management",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = onAddUserClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Staff")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Staff Management",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = onAddUserClick) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Staff")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            users.forEach { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${user.username} • ${user.role}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (user.role != UserRole.ADMIN || users.count { it.role == UserRole.ADMIN } > 1) {
                        IconButton(onClick = { onDeleteUser(user) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete ${user.fullName}", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun BusinessProfileCard(
    businessInfo: BusinessEntity?,
    onUpdate: (BusinessEntity) -> Unit,
    compactLayout: Boolean
) {
    var name by remember(businessInfo) { mutableStateOf(businessInfo?.businessName ?: "") }
    var address by remember(businessInfo) { mutableStateOf(businessInfo?.address ?: "") }
    var vat by remember(businessInfo) { mutableStateOf(businessInfo?.vat ?: "") }

    val cardPadding = if (compactLayout) 14.dp else 22.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Text(
                text = "Business Profile",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = vat,
                onValueChange = { vat = it },
                label = { Text("VAT Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    businessInfo?.let {
                        onUpdate(it.copy(businessName = name, address = address, vat = vat))
                    }
                },
                modifier = if (compactLayout) Modifier.fillMaxWidth() else Modifier.align(Alignment.End)
            ) {
                Text("Update Info")
            }
        }
    }
}

@Composable
private fun LanguageSettingsCard(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    compactLayout: Boolean
) {
    var showNotice by remember { mutableStateOf(false) }
    val cardPadding = if (compactLayout) 14.dp else 22.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(18.dp))

            if (compactLayout) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageButton(
                        text = stringResource(R.string.english),
                        isSelected = selectedLanguage == "English",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (selectedLanguage != "English") {
                            onLanguageChange("English")
                            showNotice = true
                        }
                    }
                    LanguageButton(
                        text = stringResource(R.string.french),
                        isSelected = selectedLanguage == "Français" || selectedLanguage == "French",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (selectedLanguage != "Français" && selectedLanguage != "French") {
                            onLanguageChange("Français")
                            showNotice = true
                        }
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LanguageButton(
                        text = stringResource(R.string.english),
                        isSelected = selectedLanguage == "English"
                    ) {
                        if (selectedLanguage != "English") {
                            onLanguageChange("English")
                            showNotice = true
                        }
                    }
                    LanguageButton(
                        text = stringResource(R.string.french),
                        isSelected = selectedLanguage == "Français" || selectedLanguage == "French"
                    ) {
                        if (selectedLanguage != "Français" && selectedLanguage != "French") {
                            onLanguageChange("Français")
                            showNotice = true
                        }
                    }
                }
            }

            if (showNotice) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = stringResource(R.string.language_change_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (isSelected) {
        Button(onClick = onClick, modifier = modifier) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(text) }
    }
}

@Composable
private fun ReceiptFooterSettingsCard(
    receiptFooterText: String,
    onReceiptFooterChange: (String) -> Unit,
    compactLayout: Boolean
) {
    val cardPadding = if (compactLayout) 14.dp else 22.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Text(
                text = "Receipt Footer",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(18.dp))
            OutlinedTextField(
                value = receiptFooterText,
                onValueChange = onReceiptFooterChange,
                label = { Text("Footer message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

@Composable
private fun BaristaAccessInfoCard(
    compactLayout: Boolean
) {
    val cardPadding = if (compactLayout) 14.dp else 22.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Text(
                text = "Limited Access",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "You are limited to the available settings. Other Settings are reserved for Admin Users",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, UserRole) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.BARISTA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Staff",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            BoxWithConstraints {
                val isCompactDialog = maxWidth < 360.dp

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() }) {
                                pin = value
                            }
                        },
                        label = { Text("PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Role",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (isCompactDialog) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = role == UserRole.BARISTA, onClick = { role = UserRole.BARISTA })
                                Text("Barista")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = role == UserRole.ADMIN, onClick = { role = UserRole.ADMIN })
                                Text("Admin")
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = role == UserRole.BARISTA, onClick = { role = UserRole.BARISTA })
                            Text("Barista")
                            Spacer(Modifier.width(16.dp))
                            RadioButton(selected = role == UserRole.ADMIN, onClick = { role = UserRole.ADMIN })
                            Text("Admin")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, username, pin, role) },
                enabled = name.isNotBlank() && username.isNotBlank() && pin.length >= 4
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
