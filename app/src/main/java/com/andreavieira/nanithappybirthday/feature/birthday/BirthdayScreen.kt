package com.andreavieira.nanithappybirthday.feature.birthday

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
    val closeLabel = stringResource(R.string.close_birthday)
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
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart).padding(8.dp).semantics { contentDescription = closeLabel }) {
            Image(painterResource(R.drawable.birthday_back), contentDescription = null, modifier = Modifier.size(18.dp, 15.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun BirthdayNavigationPreview() {
    MaterialTheme { BirthdayScreen("Alex", LocalDate.of(2025, 1, 15), null, {}) }
}
