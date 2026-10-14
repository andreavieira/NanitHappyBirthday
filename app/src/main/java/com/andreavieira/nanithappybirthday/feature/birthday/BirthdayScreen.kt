package com.andreavieira.nanithappybirthday.feature.birthday

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andreavieira.nanithappybirthday.R
import java.time.LocalDate

@Composable
fun BirthdayScreen(
    name: String,
    birthday: LocalDate,
    photoPath: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    theme: BirthdayTheme = BirthdayTheme.FOX,
    today: LocalDate = LocalDate.now(),
    contentPadding: PaddingValues = PaddingValues(),
    onChoosePhoto: (() -> Unit)? = null,
    photoChoiceEnabled: Boolean = true,
) {
    Box(modifier.fillMaxSize().background(theme.backgroundColor).clipToBounds()) {
        BirthdayIllustration(theme)
        Box(Modifier.fillMaxSize().padding(contentPadding)) {
            BirthdayContent(name, birthdayAge(birthday, today), photoPath, theme, onChoosePhoto, photoChoiceEnabled)
            BirthdayBackButton(onClose, Modifier.align(Alignment.TopStart).padding(start = 4.dp, top = 4.dp))
        }
    }
}

@Composable
private fun BirthdayContent(
    name: String, age: BirthdayAge, photoPath: String?, theme: BirthdayTheme,
    onChoosePhoto: (() -> Unit)?, photoChoiceEnabled: Boolean,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val availableHeight = this.maxHeight
        val availableWidth = this.maxWidth
        // At the 360 × 800 baseline, account for 24/48 system insets and preserve Pelican's 8 dp offset.
        val photoTop = (availableHeight / 2 - 122.dp - if (theme == BirthdayTheme.PELICAN) 8.dp else 0.dp)
            .coerceAtLeast(224.dp)
        val photoDiameter = (availableWidth - 100.dp).coerceIn(1.dp, 200.dp)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.fillMaxWidth().heightIn(min = photoTop).padding(top = 20.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                BirthdayHeading(name, age)
            }
            Box(Modifier.size(photoDiameter)) {
                BirthdayPhoto(photoPath, theme, photoDiameter)
                if (onChoosePhoto != null) {
                    BirthdayCameraButton(theme, photoDiameter, photoChoiceEnabled, onChoosePhoto)
                }
            }
            Spacer(Modifier.height(15.dp))
            Image(
                painter = painterResource(R.drawable.nanit_logo),
                contentDescription = stringResource(R.string.nanit_logo_description),
                modifier = Modifier.size(58.68.dp, 19.63.dp),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

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

@Composable
private fun BirthdayHeading(name: String, age: BirthdayAge) {
    val title = stringResource(R.string.birthday_heading, name.trim().uppercase())
    val unit = androidx.compose.ui.res.pluralStringResource(
        if (age.inYears) R.plurals.birthday_years_old else R.plurals.birthday_months_old,
        age.number,
    )
    Column(
        modifier = Modifier.widthIn(max = 312.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            modifier = Modifier.widthIn(max = 252.dp),
            fontSize = 21.sp,
            lineHeight = 23.sp,
            letterSpacing = (-0.42).sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = Color(0xFF394562),
        )
        Spacer(Modifier.height(13.dp))
        BirthdayNumber(age.number)
        Spacer(Modifier.height(14.dp))
        Text(
            text = unit,
            fontSize = 18.sp,
            letterSpacing = (-0.36).sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color(0xFF394562),
        )
    }
}

@Composable
private fun BirthdayNumber(number: Int) {
    val resources = listOf(
        R.drawable.birthday_number_0, R.drawable.birthday_number_1,
        R.drawable.birthday_number_2, R.drawable.birthday_number_3,
        R.drawable.birthday_number_4, R.drawable.birthday_number_5,
        R.drawable.birthday_number_6, R.drawable.birthday_number_7,
        R.drawable.birthday_number_8, R.drawable.birthday_number_9,
        R.drawable.birthday_number_10, R.drawable.birthday_number_11,
        R.drawable.birthday_number_12,
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Image(painterResource(R.drawable.birthday_swirl_left), null, Modifier.size(50.2.dp, 43.53.dp))
        Row(Modifier.height(88.dp).clearAndSetSemantics { contentDescription = number.toString() }, verticalAlignment = Alignment.CenterVertically) {
            // Use the supplied compound artwork through 12; compose digits for larger ages.
            val artwork = if (number <= 12) listOf(resources[number]) else number.toString().map { resources[it.digitToInt()] }
            artwork.forEach { resource ->
                val painter = painterResource(resource)
                Image(
                    painter, null,
                    Modifier.height(88.dp).width(88.dp * painter.intrinsicSize.width / painter.intrinsicSize.height),
                )
            }
        }
        Image(painterResource(R.drawable.birthday_swirl_right), null, Modifier.size(50.2.dp, 43.53.dp))
    }
}

@Composable
private fun BirthdayPhoto(photoPath: String?, theme: BirthdayTheme, diameter: androidx.compose.ui.unit.Dp) {
    val modifier = Modifier.size(diameter).clip(CircleShape)
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

@Composable
private fun BirthdayCameraButton(
    theme: BirthdayTheme,
    diameter: androidx.compose.ui.unit.Dp,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    // Center the badge's 48 dp touch target on the ring at 45°, independently of its 36 dp artwork.
    val center = diameter / 2
    val ringRadius = center - 3.5.dp
    val diagonal = ringRadius / kotlin.math.sqrt(2f)
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.offset(center + diagonal - 24.dp, center - diagonal - 24.dp).size(48.dp),
    ) {
        Image(
            painterResource(theme.cameraResource),
            contentDescription = stringResource(R.string.choose_picture),
            modifier = Modifier.size(36.dp),
        )
    }
}

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
    MaterialTheme { BirthdayScreen("Alex", LocalDate.of(2025, 1, 15), null, {}, onChoosePhoto = {}, theme = BirthdayTheme.FOX, today = LocalDate.of(2025, 2, 15)) }
}

@Preview(name = "Elephant", group = "Birthday themes", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ElephantBirthdayPreview() {
    MaterialTheme { BirthdayScreen("Alex", LocalDate.of(2025, 1, 15), null, {}, onChoosePhoto = {}, theme = BirthdayTheme.ELEPHANT, today = LocalDate.of(2025, 11, 15)) }
}

@Preview(name = "Pelican", group = "Birthday themes", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PelicanBirthdayPreview() {
    MaterialTheme { BirthdayScreen("Alex", LocalDate.of(2025, 1, 15), null, {}, onChoosePhoto = {}, theme = BirthdayTheme.PELICAN, today = LocalDate.of(2026, 1, 15)) }
}

@Preview(name = "Long name", group = "Birthday header", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LongNameBirthdayPreview() {
    MaterialTheme {
        BirthdayScreen("Cristiano Ronaldo", LocalDate.of(2025, 1, 15), null, {}, onChoosePhoto = {}, today = LocalDate.of(2025, 12, 15))
    }
}

@Preview(name = "Small screen, large text", group = "Birthday header", showBackground = true, widthDp = 320, heightDp = 568, fontScale = 1.5f)
@Composable
private fun SmallScreenBirthdayPreview() {
    MaterialTheme {
        BirthdayScreen("Cristiano Ronaldo", LocalDate.of(2025, 1, 15), null, {}, onChoosePhoto = {}, today = LocalDate.of(2025, 12, 15))
    }
}
