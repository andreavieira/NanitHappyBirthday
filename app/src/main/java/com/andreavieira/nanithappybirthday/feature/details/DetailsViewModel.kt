package com.andreavieira.nanithappybirthday.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreavieira.nanithappybirthday.data.profile.ProfileRepository
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Loads and automatically persists editable details, serializes photo operations, and flushes the
 * current draft before continuation.
 */
class DetailsViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(DetailsUiState(isLoading = true, hasLoadedProfile = false))
    val uiState = mutableUiState.asStateFlow()
    private val saves = Channel<SaveAction>(Channel.UNLIMITED)
    // Coalesce edits while a write is running without disabling typing or screen actions.
    private val detailsSaves = Channel<Unit>(Channel.CONFLATED)
    private val detailsSaveMutex = Mutex()

    init {
        viewModelScope.launch {
            loadProfile()
            launch { for (signal in detailsSaves) persistCurrentDetails(reportFailure = true) }
            for (action in saves) {
                try {
                    if (!uiState.value.hasLoadedProfile) continue
                    when (action) {
                        SaveAction.RemovePhoto -> {
                            repository.removePhoto()
                            mutableUiState.update { it.copy(photoPath = null) }
                        }
                        is SaveAction.Photo -> {
                            val path = repository.selectPhoto(action.source)
                            mutableUiState.update { it.copy(photoPath = path) }
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableUiState.update {
                        if (it.error == DetailsError.LOAD) it
                        else
                            it.copy(
                                error =
                                    if (action == SaveAction.RemovePhoto) DetailsError.REMOVE_PHOTO
                                    else DetailsError.PHOTO
                            )
                    }
                } finally {
                    mutableUiState.update { it.copy(pendingSaves = it.pendingSaves - 1) }
                    if (action is SaveAction.Photo) action.onFinished()
                }
            }
        }
    }

    private suspend fun loadProfile() {
        mutableUiState.update { it.copy(isLoading = true, error = null) }
        try {
            val profile = repository.profiles.first()
            mutableUiState.update {
                it.copy(
                    name = profile.name,
                    birthday = profile.birthday,
                    photoPath = profile.photoFileName?.let(repository::photoPath),
                    isLoading = false,
                    hasLoadedProfile = true,
                    error = null,
                )
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            mutableUiState.update { it.copy(isLoading = false, error = DetailsError.LOAD) }
        }
    }

    fun updateName(name: String) {
        if (!uiState.value.canEdit) return
        mutableUiState.update { it.copy(name = name) }
        detailsSaves.trySend(Unit)
    }

    fun updateBirthday(birthday: LocalDate) {
        if (!uiState.value.canEdit) return
        mutableUiState.update { it.copy(birthday = birthday) }
        detailsSaves.trySend(Unit)
    }

    fun selectPhoto(source: String, onFinished: () -> Unit = {}) {
        // Results can return while a newly recreated screen is still loading.
        mutableUiState.update {
            if (
                it.error == DetailsError.PHOTO ||
                    it.error == DetailsError.CAMERA ||
                    it.error == DetailsError.REMOVE_PHOTO
            )
                it.copy(error = null)
            else it
        }
        enqueue(SaveAction.Photo(source, onFinished))
    }

    fun removePhoto() {
        if (!uiState.value.canEdit || uiState.value.pendingSaves > 0) return
        dismissPhotoError()
        enqueue(SaveAction.RemovePhoto)
    }

    fun reportCameraError() {
        mutableUiState.update {
            if (it.error == DetailsError.LOAD) it else it.copy(error = DetailsError.CAMERA)
        }
    }

    fun dismissPhotoError() {
        mutableUiState.update {
            if (
                it.error == DetailsError.PHOTO ||
                    it.error == DetailsError.CAMERA ||
                    it.error == DetailsError.REMOVE_PHOTO
            )
                it.copy(error = null)
            else it
        }
    }

    fun retry() {
        if (uiState.value.error == DetailsError.LOAD) viewModelScope.launch { loadProfile() }
    }

    fun dismissDetailsSaveError() {
        mutableUiState.update { it.copy(detailsSaveFailed = false) }
    }

    // Flush behind any active autosave; disk failure still permits viewing the in-memory card.
    suspend fun saveBeforeBirthday(): Boolean {
        if (!uiState.value.canShowBirthday) return false
        mutableUiState.update { it.copy(pendingSaves = it.pendingSaves + 1) }
        return try {
            persistCurrentDetails(reportFailure = false)
        } finally {
            mutableUiState.update { it.copy(pendingSaves = it.pendingSaves - 1) }
        }
    }

    private suspend fun persistCurrentDetails(reportFailure: Boolean): Boolean =
        detailsSaveMutex.withLock {
            // Read after acquiring the lock so a queued write cannot restore an older draft.
            val state = uiState.value
            try {
                repository.saveDetails(state.name, state.birthday)
                mutableUiState.update { it.copy(detailsSaveFailed = false) }
                true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (reportFailure) mutableUiState.update { it.copy(detailsSaveFailed = true) }
                false
            }
        }

    private fun enqueue(action: SaveAction) {
        mutableUiState.update { it.copy(pendingSaves = it.pendingSaves + 1) }
        saves.trySend(action)
    }

    /** Describes photo imports queued for sequential processing by the ViewModel. */
    private sealed interface SaveAction {
        /** Clears the stored photo and restores the placeholder after successful removal. */
        data object RemovePhoto : SaveAction

        /** Carries a photo source URI and cleanup callback to run after the import attempt. */
        data class Photo(val source: String, val onFinished: () -> Unit) : SaveAction
    }
}
