package com.andreavieira.nanithappybirthday

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.andreavieira.nanithappybirthday.data.profile.DataStoreProfileStore
import com.andreavieira.nanithappybirthday.data.profile.DefaultProfileRepository
import com.andreavieira.nanithappybirthday.feature.birthday.BirthdayRoute
import com.andreavieira.nanithappybirthday.feature.birthday.BirthdayTheme
import com.andreavieira.nanithappybirthday.feature.details.DetailsRoute
import com.andreavieira.nanithappybirthday.feature.details.DetailsViewModel
import com.andreavieira.nanithappybirthday.platform.photo.PrivateProfilePhotos
import java.io.File
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises real system photo activities using isolated storage and a generated gallery image. */
@RunWith(AndroidJUnit4::class)
class BirthdayPhotoImportTest {
    @Test
    fun galleryAndCameraImportsPersistWithoutChangingTheme() =
        runBlocking<Unit> {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val app = instrumentation.targetContext
            val device = UiDevice.getInstance(instrumentation)
            val root =
                File(app.cacheDir, "birthday_photo_test_${UUID.randomUUID()}").apply { mkdirs() }
            val isolated =
                object : ContextWrapper(app) {
                    override fun getApplicationContext(): Context = this

                    override fun getFilesDir(): File = root
                }
            val job = SupervisorJob()
            val store =
                PreferenceDataStoreFactory.create(
                    scope = CoroutineScope(job + Dispatchers.IO),
                    produceFile = { File(root, "profile.preferences_pb") },
                )
            val repository =
                DefaultProfileRepository(
                    DataStoreProfileStore(store),
                    PrivateProfilePhotos(isolated),
                )
            repository.saveDetails("Alex", LocalDate.of(2025, 1, 15))
            val values =
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "BirthdayFlowTest.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            val image =
                requireNotNull(
                    app.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                )
            try {
                app.contentResolver.openOutputStream(image)!!.use { output ->
                    val bitmap = Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.MAGENTA)
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                    bitmap.recycle()
                }
                app.contentResolver.update(
                    image,
                    ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                    null,
                    null,
                )
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    lateinit var viewModel: DetailsViewModel
                    scenario.onActivity { activity ->
                        viewModel =
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
                            )["isolated-photo-flow", DetailsViewModel::class.java]
                        activity.setContent {
                            MaterialTheme {
                                Scaffold(
                                    containerColor = BirthdayTheme.FOX.backgroundColor,
                                    contentWindowInsets = WindowInsets.safeDrawing,
                                ) { padding ->
                                    BirthdayRoute(
                                        "Alex",
                                        LocalDate.of(2025, 1, 15),
                                        null,
                                        BirthdayTheme.FOX,
                                        viewModel,
                                        onClose = {
                                            activity.setContent {
                                                MaterialTheme {
                                                    DetailsRoute(
                                                        viewModel,
                                                        { _, _ -> },
                                                        onSaveFailed = {},
                                                    )
                                                }
                                            }
                                        },
                                        contentPadding = padding,
                                    )
                                }
                            }
                        }
                    }
                    val badge = By.desc("Choose picture").enabled(true)
                    requireNotNull(device.wait(Until.findObject(badge), 10000)).click()
                    assertTrue(device.wait(Until.hasObject(By.text("Gallery")), 5000))
                    assertTrue(
                        device.takeScreenshot(
                            File(app.getExternalFilesDir(null), "BirthdayPhotoChoices.png")
                        )
                    )
                    device.findObject(By.text("Gallery")).click()
                    assertTrue(device.wait(Until.hasObject(By.text("Photos")), 10000))
                    val thumbnail =
                        requireNotNull(
                            device.wait(Until.findObject(By.descStartsWith("Photo taken")), 10000)
                        )
                    thumbnail.click()
                    assertTrue(device.wait(Until.hasObject(badge), 10000))
                    withTimeout(10000) {
                        while (viewModel.uiState.value.photoPath == null) delay(50)
                    }
                    val path = requireNotNull(viewModel.uiState.value.photoPath)
                    assertTrue(File(path).length() > 0)
                    assertEquals(
                        path,
                        repository.photoPath(
                            requireNotNull(repository.profiles.first().photoFileName)
                        ),
                    )
                    assertTrue(
                        device.takeScreenshot(
                            File(app.getExternalFilesDir(null), "BirthdayGalleryImport.png")
                        )
                    )
                    device.findObject(badge).click()
                    requireNotNull(device.wait(Until.findObject(By.text("Take photo")), 5000))
                        .click()
                    assertTrue(device.wait(Until.hasObject(By.desc("Shutter")), 10000))
                    device.pressBack()
                    assertTrue(device.wait(Until.hasObject(badge), 10000))
                    assertEquals(path, viewModel.uiState.value.photoPath)
                    device.findObject(badge).click()
                    requireNotNull(device.wait(Until.findObject(By.text("Take photo")), 5000))
                        .click()
                    requireNotNull(device.wait(Until.findObject(By.desc("Shutter")), 10000)).click()
                    requireNotNull(device.wait(Until.findObject(By.desc("Done")), 5000)).click()
                    assertTrue(device.wait(Until.hasObject(badge), 10000))
                    withTimeout(10000) {
                        while (viewModel.uiState.value.photoPath == path) delay(50)
                    }
                    val capturedPath = requireNotNull(viewModel.uiState.value.photoPath)
                    assertTrue(File(capturedPath).length() > 0)
                    assertEquals(
                        capturedPath,
                        repository.photoPath(
                            requireNotNull(repository.profiles.first().photoFileName)
                        ),
                    )
                    assertTrue(
                        device.takeScreenshot(
                            File(app.getExternalFilesDir(null), "BirthdayCameraImport.png")
                        )
                    )
                    viewModel.selectPhoto("content://invalid.birthday.test/missing")
                    assertTrue(
                        device.wait(
                            Until.hasObject(By.text(app.getString(R.string.photo_failed))),
                            10000,
                        )
                    )
                    assertEquals(capturedPath, viewModel.uiState.value.photoPath)
                    device.findObject(badge).click()
                    assertTrue(device.wait(Until.hasObject(By.text("Gallery")), 5000))
                    assertTrue(
                        device.wait(Until.gone(By.text(app.getString(R.string.photo_failed))), 5000)
                    )
                    device.pressBack()
                    viewModel.reportCameraError()
                    assertTrue(
                        device.wait(
                            Until.hasObject(By.text(app.getString(R.string.photo_app_unavailable))),
                            5000,
                        )
                    )
                    device.findObject(badge).click()
                    assertTrue(device.wait(Until.hasObject(By.text("Gallery")), 5000))
                    assertTrue(
                        device.wait(
                            Until.gone(By.text(app.getString(R.string.photo_app_unavailable))),
                            5000,
                        )
                    )
                    device.pressBack()
                    requireNotNull(
                            device.wait(Until.findObject(By.desc("Close birthday screen")), 5000)
                        )
                        .click()
                    assertTrue(device.wait(Until.hasObject(By.text("Show birthday screen")), 5000))
                    assertTrue(device.hasObject(By.desc("Selected baby picture")))
                    assertEquals(capturedPath, viewModel.uiState.value.photoPath)
                }
            } finally {
                app.contentResolver.delete(image, null, null)
                job.cancelAndJoin()
                root.deleteRecursively()
            }
        }
}
