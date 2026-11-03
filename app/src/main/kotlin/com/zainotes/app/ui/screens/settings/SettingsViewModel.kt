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
    val amoledPureBlackCards: Boolean = false,
    val showCardBackgrounds: Boolean = true,
    val defaultCardSize: CardSize = CardSize.STANDARD,
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
    val totalNotesCount: Int = 0,
    val checklistsCount: Int = 0,
    val pinnedCount: Int = 0,
    val backupMessage: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settingsFlow,
        notesRepository.activeNotes,
        _message
    ) { snapshot, notes, msg ->
        SettingsUiState(
            themeMode = snapshot.themeMode,
            dynamicColor = snapshot.dynamicColor,
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
            totalNotesCount = notes.size,
            checklistsCount = notes.count { it.totalTasksCount > 0 },
            pinnedCount = notes.count { it.isPinned },
            backupMessage = msg
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

    fun resetSettingsToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
            _message.value = "All preferences restored to defaults"
        }
    }

    fun clearAllNotes() {
        viewModelScope.launch {
            notesRepository.deleteAllNotes()
            _message.value = "All notes have been cleared from local storage"
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.exportNotesToJson(uri)
            result.onSuccess { count ->
                _message.value = "Successfully exported $count notes to backup file"
            }.onFailure { err ->
                _message.value = "Export failed: ${err.message}"
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.importNotesFromJson(uri)
            result.onSuccess { count ->
                _message.value = "Successfully imported $count notes!"
            }.onFailure { err ->
                _message.value = "Import failed: ${err.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
