package com.royalcoffee.pos.printer

/**
 * ReceiptPrintResult represents the result of a print attempt.
 *
 * This is useful because printing can either:
 * - Succeed
 * - Fail because no printer is connected
 * - Fail because Bluetooth is off
 * - Fail because the printer is out of range
 * - Fail because the printer has no paper
 *
 * For now, we only return a professional failure message because
 * no physical printer is available yet.
 */
data class ReceiptPrintResult(
    val isSuccessful: Boolean,
    val message: String
)