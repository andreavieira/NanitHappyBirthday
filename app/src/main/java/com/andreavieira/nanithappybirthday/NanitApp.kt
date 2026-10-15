package com.andreavieira.nanithappybirthday

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andreavieira.nanithappybirthday.feature.birthday.BirthdayRoute
import com.andreavieira.nanithappybirthday.feature.birthday.BirthdayTheme
import com.andreavieira.nanithappybirthday.feature.birthday.chooseBirthdayTheme
import com.andreavieira.nanithappybirthday.feature.details.DetailsRoute
import com.andreavieira.nanithappybirthday.feature.details.DetailsViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

/** Owns the two-screen flow and preserves the displayed draft across activity recreation. */
@Composable
fun NanitApp() {
    val application = LocalContext.current.applicationContext as NanitApplication
    // Keep one ViewModel while switching destinations so closing the card preserves unsaved edits.
    val detailsViewModel: DetailsViewModel =
        viewModel(
            factory =
                viewModelFactory { initializer { DetailsViewModel(application.profileRepository) } }
        )

    // Destination snapshot: stable across recomposition and activity recreation.
    var showingBirthday by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var birthdayEpochDay by rememberSaveable { mutableStateOf(0L) }
    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var themeName by rememberSaveable { mutableStateOf(BirthdayTheme.FOX.name) }

    // Save-failure feedback belongs to the app flow so it survives switching screens.
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val saveFailedMessage = stringResource(R.string.save_failed)

    val selectedTheme = BirthdayTheme.valueOf(themeName)

    BackHandler(enabled = showingBirthday) { showingBirthday = false }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor =
            if (showingBirthday) selectedTheme.backgroundColor
            else MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        if (showingBirthday) {
            BirthdayRoute(
                name = name,
                birthday = LocalDate.ofEpochDay(birthdayEpochDay),
                initialPhotoPath = photoPath,
                detailsViewModel = detailsViewModel,
                theme = selectedTheme,
                onClose = { showingBirthday = false },
                contentPadding = padding,
            )
        } else {
            DetailsRoute(
                detailsViewModel = detailsViewModel,
                onShowBirthday = { draft, saved ->
                    name = draft.name.trim()
                    birthdayEpochDay = requireNotNull(draft.birthday).toEpochDay()
                    photoPath = draft.photoPath
                    themeName = chooseBirthdayTheme().name
                    showingBirthday = true
                    if (!saved) scope.launch { snackbarHost.showSnackbar(saveFailedMessage) }
                },
                onSaveFailed = {
                    scope.launch {
                        snackbarHost.showSnackbar(saveFailedMessage, withDismissAction = true)
                    }
                },
                modifier = Modifier.padding(padding),
            )
        }
    }
}
