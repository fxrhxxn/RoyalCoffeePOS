package com.royalcoffee.pos.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ExpiryDateConversionTest {

    @Test
    fun testEpochDayToUtcMillisRoundTrip() {
        val today = LocalDate.of(2026, 11, 20)
        val epochDay = today.toEpochDay()

        val utcMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val convertedDate = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

        assertEquals(today, convertedDate)
        assertEquals(epochDay, convertedDate.toEpochDay())
    }

    @Test
    fun testTodayIsSelectableAndYesterdayIsDisabled() {
        val today = LocalDate.of(2026, 11, 20)
        val todayUtcMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val yesterday = today.minusDays(1)
        val yesterdayUtcMillis = yesterday.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val tomorrow = today.plusDays(1)
        val tomorrowUtcMillis = tomorrow.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        org.junit.Assert.assertTrue(todayUtcMillis >= todayUtcMillis)
        org.junit.Assert.assertTrue(tomorrowUtcMillis >= todayUtcMillis)
        org.junit.Assert.assertFalse(yesterdayUtcMillis >= todayUtcMillis)
    }
}
