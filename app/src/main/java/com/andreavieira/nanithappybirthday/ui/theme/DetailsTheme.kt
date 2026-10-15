package com.andreavieira.nanithappybirthday.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Applies Fox-inspired accents only to the details screen and its Compose dialogs. */
@Composable
internal fun DetailsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            MaterialTheme.colorScheme.copy(
                primary = NanitColors.DetailsAccent,
                onPrimary = Color.White,
                primaryContainer = NanitColors.FoxBorder,
                onPrimaryContainer = NanitColors.DetailsOnAccentContainer,
                secondary = NanitColors.DetailsAccent,
                secondaryContainer = NanitColors.FoxBackground,
                onSecondaryContainer = NanitColors.DetailsOnAccentContainer,
            ),
        content = content,
    )
}
