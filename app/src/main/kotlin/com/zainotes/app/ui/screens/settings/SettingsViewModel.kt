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
    val defaultCardSize: CardSize = CardSize.STANDARD,
    val gridColumns: Int = 2,
    val showTaskProgress: Boolean = true,
    val showNoteMetadata: Boolean = true,
    val confirmNoteDelete: Boolean = true,
    val strikethroughCompleted: Boolean = true,
    val totalNotesCount: Int = 0,
    val backupMessage: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            settingsRepository.themeMode,
            settingsRepository.dynamicColor,
            settingsRepository.amoledPureBlackCards,
            settingsRepository.defaultCardSize,
            settingsRepository.gridColumns
        ) { theme, dynamic, amoledCards, cardSize, cols ->
            FiveSettings(theme, dynamic, amoledCards, cardSize, cols)
        },
        combine(
            settingsRepository.showTaskProgress,
            settingsRepository.showNoteMetadata,
            settingsRepository.confirmNoteDelete,
            settingsRepository.strikethroughCompleted,
            notesRepository.activeNotes
        ) { progress, meta, confirmDel, strike, notes ->
            FiveDetails(progress, meta, confirmDel, strike, notes.size)
        },
        _message
    ) { s1, s2, msg ->
        SettingsUiState(
            themeMode = s1.theme,
            dynamicColor = s1.dynamic,
            amoledPureBlackCards = s1.amoledCards,
            defaultCardSize = s1.cardSize,
            gridColumns = s1.cols,
            showTaskProgress = s2.progress,
            showNoteMetadata = s2.meta,
            confirmNoteDelete = s2.confirmDel,
            strikethroughCompleted = s2.strike,
            totalNotesCount = s2.noteCount,
            backupMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    private data class FiveSettings(
        val theme: ThemeMode,
        val dynamic: Boolean,
        val amoledCards: Boolean,
        val cardSize: CardSize,
        val cols: Int
    )

    private data class FiveDetails(
        val progress: Boolean,
        val meta: Boolean,
        val confirmDel: Boolean,
        val strike: Boolean,
        val noteCount: Int
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColor(enabled)
        }
    }

    fun setAmoledPureBlackCards(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAmoledPureBlackCards(enabled)
        }
    }

    fun setDefaultCardSize(size: CardSize) {
        viewModelScope.launch {
            settingsRepository.setDefaultCardSize(size)
        }
    }

    fun setGridColumns(columns: Int) {
        viewModelScope.launch {
            settingsRepository.setGridColumns(columns)
        }
    }

    fun setShowTaskProgress(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowTaskProgress(enabled)
        }
    }

    fun setShowNoteMetadata(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowNoteMetadata(enabled)
        }
    }

    fun setConfirmNoteDelete(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setConfirmNoteDelete(enabled)
        }
    }

    fun setStrikethroughCompleted(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setStrikethroughCompleted(enabled)
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
