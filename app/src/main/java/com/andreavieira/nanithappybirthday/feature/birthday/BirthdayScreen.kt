package com.andreavieira.nanithappybirthday.feature.birthday

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.layout.ContentScale
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

/** Initial birthday destination; artwork and age presentation follow in the next stage 3 changes. */
@Composable
fun BirthdayScreen(
    name: String,
    birthday: LocalDate,
    photoPath: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, maxLines = 2)
            Text(birthday.toString())
            val photoModifier = Modifier.size(200.dp).clip(CircleShape)
            if (photoPath == null) {
                Image(painterResource(R.drawable.baby_placeholder_fox), stringResource(R.string.baby_placeholder_description), photoModifier)
            } else {
                AsyncImage(photoPath, stringResource(R.string.baby_photo_description), photoModifier, contentScale = ContentScale.Crop)
            }
        }
        BirthdayBackButton(onClose, Modifier.align(Alignment.TopStart).padding(16.dp))
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun BirthdayNavigationPreview() {
    MaterialTheme { BirthdayScreen("Alex", LocalDate.of(2025, 1, 15), null, {}) }
}
