package com.andreavieira.nanithappybirthday

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andreavieira.nanithappybirthday.feature.details.DetailsScreen
import com.andreavieira.nanithappybirthday.feature.details.DetailsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val detailsViewModel: DetailsViewModel = viewModel()
                val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHost = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val readyMessage = stringResource(R.string.details_ready)
                Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
                    DetailsScreen(
                        uiState = uiState,
                        onNameChange = detailsViewModel::updateName,
                        onChooseBirthday = {
                            val today = LocalDate.now()
                            val initial = uiState.birthday ?: today
                            DatePickerDialog(
                                this,
                                { _, year, month, day ->
                                    detailsViewModel.updateBirthday(LocalDate.of(year, month + 1, day))
                                },
                                initial.year, initial.monthValue - 1, initial.dayOfMonth,
                            ).apply {
                                datePicker.maxDate = today.atStartOfDay(ZoneId.systemDefault())
                                    .toInstant().toEpochMilli()
                            }.show()
                        },
                        // Navigation is connected when the birthday destination is added in stage 3.
                        onShowBirthday = { scope.launch { snackbarHost.showSnackbar(readyMessage) } },
                        modifier = Modifier.padding(padding),
                    )
                }
            }
        }
    }
}
