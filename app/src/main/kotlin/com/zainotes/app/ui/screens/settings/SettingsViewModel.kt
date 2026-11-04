/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zainotes.app.data.ai.AiProvider
import com.zainotes.app.data.ai.AiProviderClient
import com.zainotes.app.data.backup.BackupManager
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.preferences.SettingsRepository
import com.zainotes.app.data.repository.NotesRepository
import com.zainotes.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val themeAccentColor: String = "violet",
    val amoledPureBlackCards: Boolean = false,
    val showCardBackgrounds: Boolean = true,
    val defaultCardSize: CardSize = CardSize.FREEFORM,
    val defaultNoteColor: String = "default",
    val defaultBackgroundTheme: String = "none",
    val gridColumns: Int = 2,
    val compactCardSpacing: Boolean = false,
    val showTaskProgress: Boolean = true,
    val showNoteMetadata: Boolean = true,
    val confirmNoteDelete: Boolean = true,
    val strikethroughCompleted: Boolean = true,
    val moveCheckedToBottom: Boolean = false,
    val autoFocusNewNote: Boolean = true,
    val aiProvider: AiProvider = AiProvider.GEMINI,
    val activeApiKey: String = "",
    val activeModelName: String = AiProvider.GEMINI.defaultModel,
    val availableModels: List<String> = AiProvider.GEMINI.suggestedModels,
    val aiCustomBaseUrl: String = "https://api.openai.com/v1",
    val aiMemoriesCount: Int = 0,
    val isFetchingModels: Boolean = false,
    val promptModelSelector: Boolean = false,
    val totalNotesCount: Int = 0,
    val checklistsCount: Int = 0,
    val pinnedCount: Int = 0,
    val autoCheckUpdates: Boolean = true,
    val backupMessage: String? = null
)

