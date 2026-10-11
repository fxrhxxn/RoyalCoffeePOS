package com.royalcoffee.pos.ui.receipt

/*package com.royalcoffee.pos.ui.receipt

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.model.Money
import com.royalcoffee.pos.util.ReceiptPdfGenerator
import kotlinx.coroutines.*
import java.io.File

internal fun normalizeCustomerPhone(value: String): String {
    val trimmed = value.trim()
    val digits = trimmed.filter { it in '0'..'9' }
    return when {
        trimmed.startsWith("+") -> "+$digits"
        digits.startsWith("00") -> "+${digits.drop(2)}"
        digits.length in 7..8 -> "+230$digits"
        digits.isEmpty() -> ""
        else -> "+$digits"
    }
}

private data class PhoneContact(val name: String, val number: String)

private fun searchContacts(context: Context, query: String): List<PhoneContact> {
    val result = mutableListOf<PhoneContact>()
    val digits = query.filter(Char::isDigit)
    context.contentResolver.query(Phone.CONTENT_URI, arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER), null, null, Phone.DISPLAY_NAME + " ASC")?.use { cursor ->
        while (cursor.moveToNext() && result.size < 30) {
            val name = cursor.getString(0).orEmpty()
            val number = cursor.getString(1).orEmpty()
            if (query.isBlank() || name.contains(query, true) || (digits.isNotEmpty() && number.filter(Char::isDigit).contains(digits))) result += PhoneContact(name, number)
        }
    }
    return result.distinct()
}

private fun knownNumber(context: Context, number: String): Boolean? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null
    return try {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)?.use { it.moveToFirst() }
    } catch (_: SecurityException) { null }
}

@Composable
fun WhatsAppReceiptDialog(receipt: Receipt, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var number by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var contactsEnabled by remember { mutableStateOf(false) }
    var contacts by remember { mutableStateOf(emptyList<PhoneContact>()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var unavailable by remember { mutableStateOf(false) }
    var offerSave by remember { mutableStateOf(false) }
    var sharedNumber by remember { mutableStateOf("") }
    var sharedName by remember { mutableStateOf("") }
    var unknownCustomer by remember { mutableStateOf(false) }
    val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Returning from WhatsApp is not evidence that a message was sent.
        offerSave = unknownCustomer
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        contactsEnabled = granted
        if (!granted) error = "Contact search is unavailable. You can still enter a number."
    }
    LaunchedEffect(query, contactsEnabled) {
        if (contactsEnabled) {
            delay(180)
            contacts = withContext(Dispatchers.IO) {
                try { searchContacts(context, query) } catch (_: SecurityException) { emptyList() }
            }
        }
    }
    fun saveContact(name: String, phone: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.Contacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.NAME, name)
                putExtra(ContactsContract.Intents.Insert.PHONE, phone)
            })
        } catch (_: ActivityNotFoundException) { error = "No contacts app is available." }
    }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("Share receipt via WhatsApp") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(number, { number = it; customerName = "" }, label = { Text("Customer phone number") }, singleLine = true)
            Text("Local numbers use +230. In WhatsApp, select ${normalizeCustomerPhone(number).ifBlank { "the customer" }} to attach the PDF.")
            OutlinedTextField(customerName, { customerName = it }, label = { Text("Customer name (optional)") }, singleLine = true)
            TextButton(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) contactsEnabled = true
                else permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }) { Text("Search contacts") }
            if (contactsEnabled) {
                OutlinedTextField(query, { query = it }, label = { Text("Search name or number") }, singleLine = true)
                LazyColumn(Modifier.heightIn(max = 140.dp)) {
                    items(contacts) { contact -> TextButton(onClick = {
                        number = normalizeCustomerPhone(contact.number); customerName = contact.name; contactsEnabled = false
                    }) { Text("${contact.name} · ${contact.number}") } }
                }
            }
            Row {
                TextButton(enabled = normalizeCustomerPhone(number).length > 1, onClick = {
                    context.getSystemService(android.content.ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Customer number", normalizeCustomerPhone(number)))
                }) { Text("Copy number") }
                TextButton(enabled = normalizeCustomerPhone(number).length > 1, onClick = {
                    val text = "Thank you for visiting ${receipt.storeName}. Receipt ${receipt.receiptNumber}, Rs ${Money.format(receipt.total)}."
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${normalizeCustomerPhone(number).drop(1)}?text=${Uri.encode(text)}")).setPackage("com.whatsapp"))
                    } catch (_: ActivityNotFoundException) { unavailable = true }
                }) { Text("Open chat") }
            }
            Text("WhatsApp will ask you to choose the recipient for the PDF and confirm sending.")
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(enabled = !busy && normalizeCustomerPhone(number).length > 1, onClick = {
        busy = true; error = null
        val recipient = normalizeCustomerPhone(number)
        val name = customerName
        scope.launch {
            try {
                val share = Intent(Intent.ACTION_SEND).setPackage("com.whatsapp").setType("application/pdf")
                if (share.resolveActivity(context.packageManager) == null) { unavailable = true; return@launch }
                val uri = withContext(Dispatchers.IO) {
                    val folder = File(context.cacheDir, "shared_receipts").apply { mkdirs() }
                    folder.listFiles()?.filter { System.currentTimeMillis() - it.lastModified() > 7 * 86400000L }?.forEach { it.delete() }
                    val file = File(folder, "Receipt_${receipt.receiptNumber.replace(Regex("[^A-Za-z0-9_-]"), "_")}_${System.currentTimeMillis()}.pdf")
                    val target = FileProvider.getUriForFile(context, "${context.packageName}.receipts", file)
                    check(ReceiptPdfGenerator.writePdf(context, receipt, target)) { "Unable to create the receipt PDF." }
                    target
                }
                unknownCustomer = withContext(Dispatchers.IO) { knownNumber(context, recipient) == false }
                sharedNumber = recipient; sharedName = name
                share.putExtra(Intent.EXTRA_STREAM, uri)
                share.putExtra(Intent.EXTRA_TEXT, "Thank you for visiting ${receipt.storeName}! Your receipt ${receipt.receiptNumber} for Rs ${Money.format(receipt.total)} is attached.")
                share.clipData = ClipData.newUri(context.contentResolver, "Receipt PDF", uri)
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareLauncher.launch(share)
            } catch (_: ActivityNotFoundException) { unavailable = true }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) { error = failure.localizedMessage ?: "Unable to share receipt." }
            finally { busy = false }
        }
    }) { Text(if (busy) "Preparing…" else "Share PDF") } }, dismissButton = {
        Row {
            TextButton(enabled = normalizeCustomerPhone(number).length > 1, onClick = { saveContact(customerName, normalizeCustomerPhone(number)) }) { Text("Save customer") }
            TextButton(enabled = !busy, onClick = onDismiss) { Text("Close") }
        }
    })
    if (unavailable) AlertDialog(onDismissRequest = { unavailable = false }, title = { Text("WhatsApp unavailable") },
        text = { Text("Install or enable standard WhatsApp to share receipts.") },
        confirmButton = { TextButton(onClick = { unavailable = false }) { Text("OK") } })
    if (offerSave) AlertDialog(onDismissRequest = { offerSave = false }, title = { Text("Save customer?") },
        text = { Text("$sharedNumber is not in your contacts. Would you like to save this customer?") },
        confirmButton = { TextButton(onClick = { offerSave = false; saveContact(sharedName, sharedNumber) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = { offerSave = false }) { Text("Not now") } })
}
*/