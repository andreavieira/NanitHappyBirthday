package com.andreavieira.nanithappybirthday.feature.details

import java.time.LocalDate

/** Identifies recoverable profile-loading, photo-import, and picker-launch errors for the UI. */
enum class DetailsError {
    LOAD,
    PHOTO,
    CAMERA,
    REMOVE_PHOTO,
}

/** Describes the editable details, operation progress, errors, and available screen actions. */
data class DetailsUiState(
    val name: String = "",
    val birthday: LocalDate? = null,
    val photoPath: String? = null,
    val isLoading: Boolean = false,
    val hasLoadedProfile: Boolean = true,
    val pendingSaves: Int = 0,
    val error: DetailsError? = null,
) {
    val canShowBirthday: Boolean
        get() =
            name.isNotBlank() &&
                birthday != null &&
                hasLoadedProfile &&
                !isLoading &&
                pendingSaves == 0 &&
                error != DetailsError.LOAD

    val canEdit: Boolean
        get() = hasLoadedProfile && !isLoading
}
