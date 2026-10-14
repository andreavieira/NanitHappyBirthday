package com.andreavieira.nanithappybirthday

import android.os.SystemClock
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.graphics.toArgb
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.andreavieira.nanithappybirthday.feature.birthday.BirthdayScreen
import com.andreavieira.nanithappybirthday.feature.birthday.BirthdayTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

/** Renders every supplied variant with a placeholder and captures isolated review screenshots. */
@RunWith(AndroidJUnit4::class)
class BirthdayThemeRenderingTest {
    @Test fun renderAllThemeVariants() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val context = instrumentation.targetContext
        val statusBarId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = context.resources.getDimensionPixelSize(statusBarId)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            for (theme in BirthdayTheme.entries) {
                scenario.onActivity { activity ->
                    activity.setContent {
                        MaterialTheme {
                            Scaffold(containerColor = theme.backgroundColor, contentWindowInsets = WindowInsets.safeDrawing) { padding ->
                                BirthdayScreen("Alex", LocalDate.of(2025, 1, 15), null, {}, theme = theme, contentPadding = padding, onChoosePhoto = {})
                            }
                        }
                    }
                }
                assertTrue(device.wait(Until.hasObject(By.desc("Close birthday screen")), 5000))
                device.waitForIdle()
                // setContent schedules a frame; the existing back control alone does not signal it has drawn.
                val deadline = SystemClock.uptimeMillis() + 5000
                var renderedColor: Int
                do {
                    val screenshot = instrumentation.uiAutomation.takeScreenshot()
                    renderedColor = screenshot.getPixel(screenshot.width / 2, statusBarHeight + 5)
                    screenshot.recycle()
                    if (renderedColor == theme.backgroundColor.toArgb()) break
                    SystemClock.sleep(50)
                } while (SystemClock.uptimeMillis() < deadline)
                assertEquals(theme.backgroundColor.toArgb(), renderedColor)
                assertTrue(device.takeScreenshot(File(context.getExternalFilesDir(null), "Birthday${theme.name}.png")))
            }
        }
    }
}
