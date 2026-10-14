package com.andreavieira.nanithappybirthday

import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

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
                device.findObject(By.text("Show birthday screen")).click()
                assertTrue(device.wait(Until.hasObject(By.desc("Close birthday screen")), 5000))
                assertTrue(device.hasObject(By.text("Navigation test")))
                if (recordDemo) SystemClock.sleep(1200)
                scenario.recreate()
                assertTrue(device.wait(Until.hasObject(By.desc("Close birthday screen")), 5000))
                if (recordDemo) SystemClock.sleep(1000)
                device.findObject(By.desc("Close birthday screen")).click()
                assertTrue(device.wait(Until.hasObject(By.clazz("android.widget.EditText")), 5000))
                assertEquals("Navigation test", device.findObject(By.clazz("android.widget.EditText")).text)
                if (recordDemo) SystemClock.sleep(1000)
                device.findObject(By.text("Show birthday screen")).click()
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
}
