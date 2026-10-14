package com.andreavieira.nanithappybirthday

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Checks birthday photo choices and cancellation without changing the stored profile or theme. */
@RunWith(AndroidJUnit4::class)
class BirthdayPhotoChoiceTest {
    @Test
    fun cancellingDialogAndGalleryPreservesPhotoAndTheme() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext.applicationContext as NanitApplication
        val device = UiDevice.getInstance(instrumentation)
        val previous = runBlocking { app.profileRepository.profiles.first() }
        runBlocking { app.profileRepository.saveDetails("Alex", LocalDate.of(2025, 1, 15)) }
        try {
            ActivityScenario.launch(MainActivity::class.java).use {
                device.waitForIdle()
                val show =
                    device.wait(
                        Until.findObject(By.text("Show birthday screen").enabled(true)),
                        10000,
                    )
                requireNotNull(show).click()
                assertTrue(device.wait(Until.hasObject(By.desc("Choose picture")), 5000))
                device.waitForIdle()
                val before = instrumentation.uiAutomation.takeScreenshot()
                val themeColor = before.getPixel(before.width / 2, before.height / 10)
                before.recycle()
                device.findObject(By.desc("Choose picture")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Gallery")), 5000))
                assertTrue(device.hasObject(By.text("Take photo")))
                device.pressBack()
                assertTrue(device.wait(Until.hasObject(By.desc("Choose picture")), 5000))
                device.findObject(By.desc("Choose picture")).click()
                requireNotNull(device.wait(Until.findObject(By.text("Gallery")), 5000)).click()
                assertTrue(device.wait(Until.hasObject(By.text("Photos")), 10000))
                device.waitForIdle()
                device.pressBack()
                assertTrue(device.wait(Until.hasObject(By.desc("Choose picture")), 5000))
                device.waitForIdle()
                val after = instrumentation.uiAutomation.takeScreenshot()
                assertEquals(themeColor, after.getPixel(after.width / 2, after.height / 10))
                after.recycle()
                assertEquals(
                    previous.photoFileName,
                    runBlocking { app.profileRepository.profiles.first() }.photoFileName,
                )
            }
        } finally {
            runBlocking { app.profileRepository.saveDetails(previous.name, previous.birthday) }
        }
    }
}
