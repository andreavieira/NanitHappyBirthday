package com.andreavieira.nanithappybirthday.feature.birthday

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifies theme selection supports every variant and permits consecutive identical choices. */
class BirthdayThemeTest {
    @Test
    fun everyThemeCanBeSelected() {
        BirthdayTheme.entries.forEachIndexed { index, theme ->
            assertEquals(theme, chooseBirthdayTheme(FixedIndexRandom(index)))
        }
    }

    @Test
    fun consecutiveEntriesMayRepeatTheSameTheme() {
        val random = FixedIndexRandom(1)
        assertEquals(BirthdayTheme.ELEPHANT, chooseBirthdayTheme(random))
        assertEquals(BirthdayTheme.ELEPHANT, chooseBirthdayTheme(random))
    }

    /** Supplies a deterministic selection index without changing production selection behavior. */
    private class FixedIndexRandom(private val index: Int) : Random() {
        override fun nextBits(bitCount: Int): Int = error("Use bounded selection")

        override fun nextInt(until: Int): Int {
            require(index in 0 until until)
            return index
        }
    }
}
