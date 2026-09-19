package com.royalcoffee.pos.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Utility for calculating date ranges for receipt filtering.
 */
object DateFilterUtils {

    /**
     * Represents the result of a date range calculation.
     */
    data class DateRange(val start: Long, val end: Long)

    /**
     * Gets the timestamp range for today (00:00:00 to 23:59:59).
     */
    fun getTodayRange(): DateRange {
        val startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        val endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        return DateRange(
            start = startOfDay.toEpochMillis(),
            end = endOfDay.toEpochMillis()
        )
    }

    /**
     * Gets the timestamp range for yesterday.
     */
    fun getYesterdayRange(): DateRange {
        val yesterday = LocalDate.now().minusDays(1)
        val startOfDay = LocalDateTime.of(yesterday, LocalTime.MIN)
        val endOfDay = LocalDateTime.of(yesterday, LocalTime.MAX)
        return DateRange(
            start = startOfDay.toEpochMillis(),
            end = endOfDay.toEpochMillis()
        )
    }

    /**
     * Gets the timestamp range for the current week (Monday to Sunday).
     */
    fun getThisWeekRange(): DateRange {
        val now = LocalDate.now()
        val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        
        return DateRange(
            start = LocalDateTime.of(startOfWeek, LocalTime.MIN).toEpochMillis(),
            end = LocalDateTime.of(endOfWeek, LocalTime.MAX).toEpochMillis()
        )
    }

    /**
     * Gets the timestamp range for the current month.
     */
    fun getThisMonthRange(): DateRange {
        val now = LocalDate.now()
        val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
        val endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth())
        
        return DateRange(
            start = LocalDateTime.of(startOfMonth, LocalTime.MIN).toEpochMillis(),
            end = LocalDateTime.of(endOfMonth, LocalTime.MAX).toEpochMillis()
        )
    }

    /**
     * Gets the timestamp range for a specific custom date.
     */
    fun getCustomDateRange(date: LocalDate): DateRange {
        val startOfDay = LocalDateTime.of(date, LocalTime.MIN)
        val endOfDay = LocalDateTime.of(date, LocalTime.MAX)
        return DateRange(
            start = startOfDay.toEpochMillis(),
            end = endOfDay.toEpochMillis()
        )
    }

    private fun LocalDateTime.toEpochMillis(): Long {
        return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
