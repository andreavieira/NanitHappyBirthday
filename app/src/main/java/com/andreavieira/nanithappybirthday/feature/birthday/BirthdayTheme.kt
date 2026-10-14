package com.andreavieira.nanithappybirthday.feature.birthday

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.andreavieira.nanithappybirthday.R
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
        Color(0xFFC5E8DF), Color(0xFFA9DCCF), Color(0xFF6FC5AF),
        R.drawable.birthday_background_fox, R.drawable.baby_placeholder_fox, R.drawable.camera_badge_fox,
    ),
    ELEPHANT(
        Color(0xFFFEEFCB), Color(0xFFFEE7B7), Color(0xFFFEBE21),
        R.drawable.birthday_background_elephant, R.drawable.baby_placeholder_elephant, R.drawable.camera_badge_elephant,
    ),
    PELICAN(
        Color(0xFFDAF1F6), Color(0xFFB9E5EF), Color(0xFF8BD3E4),
        R.drawable.birthday_background_pelican, R.drawable.baby_placeholder_pelican, R.drawable.camera_badge_pelican,
    ),
}

/** Chooses independently on entry; consecutive visits are allowed to select the same theme. */
internal fun chooseBirthdayTheme(random: Random = Random.Default): BirthdayTheme =
    BirthdayTheme.entries.random(random)
