package com.andreavieira.nanithappybirthday.platform.photo

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.andreavieira.nanithappybirthday.data.profile.ProfilePhotos
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Copies and validates selected images in app-private storage and manages their files. */
class PrivateProfilePhotos(context: Context) : ProfilePhotos {
    private val appContext = context.applicationContext
    private val directory = File(appContext.filesDir, "profile_photos")

    override suspend fun importPhoto(source: String): String =
        withContext(Dispatchers.IO) {
            if (!directory.isDirectory && !directory.mkdirs())
                throw IOException("Photo storage unavailable")
            val destination = File(directory, "${UUID.randomUUID()}.image")
            try {
                val input =
                    appContext.contentResolver.openInputStream(Uri.parse(source))
                        ?: throw IOException("Photo cannot be opened")
                input.use { stream ->
                    destination.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var total = 0L
                        while (true) {
                            val count = stream.read(buffer)
                            if (count == -1) break
                            total += count
                            if (total > MAX_PHOTO_BYTES)
                                throw IOException("Photo exceeds size limit")
                            output.write(buffer, 0, count)
                        }
                    }
                }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(destination.path, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0)
                    throw IOException("Invalid image")
                destination.name
            } catch (failure: Throwable) {
                destination.delete()
                throw failure
            }
        }

    override suspend fun delete(fileName: String) {
        withContext(Dispatchers.IO) { File(path(fileName)).delete() }
    }

    override fun path(fileName: String): String {
        require(File(fileName).name == fileName && fileName !in listOf(".", ".."))
        return File(directory, fileName).absolutePath
    }

    private companion object {
        const val MAX_PHOTO_BYTES = 20L * 1024 * 1024
    }
}
