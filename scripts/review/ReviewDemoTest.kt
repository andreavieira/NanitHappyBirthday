package com.andreavieira.nanithappybirthday

import android.content.ContentValues
import android.provider.MediaStore
import android.os.SystemClock
import android.os.ParcelFileDescriptor
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.andreavieira.nanithappybirthday.data.profile.profileDataStore
import com.andreavieira.nanithappybirthday.feature.birthday.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Records opt-in review demonstrations in an isolated application installation. */
@RunWith(AndroidJUnit4::class)
class ReviewDemoTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app = instrumentation.targetContext.applicationContext as NanitApplication
    private val device = UiDevice.getInstance(instrumentation)
    private val output get() = requireNotNull(app.getExternalFilesDir(null))
    private val repository get() = app.profileRepository

    @Test fun recordFlow() {
        check(app.packageName.endsWith(".demo")) { "Review demos must never modify the normal app's profile" }
        when (InstrumentationRegistry.getArguments().getString("demo")) {
            "persistence" -> persistence()
            "reload" -> reload("PersistenceReload")
            "themes" -> themes()
            "photos" -> photos()
            "photoReload" -> reload("PhotoReload")
            "share" -> share()
            "ages" -> dates(false)
            "edges" -> dates(true)
            else -> error("Choose a review demo")
        }
    }

    private fun persistence() {
        runBlocking { app.profileDataStore.edit { it.clear() } }
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(By.text("Choose birthday"))
            record("PersistenceSetup") {
                assertEquals("", waitFor(By.clazz("android.widget.EditText")).text)
                pause()
                waitFor(By.clazz("android.widget.EditText")).text = "Alex"
                waitFor(By.text("Choose birthday")).click()
                pause()
                waitFor(By.res("android:id/button1")).click()
                pause()
                showBirthday()
                pause()
                closeBirthday()
                // Edit the actual date picker to show a different saved birthday.
                val birthday = runBlocking { repository.profiles.first().birthday!! }
                waitFor(By.text(birthday.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))).click()
                waitFor(By.res("android:id/date_picker_header_year")).click()
                waitFor(By.text((birthday.year - 1).toString())).click()
                pause()
                waitFor(By.res("android:id/button1")).click()
                waitFor(By.clazz("android.widget.EditText")).text = "Jamie"
                showBirthday()
                pause()
                closeBirthday()
                pause()
            }
        }
    }

    private fun reload(name: String) {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(By.text("Show birthday screen").enabled(true))
            record(name) {
                assertEquals("Jamie", waitFor(By.clazz("android.widget.EditText")).text)
                if (name == "PhotoReload") assertTrue(device.hasObject(By.desc("Selected baby picture")))
                pause()
                device.takeScreenshot(File(output, "DetailsFinal.png"))
                showBirthday()
                pause()
                device.takeScreenshot(File(output, "BirthdayFinal.png"))
            }
        }
    }

    private fun themes() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(By.text("Show birthday screen").enabled(true))
            val seen = mutableSetOf<Int>()
            record("RandomThemes") {
                repeat(12) {
                    if (seen.size < 3) {
                        showBirthday()
                        pause(500)
                        val expectedColors = BirthdayTheme.entries.map { it.backgroundColor.toArgb() }.toSet()
                        var color: Int? = null
                        repeat(30) {
                            if (color == null) {
                                val image = instrumentation.uiAutomation.takeScreenshot()
                                val pixel = image.getPixel(image.width / 2, 8)
                                image.recycle()
                                if (pixel in expectedColors) color = pixel else pause(100)
                            }
                        }
                        seen += requireNotNull(color) { "Theme background did not finish rendering" }
                        pause(1800)
                        closeBirthday()
                    }
                }
                assertEquals("Bounded random-entry demo should encounter all three variants", 3, seen.size)
            }
        }
    }

    private fun photos() {
        val fixture = File(output, "ReviewBaby.jpeg")
        check(fixture.exists())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "NanitReviewBaby.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NanitReview")
            put(MediaStore.Images.Media.IS_FAVORITE, 1)
        }
        val uri = requireNotNull(app.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        try {
            app.contentResolver.openOutputStream(uri)!!.use { stream -> fixture.inputStream().use { it.copyTo(stream) } }
            app.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID), "is_favorite = 1", null, null)!!.use {
                check(it.count == 1) { "Use a demo gallery with no other Favorites before recording" }
            }
            ActivityScenario.launch(MainActivity::class.java).use {
                waitFor(By.text("Show birthday screen").enabled(true))
                record("PhotoSelection") {
                    showBirthday()
                    chooseGallery()
                    pause()
                    closeBirthday()
                    waitFor(By.desc("Remove picture")).click()
                    waitFor(By.desc("Default baby picture"))
                    pause()
                    waitFor(By.text("Choose picture")).click()
                    selectGallery()
                    pause()
                    showBirthday()
                    pause()

                }
            }
        } finally {
            app.contentResolver.delete(uri, null, null)
        }
    }

    private fun chooseGallery() {
        waitFor(By.desc("Choose picture")).click()
        selectGallery()
    }

    private fun selectGallery() {
        // Omit navigation through unrelated personal albums from public review footage.
        pauseRecording()
        waitFor(By.text("Gallery")).click()
        waitFor(By.text("Albums")).click()
        waitFor(By.text("Favorites")).click()
        waitFor(By.descStartsWith("Photo taken"))
        device.waitForIdle()
        resumeRecording()
        pause()
        waitFor(By.descStartsWith("Photo taken")).click()
        waitFor(By.desc("Selected baby picture"))
        device.waitForIdle()
        val profile = runBlocking { repository.profiles.first() }
        val stored = File(repository.photoPath(requireNotNull(profile.photoFileName)))
        assertArrayEquals(File(output, "ReviewBaby.jpeg").readBytes(), stored.readBytes())
    }

    private fun share() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(By.text("Show birthday screen").enabled(true))
            showBirthday()
            waitForShareButton()
            record("ShareFlow") {
                pause()
                waitForShareButton().click()
                waitFor(By.pkg("com.android.intentresolver"))
                pause(2500)
                val exported = requireNotNull(File(app.cacheDir, "birthday-share").listFiles()?.maxByOrNull { it.lastModified() })
                exported.copyTo(File(output, "SharedBirthdayFinal.png"), overwrite = true)
                device.pressBack()
                waitForShareButton()
                pause()
            }
        }
    }

    private fun dates(edges: Boolean) {
        val today = LocalDate.now()
        val cases = if (edges) listOf(
            DateCase("Month end: before milestone", "2025-01-31", "2025-02-27", 0, false),
            DateCase("Month end: clamped milestone", "2025-01-31", "2025-02-28", 1, false),
            DateCase("Leap birthday: day before", "2024-02-29", "2025-02-27", 11, false),
            DateCase("Leap birthday: non-leap milestone", "2024-02-29", "2025-02-28", 1, true),
            DateCase("Leap birthday: leap-year milestone", "2024-02-29", "2028-02-29", 4, true),
        ) else listOf(
            DateCase("Newborn", today.toString(), today.toString(), 0, false),
            DateCase("Completed months", today.minusMonths(6).toString(), today.toString(), 6, false),
            DateCase("First birthday", today.minusYears(1).toString(), today.toString(), 1, true),
            DateCase("Completed years", today.minusYears(2).toString(), today.toString(), 2, true),
        )
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fun render(value: DateCase) {
                val birthday = LocalDate.parse(value.birthday)
                val now = LocalDate.parse(value.today)
                assertEquals(BirthdayAge(value.number, value.years), birthdayAge(birthday, now))
                scenario.onActivity { activity ->
                    activity.setContent {
                        MaterialTheme {
                            Box {
                                BirthdayScreen("Alex", birthday, null, {}, theme = BirthdayTheme.PELICAN,
                                    today = now, contentPadding = PaddingValues(top = 92.dp, bottom = 24.dp))
                                Column(Modifier.padding(start = 20.dp, top = 30.dp)) {
                                    Text(value.label)
                                    Text("Birthday: ${value.birthday}  •  Today: ${value.today}")
                                }
                            }
                        }
                    }
                }
                waitFor(By.text(value.label))
                device.waitForIdle()
            }
            render(cases.first())
            record(if (edges) "DateEdgeCases" else "AgeOptions") {
                for (value in cases) { render(value); pause(2400) }
            }
        }
    }

    private fun waitForShareButton(): UiObject2 {
        val deadline = SystemClock.uptimeMillis() + 10000
        while (SystemClock.uptimeMillis() < deadline) {
            val text = device.findObject(By.text("Share the news"))
            val button = text?.parent
            if (button?.isEnabled == true) return button
            pause(100)
        }
        error("Share button did not become enabled")
    }

    private fun showBirthday() {
        waitFor(By.text("Show birthday screen").enabled(true)).click()
        waitFor(By.desc("Close birthday screen"))
        device.waitForIdle()
    }

    private fun closeBirthday() {
        waitFor(By.desc("Close birthday screen")).click()
        waitFor(By.text("Show birthday screen").enabled(true))
    }

    private fun waitFor(selector: BySelector): UiObject2 = requireNotNull(device.wait(Until.findObject(selector), 10000)) { selector.toString() }
    private fun pause(time: Long = 1400) { SystemClock.sleep(time) }

    private var recordingName = ""
    private var recordingPart = 0
    private var recording: ParcelFileDescriptor? = null
    private var recorderPid: String? = null

    private fun record(name: String, actions: () -> Unit) {
        recordingName = name
        recordingPart = 0
        resumeRecording()
        try { actions() } finally {
            pauseRecording()
            device.dumpWindowHierarchy(File(output, "$name.xml"))
        }
    }

    private fun resumeRecording() {
        val remote = File(output, "$recordingName-${recordingPart++}.mp4").absolutePath
        check(device.executeShellCommand("pidof screenrecord").trim().isEmpty()) { "Another screen recording is active" }
        recording = instrumentation.uiAutomation.executeShellCommand("screenrecord --size 720x1600 --bit-rate 2000000 --time-limit 150 $remote")
        pause(700)
        recorderPid = device.executeShellCommand("pidof screenrecord").trim().also {
            check(it.toIntOrNull() != null) { "Recorder PID was: $it" }
        }
    }

    private fun pauseRecording() {
        recorderPid?.let { device.executeShellCommand("kill -2 $it") }
        pause(1000)
        recording?.close()
        recording = null
        recorderPid = null
    }

    /** Supplies explicit dates and expected age for controlled visual boundary demonstrations. */
    private data class DateCase(val label: String, val birthday: String, val today: String, val number: Int, val years: Boolean)
}
