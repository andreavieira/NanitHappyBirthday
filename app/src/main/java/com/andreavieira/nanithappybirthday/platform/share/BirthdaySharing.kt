package com.andreavieira.nanithappybirthday.platform.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/** Stores shareable card images in a dedicated cache directory and grants temporary read access. */
object BirthdaySharing {
    fun writeImage(context: Context, bitmap: Bitmap): Uri {
        val directory =
            File(context.cacheDir, "birthday-share").apply { check(mkdirs() || isDirectory) }
        // Keep recent URIs valid for receivers; remove only our exports older than one day.
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        directory.listFiles()?.filter { it.lastModified() < cutoff }?.forEach { it.delete() }
        val file = File(directory, "Birthday-${UUID.randomUUID()}.png")
        try {
            file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (failure: Exception) {
            file.delete()
            throw failure
        }
    }

    fun shareIntent(context: Context, uri: Uri): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(context.contentResolver, "Birthday", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
}
