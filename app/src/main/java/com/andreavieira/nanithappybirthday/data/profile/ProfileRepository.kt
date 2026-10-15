package com.andreavieira.nanithappybirthday.data.profile

import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Exposes profile reads and writes to features without exposing storage implementation details. */
interface ProfileRepository {
    val profiles: Flow<BabyProfile>

    suspend fun saveDetails(name: String, birthday: LocalDate?)

    suspend fun removePhoto()

    suspend fun selectPhoto(source: String): String

    fun photoPath(fileName: String): String
}

/** Validates birthdays and coordinates profile metadata with safe photo replacement and cleanup. */
class DefaultProfileRepository(
    private val store: ProfileStore,
    private val photos: ProfilePhotos,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ProfileRepository {
    override val profiles = store.profiles
    private val photoMutex = Mutex()

    override suspend fun saveDetails(name: String, birthday: LocalDate?) {
        require(birthday == null || !birthday.isAfter(LocalDate.now(clock))) {
            "Birthday cannot be in the future"
        }
        // Empty drafts are valid storage; the UI requires both fields to proceed.
        store.update { it.copy(name = name, birthday = birthday) }
    }

    override suspend fun selectPhoto(source: String): String =
        photoMutex.withLock {
            val previous = profiles.first().photoFileName
            val imported = photos.importPhoto(source)
            try {
                store.update { it.copy(photoFileName = imported) }
            } catch (failure: Throwable) {
                // No profile points at the new file if the transaction did not commit.
                photos.delete(imported)
                throw failure
            }
            previous?.takeIf { it != imported }?.let { photos.delete(it) }
            photos.path(imported)
        }

    override suspend fun removePhoto() =
        photoMutex.withLock {
            val previous = profiles.first().photoFileName
            // Clear metadata first so a failed write cannot leave a reference to a deleted file.
            store.update { it.copy(photoFileName = null) }
            previous?.let { photos.delete(it) }
            Unit
        }

    override fun photoPath(fileName: String): String = photos.path(fileName)
}
