package com.royalcoffee.pos.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WhatsAppShareUtilsTest {

    @Test
    fun testBuildRecipientJid() {
        assertEquals("23052581234@s.whatsapp.net", WhatsAppShareUtils.buildRecipientJid("+23052581234"))
        assertEquals("23052581234@s.whatsapp.net", WhatsAppShareUtils.buildRecipientJid("5258 1234"))
        assertEquals("447700900123@s.whatsapp.net", WhatsAppShareUtils.buildRecipientJid("+447700900123"))

        assertNull(WhatsAppShareUtils.buildRecipientJid(""))
        assertNull(WhatsAppShareUtils.buildRecipientJid("   "))
        assertNull(WhatsAppShareUtils.buildRecipientJid("123"))
    }
}
