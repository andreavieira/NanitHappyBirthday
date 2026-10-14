package com.andreavieira.nanithappybirthday.data.profile

import java.time.LocalDate

/** Represents stored baby details and the filename of an optional privately stored photo. */
data class BabyProfile(
    val name: String = "",
    val birthday: LocalDate? = null,
    val photoFileName: String? = null,
)
