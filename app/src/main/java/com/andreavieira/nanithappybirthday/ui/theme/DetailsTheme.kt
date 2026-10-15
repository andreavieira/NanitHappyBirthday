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
                primary = Color(0xFF26745F),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF6FC5AF),
                onPrimaryContainer = Color(0xFF123C30),
                secondary = Color(0xFF26745F),
                secondaryContainer = Color(0xFFC5E8DF),
                onSecondaryContainer = Color(0xFF123C30),
            ),
        content = content,
    )
}
