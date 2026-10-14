package com.andreavieira.nanithappybirthday.ui.photo

import androidx.compose.runtime.Composable

@Composable
internal fun PhotoSelectionFlow(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onPhotoSelected: (String, () -> Unit) -> Unit,
    onLaunchFailed: () -> Unit,
) {
    // Keep launchers registered while the dialog is hidden and an external app is open.
    val chooseGalleryPhoto =
        rememberGalleryPhotoPicker(
            onPhotoSelected = { source -> onPhotoSelected(source, {}) },
            onLaunchFailed = onLaunchFailed,
        )
    val takeCameraPhoto = rememberCameraPhotoCapture(onPhotoSelected, onLaunchFailed)

    if (isVisible) {
        PhotoChoiceDialog(
            onDismiss = onDismiss,
            onChooseGallery = {
                onDismiss()
                chooseGalleryPhoto()
            },
            onTakePhoto = {
                onDismiss()
                takeCameraPhoto()
            },
        )
    }
}
