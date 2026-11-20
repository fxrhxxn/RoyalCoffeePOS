package com.royalcoffee.pos.ui.receipt

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.royalcoffee.pos.model.Money
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.util.ReceiptPdfGenerator
import com.royalcoffee.pos.util.WhatsAppShareUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Creates a PDF receipt and opens WhatsApp's share flow.
 * Accepts an optional target phone number for best-effort recipient targeting.
 */
@Composable
fun rememberWhatsAppReceiptSharer(): (receipt: Receipt, targetPhoneNumber: String?) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPreparing by remember { mutableStateOf(false) }

    return { receipt, targetPhoneNumber ->
        // Ignore repeated taps while the PDF is being generated.
        if (!isPreparing) {
            isPreparing = true
            scope.launch {
                try {
                    val pdfUri = withContext(Dispatchers.IO) {
                        val folder = File(context.cacheDir, "shared_receipts")
                        if (!folder.exists() && !folder.mkdirs()) {
                            error("Unable to create a temporary receipt folder.")
                        }

                        // Remove temporary PDFs older than seven days.
                        folder.listFiles()
                            ?.filter {
                                System.currentTimeMillis() - it.lastModified() >
                                        7 * 24 * 60 * 60 * 1000L
                            }
                            ?.forEach { it.delete() }

                        val safeReceiptNumber = receipt.receiptNumber.replace(
                            Regex("[^A-Za-z0-9_-]"),
                            "_"
                        )
                        val pdfFile = File(
                            folder,
                            "Receipt_${safeReceiptNumber}_${System.currentTimeMillis()}.pdf"
                        )
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.receipts",
                            pdfFile
                        )

                        check(ReceiptPdfGenerator.writePdf(context, receipt, uri)) {
                            "Unable to create the receipt PDF."
                        }
                        uri
                    }

                    val jid = targetPhoneNumber?.let { WhatsAppShareUtils.buildRecipientJid(it) }

                    fun createShareIntent(includeJid: Boolean): Intent {
                        return Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            setPackage("com.whatsapp")
                            if (includeJid && jid != null) {
                                // "jid" is an undocumented, best-effort WhatsApp intent parameter for targeting a recipient.
                                putExtra("jid", jid)
                            }
                            putExtra(Intent.EXTRA_STREAM, pdfUri)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Thank you for visiting ${receipt.storeName}! " +
                                        "Your receipt ${receipt.receiptNumber} for " +
                                        "Rs ${Money.format(receipt.total)} is attached."
                            )
                            clipData = ClipData.newUri(
                                context.contentResolver,
                                "Receipt PDF",
                                pdfUri
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }

                    try {
                        context.startActivity(createShareIntent(includeJid = true))
                    } catch (activityException: ActivityNotFoundException) {
                        throw activityException
                    } catch (_: Exception) {
                        // Fallback: Retry without recipient JID if targeted intent fails unexpectedly.
                        context.startActivity(createShareIntent(includeJid = false))
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "WhatsApp is not installed or available.",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (failure: Exception) {
                    Toast.makeText(
                        context,
                        failure.localizedMessage ?: "Unable to share the receipt.",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    isPreparing = false
                }
            }
        }
    }
}
