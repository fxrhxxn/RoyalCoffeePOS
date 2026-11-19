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
                text = stringResource(R.string.settings),
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
                    text = stringResource(R.string.settings_staff_management_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = onAddUserClick) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_add_staff_button),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            users.forEach { user ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val roleText = when (user.role) {
                            UserRole.ADMIN -> stringResource(R.string.settings_role_admin)
                            UserRole.BARISTA -> stringResource(R.string.settings_role_barista)
                        }
                        Text(
                            text = stringResource(R.string.settings_staff_user_subtitle, user.username, roleText),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                text = stringResource(R.string.settings_business_profile_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.settings_business_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(stringResource(R.string.settings_address)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = vat,
                onValueChange = { vat = it },
                label = { Text(stringResource(R.string.settings_vat_number)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    businessInfo?.let {
                        onUpdate(it.copy(businessName = name, address = address, vat = vat))
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.settings_update_info_button),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
    if (isSelected) Button(onClick) { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    else OutlinedButton(onClick) { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
private fun ReceiptFooterSettingsCard(receiptFooterText: String, onReceiptFooterChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = stringResource(R.string.settings_receipt_footer_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(18.dp))
            OutlinedTextField(
                value = receiptFooterText,
                onValueChange = onReceiptFooterChange,
                label = { Text(stringResource(R.string.settings_footer_message)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
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
            Text(
                text = stringResource(R.string.settings_limited_access_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.settings_limited_access_message),
                style = MaterialTheme.typography.bodyMedium
            )
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
        title = { Text(stringResource(R.string.settings_add_user_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.settings_full_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text(stringResource(R.string.pin)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = role == UserRole.BARISTA, onClick = { role = UserRole.BARISTA })
                    Text(stringResource(R.string.settings_role_barista))
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = role == UserRole.ADMIN, onClick = { role = UserRole.ADMIN })
                    Text(stringResource(R.string.settings_role_admin))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, username, pin, role) },
                enabled = name.isNotBlank() && username.isNotBlank() && pin.length >= 4
            ) {
                Text(
                    text = stringResource(R.string.settings_add_button),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}
