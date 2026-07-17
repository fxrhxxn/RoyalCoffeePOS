package com.royalcoffee.pos.printer

/**
 * PrinterConnectionCheckResult stores the result of checking whether a printer
 * can be used.
 *
 * This is useful because we do not want to return only true/false.
 * We also want a clear message that can be shown to the cashier.
 */
data class PrinterConnectionCheckResult(
    val status: PrinterConnectionStatus,
    val canPrint: Boolean,
    val message: String
)