package com.andreavieira.nanithappybirthday.feature.birthday

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andreavieira.nanithappybirthday.R
import java.time.LocalDate

/** Renders the selected birthday variant; final heading and age presentation follow separately. */
@Composable
fun BirthdayScreen(
    name: String,
    birthday: LocalDate,
    photoPath: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    theme: BirthdayTheme = BirthdayTheme.FOX,
) {
    Box(modifier.fillMaxSize().background(theme.backgroundColor).clipToBounds()) {
        BirthdayIllustration(theme)
        Column(
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BirthdayHeading(name, birthday)
            BirthdayPhoto(photoPath, theme)
        }
        BirthdayBackButton(onClose, Modifier.align(Alignment.TopStart).padding(16.dp))
    }
}

/** Pins the original transparent illustration to the bottom, preserving its aspect ratio. */
@Composable
private fun BirthdayIllustration(theme: BirthdayTheme) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Recorded layer is 459 wide within a 360-wide frame; clip its side overflow.
        val illustrationWidth = this.maxWidth * (459f / 360f)
        Image(
            painter = painterResource(theme.backgroundResource),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.BottomCenter)
                .requiredWidth(illustrationWidth)
                .aspectRatio(1083f / 1776f),
        )
    }
}

/** Groups the provisional name/date heading; age artwork will replace the raw date later. */
@Composable
private fun BirthdayHeading(name: String, birthday: LocalDate) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = Color(0xFF394562),
        )
        Text(birthday.toString(), color = Color(0xFF394562))
    }
}

/** Displays the placeholder or selected image using the same circular photo bounds. */
@Composable
private fun BirthdayPhoto(photoPath: String?, theme: BirthdayTheme) {
    val modifier = Modifier.size(200.dp).clip(CircleShape)
        .background(theme.placeholderColor)
        .border(7.dp, theme.borderColor.copy(alpha = 0.78f), CircleShape)
    if (photoPath == null) {
        Image(
            painter = painterResource(theme.placeholderResource),
            contentDescription = stringResource(R.string.baby_placeholder_description),
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = photoPath,
            contentDescription = stringResource(R.string.baby_photo_description),
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(theme.placeholderResource),
            error = painterResource(theme.placeholderResource),
        )
    }
}

/** Preserves the supplied icon geometry while providing an accessible Android touch target. */
@Composable
private fun BirthdayBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.close_birthday)
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp).semantics { contentDescription = label },
    ) {
        // The export is the inner Union; restore its measured padding in the 24 dp Figma slot.
        Box(Modifier.size(24.dp)) {
            Image(
                painter = painterResource(R.drawable.birthday_back),
                contentDescription = null,
                modifier = Modifier.offset(3.dp, 5.dp).size(18.dp, 15.dp),
            )
        }
    }
}

@Preview(name = "Fox", group = "Birthday themes", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun FoxBirthdayPreview() {
    MaterialTheme { BirthdayScreen("Alex", LocalDate.of(2025, 1, 15), null, {}, theme = BirthdayTheme.FOX) }
}

@Preview(name = "Elephant", group = "Birthday themes", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ElephantBirthdayPreview() {
    MaterialTheme { BirthdayScreen("Alex", LocalDate.of(2025, 1, 15), null, {}, theme = BirthdayTheme.ELEPHANT) }
}

@Preview(name = "Pelican", group = "Birthday themes", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PelicanBirthdayPreview() {
    MaterialTheme { BirthdayScreen("Alex", LocalDate.of(2025, 1, 15), null, {}, theme = BirthdayTheme.PELICAN) }
}
