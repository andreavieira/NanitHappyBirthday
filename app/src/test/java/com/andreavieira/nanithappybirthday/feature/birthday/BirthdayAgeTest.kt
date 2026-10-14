package com.andreavieira.nanithappybirthday.feature.birthday

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/** Checks completed ages at newborn, month-end, leap-day and year boundaries. */
class BirthdayAgeTest {
    @Test fun newbornShowsZeroMonths() = checkAge("2025-01-15", "2025-01-15", 0, false)
    @Test fun incompleteMonthIsNotCounted() = checkAge("2025-01-15", "2025-02-14", 0, false)
    @Test fun completedMonthIsCounted() = checkAge("2025-01-15", "2025-02-15", 1, false)
    @Test fun monthEndUsesLastAvailableDay() = checkAge("2025-01-31", "2025-02-28", 1, false)
    @Test fun beforeFirstBirthdayUsesMonths() = checkAge("2025-01-15", "2026-01-14", 11, false)
    @Test fun firstBirthdayUsesYears() = checkAge("2025-01-15", "2026-01-15", 1, true)
    @Test fun incompleteYearIsNotCounted() = checkAge("2020-01-15", "2026-01-14", 5, true)
    @Test fun leapDayAnniversaryUsesFebruaryEnd() = checkAge("2024-02-29", "2025-02-28", 1, true)
    @Test(expected = IllegalArgumentException::class)
    fun futureBirthdayIsRejected() { birthdayAge(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1)) }

    private fun checkAge(birth: String, today: String, number: Int, years: Boolean) {
        assertEquals(BirthdayAge(number, years), birthdayAge(LocalDate.parse(birth), LocalDate.parse(today)))
    }
}
