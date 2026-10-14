package com.andreavieira.nanithappybirthday.ui.photo

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.andreavieira.nanithappybirthday.R

@Composable
internal fun PhotoChoiceDialog(
    onDismiss: () -> Unit,
    onChooseGallery: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_picture)) },
        text = {
            Text(stringResource(R.string.photo_choice_description), style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(onClick = onChooseGallery) { Text(stringResource(R.string.gallery)) }
        },
        dismissButton = {
            TextButton(onClick = onTakePhoto) { Text(stringResource(R.string.take_photo)) }
        },
    )
}
