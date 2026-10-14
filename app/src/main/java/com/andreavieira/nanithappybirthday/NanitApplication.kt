package com.andreavieira.nanithappybirthday

import android.app.Application
import com.andreavieira.nanithappybirthday.data.profile.DataStoreProfileStore
import com.andreavieira.nanithappybirthday.data.profile.DefaultProfileRepository
import com.andreavieira.nanithappybirthday.data.profile.profileDataStore
import com.andreavieira.nanithappybirthday.platform.photo.PrivateProfilePhotos

/** Owns the app-wide profile repository and its single DataStore instance. */
class NanitApplication : Application() {
    val profileRepository by lazy {
        DefaultProfileRepository(
            DataStoreProfileStore(profileDataStore),
            PrivateProfilePhotos(this),
        )
    }
}
