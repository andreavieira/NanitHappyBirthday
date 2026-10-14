package com.andreavieira.nanithappybirthday.feature.birthday

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andreavieira.nanithappybirthday.R
import com.andreavieira.nanithappybirthday.feature.details.DetailsError
import com.andreavieira.nanithappybirthday.feature.details.DetailsViewModel
import com.andreavieira.nanithappybirthday.ui.photo.PhotoSelectionFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BirthdayRoute(
    name: String,
    birthday: LocalDate,
    initialPhotoPath: String?,
    theme: BirthdayTheme,
    detailsViewModel: DetailsViewModel,
    onClose: () -> Unit,
    contentPadding: PaddingValues,
) {
    val state by detailsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val photoPath = if (state.hasLoadedProfile) state.photoPath else initialPhotoPath

    // Only the artwork layer is recorded. Controls and system UI are excluded from the export.
    val shareImageLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val sharing = rememberBirthdayShareAction(shareImageLayer, contentPadding) {
        scope.launch { snackbar.showSnackbar(context.getString(R.string.share_failed), withDismissAction = true) }
    }
    var photoReady by remember(photoPath) { mutableStateOf(photoPath == null) }

    var showPhotoPicker by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.error) {
        val message = when (state.error) {
            DetailsError.PHOTO -> R.string.photo_failed
            DetailsError.CAMERA -> R.string.photo_app_unavailable
            else -> null
        }
        if (message != null) {
            snackbar.showSnackbar(context.getString(message), withDismissAction = true)
            detailsViewModel.dismissPhotoError()
        }
    }
    Box {
        BirthdayScreen(
            name = name,
            birthday = birthday,
            photoPath = photoPath,
            theme = theme,
            onClose = onClose,
            contentPadding = contentPadding,
            onChoosePhoto = {
                detailsViewModel.dismissPhotoError()
                showPhotoPicker = true
            },
            photoChoiceEnabled = state.canEdit && state.pendingSaves == 0 && !sharing.isPreparing,
            shareImageLayer = shareImageLayer,
            onPhotoReady = { photoReady = it },
            shareEnabled = photoReady && state.pendingSaves == 0 && !sharing.isPreparing,
            onShare = sharing.launch,
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(contentPadding))
    }
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(
        primary = Color(0xFF394562),
        surfaceContainerHigh = theme.backgroundColor,
    )) {
        PhotoSelectionFlow(
            isVisible = showPhotoPicker,
            onDismiss = { showPhotoPicker = false },
            onPhotoSelected = { source, onFinished -> detailsViewModel.selectPhoto(source, onFinished) },
            onLaunchFailed = detailsViewModel::reportCameraError,
        )
    }
}
