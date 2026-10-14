package com.andreavieira.nanithappybirthday.feature.details

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andreavieira.nanithappybirthday.NanitApplication
import com.andreavieira.nanithappybirthday.R
import com.andreavieira.nanithappybirthday.ui.theme.DetailsTheme
import com.andreavieira.nanithappybirthday.ui.photo.PhotoChoiceDialog
import com.andreavieira.nanithappybirthday.ui.photo.rememberCameraPhotoCapture
import com.andreavieira.nanithappybirthday.ui.photo.rememberGalleryPhotoPicker
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun DetailsRoute() {
    DetailsTheme { DetailsRouteContent() }
}

@Composable
private fun DetailsRouteContent() {
    // Screen dependencies: the UI context opens dialogs; the application owns the repository.
    val context = LocalContext.current
    val application = context.applicationContext as NanitApplication
    val detailsViewModel: DetailsViewModel = viewModel(factory = viewModelFactory {
        initializer { DetailsViewModel(application.profileRepository) }
    })
    val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()

    // Temporary birthday action feedback, replaced by navigation in stage 3.
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val readyMessage = stringResource(R.string.details_ready)
    val saveFailedMessage = stringResource(R.string.save_failed)

    // Photo selection: dialog visibility and separate gallery/camera actions.
    var showPhotoPicker by rememberSaveable { mutableStateOf(false) }
    val chooseGalleryPhoto = rememberGalleryPhotoPicker(
        onPhotoSelected = detailsViewModel::selectPhoto,
        onLaunchFailed = detailsViewModel::reportCameraError,
    )
    val takeCameraPhoto = rememberCameraPhotoCapture(
        onPhotoSelected = { source, onFinished -> detailsViewModel.selectPhoto(source, onFinished) },
        onLaunchFailed = detailsViewModel::reportCameraError,
    )

    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        DetailsScreen(
            uiState = uiState,
            onNameChange = detailsViewModel::updateName,
            onChooseBirthday = {
                showBirthdayPicker(context, uiState.birthday, detailsViewModel::updateBirthday)
            },
            onChoosePhoto = {
                detailsViewModel.dismissPhotoError()
                showPhotoPicker = true
            },
            onRemovePhoto = detailsViewModel::removePhoto,
            onRetry = detailsViewModel::retry,
            onDismissError = detailsViewModel::dismissPhotoError,
            // Birthday navigation is added in stage 3.
            onShowBirthday = {
                scope.launch {
                    val saved = detailsViewModel.saveBeforeBirthday()
                    // Stage 3 will navigate with the in-memory draft regardless of this result.
                    snackbarHost.showSnackbar(if (saved) readyMessage else saveFailedMessage)
                }
            },
            modifier = Modifier.padding(padding),
        )
    }
    if (showPhotoPicker) {
        PhotoChoiceDialog(
            onDismiss = { showPhotoPicker = false },
            onChooseGallery = {
                showPhotoPicker = false
                chooseGalleryPhoto()
            },
            onTakePhoto = {
                showPhotoPicker = false
                takeCameraPhoto()
            },
        )
    }
}

private fun showBirthdayPicker(
    context: Context,
    birthday: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()
    val initial = birthday ?: today
    DatePickerDialog(
        context,
        R.style.Theme_NanitHappyBirthday_DetailsDatePicker,
        { _, year, month, day -> onDateSelected(LocalDate.of(year, month + 1, day)) },
        initial.year, initial.monthValue - 1, initial.dayOfMonth,
    ).apply {
        datePicker.maxDate = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.show()
}
