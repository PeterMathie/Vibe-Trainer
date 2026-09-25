package com.petermathie.vibecheck.ui

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class FreshnessCalendarTest {
    @Test
    fun pastMonthsExposeEveryCalendarDayIncludingLeapFebruary() {
        val today = LocalDate.of(2026, 9, 25)

        assertEquals(29, freshnessMonthRange(YearMonth.of(2024, 2), today).dayCount)
        assertEquals(28, freshnessMonthRange(YearMonth.of(2025, 2), today).dayCount)
        assertEquals(30, freshnessMonthRange(YearMonth.of(2026, 4), today).dayCount)
        assertEquals(31, freshnessMonthRange(YearMonth.of(2025, 12), today).dayCount)
    }

    @Test
    fun currentMonthStopsAtTodayAndFutureMonthsCannotBeSelected() {
        val today = LocalDate.of(2026, 9, 25)

        assertEquals(today, freshnessMonthRange(YearMonth.of(2026, 9), today).lastDate)
        assertEquals(
            FreshnessMonthRange(YearMonth.of(2026, 9), LocalDate.of(2026, 9, 1), today),
            freshnessMonthRange(YearMonth.of(2027, 1), today),
        )
    }

    @Test
    fun monthSwitchKeepsDayWhenValidAndClampsAcrossBoundaries() {
        val today = LocalDate.of(2026, 9, 25)

        assertEquals(
            LocalDate.of(2026, 8, 25),
            freshnessDateInMonth(today, YearMonth.of(2026, 8), today),
        )
        assertEquals(
            LocalDate.of(2024, 2, 29),
            freshnessDateInMonth(LocalDate.of(2024, 1, 31), YearMonth.of(2024, 2), today),
        )
        assertEquals(
            LocalDate.of(2025, 12, 29),
            freshnessDateInMonth(LocalDate.of(2024, 2, 29), YearMonth.of(2025, 12), today),
        )
        assertEquals(
            today,
            freshnessDateInMonth(LocalDate.of(2025, 12, 31), YearMonth.of(2027, 1), today),
        )
    }
}
