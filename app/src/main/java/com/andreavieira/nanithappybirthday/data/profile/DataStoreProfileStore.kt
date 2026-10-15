package com.andreavieira.nanithappybirthday.data.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import kotlinx.coroutines.flow.map

val Context.profileDataStore by preferencesDataStore(name = "baby_profile")

/** Persists profile metadata as Preferences DataStore keys and applies updates atomically. */
class DataStoreProfileStore(private val dataStore: DataStore<Preferences>) : ProfileStore {
    override val profiles = dataStore.data.map(::decode)

    override suspend fun update(transform: (BabyProfile) -> BabyProfile) {
        dataStore.edit { preferences ->
            val updated = transform(decode(preferences))
            preferences[NAME] = updated.name
            updated.birthday?.let { preferences[BIRTHDAY] = it.toEpochDay() }
                ?: preferences.remove(BIRTHDAY)
            updated.photoFileName?.let { preferences[PHOTO] = it } ?: preferences.remove(PHOTO)
        }
    }

    private fun decode(preferences: Preferences) =
        BabyProfile(
            name = preferences[NAME] ?: "",
            birthday = preferences[BIRTHDAY]?.let(LocalDate::ofEpochDay),
            photoFileName = preferences[PHOTO],
        )

    private companion object {
        val NAME = stringPreferencesKey("name")
        val BIRTHDAY = longPreferencesKey("birthday_epoch_day")
        val PHOTO = stringPreferencesKey("photo_file_name")
    }
}
