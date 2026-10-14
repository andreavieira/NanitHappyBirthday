package com.andreavieira.nanithappybirthday.feature.birthday

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Represents completed calendar months below one year, then completed calendar years. */
data class BirthdayAge(val number: Int, val inYears: Boolean)

internal fun birthdayAge(birthday: LocalDate, today: LocalDate): BirthdayAge {
    require(!birthday.isAfter(today)) { "Birthday must not be in the future" }
    // Calendar anniversaries clamp to the month's last day, including Feb 29 in non-leap years.
    val inYears = !today.isBefore(birthday.plusYears(1))
    val unit = if (inYears) ChronoUnit.YEARS else ChronoUnit.MONTHS
    var completed = unit.between(birthday, today).toInt()
    fun anniversary(value: Int) =
        if (inYears) birthday.plusYears(value.toLong()) else birthday.plusMonths(value.toLong())
    // between() can undercount our last-valid-day policy: Jan 31 → Feb 28 returns 0 months,
    // but plusMonths(1) gives Feb 28. If today has reached that next anniversary, count it too.
    if (!today.isBefore(anniversary(completed + 1))) completed++
    return BirthdayAge(completed, inYears)
}
