package com.andreavieira.nanithappybirthday.platform.photo

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/** Creates FileProvider capture URIs and safely removes temporary camera files. */
object CameraCaptureFiles {
    fun create(context: Context): Uri {
        val directory = File(context.cacheDir, "camera")
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Camera storage unavailable")
        val file = File.createTempFile("capture_", ".jpg", directory)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun discard(context: Context, source: String) {
        val uri = Uri.parse(source)
        if (uri.authority != "${context.packageName}.fileprovider") return
        val segments = uri.pathSegments
        if (segments.size != 2 || segments.first() != "camera") return
        val name = segments.last()
        if (File(name).name == name && name.startsWith("capture_")) {
            File(File(context.cacheDir, "camera"), name).delete()
        }
    }
}
