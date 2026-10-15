package com.andreavieira.nanithappybirthday.feature.birthday

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.andreavieira.nanithappybirthday.R
import com.andreavieira.nanithappybirthday.ui.theme.NanitColors
import kotlin.random.Random

/** Maps each birthday variant to its supplied artwork and measured Figma colors. */
enum class BirthdayTheme(
    val backgroundColor: Color,
    val placeholderColor: Color,
    val borderColor: Color,
    @DrawableRes val backgroundResource: Int,
    @DrawableRes val placeholderResource: Int,
    @DrawableRes val cameraResource: Int,
) {
    FOX(
        NanitColors.FoxBackground,
        NanitColors.FoxPlaceholder,
        NanitColors.FoxBorder,
        R.drawable.birthday_background_fox,
        R.drawable.baby_placeholder_fox,
        R.drawable.camera_badge_fox,
    ),
    ELEPHANT(
        NanitColors.ElephantBackground,
        NanitColors.ElephantPlaceholder,
        NanitColors.ElephantBorder,
        R.drawable.birthday_background_elephant,
        R.drawable.baby_placeholder_elephant,
        R.drawable.camera_badge_elephant,
    ),
    PELICAN(
        NanitColors.PelicanBackground,
        NanitColors.PelicanPlaceholder,
        NanitColors.PelicanBorder,
        R.drawable.birthday_background_pelican,
        R.drawable.baby_placeholder_pelican,
        R.drawable.camera_badge_pelican,
    ),
}

/** Chooses independently on entry; consecutive visits are allowed to select the same theme. */
internal fun chooseBirthdayTheme(random: Random = Random.Default): BirthdayTheme =
    BirthdayTheme.entries.random(random)
