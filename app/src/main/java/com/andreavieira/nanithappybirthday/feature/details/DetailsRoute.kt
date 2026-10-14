package com.andreavieira.nanithappybirthday.feature.details

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andreavieira.nanithappybirthday.R
import com.andreavieira.nanithappybirthday.ui.theme.DetailsTheme
import com.andreavieira.nanithappybirthday.ui.photo.PhotoChoiceDialog
import com.andreavieira.nanithappybirthday.ui.photo.rememberCameraPhotoCapture
import com.andreavieira.nanithappybirthday.ui.photo.rememberGalleryPhotoPicker
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun DetailsRoute(
    detailsViewModel: DetailsViewModel,
    onShowBirthday: (DetailsUiState, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailsTheme { DetailsRouteContent(detailsViewModel, onShowBirthday, modifier) }
}

@Composable
private fun DetailsRouteContent(
    detailsViewModel: DetailsViewModel,
    onShowBirthday: (DetailsUiState, Boolean) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

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

    Box(modifier) {
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
            onShowBirthday = {
                scope.launch {
                    val draft = detailsViewModel.uiState.value
                    if (draft.canShowBirthday) {
                        val saved = detailsViewModel.saveBeforeBirthday()
                        onShowBirthday(draft, saved)
                    }
                }
            },
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
