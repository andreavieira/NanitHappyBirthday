package com.andreavieira.nanithappybirthday.feature.details

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DetailsUiStateTest {
    private val birthday = LocalDate.of(2025, 1, 15)

    @Test fun missingEitherRequiredFieldDisablesAction() {
        assertFalse(DetailsUiState().canShowBirthday)
        assertFalse(DetailsUiState(name = "Alex").canShowBirthday)
        assertFalse(DetailsUiState(birthday = birthday).canShowBirthday)
    }

    @Test fun whitespaceNameDoesNotCountAsInput() {
        assertFalse(DetailsUiState(name = " \t\n", birthday = birthday).canShowBirthday)
    }

    @Test fun nameAndBirthdayEnableActionWithoutPhoto() {
        assertTrue(DetailsUiState(name = "Alex", birthday = birthday).canShowBirthday)
    }
}
