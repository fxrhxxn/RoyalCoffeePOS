package com.royalcoffee.pos.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.res.ResourcesCompat
import com.royalcoffee.pos.R
import com.royalcoffee.pos.model.Receipt
import java.io.File
import java.io.FileOutputStream

/**
 * ReceiptPdfGenerator handles creating a PDF file from a Receipt object.
 * 
 * It formats the receipt for a narrow 80mm-style page and saves it 
 * to the device's Downloads folder.
 */
object ReceiptPdfGenerator {

    /**
     * Generates a PDF for the given receipt and saves it to the public Downloads folder.
     * 
     * @return The Uri of the saved file if successful, null otherwise.
     */
    fun generateAndSavePdf(context: Context, receipt: Receipt): Uri? {
        val pdfDocument = PdfDocument()

        // Page settings: 300 points wide (approx 105mm), dynamic height.
        // We use a reasonably tall page and will trim later if needed, 
        // or just use a fixed 800pt height which covers most receipts.
        val pageWidth = 300
        val pageHeight = calculateEstimatedHeight(receipt)
        
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Load custom font if possible, otherwise fallback to default
        val cinzelBold = try {
            ResourcesCompat.getFont(context, R.font.cinzel_semibold)
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }

        var yPos = 50f

        // 1. Header (Store Name)
        paint.typeface = cinzelBold
        paint.textSize = 22f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(receipt.storeName, (pageWidth / 2).toFloat(), yPos, paint)
        yPos += 30f

        // 2. Address & Contact
        paint.typeface = Typeface.MONOSPACE
        paint.textSize = 10f
        receipt.storeAddressLines.forEach { line ->
            canvas.drawText(line, (pageWidth / 2).toFloat(), yPos, paint)
            yPos += 16f
        }
        canvas.drawText("Tel: ${receipt.storePhone}", (pageWidth / 2).toFloat(), yPos, paint)
        yPos += 30f

        // 3. Receipt Info
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("RECEIPT: ${receipt.receiptNumber}", 25f, yPos, paint)
        yPos += 16f
        canvas.drawText("DATE:    ${receipt.receiptDateTime}", 25f, yPos, paint)
        yPos += 16f
        canvas.drawText("CASHIER: ${receipt.cashierName}", 25f, yPos, paint)
        yPos += 24f

        // Divider
        drawDashedLine(canvas, 25f, pageWidth - 25f, yPos, paint)
        yPos += 24f

        // 4. Items Table
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("ITEM", 25f, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", pageWidth - 25f, yPos, paint)
        yPos += 20f

        paint.typeface = Typeface.MONOSPACE
        paint.textAlign = Paint.Align.LEFT
        receipt.items.forEach { item ->
            // Product name and line total
            canvas.drawText(item.productName, 25f, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Rs ${item.lineTotal}", pageWidth - 25f, yPos, paint)
            yPos += 16f

            // Quantity and unit price
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 9f
            paint.color = Color.DKGRAY
            canvas.drawText("  ${item.quantity} x Rs ${item.unitPrice}", 25f, yPos, paint)
            yPos += 18f
            paint.textSize = 10f
            paint.color = Color.BLACK
        }

        yPos += 6f
        drawDashedLine(canvas, 25f, pageWidth - 25f, yPos, paint)
        yPos += 24f

        // 5. Summary
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Subtotal", 25f, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Rs ${receipt.subtotal}", pageWidth - 25f, yPos, paint)
        yPos += 16f

        if (receipt.discount > 0) {
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Discount", 25f, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("-Rs ${receipt.discount}", pageWidth - 25f, yPos, paint)
            yPos += 16f
        }

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Tax (15%)", 25f, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Rs ${receipt.tax}", pageWidth - 25f, yPos, paint)
        yPos += 24f

        // Grand Total
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 14f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL", 25f, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Rs ${receipt.total}", pageWidth - 25f, yPos, paint)
        yPos += 34f

        // 6. Payment Info
        paint.typeface = Typeface.MONOSPACE
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("PAID BY ${receipt.paymentMethod.name}", (pageWidth / 2).toFloat(), yPos, paint)
        yPos += 16f

        if (receipt.amountPaid != null) {
            canvas.drawText("Amount Paid: Rs ${receipt.amountPaid}", (pageWidth / 2).toFloat(), yPos, paint)
            yPos += 16f
        }

        if (receipt.hasChange) {
            canvas.drawText("Change: Rs ${receipt.changeGiven}", (pageWidth / 2).toFloat(), yPos, paint)
            yPos += 24f
        }

        yPos += 10f
        drawDashedLine(canvas, 25f, pageWidth - 25f, yPos, paint)
        yPos += 24f

        // 7. Footer
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(receipt.footerMessage.uppercase(), (pageWidth / 2).toFloat(), yPos, paint)

        pdfDocument.finishPage(page)

        val fileName = "RoyalCoffee_Receipt_${receipt.receiptNumber}.pdf"
        var uri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                }
            } else {
                // Legacy approach for API 26-28
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                uri = Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return uri
    }

    private fun calculateEstimatedHeight(receipt: Receipt): Int {
        // Base height for headers, summary, payment, and footer
        var height = 350
        // Add height for each item (approx 34 points per item row)
        height += receipt.items.size * 34
        // Add height for address lines
        height += receipt.storeAddressLines.size * 16
        return height
    }

    private fun drawDashedLine(canvas: Canvas, startX: Float, endX: Float, y: Float, paint: Paint) {
        paint.typeface = Typeface.MONOSPACE
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("-".repeat(32), (startX + endX) / 2, y, paint)
    }
}