private data class AiTransientUiState(
    val message: String? = null,
    val isFetchingModels: Boolean = false,
    val promptModelSelector: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _transient = MutableStateFlow(AiTransientUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settingsFlow,
        notesRepository.activeNotes,
        _transient
    ) { snapshot, notes, transient ->
        SettingsUiState(
            themeMode = snapshot.themeMode,
            dynamicColor = snapshot.dynamicColor,
            themeAccentColor = snapshot.themeAccentColor,
            amoledPureBlackCards = snapshot.amoledPureBlackCards,
            showCardBackgrounds = snapshot.showCardBackgrounds,
            defaultCardSize = snapshot.defaultCardSize,
            defaultNoteColor = snapshot.defaultNoteColor,
            defaultBackgroundTheme = snapshot.defaultBackgroundTheme,
            gridColumns = snapshot.gridColumns,
            compactCardSpacing = snapshot.compactCardSpacing,
            showTaskProgress = snapshot.showTaskProgress,
            showNoteMetadata = snapshot.showNoteMetadata,
            confirmNoteDelete = snapshot.confirmNoteDelete,
            strikethroughCompleted = snapshot.strikethroughCompleted,
            moveCheckedToBottom = snapshot.moveCheckedToBottom,
            autoFocusNewNote = snapshot.autoFocusNewNote,
            aiProvider = snapshot.activeAiProvider,
            activeApiKey = snapshot.activeApiKey,
            activeModelName = snapshot.activeModelName,
            availableModels = snapshot.activeAvailableModels,
            aiCustomBaseUrl = snapshot.aiCustomBaseUrl,
            aiMemoriesCount = snapshot.aiMemories.size,
            isFetchingModels = transient.isFetchingModels,
            promptModelSelector = transient.promptModelSelector,
            totalNotesCount = notes.size,
            checklistsCount = notes.count { it.totalTasksCount > 0 },
            pinnedCount = notes.count { it.isPinned },
            autoCheckUpdates = snapshot.autoCheckUpdates,
            backupMessage = transient.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    }

    fun setThemeAccentColor(colorId: String) {
        viewModelScope.launch { settingsRepository.setThemeAccentColor(colorId) }
    }

    fun setAmoledPureBlackCards(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAmoledPureBlackCards(enabled) }
    }

    fun setShowCardBackgrounds(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowCardBackgrounds(enabled) }
    }

    fun setDefaultCardSize(size: CardSize) {
        viewModelScope.launch { settingsRepository.setDefaultCardSize(size) }
    }

    fun setDefaultNoteColor(colorId: String) {
        viewModelScope.launch { settingsRepository.setDefaultNoteColor(colorId) }
    }

    fun setDefaultBackgroundTheme(themeId: String) {
        viewModelScope.launch { settingsRepository.setDefaultBackgroundTheme(themeId) }
    }

    fun setGridColumns(columns: Int) {
        viewModelScope.launch { settingsRepository.setGridColumns(columns) }
    }

    fun setCompactCardSpacing(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCompactCardSpacing(enabled) }
    }

    fun setShowTaskProgress(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowTaskProgress(enabled) }
    }

    fun setShowNoteMetadata(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setShowNoteMetadata(enabled) }
    }

    fun setConfirmNoteDelete(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setConfirmNoteDelete(enabled) }
    }

    fun setStrikethroughCompleted(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setStrikethroughCompleted(enabled) }
    }

    fun setMoveCheckedToBottom(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMoveCheckedToBottom(enabled) }
    }

    fun setAutoFocusNewNote(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoFocusNewNote(enabled) }
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoCheckUpdates(enabled) }
    }

    fun setDismissedUpdateTag(tagName: String) {
        viewModelScope.launch { settingsRepository.setDismissedUpdateTag(tagName) }
    }

    fun setAiProvider(provider: AiProvider) {
        viewModelScope.launch {
            settingsRepository.setAiProviderId(provider.id)
        }
    }

    fun setAiCustomBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            settingsRepository.setAiCustomBaseUrl(baseUrl)
        }
    }

    fun saveApiKeyAndFetchModels(provider: AiProvider, apiKey: String, customBaseUrl: String) {
        viewModelScope.launch {
            val trimmedKey = apiKey.trim()
            settingsRepository.setAiProviderId(provider.id)
            settingsRepository.setAiApiKey(provider.id, trimmedKey)
            if (provider == AiProvider.CUSTOM && customBaseUrl.isNotBlank()) {
                settingsRepository.setAiCustomBaseUrl(customBaseUrl.trim())
            }
            val allowBlankKey = provider == AiProvider.CUSTOM && AiProviderClient.isLocalEndpoint(customBaseUrl)
            if (trimmedKey.isBlank() && !allowBlankKey) {
                _transient.value = _transient.value.copy(
                    message = "Removed API key for ${provider.displayName}"
                )
                return@launch
            }

            _transient.value = _transient.value.copy(isFetchingModels = true)
            val result = AiProviderClient.fetchModelsFromApi(
                provider = provider,
                apiKey = trimmedKey,
                customBaseUrl = customBaseUrl
            )
            result.onSuccess { fetchedModels ->
                settingsRepository.setAiFetchedModels(provider.id, fetchedModels)
                val firstModel = fetchedModels.firstOrNull() ?: provider.defaultModel
                if (uiState.value.activeModelName.isBlank()) {
                    settingsRepository.setAiSelectedModel(provider.id, firstModel)
                }
                _transient.value = _transient.value.copy(
                    isFetchingModels = false,
                    promptModelSelector = true,
                    message = "Loaded ${fetchedModels.size} AI models from ${provider.displayName}"
                )
            }.onFailure { err ->
                _transient.value = _transient.value.copy(
                    isFetchingModels = false,
                    promptModelSelector = true,
                    message = "Saved! Pick a recommended model or type a model name below (${err.message ?: "offline"})."
                )
            }
        }
    }

    fun selectAiModel(provider: AiProvider, modelName: String) {
        val clean = modelName.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            settingsRepository.setAiSelectedModel(provider.id, clean)
            _transient.value = _transient.value.copy(
                promptModelSelector = false,
                message = "Now using $clean"
            )
        }
    }

    fun dismissModelSelectorPrompt() {
        _transient.value = _transient.value.copy(promptModelSelector = false)
    }

    fun openModelSelectorPrompt() {
        _transient.value = _transient.value.copy(promptModelSelector = true)
    }

    fun resetSettingsToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
            _transient.value = _transient.value.copy(message = "Settings have been reset to default")
        }
    }

    fun clearAllNotes() {
        viewModelScope.launch {
            notesRepository.deleteAllNotes()
            _transient.value = _transient.value.copy(message = "All notes have been deleted")
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.exportNotesToJson(uri)
            result.onSuccess { count ->
                _transient.value = _transient.value.copy(message = "Saved $count notes to your backup file")
            }.onFailure { err ->
                _transient.value = _transient.value.copy(message = "Could not save backup: ${err.message}")
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.importNotesFromJson(uri)
            result.onSuccess { count ->
                _transient.value = _transient.value.copy(message = "Restored $count notes from backup!")
            }.onFailure { err ->
                _transient.value = _transient.value.copy(message = "Could not restore backup: ${err.message}")
            }
        }
    }

    fun clearMessage() {
        _transient.value = _transient.value.copy(message = null)
    }
}
