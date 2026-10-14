package com.andreavieira.nanithappybirthday

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import coil.imageLoader
import coil.request.ImageRequest
import com.andreavieira.nanithappybirthday.data.profile.BabyProfile
import com.andreavieira.nanithappybirthday.data.profile.DefaultProfileRepository
import com.andreavieira.nanithappybirthday.data.profile.ProfileStore
import com.andreavieira.nanithappybirthday.feature.birthday.BirthdayRoute
import com.andreavieira.nanithappybirthday.feature.birthday.BirthdayTheme
import com.andreavieira.nanithappybirthday.feature.details.DetailsViewModel
import com.andreavieira.nanithappybirthday.platform.photo.PrivateProfilePhotos
import com.andreavieira.nanithappybirthday.platform.share.BirthdaySharing
import java.io.File
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies real Sharesheet exports omit controls/system bars and preserve each birthday theme. */
@RunWith(AndroidJUnit4::class)
class BirthdaySharingTest {
    @Test
    fun exportedCardsExcludeControlsAndSystemBars() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        val directory = File(app.cacheDir, "birthday-share")
        val existing = directory.listFiles()?.map { it.name }?.toSet().orEmpty()
        val photo =
            File(
                File(app.filesDir, "profile_photos").apply { mkdirs() },
                "sharing-test-${UUID.randomUUID()}.image",
            )
        photo.outputStream().use { output ->
            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.MAGENTA)
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            bitmap.recycle()
        }
        // Warm the image cache to cover photos that finish before composition effects run.
        runBlocking {
            app.imageLoader.execute(
                ImageRequest.Builder(app)
                    .data(photo.absolutePath)
                    .size((200 * app.resources.displayMetrics.density).toInt())
                    .build()
            )
        }
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                var top = 0
                var bottom = 0
                var left = 0
                var right = 0
                for (theme in BirthdayTheme.entries) {
                    val prior = directory.listFiles()?.map { it.name }?.toSet().orEmpty()
                    scenario.onActivity { activity ->
                        val repository =
                            DefaultProfileRepository(
                                MemoryProfileStore(
                                    if (theme == BirthdayTheme.PELICAN) photo.name else null
                                ),
                                PrivateProfilePhotos(app),
                            )
                        val vm =
                            ViewModelProvider(
                                activity,
                                object : ViewModelProvider.Factory {
                                    override fun <T : androidx.lifecycle.ViewModel> create(
                                        modelClass: Class<T>
                                    ): T {
                                        @Suppress("UNCHECKED_CAST")
                                        return DetailsViewModel(repository) as T
                                    }
                                },
                            )["sharing-${theme.name}", DetailsViewModel::class.java]
                        val insets =
                            requireNotNull(
                                    ViewCompat.getRootWindowInsets(activity.window.decorView)
                                )
                                .getInsets(
                                    WindowInsetsCompat.Type.systemBars() or
                                        WindowInsetsCompat.Type.displayCutout()
                                )
                        top = insets.top
                        bottom = insets.bottom
                        left = insets.left
                        right = insets.right
                        activity.setContent {
                            MaterialTheme {
                                Scaffold(
                                    containerColor = theme.backgroundColor,
                                    contentWindowInsets = WindowInsets.safeDrawing,
                                ) { padding ->
                                    BirthdayRoute(
                                        "Alex",
                                        LocalDate.of(2025, 1, 15),
                                        null,
                                        theme,
                                        vm,
                                        {},
                                        padding,
                                    )
                                }
                            }
                        }
                    }
                    val photoDescription =
                        if (theme == BirthdayTheme.PELICAN) "Selected baby picture"
                        else "Default baby picture"
                    val photoObject =
                        requireNotNull(
                            device.wait(Until.findObject(By.desc(photoDescription)), 10000)
                        )
                    val share =
                        requireNotNull(
                            device.wait(
                                Until.findObject(By.text("Share the news").enabled(true)),
                                10000,
                            )
                        )
                    device.waitForIdle()
                    val backBounds =
                        device.findObject(By.desc("Close birthday screen")).visibleBounds
                    val cameraBounds = device.findObject(By.desc("Choose picture")).visibleBounds
                    val shareBounds = requireNotNull(share.parent).visibleBounds
                    val photoBounds = photoObject.visibleBounds
                    val logoBounds =
                        requireNotNull(device.findObject(By.desc("Nanit"))).visibleBounds
                    val live = instrumentation.uiAutomation.takeScreenshot()
                    val density = app.resources.displayMetrics.density
                    assertEquals(
                        "Share is anchored above the safe bottom",
                        53 * density,
                        (live.height - bottom - shareBounds.bottom).toFloat(),
                        2f,
                    )
                    assertTrue(
                        "Share remains at least 53 dp below the logo",
                        shareBounds.top - logoBounds.bottom >= 53 * density - 2,
                    )
                    assertEquals(
                        "Photo-to-logo spacing",
                        15 * density,
                        (logoBounds.top - photoBounds.bottom).toFloat(),
                        2f,
                    )
                    val safeHeight = (live.height - top - bottom) / density
                    val expectedPhotoTop =
                        (safeHeight / 2 - 122 - if (theme == BirthdayTheme.PELICAN) 8 else 0)
                            .coerceAtLeast(224f)
                    assertEquals(
                        "Photo keeps its Stage 3 placement",
                        expectedPhotoTop * density,
                        (photoBounds.top - top).toFloat(),
                        2f,
                    )
                    assertTrue(
                        device.takeScreenshot(
                            File(app.getExternalFilesDir(null), "BirthdayShare${theme.name}.png")
                        )
                    )
                    share.click()
                    assertTrue(
                        device.wait(Until.hasObject(By.pkg("com.android.intentresolver")), 10000)
                    )
                    val file =
                        requireNotNull(directory.listFiles()?.firstOrNull { it.name !in prior })
                    val exported = requireNotNull(BitmapFactory.decodeFile(file.absolutePath))
                    assertEquals(live.width - left - right, exported.width)
                    assertEquals(live.height - top - bottom, exported.height)
                    assertControlOmitted(live, exported, backBounds, left, top)
                    assertControlOmitted(live, exported, cameraBounds, left, top)
                    assertControlOmitted(live, exported, shareBounds, left, top)
                    if (theme == BirthdayTheme.PELICAN) {
                        assertEquals(
                            android.graphics.Color.MAGENTA,
                            exported.getPixel(
                                photoBounds.centerX() - left,
                                photoBounds.centerY() - top,
                            ),
                        )
                    }
                    file.copyTo(
                        File(app.getExternalFilesDir(null), "BirthdayShared${theme.name}.png"),
                        overwrite = true,
                    )
                    val uri = BirthdaySharing.writeImage(app, exported)
                    val intent = BirthdaySharing.shareIntent(app, uri)
                    assertEquals(Intent.ACTION_SEND, intent.action)
                    assertEquals("image/png", intent.type)
                    assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
                    assertEquals(uri, intent.clipData!!.getItemAt(0).uri)
                    app.contentResolver.openInputStream(uri)!!.use { assertTrue(it.read() >= 0) }
                    exported.recycle()
                    live.recycle()
                    device.pressBack()
                    assertTrue(
                        device.wait(Until.hasObject(By.text("Share the news").enabled(true)), 5000)
                    )
                }
            }
        } finally {
            photo.delete()
            directory.listFiles()?.filter { it.name !in existing }?.forEach { it.delete() }
        }
    }

    @Test
    fun shortViewportExportsTheWholeCardAfterScrolling() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        val directory = File(app.cacheDir, "birthday-share")
        val existing = directory.listFiles()?.map { it.name }?.toSet().orEmpty()
        var viewportHeight = 0
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    viewportHeight = (360 * activity.resources.displayMetrics.density).toInt()
                    val repository =
                        DefaultProfileRepository(MemoryProfileStore(), PrivateProfilePhotos(app))
                    val vm =
                        ViewModelProvider(
                            activity,
                            object : ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(
                                    modelClass: Class<T>
                                ): T {
                                    @Suppress("UNCHECKED_CAST")
                                    return DetailsViewModel(repository) as T
                                }
                            },
                        )["short-sharing", DetailsViewModel::class.java]
                    activity.setContent {
                        MaterialTheme {
                            Box(Modifier.height(360.dp)) {
                                BirthdayRoute(
                                    "Alex",
                                    LocalDate.of(2025, 1, 15),
                                    null,
                                    BirthdayTheme.FOX,
                                    vm,
                                    {},
                                    androidx.compose.foundation.layout.PaddingValues(),
                                )
                            }
                        }
                    }
                }
                assertTrue(device.wait(Until.hasObject(By.text("TODAY ALEX IS")), 10000))
                device.swipe(
                    device.displayWidth / 2,
                    viewportHeight - 80,
                    device.displayWidth / 2,
                    100,
                    30,
                )
                val share =
                    requireNotNull(
                        device.wait(
                            Until.findObject(By.text("Share the news").enabled(true)),
                            10000,
                        )
                    )
                share.click()
                assertTrue(
                    device.wait(Until.hasObject(By.pkg("com.android.intentresolver")), 10000)
                )
                val file =
                    requireNotNull(directory.listFiles()?.firstOrNull { it.name !in existing })
                val exported = requireNotNull(BitmapFactory.decodeFile(file.absolutePath))
                assertTrue(
                    "Export must include content beyond the scrolled viewport",
                    exported.height > viewportHeight,
                )
                file.copyTo(
                    File(app.getExternalFilesDir(null), "BirthdaySharedShortViewport.png"),
                    overwrite = true,
                )
                exported.recycle()
                device.pressBack()
            }
        } finally {
            directory.listFiles()?.filter { it.name !in existing }?.forEach { it.delete() }
        }
    }

    private fun assertControlOmitted(
        live: Bitmap,
        exported: Bitmap,
        bounds: Rect,
        left: Int,
        top: Int,
    ) {
        var different = 0
        for (y in bounds.top until bounds.bottom) for (x in bounds.left until bounds.right) {
            val ex = x - left
            val ey = y - top
            if (
                ex in 0 until exported.width &&
                    ey in 0 until exported.height &&
                    live.getPixel(x, y) != exported.getPixel(ex, ey)
            )
                different++
        }
        assertTrue("Control must not appear in the exported card", different > 50)
    }

    /** Keeps sharing fixtures separate from the saved child profile. */
    private class MemoryProfileStore(photoName: String? = null) : ProfileStore {
        override val profiles =
            MutableStateFlow(BabyProfile("Alex", LocalDate.of(2025, 1, 15), photoName))

        override suspend fun update(transform: (BabyProfile) -> BabyProfile) {
            profiles.value = transform(profiles.value)
        }
    }
}
