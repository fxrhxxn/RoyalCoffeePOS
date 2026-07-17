package com.royalcoffee.pos.printer

import com.royalcoffee.pos.model.PrinterConfig
import com.royalcoffee.pos.model.Receipt

/**
 * ReceiptPrinter defines what any receipt printer service must be able to do.
 *
 * This is an interface because later we can have different implementations:
 *
 * - FakeReceiptPrinter for development without a real printer
 * - BluetoothReceiptPrinter for Version 1
 * - WifiReceiptPrinter for a future version
 */
interface ReceiptPrinter {

    /**
     * Attempts to print a receipt using the selected printer configuration.
     *
     * The function returns a ReceiptPrintResult so the UI can show
     * a clear success or error message to the user.
     */
    fun printReceipt(
        receipt: Receipt,
        printerConfig: PrinterConfig
    ): ReceiptPrintResult
}