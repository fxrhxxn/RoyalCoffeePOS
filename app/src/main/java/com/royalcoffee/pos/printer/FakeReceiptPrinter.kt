package com.royalcoffee.pos.printer

import com.royalcoffee.pos.model.PrinterConfig
import com.royalcoffee.pos.model.Receipt

/**
 * FakeReceiptPrinter is used during development when no physical printer exists.
 *
 * This class now uses EscPosReceiptFormatter.
 *
 * Why?
 * - The app can already prepare printer-ready ESC/POS receipt data.
 * - We can test that receipt formatting does not crash.
 * - Later, BluetoothReceiptPrinter can reuse the same formatter.
 */
class FakeReceiptPrinter(
    private val receiptFormatter: EscPosReceiptFormatter = EscPosReceiptFormatter()
) : ReceiptPrinter {

    override fun printReceipt(
        receipt: Receipt,
        printerConfig: PrinterConfig
    ): ReceiptPrintResult {
        /**
         * Prepare ESC/POS printer bytes.
         *
         * Since this is a fake printer, the bytes are not sent anywhere yet.
         * This line exists so we can confirm the formatter is connected
         * to the print flow.
         */
        val printerBytes = receiptFormatter.formatReceiptBytes(
            receipt = receipt,
            printerConfig = printerConfig
        )

        /**
         * This check keeps the variable intentionally used.
         *
         * It also guards against a formatting failure that somehow creates
         * no printable content.
         */
        if (printerBytes.isEmpty()) {
            return ReceiptPrintResult(
                isSuccessful = false,
                message = "Unable to prepare receipt for printing."
            )
        }

        /**
         * In the current version, no real printer is connected.
         *
         * We show a professional POS-style printer error.
         */
        if (!printerConfig.canAttemptConnection) {
            return ReceiptPrintResult(
                isSuccessful = false,
                message = "Printer not available. Check connection."
            )
        }

        /**
         * Even if printer configuration exists, FakeReceiptPrinter still does
         * not connect to real hardware.
         *
         * A real BluetoothReceiptPrinter will be added later.
         */
        return ReceiptPrintResult(
            isSuccessful = false,
            message = "Printer not available. Check connection."
        )
    }
}