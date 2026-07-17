package com.royalcoffee.pos.printer

import com.royalcoffee.pos.model.PrinterConfig
import com.royalcoffee.pos.model.Receipt

/**
 * FakeReceiptPrinter is used during development when no physical printer exists.
 *
 * Important:
 * This class does not actually print.
 *
 * It gives the app a printer service layer now, so that later we can replace it
 * with a real Bluetooth ESC/POS printer implementation without changing the UI.
 */
class FakeReceiptPrinter : ReceiptPrinter {

    override fun printReceipt(
        receipt: Receipt,
        printerConfig: PrinterConfig
    ): ReceiptPrintResult {
        /**
         * In this version of the app, there is no connected printer.
         *
         * We return a professional POS-style error message rather than saying
         * "simulated printer" or "fake printer".
         */
        if (!printerConfig.canAttemptConnection) {
            return ReceiptPrintResult(
                isSuccessful = false,
                message = "Printer not available. Check connection."
            )
        }

        /**
         * Even if placeholder printer data is added later, this fake printer
         * still does not connect to hardware.
         *
         * The real implementation will be BluetoothReceiptPrinter.
         */
        return ReceiptPrintResult(
            isSuccessful = false,
            message = "Printer not available. Check connection."
        )
    }
}