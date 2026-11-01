/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zainotes_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val AMOLED_PURE_BLACK_CARDS = booleanPreferencesKey("amoled_pure_black_cards")
        val DEFAULT_CARD_SIZE = stringPreferencesKey("default_card_size")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val SHOW_TASK_PROGRESS = booleanPreferencesKey("show_task_progress")
        val SHOW_NOTE_METADATA = booleanPreferencesKey("show_note_metadata")
        val CONFIRM_NOTE_DELETE = booleanPreferencesKey("confirm_note_delete")
        val STRIKETHROUGH_COMPLETED = booleanPreferencesKey("strikethrough_completed")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeName = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            if (modeName.equals("DARK", ignoreCase = true)) {
                ThemeMode.AMOLED
            } else {
                ThemeMode.valueOf(modeName)
            }
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
    }

    val amoledPureBlackCards: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AMOLED_PURE_BLACK_CARDS] ?: false
    }

    val defaultCardSize: Flow<CardSize> = context.dataStore.data.map { preferences ->
        val sizeName = preferences[PreferencesKeys.DEFAULT_CARD_SIZE] ?: CardSize.STANDARD.name
        CardSize.fromName(sizeName)
    }

    val gridColumns: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GRID_COLUMNS] ?: 2
    }

    val showTaskProgress: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_TASK_PROGRESS] ?: true
    }

    val showNoteMetadata: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_NOTE_METADATA] ?: true
    }

    val confirmNoteDelete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CONFIRM_NOTE_DELETE] ?: true
    }

    val strikethroughCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.STRIKETHROUGH_COMPLETED] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setAmoledPureBlackCards(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AMOLED_PURE_BLACK_CARDS] = enabled
        }
    }

    suspend fun setDefaultCardSize(cardSize: CardSize) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_CARD_SIZE] = cardSize.name
        }
    }

    suspend fun setGridColumns(columns: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_COLUMNS] = columns
        }
    }

    suspend fun setShowTaskProgress(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_TASK_PROGRESS] = enabled
        }
    }

    suspend fun setShowNoteMetadata(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_NOTE_METADATA] = enabled
        }
    }

    suspend fun setConfirmNoteDelete(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONFIRM_NOTE_DELETE] = enabled
        }
    }

    suspend fun setStrikethroughCompleted(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STRIKETHROUGH_COMPLETED] = enabled
        }
    }
}
