package com.andreavieira.nanithappybirthday.feature.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.andreavieira.nanithappybirthday.R
import com.andreavieira.nanithappybirthday.ui.theme.DetailsTheme
import com.andreavieira.nanithappybirthday.ui.theme.NanitColors
import com.andreavieira.nanithappybirthday.ui.theme.NanitDimens
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.delay

@Composable
fun DetailsScreen(
    uiState: DetailsUiState,
    onNameChange: (String) -> Unit,
    onChooseBirthday: () -> Unit,
    onShowBirthday: () -> Unit,
    modifier: Modifier = Modifier,
    onChoosePhoto: () -> Unit = {},
    onRemovePhoto: () -> Unit = {},
    onRetry: () -> Unit = {},
    onDismissError: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(NanitDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(DetailsDimens.SectionSpacing),
    ) {
        DetailsHeader()
        BabyNameField(uiState, onNameChange)
        BirthdayField(uiState, onChooseBirthday)
        BabyPhotoSection(uiState, onChoosePhoto, onRemovePhoto)
        DetailsSaveProgress(uiState)
        DetailsErrorMessage(uiState, onRetry, onDismissError)
        ShowBirthdayButton(uiState, onShowBirthday)
    }
}

@Composable
private fun DetailsHeader() {
    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
}

@Composable
private fun BabyNameField(uiState: DetailsUiState, onNameChange: (String) -> Unit) {
    OutlinedTextField(
        value = uiState.name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.baby_name)) },
        singleLine = true,
        enabled = uiState.canEdit,
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun BirthdayField(uiState: DetailsUiState, onChooseBirthday: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(DetailsDimens.FieldSpacing)) {
        Text(stringResource(R.string.birthday), style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = onChooseBirthday,
            enabled = uiState.canEdit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                uiState.birthday?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    ?: stringResource(R.string.choose_birthday)
            )
        }
    }
}

