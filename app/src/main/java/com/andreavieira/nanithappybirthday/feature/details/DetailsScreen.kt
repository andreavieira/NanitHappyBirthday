package com.andreavieira.nanithappybirthday.feature.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andreavieira.nanithappybirthday.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DetailsScreen(
    uiState: DetailsUiState,
    onNameChange: (String) -> Unit,
    onChooseBirthday: () -> Unit,
    onShowBirthday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.baby_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.birthday), style = MaterialTheme.typography.labelLarge)
            OutlinedButton(onClick = onChooseBirthday, modifier = Modifier.fillMaxWidth()) {
                Text(
                    uiState.birthday?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                        ?: stringResource(R.string.choose_birthday),
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.picture), style = MaterialTheme.typography.labelLarge)
            Image(
                painter = painterResource(R.drawable.baby_placeholder_fox),
                contentDescription = stringResource(R.string.baby_placeholder_description),
                modifier = Modifier.size(144.dp).clip(CircleShape)
                    .border(4.dp, Color(0xFF6FC5AF), CircleShape),
            )
        }
        Button(
            onClick = onShowBirthday,
            enabled = uiState.canShowBirthday,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.show_birthday_screen))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyDetailsPreview() {
    MaterialTheme { DetailsScreen(DetailsUiState(), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ValidDetailsPreview() {
    MaterialTheme {
        DetailsScreen(DetailsUiState("Alex", LocalDate.of(2025, 1, 15)), {}, {}, {})
    }
}
