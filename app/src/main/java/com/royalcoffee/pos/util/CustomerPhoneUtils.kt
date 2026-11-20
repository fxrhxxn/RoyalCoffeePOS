package com.royalcoffee.pos.util

object CustomerPhoneUtils {

    /**
     * Normalizes a phone number into international E.164 format (+23052581234).
     *
     * Returns null if input is blank or invalid.
     */
    fun normalizeCustomerPhone(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null

        val (rawDigits, isInternational) = when {
            trimmed.startsWith("+") -> {
                val rest = trimmed.substring(1)
                if (!isValidPhoneChars(rest)) return null
                Pair(rest.filter { it.isDigit() }, true)
            }
            trimmed.startsWith("00") -> {
                val rest = trimmed.substring(2)
                if (!isValidPhoneChars(rest)) return null
                Pair(rest.filter { it.isDigit() }, true)
            }
            else -> {
                if (!isValidPhoneChars(trimmed)) return null
                Pair(trimmed.filter { it.isDigit() }, false)
            }
        }

        if (rawDigits.isEmpty()) return null

        val finalDigits = if (isInternational) {
            rawDigits
        } else {
            if (rawDigits.startsWith("230") && rawDigits.length >= 10) {
                rawDigits
            } else {
                "230$rawDigits"
            }
        }

        if (finalDigits.length !in 7..15) {
            return null
        }

        return "+$finalDigits"
    }

    /**
     * Extracts digits from input string for phone search.
     */
    fun extractPhoneDigits(input: String): String {
        return input.filter { it.isDigit() }
    }

    /**
     * Formats a normalized phone number for friendly UI display.
     * e.g. "+23052581234" -> "+230 5258 1234"
     */
    fun formatCustomerPhoneForDisplay(phoneNumber: String): String {
        val normalized = normalizeCustomerPhone(phoneNumber) ?: phoneNumber.trim()
        if (normalized.startsWith("+230") && normalized.length == 12) {
            val digits = normalized.substring(4)
            return "+230 ${digits.substring(0, 4)} ${digits.substring(4)}"
        }
        return normalized
    }

    private fun isValidPhoneChars(s: String): Boolean {
        return s.all { it.isDigit() || it.isWhitespace() || it == '-' || it == '(' || it == ')' || it == '.' }
    }
}
