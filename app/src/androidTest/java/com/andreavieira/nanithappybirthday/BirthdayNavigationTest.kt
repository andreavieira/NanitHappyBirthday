package com.andreavieira.nanithappybirthday

import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.io.File

/** Exercises birthday entry, close/back, and activity recreation while preserving the prior stored profile. */
@RunWith(AndroidJUnit4::class)
class BirthdayNavigationTest {
    @Test fun birthdayEntryCloseBackAndRecreation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext.applicationContext as NanitApplication
        val recordDemo = InstrumentationRegistry.getArguments().getString("recordDemo") == "true"
        val previous = runBlocking { app.profileRepository.profiles.first() }
        val device = UiDevice.getInstance(instrumentation)
        runBlocking { app.profileRepository.saveDetails("Navigation test", LocalDate.of(2025, 1, 15)) }
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                assertTrue(device.wait(Until.hasObject(By.text("Show birthday screen").enabled(true)), 10000))
                if (recordDemo) SystemClock.sleep(1000)
                clickShowBirthday(device)
                assertTrue(device.wait(Until.hasObject(By.desc("Close birthday screen")), 5000))
                assertTrue(device.hasObject(By.text("TODAY NAVIGATION TEST IS")))
                val backBounds = device.findObject(By.desc("Close birthday screen")).visibleBounds
                assertTrue(backBounds.width() >= 48 * app.resources.displayMetrics.density - 1)
                assertTrue(backBounds.height() >= 48 * app.resources.displayMetrics.density - 1)
                assertTrue(kotlin.math.abs(backBounds.left - 4 * app.resources.displayMetrics.density) <= 1)
                assertTrue(backBounds.top >= 4 * app.resources.displayMetrics.density - 1)
                if (recordDemo) {
                    SystemClock.sleep(1200)
                    assertTrue(device.takeScreenshot(File(app.getExternalFilesDir(null), "BirthdayNavigation.png")))
                }
                val beforeRotation = instrumentation.uiAutomation.takeScreenshot()
                val themePixel = beforeRotation.getPixel(beforeRotation.width / 2, backBounds.top)
                beforeRotation.recycle()
                scenario.recreate()
                assertTrue(device.wait(Until.hasObject(By.desc("Close birthday screen")), 5000))
                device.waitForIdle()
                val afterRotation = instrumentation.uiAutomation.takeScreenshot()
                assertEquals(themePixel, afterRotation.getPixel(afterRotation.width / 2, backBounds.top))
                afterRotation.recycle()
                if (recordDemo) SystemClock.sleep(1000)
                device.findObject(By.desc("Close birthday screen")).click()
                assertTrue(device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 5000))
                assertEquals("Navigation test", device.findObject(By.clazz("android.widget.EditText")).text)
                if (recordDemo) SystemClock.sleep(1000)
                clickShowBirthday(device)
                assertTrue(device.wait(Until.hasObject(By.desc("Close birthday screen")), 5000))
                if (recordDemo) SystemClock.sleep(1000)
                device.pressBack()
                assertTrue(device.wait(Until.hasObject(By.text("Show birthday screen")), 5000))
                if (recordDemo) SystemClock.sleep(1000)
            }
        } finally {
            runBlocking { app.profileRepository.saveDetails(previous.name, previous.birthday) }
        }
    }

    private fun clickShowBirthday(device: UiDevice) {
        // Profile loading can replace the accessibility node after it first becomes enabled.
        repeat(3) { attempt ->
            device.waitForIdle()
            val button = device.wait(Until.findObject(By.text("Show birthday screen").enabled(true)), 5000)
            requireNotNull(button)
            try {
                button.click()
                return
            } catch (stale: StaleObjectException) {
                if (attempt == 2) throw stale
            }
        }
    }

}
