package com.andreavieira.nanithappybirthday

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andreavieira.nanithappybirthday.feature.birthday.BirthdayScreen
import com.andreavieira.nanithappybirthday.feature.details.DetailsRoute
import com.andreavieira.nanithappybirthday.feature.details.DetailsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Owns the two-screen flow and preserves the displayed draft across activity recreation. */
@Composable
fun NanitApp() {
    val application = LocalContext.current.applicationContext as NanitApplication
    // Keep one ViewModel while switching destinations so closing the card preserves unsaved edits.
    val detailsViewModel: DetailsViewModel = viewModel(factory = viewModelFactory {
        initializer { DetailsViewModel(application.profileRepository) }
    })
    var showingBirthday by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var birthdayEpochDay by rememberSaveable { mutableStateOf(0L) }
    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val saveFailedMessage = stringResource(R.string.save_failed)

    BackHandler(enabled = showingBirthday) { showingBirthday = false }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        if (showingBirthday) {
            BirthdayScreen(
                name = name,
                birthday = LocalDate.ofEpochDay(birthdayEpochDay),
                photoPath = photoPath,
                onClose = { showingBirthday = false },
                modifier = Modifier.padding(padding),
            )
        } else {
            DetailsRoute(
                detailsViewModel = detailsViewModel,
                onShowBirthday = { draft, saved ->
                    name = draft.name.trim()
                    birthdayEpochDay = requireNotNull(draft.birthday).toEpochDay()
                    photoPath = draft.photoPath
                    showingBirthday = true
                    if (!saved) scope.launch { snackbarHost.showSnackbar(saveFailedMessage) }
                },
                modifier = Modifier.padding(padding),
            )
        }
    }
}
