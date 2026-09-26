package com.petermathie.vibecheck.ui

import java.time.LocalDate
import java.time.YearMonth

internal data class FreshnessMonthRange(
    val month: YearMonth,
    val firstDate: LocalDate,
    val lastDate: LocalDate,
) {
    val dayCount: Int = lastDate.dayOfMonth
}

internal fun freshnessMonthRange(
    requestedMonth: YearMonth,
    today: LocalDate,
): FreshnessMonthRange {
    val currentMonth = YearMonth.from(today)
    val month = requestedMonth.coerceAtMost(currentMonth)
    return FreshnessMonthRange(
        month = month,
        firstDate = month.atDay(1),
        lastDate = if (month == currentMonth) today else month.atEndOfMonth(),
    )
}

internal fun freshnessDateInMonth(
    selectedDate: LocalDate,
    requestedMonth: YearMonth,
    today: LocalDate,
): LocalDate {
    val range = freshnessMonthRange(requestedMonth, today)
    return range.month.atDay(selectedDate.dayOfMonth.coerceAtMost(range.dayCount))
}
