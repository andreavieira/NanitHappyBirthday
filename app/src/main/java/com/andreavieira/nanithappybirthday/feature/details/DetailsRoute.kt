package com.andreavieira.nanithappybirthday.feature.details

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andreavieira.nanithappybirthday.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun DetailsRoute(detailsViewModel: DetailsViewModel = viewModel()) {
    val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val readyMessage = stringResource(R.string.details_ready)

    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        DetailsScreen(
            uiState = uiState,
            onNameChange = detailsViewModel::updateName,
            onChooseBirthday = {
                showBirthdayPicker(context, uiState.birthday, detailsViewModel::updateBirthday)
            },
            // Navigation is connected when the birthday destination is added in stage 3.
            onShowBirthday = { scope.launch { snackbarHost.showSnackbar(readyMessage) } },
            modifier = Modifier.padding(padding),
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
        { _, year, month, day -> onDateSelected(LocalDate.of(year, month + 1, day)) },
        initial.year, initial.monthValue - 1, initial.dayOfMonth,
    ).apply {
        datePicker.maxDate = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.show()
}
