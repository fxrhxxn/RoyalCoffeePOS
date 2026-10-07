package com.royalcoffee.pos.ui.receipt

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.util.ReceiptPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun rememberReceiptDownloader(): (Receipt) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<Receipt?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { destination ->
        val receipt = pending
        pending = null
        if (destination != null && receipt != null) scope.launch {
            val saved = withContext(Dispatchers.IO) { runCatching { ReceiptPdfGenerator.writePdf(context, receipt, destination) }.getOrDefault(false) }
            Toast.makeText(context, if (saved) "Receipt saved." else "Unable to save PDF.", Toast.LENGTH_LONG).show()
        }
    }
    return { receipt ->
        if (pending == null) {
            pending = receipt
            try { launcher.launch("Receipt_${receipt.receiptNumber}.pdf") }
            catch (_: android.content.ActivityNotFoundException) {
                pending = null
                Toast.makeText(context, "No document save app is available.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
