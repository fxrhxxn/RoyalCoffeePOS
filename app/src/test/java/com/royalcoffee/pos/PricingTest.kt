package com.royalcoffee.pos

import com.royalcoffee.pos.model.*
import com.royalcoffee.pos.printer.EscPosReceiptFormatter
import org.junit.Assert.*
import org.junit.Test

class PricingTest {
    @Test fun decimalInputsRoundTripExactly() {
        assertEquals(9950L, Money.parse("99.50"))
        assertEquals("99.50", Money.format(9950))
        listOf("", "-1", "1.001", "NaN", "1e2", "999999999999999999999").forEach {
            assertNull(Money.parse(it))
        }
    }

    @Test fun inclusivePricesAreDiscountedOnce() {
        assertEquals(10000L, 10000L - Money.discount(10000, 0))
        assertEquals(9500L, 10000L - Money.discount(10000, 5))
        assertEquals(498L, Money.discount(9950, 5)) // Rs 4.975 rounds to Rs 4.98.
        assertEquals(9452L, 9950L - Money.discount(9950, 5))
        assertEquals(8457L, 9950L - Money.discount(9950, 15))
    }

    @Test fun discountsRespectBothRoles() {
        assertEquals(listOf(0, 5), DiscountPolicy.options(UserRole.BARISTA))
        assertEquals(listOf(0, 5, 10, 15), DiscountPolicy.options(UserRole.ADMIN))
        assertThrows(IllegalArgumentException::class.java) {
            DiscountPolicy.requireAllowed(UserRole.BARISTA, 10)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DiscountPolicy.requireAllowed(UserRole.ADMIN, 7)
        }
    }

    @Test fun lineAllocationsAlwaysMatchOrderDiscount() {
        for (price in 0L..100L) {
            val lines = listOf(price, 17L, 99L)
            for (percent in listOf(0, 5, 10, 15)) {
                val discount = Money.discount(Money.sum(lines), percent)
                val allocations = Money.allocateDiscount(lines, discount)
                assertEquals(discount, Money.sum(allocations))
                allocations.zip(lines).forEach { (part, line) -> assertTrue(part in 0..line) }
            }
        }
    }

    @Test fun largeMoneyDoesNotUseIntOrSilentlyOverflow() {
        assertEquals(6_000_000_000L, Money.lineTotal(3_000_000_000L, 2))
        assertThrows(ArithmeticException::class.java) { Money.lineTotal(Long.MAX_VALUE, 2) }
    }

    private fun receipt(inclusive: Boolean) = Receipt(
        "RC-2026-000001", "07/10/2026 14:00", "Royal Coffee", listOf("Mauritius"), "",
        "Cashier", listOf(ReceiptItem("Coffee", 1, 9950, 3000)),
        9950, if (inclusive) 498 else 0, if (inclusive) 0 else 1492,
        if (inclusive) 9452 else 11442, PaymentMethod.CASH, 12000, if (inclusive) 2548 else 558,
        discountPercent = if (inclusive) 5 else null, vatIncluded = inclusive
    )

    @Test fun printedReceiptsPreserveLegacyTaxAndShowNewDiscount() {
        val formatter = EscPosReceiptFormatter()
        val current = formatter.formatReceiptText(receipt(true), PrinterConfig(printerName = "Test", connectionType = PrinterConnectionType.BLUETOOTH))
        assertTrue(current.contains("VAT Included"))
        assertTrue(current.contains("Discount (5%)"))
        assertTrue(current.contains("94.52"))
        val legacy = formatter.formatReceiptText(receipt(false), PrinterConfig(printerName = "Test", connectionType = PrinterConnectionType.BLUETOOTH))
        assertFalse(legacy.contains("VAT Included"))
        assertTrue(legacy.contains("114.42"))
        assertTrue(legacy.contains("14.92"))
    }
}
