package com.royalcoffee.pos.model

import java.math.BigDecimal
import java.math.RoundingMode

/** All monetary Long values in the app and Room are cents, never whole rupees. */
object Money {
    // Bound input so even a large cart can be added/multiplied safely.
    const val MAX_INPUT_CENTS = 100_000_000_000L

    fun parse(text: String): Long? = runCatching {
        require(text.trim().matches(Regex("[0-9]+(\\.[0-9]{1,2})?")))
        BigDecimal(text.trim()).movePointRight(2).longValueExact()
            .also { require(it in 0..MAX_INPUT_CENTS) }
    }.getOrNull()

    fun format(cents: Long): String = BigDecimal.valueOf(cents, 2).toPlainString()

    fun sum(values: Iterable<Long>): Long = values.fold(0L, Math::addExact)
    fun lineTotal(price: Long, quantity: Int): Long {
        require(price >= 0 && quantity > 0)
        return Math.multiplyExact(price, quantity.toLong())
    }

    fun discount(subtotal: Long, percent: Int): Long {
        require(subtotal >= 0 && percent in 0..100)
        return BigDecimal.valueOf(subtotal).multiply(BigDecimal.valueOf(percent.toLong()))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).longValueExact()
    }

    /** Cumulative rounding allocates the exact order discount without losing cents. */
    fun allocateDiscount(lines: List<Long>, discount: Long): List<Long> {
        require(lines.all { it >= 0 })
        val subtotal = sum(lines)
        require(discount in 0..subtotal)
        if (subtotal == 0L) return lines.map { 0L }
        var cumulative = 0L
        var allocated = 0L
        return lines.map { line ->
            cumulative = Math.addExact(cumulative, line)
            val target = BigDecimal.valueOf(discount).multiply(BigDecimal.valueOf(cumulative))
                .divide(BigDecimal.valueOf(subtotal), 0, RoundingMode.HALF_UP).longValueExact()
            (target - allocated).also { allocated = target }
        }
    }
}

object DiscountPolicy {
    fun options(role: UserRole): List<Int> =
        if (role == UserRole.ADMIN) listOf(0, 5, 10, 15) else listOf(0, 5)

    fun requireAllowed(role: UserRole, percent: Int) {
        require(percent in options(role)) { "Discount is not permitted for this user." }
    }
}
