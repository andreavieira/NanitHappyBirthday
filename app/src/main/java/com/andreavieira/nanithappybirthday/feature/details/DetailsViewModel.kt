package com.andreavieira.nanithappybirthday.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class DetailsViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        DetailsUiState(
            name = savedState[NAME] ?: "",
            birthday = savedState.get<Long>(BIRTHDAY)?.let(LocalDate::ofEpochDay),
        ),
    )
    val uiState = mutableUiState.asStateFlow()

    fun updateName(name: String) {
        savedState[NAME] = name
        mutableUiState.value = mutableUiState.value.copy(name = name)
    }

    fun updateBirthday(birthday: LocalDate) {
        savedState[BIRTHDAY] = birthday.toEpochDay()
        mutableUiState.value = mutableUiState.value.copy(birthday = birthday)
    }

    private companion object {
        const val NAME = "details_name"
        const val BIRTHDAY = "details_birthday_epoch_day"
    }
}
