package com.andreavieira.nanithappybirthday

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.andreavieira.nanithappybirthday.data.profile.DataStoreProfileStore
import com.andreavieira.nanithappybirthday.data.profile.DefaultProfileRepository
import com.andreavieira.nanithappybirthday.platform.photo.CameraCaptureFiles
import com.andreavieira.nanithappybirthday.platform.photo.PrivateProfilePhotos
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.util.UUID

/** Verifies that real DataStore metadata and private photo files survive storage recreation. */
@RunWith(AndroidJUnit4::class)
class ProfileStorageTest {
    @Test fun profileAndCopiedPhotoSurviveDataStoreRecreation() = runBlocking {
        val app = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(app.cacheDir, "storage_test_${UUID.randomUUID()}").apply { mkdirs() }
        val isolated = object : ContextWrapper(app) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = root
        }
        val preferences = File(root, "test.preferences_pb")
        var job = SupervisorJob()
        fun store() = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(job + Dispatchers.IO), produceFile = { preferences },
        )
        val source = CameraCaptureFiles.create(app)
        try {
            app.contentResolver.openOutputStream(source)!!.use {
                val bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.GREEN)
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                bitmap.recycle()
            }
            val repository = DefaultProfileRepository(DataStoreProfileStore(store()), PrivateProfilePhotos(isolated))
            repository.saveDetails("Storage test", LocalDate.of(2025, 1, 15))
            val copiedPath = repository.selectPhoto(source.toString())
            CameraCaptureFiles.discard(app, source.toString())
            assertTrue(File(copiedPath).isFile)
            job.cancelAndJoin()
            job = SupervisorJob()
            val restored = DefaultProfileRepository(DataStoreProfileStore(store()), PrivateProfilePhotos(isolated))
            val profile = restored.profiles.first()
            assertEquals("Storage test", profile.name)
            assertEquals(LocalDate.of(2025, 1, 15), profile.birthday)
            assertEquals(copiedPath, restored.photoPath(profile.photoFileName!!))
            assertTrue(File(copiedPath).length() > 0)
        } finally {
            job.cancelAndJoin()
            CameraCaptureFiles.discard(app, source.toString())
            root.deleteRecursively()
        }
    }
}
