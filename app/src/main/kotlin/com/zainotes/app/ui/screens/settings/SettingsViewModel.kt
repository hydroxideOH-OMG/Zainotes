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
    val defaultCardSize: CardSize = CardSize.STANDARD,
    val backupMessage: String? = null
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.themeMode,
        settingsRepository.dynamicColor,
        settingsRepository.defaultCardSize,
        _message
    ) { theme, dynamicColor, cardSize, msg ->
        SettingsUiState(
            themeMode = theme,
            dynamicColor = dynamicColor,
            defaultCardSize = cardSize,
            backupMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
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

    fun setDefaultCardSize(size: CardSize) {
        viewModelScope.launch {
            settingsRepository.setDefaultCardSize(size)
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
