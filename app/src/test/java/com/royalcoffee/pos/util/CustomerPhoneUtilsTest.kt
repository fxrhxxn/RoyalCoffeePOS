package com.royalcoffee.pos.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomerPhoneUtilsTest {

    @Test
    fun testNormalizeCustomerPhone() {
        assertEquals("+23052581234", CustomerPhoneUtils.normalizeCustomerPhone("52581234"))
        assertEquals("+23052581234", CustomerPhoneUtils.normalizeCustomerPhone("5258 1234"))
        assertEquals("+23052581234", CustomerPhoneUtils.normalizeCustomerPhone("5258-1234"))
        assertEquals("+23052581234", CustomerPhoneUtils.normalizeCustomerPhone("+230 5258 1234"))
        assertEquals("+23052581234", CustomerPhoneUtils.normalizeCustomerPhone("0023052581234"))
        assertEquals("+23052581234", CustomerPhoneUtils.normalizeCustomerPhone("23052581234"))
        assertEquals("+447700900123", CustomerPhoneUtils.normalizeCustomerPhone("+447700900123"))

        assertNull(CustomerPhoneUtils.normalizeCustomerPhone(""))
        assertNull(CustomerPhoneUtils.normalizeCustomerPhone("   "))
        assertNull(CustomerPhoneUtils.normalizeCustomerPhone("123"))
        assertNull(CustomerPhoneUtils.normalizeCustomerPhone("abc"))
    }

    @Test
    fun incompleteMauritianNumbersAreRejected() {
        assertNull(CustomerPhoneUtils.normalizeCustomerPhone("5258"))
        assertNull(CustomerPhoneUtils.normalizeCustomerPhone("5258123"))
        assertNull(CustomerPhoneUtils.normalizeCustomerPhone("2305258"))
        assertNull(CustomerPhoneUtils.normalizeCustomerPhone("2305258123"))
    }

    @Test
    fun testFormatCustomerPhoneForDisplay() {
        assertEquals("+230 5258 1234", CustomerPhoneUtils.formatCustomerPhoneForDisplay("+23052581234"))
        assertEquals("+230 5258 1234", CustomerPhoneUtils.formatCustomerPhoneForDisplay("5258 1234"))
        assertEquals("+447700900123", CustomerPhoneUtils.formatCustomerPhoneForDisplay("+447700900123"))
    }

    @Test
    fun testExtractPhoneDigits() {
        assertEquals("52581234", CustomerPhoneUtils.extractPhoneDigits("5258 1234"))
        assertEquals("23052581234", CustomerPhoneUtils.extractPhoneDigits("+230 5258-1234"))
    }

    @Test
    fun validMauritianNumbersAreNormalized() {
        assertEquals("+23052581234",CustomerPhoneUtils.normalizeCustomerPhone("52581234"))
        assertEquals("+23052581234",CustomerPhoneUtils.normalizeCustomerPhone("5258 1234"))
        assertEquals("+23052581234",CustomerPhoneUtils.normalizeCustomerPhone("23052581234"))
        assertEquals("+23052581234",CustomerPhoneUtils.normalizeCustomerPhone("+23052581234"))
    }
}
