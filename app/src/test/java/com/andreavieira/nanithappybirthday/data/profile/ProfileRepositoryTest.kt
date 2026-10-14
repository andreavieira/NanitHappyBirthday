package com.andreavieira.nanithappybirthday.data.profile

import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Verifies birthday validation and safe photo replacement when storage operations succeed or fail.
 */
class ProfileRepositoryTest {
    private val today = LocalDate.of(2026, 10, 14)
    private val clock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC)
    private val store = MemoryStore()
    private val photos = MemoryPhotos()
    private val repository = DefaultProfileRepository(store, photos, clock)

    @Test
    fun futureBirthdayIsRejectedWithoutChangingStoredProfile() = runTest {
        repository.saveDetails("Alex", today)
        try {
            repository.saveDetails("Changed", today.plusDays(1))
            fail("Future date accepted")
        } catch (_: IllegalArgumentException) {}
        assertEquals(BabyProfile("Alex", today), store.profiles.value)
    }

    @Test
    fun partialDraftAndPhotoUpdatesPreserveOtherFields() = runTest {
        repository.selectPhoto("gallery")
        repository.saveDetails("", null)
        assertEquals("new.image", store.profiles.value.photoFileName)
        repository.saveDetails("Alex", today)
        repository.selectPhoto("replacement")
        assertEquals("Alex", store.profiles.value.name)
        assertEquals(today, store.profiles.value.birthday)
    }

    @Test
    fun failedPhotoImportRetainsExistingPhoto() = runTest {
        store.profiles.value = BabyProfile(photoFileName = "old.image")
        photos.failImport = true
        try {
            repository.selectPhoto("bad")
            fail("Import should fail")
        } catch (_: IOException) {}
        assertEquals("old.image", store.profiles.value.photoFileName)
        assertTrue(photos.deleted.isEmpty())
    }

    @Test
    fun failedMetadataWriteDeletesNewPhotoAndRetainsOldPhoto() = runTest {
        store.profiles.value = BabyProfile(photoFileName = "old.image")
        store.failWrite = true
        try {
            repository.selectPhoto("gallery")
            fail("Write should fail")
        } catch (_: IOException) {}
        assertEquals("old.image", store.profiles.value.photoFileName)
        assertEquals(listOf("new.image"), photos.deleted)
    }

    @Test
    fun successfulPhotoReplacementRemovesPreviousFile() = runTest {
        store.profiles.value = BabyProfile(photoFileName = "old.image")
        assertEquals("/photos/new.image", repository.selectPhoto("gallery"))
        assertEquals("new.image", store.profiles.value.photoFileName)
        assertEquals(listOf("old.image"), photos.deleted)
    }

    @Test
    fun removePhotoClearsReferenceDeletesFileAndPreservesDetails() = runTest {
        store.profiles.value = BabyProfile("Alex", today, "old.image")
        repository.removePhoto()
        assertEquals(BabyProfile("Alex", today), store.profiles.value)
        assertEquals(listOf("old.image"), photos.deleted)
    }

    @Test
    fun failedRemovalWriteKeepsPhotoAndFile() = runTest {
        store.profiles.value = BabyProfile(photoFileName = "old.image")
        store.failWrite = true
        try {
            repository.removePhoto()
            fail("Write should fail")
        } catch (_: IOException) {}
        assertEquals("old.image", store.profiles.value.photoFileName)
        assertTrue(photos.deleted.isEmpty())
    }

    /**
     * Provides in-memory profile metadata with controllable write failures for repository tests.
     */
    private class MemoryStore : ProfileStore {
        override val profiles = MutableStateFlow(BabyProfile())
        var failWrite = false

        override suspend fun update(transform: (BabyProfile) -> BabyProfile) {
            if (failWrite) throw IOException("Write failed")
            profiles.value = transform(profiles.value)
        }
    }

    /** Simulates photo file operations and records cleanup for repository tests. */
    private class MemoryPhotos : ProfilePhotos {
        var failImport = false
        val deleted = mutableListOf<String>()

        override suspend fun importPhoto(source: String): String {
            if (failImport) throw IOException("Import failed")
            return "new.image"
        }

        override suspend fun delete(fileName: String) {
            deleted += fileName
        }

        override fun path(fileName: String) = "/photos/$fileName"
    }
}
