package com.andreavieira.nanithappybirthday.feature.details

import java.time.LocalDate

data class DetailsUiState(
    val name: String = "",
    val birthday: LocalDate? = null,
) {
    val canShowBirthday: Boolean get() = name.isNotBlank() && birthday != null
}
