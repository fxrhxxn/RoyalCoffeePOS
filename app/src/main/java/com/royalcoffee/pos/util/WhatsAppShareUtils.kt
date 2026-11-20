package com.royalcoffee.pos.util

/**
 * Utility for WhatsApp recipient targeting.
 */
object WhatsAppShareUtils {

    /**
     * Builds a WhatsApp JID for best-effort recipient targeting.
     *
     * Note: The "jid" extra is an undocumented, best-effort WhatsApp intent parameter for opening a chat.
     *
     * Example:
     * "+23052581234" -> "23052581234@s.whatsapp.net"
     * "+447700900123" -> "447700900123@s.whatsapp.net"
     */
    fun buildRecipientJid(normalizedPhone: String): String? {
        val normalized = CustomerPhoneUtils.normalizeCustomerPhone(normalizedPhone) ?: return null
        val digits = CustomerPhoneUtils.extractPhoneDigits(normalized)
        if (digits.isEmpty()) return null
        return "$digits@s.whatsapp.net"
    }
}
