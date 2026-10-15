package com.andreavieira.nanithappybirthday.ui.photo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.andreavieira.nanithappybirthday.platform.photo.CameraCaptureFiles

/** Owns the pending capture URI and keeps its file until photo import finishes. */
@Composable
internal fun rememberCameraPhotoCapture(
    onPhotoSelected: (String, () -> Unit) -> Unit,
    onLaunchFailed: () -> Unit,
): () -> Unit {
    // File creation and cleanup need only the application context. Cleanup can outlive this UI.
    val appContext = LocalContext.current.applicationContext

    // Keep the output URI across recreation while the external camera is open.
    var pendingCapture by rememberSaveable { mutableStateOf<String?>(null) }

    // Handle the camera result; delete the temporary file only after import or cancellation.
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val source = pendingCapture
            pendingCapture = null
            if (source != null) {
                if (success) {
                    onPhotoSelected(source) { CameraCaptureFiles.discard(appContext, source) }
                } else {
                    CameraCaptureFiles.discard(appContext, source)
                }
            }
        }

    // Start a new capture with a writable URI supplied through FileProvider.
    return {
        try {
            val uri = CameraCaptureFiles.create(appContext)
            pendingCapture = uri.toString()
            launcher.launch(uri)
        } catch (_: Exception) {
            pendingCapture?.let { CameraCaptureFiles.discard(appContext, it) }
            pendingCapture = null
            onLaunchFailed()
        }
    }
}
