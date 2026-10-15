package com.andreavieira.nanithappybirthday.ui.photo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/** Returns an action that opens the gallery. Cancellation leaves the profile unchanged. */
@Composable
internal fun rememberGalleryPhotoPicker(
    onPhotoSelected: (String) -> Unit,
    onLaunchFailed: () -> Unit,
): () -> Unit {
    // The system picker supplies a URI; the repository copies its contents into private storage.
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { onPhotoSelected(it.toString()) }
        }

    // Request images only. Dismissing the picker produces no profile update.
    return {
        try {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (_: Exception) {
            onLaunchFailed()
        }
    }
}
