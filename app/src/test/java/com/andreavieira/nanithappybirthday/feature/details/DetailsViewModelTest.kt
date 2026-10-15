package com.andreavieira.nanithappybirthday.feature.details

import com.andreavieira.nanithappybirthday.data.profile.BabyProfile
import com.andreavieira.nanithappybirthday.data.profile.ProfileRepository
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Verifies draft editing, save-on-continue behavior, and asynchronous photo result handling. */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun savedProfileLoadsOnNewViewModel() =
        runTest(dispatcher) {
            val repository = FakeRepository()
            repository.profiles.value =
                BabyProfile("Alex", LocalDate.of(2025, 1, 15), "photo.image")
            val vm = DetailsViewModel(repository)
            advanceUntilIdle()
            assertEquals("Alex", vm.uiState.value.name)
            assertEquals("/photos/photo.image", vm.uiState.value.photoPath)
            assertTrue(vm.uiState.value.canShowBirthday)
        }

    @Test
    fun editsStayInMemoryUntilContinue() =
        runTest(dispatcher) {
            val repository = FakeRepository()
            val vm = DetailsViewModel(repository)
            advanceUntilIdle()
            vm.updateName("Alex")
            vm.updateBirthday(LocalDate.of(2025, 1, 15))
            assertEquals("", repository.profiles.value.name)
            assertTrue(vm.saveBeforeBirthday())
            assertEquals("Alex", repository.profiles.value.name)
        }

    @Test
    fun failedSaveRetainsDraftAndDoesNotBlockContinue() =
        runTest(dispatcher) {
            val repository = FakeRepository()
            val vm = DetailsViewModel(repository)
            advanceUntilIdle()
            vm.updateName("Alex")
            vm.updateBirthday(LocalDate.of(2025, 1, 15))
            repository.failSave = true
            assertFalse(vm.saveBeforeBirthday())
            assertEquals("Alex", vm.uiState.value.name)
            assertTrue(vm.uiState.value.canShowBirthday)
            assertEquals(0, vm.uiState.value.pendingSaves)
            repository.failSave = false
            assertTrue(vm.saveBeforeBirthday())
        }

    @Test
    fun photoResultDuringInitialLoadIsNotLost() =
        runTest(dispatcher) {
            val repository = FakeRepository()
            val vm = DetailsViewModel(repository)
            var cleaned = false
            vm.selectPhoto("camera") { cleaned = true }
            advanceUntilIdle()
            assertEquals("/photos/photo.image", vm.uiState.value.photoPath)
            assertEquals(0, vm.uiState.value.pendingSaves)
            assertTrue(cleaned)
        }

    @Test
    fun photoFailureCanBeDismissedWithoutLosingDraft() =
        runTest(dispatcher) {
            val repository = FakeRepository()
            val vm = DetailsViewModel(repository)
            advanceUntilIdle()
            vm.updateName("Alex")
            vm.updateBirthday(LocalDate.of(2025, 1, 15))
            repository.failPhoto = true
            vm.selectPhoto("bad")
            advanceUntilIdle()
            assertEquals(DetailsError.PHOTO, vm.uiState.value.error)
            assertTrue(vm.uiState.value.canShowBirthday)
            vm.dismissPhotoError()
            assertNull(vm.uiState.value.error)
            assertEquals("Alex", vm.uiState.value.name)
        }

    @Test
    fun removingPhotoRestoresPlaceholderAndPreservesDraft() =
        runTest(dispatcher) {
            val repository = FakeRepository()
            repository.profiles.value =
                BabyProfile("Alex", LocalDate.of(2025, 1, 15), "photo.image")
            val vm = DetailsViewModel(repository)
            advanceUntilIdle()
            vm.removePhoto()
            advanceUntilIdle()
            assertNull(vm.uiState.value.photoPath)
            assertNull(repository.profiles.value.photoFileName)
            assertEquals("Alex", vm.uiState.value.name)
        }

    /** Provides controllable persistence and photo failures for ViewModel behavior tests. */
    private class FakeRepository : ProfileRepository {
        override val profiles = MutableStateFlow(BabyProfile())
        var failSave = false
        var failPhoto = false

        override suspend fun saveDetails(name: String, birthday: LocalDate?) {
            if (failSave) throw IOException("Disk unavailable")
            profiles.value = profiles.value.copy(name = name, birthday = birthday)
        }

        override suspend fun removePhoto() {
            if (failPhoto) throw IOException("Remove failed")
            profiles.value = profiles.value.copy(photoFileName = null)
        }

        override suspend fun selectPhoto(source: String): String {
            if (failPhoto) throw IOException("Invalid photo")
            delay(100)
            profiles.value = profiles.value.copy(photoFileName = "photo.image")
            return photoPath("photo.image")
        }

        override fun photoPath(fileName: String) = "/photos/$fileName"
    }
}
