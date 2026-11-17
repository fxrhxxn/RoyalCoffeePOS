package com.royalcoffee.pos.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
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
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
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

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier.widthIn(max = 800.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LanguageSettingsCard(
                    selectedLanguage = selectedLanguage,
                    onLanguageChange = onLanguageChange
                )

                if (userRole == UserRole.ADMIN) {
                    BusinessProfileCard(
                        businessInfo = businessInfo,
                        onUpdate = { viewModel.updateBusinessInfo(it) }
                    )

                    StaffManagementCard(
                        users = users,
                        onAddUserClick = { showAddUserDialog = true },
                        onDeleteUser = { viewModel.deleteUser(it) }
                    )

                    ReceiptFooterSettingsCard(
                        receiptFooterText = receiptFooterText,
                        onReceiptFooterChange = onReceiptFooterChange
                    )
                } else {
                    BaristaAccessInfoCard()
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
    onDeleteUser: (UserEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(
                    text = "Staff Management",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Button(onClick = onAddUserClick) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Staff")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            users.forEach { user ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Column {
                        Text(user.fullName, fontWeight = FontWeight.Bold)
                        Text("${user.username} • ${user.role}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (user.role != UserRole.ADMIN || users.count { it.role == UserRole.ADMIN } > 1) {
                        IconButton(onClick = { onDeleteUser(user) }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
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
    onUpdate: (BusinessEntity) -> Unit
) {
    var name by remember(businessInfo) { mutableStateOf(businessInfo?.businessName ?: "") }
    var address by remember(businessInfo) { mutableStateOf(businessInfo?.address ?: "") }
    var vat by remember(businessInfo) { mutableStateOf(businessInfo?.vat ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "Business Profile",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = vat, onValueChange = { vat = it }, label = { Text("VAT Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    businessInfo?.let {
                        onUpdate(it.copy(businessName = name, address = address, vat = vat))
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Update Info")
            }
        }
    }
}

@Composable
private fun LanguageSettingsCard(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var showNotice by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LanguageButton(stringResource(R.string.english), selectedLanguage == "English") {
                    if (selectedLanguage != "English") {
                        onLanguageChange("English")
                        showNotice = true
                    }
                }
                LanguageButton(stringResource(R.string.french), selectedLanguage == "Français" || selectedLanguage == "French") {
                    if (selectedLanguage != "Français" && selectedLanguage != "French") {
                        onLanguageChange("Français")
                        showNotice = true
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
private fun LanguageButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    if (isSelected) Button(onClick) { Text(text) }
    else OutlinedButton(onClick) { Text(text) }
}

@Composable
private fun ReceiptFooterSettingsCard(receiptFooterText: String, onReceiptFooterChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(text = "Receipt Footer", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(18.dp))
            OutlinedTextField(value = receiptFooterText, onValueChange = onReceiptFooterChange, label = { Text("Footer message") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        }
    }
}

@Composable
private fun BaristaAccessInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(text = "Limited Access", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "You are limited to the available settings. Other Settings are reserved for Admin Users", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AddUserDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, UserRole) -> Unit) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.BARISTA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Staff") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
                OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("PIN") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = role == UserRole.BARISTA, onClick = { role = UserRole.BARISTA })
                    Text("Barista")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = role == UserRole.ADMIN, onClick = { role = UserRole.ADMIN })
                    Text("Admin")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, username, pin, role) }, enabled = name.isNotBlank() && username.isNotBlank() && pin.length >= 4) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
