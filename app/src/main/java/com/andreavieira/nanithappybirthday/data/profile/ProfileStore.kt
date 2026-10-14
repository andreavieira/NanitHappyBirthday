package com.andreavieira.nanithappybirthday.data.profile

import kotlinx.coroutines.flow.Flow

/** Defines observable profile metadata and atomic updates independently of DataStore. */
interface ProfileStore {
    val profiles: Flow<BabyProfile>
    suspend fun update(transform: (BabyProfile) -> BabyProfile)
}

/** Defines photo import, deletion, and path lookup independently of Android file access. */
interface ProfilePhotos {
    suspend fun importPhoto(source: String): String
    suspend fun delete(fileName: String)
    fun path(fileName: String): String
}