@Composable
private fun BabyPhotoSection(
    uiState: DetailsUiState,
    onChoosePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DetailsDimens.FieldSpacing),
    ) {
        Text(stringResource(R.string.picture), style = MaterialTheme.typography.labelLarge)
        val photoModifier =
            Modifier.size(DetailsDimens.PhotoDiameter)
                .clip(CircleShape)
                .border(DetailsDimens.PhotoBorderWidth, NanitColors.FoxBorder, CircleShape)
        val removeLabel = stringResource(R.string.remove_picture)
        Box(Modifier.size(DetailsDimens.PhotoContainerSize), contentAlignment = Alignment.Center) {
            if (uiState.photoPath == null) {
                Image(
                    painter = painterResource(R.drawable.baby_placeholder_fox),
                    contentDescription = stringResource(R.string.baby_placeholder_description),
                    modifier = photoModifier,
                )
            } else {
                AsyncImage(
                    model = uiState.photoPath,
                    contentDescription = stringResource(R.string.baby_photo_description),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.baby_placeholder_fox),
                    modifier = photoModifier,
                )
            }
            if (uiState.photoPath != null) {
                // Keep a 48 dp touch target around the smaller visible close button.
                IconButton(
                    onClick = onRemovePhoto,
                    enabled = uiState.canEdit && uiState.pendingSaves == 0,
                    modifier =
                        Modifier.align(Alignment.TopEnd).semantics {
                            contentDescription = removeLabel
                        },
                ) {
                    Box(
                        modifier =
                            Modifier.size(DetailsDimens.RemovePhotoVisualSize)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .border(
                                    DetailsDimens.RemovePhotoBorderWidth,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        OutlinedButton(
            onClick = onChoosePhoto,
            enabled = uiState.canEdit && uiState.pendingSaves == 0,
        ) {
            Text(stringResource(R.string.choose_picture))
        }
    }
}

@Composable
private fun DetailsSaveProgress(uiState: DetailsUiState) {
    val isSaving = uiState.pendingSaves > 0
    val isPreview = LocalInspectionMode.current
    var showSaving by remember { mutableStateOf(false) }
    // Avoid flashing feedback for quick disk operations; completion cancels the delay.
    LaunchedEffect(isSaving) {
        showSaving = false
        if (isSaving) {
            delay(300)
            showSaving = true
        }
    }
    // Reserve a fixed feedback area so the button and photo never move when saving starts.
    Box(Modifier.fillMaxWidth().height(DetailsDimens.ProgressSlotHeight)) {
        if (uiState.isLoading || (isSaving && (showSaving || isPreview))) {
            Column(verticalArrangement = Arrangement.spacedBy(DetailsDimens.FieldSpacing)) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    stringResource(
                        if (uiState.isLoading) R.string.loading_details else R.string.saving_details
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun DetailsErrorMessage(
    uiState: DetailsUiState,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
) {
    val dismissLabel = stringResource(R.string.dismiss)
    uiState.error?.let { error ->
        Card(
            colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(Modifier.padding(DetailsDimens.ErrorPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(
                            when (error) {
                                DetailsError.LOAD -> R.string.load_failed
                                DetailsError.REMOVE_PHOTO -> R.string.remove_photo_failed
                                DetailsError.PHOTO -> R.string.photo_failed
                                DetailsError.CAMERA -> R.string.photo_app_unavailable
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    if (error != DetailsError.LOAD) {
                        IconButton(onClick = onDismissError) {
                            Text(
                                "×",
                                modifier = Modifier.semantics { contentDescription = dismissLabel },
                            )
                        }
                    }
                }
                if (error == DetailsError.LOAD) {
                    TextButton(onClick = onRetry, enabled = !uiState.isLoading) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowBirthdayButton(uiState: DetailsUiState, onShowBirthday: () -> Unit) {
    Button(
        onClick = onShowBirthday,
        enabled = uiState.canShowBirthday,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.show_birthday_screen))
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyDetailsPreview() {
    DetailsTheme { DetailsScreen(DetailsUiState(), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ValidDetailsPreview() {
    DetailsTheme { DetailsScreen(DetailsUiState("Alex", LocalDate.of(2025, 1, 15)), {}, {}, {}) }
}

@Preview(
    name = "Saving details",
    group = "Details feedback",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun SavingDetailsPreview() {
    DetailsTheme {
        DetailsScreen(
            uiState =
                DetailsUiState(
                    name = "Alex",
                    birthday = LocalDate.of(2025, 1, 15),
                    pendingSaves = 1,
                ),
            onNameChange = {},
            onChooseBirthday = {},
            onShowBirthday = {},
        )
    }
}

@Preview(
    name = "Photo error — close",
    group = "Details feedback",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun PhotoErrorDetailsPreview() {
    DetailsTheme {
        DetailsScreen(
            uiState =
                DetailsUiState(
                    name = "Alex",
                    birthday = LocalDate.of(2025, 1, 15),
                    error = DetailsError.PHOTO,
                ),
            onNameChange = {},
            onChooseBirthday = {},
            onShowBirthday = {},
        )
    }
}

@Preview(
    name = "Camera unavailable — close",
    group = "Details feedback",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun CameraErrorDetailsPreview() {
    DetailsTheme {
        DetailsScreen(
            uiState =
                DetailsUiState(
                    name = "Alex",
                    birthday = LocalDate.of(2025, 1, 15),
                    error = DetailsError.CAMERA,
                ),
            onNameChange = {},
            onChooseBirthday = {},
            onShowBirthday = {},
        )
    }
}

@Preview(
    name = "Load error — retry",
    group = "Details feedback",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun LoadErrorDetailsPreview() {
    DetailsTheme {
        DetailsScreen(
            uiState = DetailsUiState(hasLoadedProfile = false, error = DetailsError.LOAD),
            onNameChange = {},
            onChooseBirthday = {},
            onShowBirthday = {},
        )
    }
}

@Preview(
    name = "Selected photo — remove control",
    group = "Details feedback",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun SelectedPhotoDetailsPreview() {
    DetailsTheme {
        DetailsScreen(
            uiState =
                DetailsUiState(
                    photoPath =
                        "android.resource://com.andreavieira.nanithappybirthday/${R.drawable.baby_placeholder_fox}"
                ),
            onNameChange = {},
            onChooseBirthday = {},
            onShowBirthday = {},
        )
    }
}
