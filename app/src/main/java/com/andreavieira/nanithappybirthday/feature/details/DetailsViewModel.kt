package com.andreavieira.nanithappybirthday.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andreavieira.nanithappybirthday.data.profile.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Loads the profile, holds editable details, and coordinates photo imports and saving before continuation. */
class DetailsViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val mutableUiState = MutableStateFlow(DetailsUiState(isLoading = true, hasLoadedProfile = false))
    val uiState = mutableUiState.asStateFlow()
    private val saves = Channel<SaveAction>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            loadProfile()
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
                        if (it.error == DetailsError.LOAD) it else it.copy(error = if (action == SaveAction.RemovePhoto) DetailsError.REMOVE_PHOTO else DetailsError.PHOTO)
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
    }

    fun updateBirthday(birthday: LocalDate) {
        if (!uiState.value.canEdit) return
        mutableUiState.update { it.copy(birthday = birthday) }
    }

    fun selectPhoto(source: String, onFinished: () -> Unit = {}) {
        // Results can return while a newly recreated screen is still loading.
        mutableUiState.update {
            if (it.error == DetailsError.PHOTO || it.error == DetailsError.CAMERA || it.error == DetailsError.REMOVE_PHOTO) it.copy(error = null) else it
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
            if (it.error == DetailsError.LOAD) it
            else it.copy(error = DetailsError.CAMERA)
        }
    }
    fun dismissPhotoError() {
        mutableUiState.update {
            if (it.error == DetailsError.PHOTO || it.error == DetailsError.CAMERA || it.error == DetailsError.REMOVE_PHOTO) it.copy(error = null) else it
        }
    }

    fun retry() {
        if (uiState.value.error == DetailsError.LOAD) viewModelScope.launch { loadProfile() }

    }

    // Persist the current draft before continuing, but a disk failure must not block the card.
    suspend fun saveBeforeBirthday(): Boolean {
        val state = uiState.value
        if (!state.canShowBirthday) return false
        mutableUiState.update { it.copy(pendingSaves = it.pendingSaves + 1) }
        return try {
            repository.saveDetails(state.name, state.birthday)
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            false
        } finally {
            mutableUiState.update { it.copy(pendingSaves = it.pendingSaves - 1) }
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
