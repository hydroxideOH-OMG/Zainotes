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

data class AppSettingsSnapshot(
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
    val autoFocusNewNote: Boolean = true
)

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val AMOLED_PURE_BLACK_CARDS = booleanPreferencesKey("amoled_pure_black_cards")
        val SHOW_CARD_BACKGROUNDS = booleanPreferencesKey("show_card_backgrounds")
        val DEFAULT_CARD_SIZE = stringPreferencesKey("default_card_size")
        val DEFAULT_NOTE_COLOR = stringPreferencesKey("default_note_color")
        val DEFAULT_BACKGROUND_THEME = stringPreferencesKey("default_background_theme")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val COMPACT_CARD_SPACING = booleanPreferencesKey("compact_card_spacing")
        val SHOW_TASK_PROGRESS = booleanPreferencesKey("show_task_progress")
        val SHOW_NOTE_METADATA = booleanPreferencesKey("show_note_metadata")
        val CONFIRM_NOTE_DELETE = booleanPreferencesKey("confirm_note_delete")
        val STRIKETHROUGH_COMPLETED = booleanPreferencesKey("strikethrough_completed")
        val MOVE_CHECKED_TO_BOTTOM = booleanPreferencesKey("move_checked_to_bottom")
        val AUTO_FOCUS_NEW_NOTE = booleanPreferencesKey("auto_focus_new_note")
    }

    val settingsFlow: Flow<AppSettingsSnapshot> = context.dataStore.data.map { preferences ->
        val modeName = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val resolvedTheme = try {
            if (modeName.equals("DARK", ignoreCase = true)) ThemeMode.AMOLED
            else ThemeMode.valueOf(modeName)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        val sizeName = preferences[PreferencesKeys.DEFAULT_CARD_SIZE] ?: CardSize.STANDARD.name
        AppSettingsSnapshot(
            themeMode = resolvedTheme,
            dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
            amoledPureBlackCards = preferences[PreferencesKeys.AMOLED_PURE_BLACK_CARDS] ?: false,
            showCardBackgrounds = preferences[PreferencesKeys.SHOW_CARD_BACKGROUNDS] ?: true,
            defaultCardSize = CardSize.fromName(sizeName),
            defaultNoteColor = preferences[PreferencesKeys.DEFAULT_NOTE_COLOR] ?: "default",
            defaultBackgroundTheme = preferences[PreferencesKeys.DEFAULT_BACKGROUND_THEME] ?: "none",
            gridColumns = preferences[PreferencesKeys.GRID_COLUMNS] ?: 2,
            compactCardSpacing = preferences[PreferencesKeys.COMPACT_CARD_SPACING] ?: false,
            showTaskProgress = preferences[PreferencesKeys.SHOW_TASK_PROGRESS] ?: true,
            showNoteMetadata = preferences[PreferencesKeys.SHOW_NOTE_METADATA] ?: true,
            confirmNoteDelete = preferences[PreferencesKeys.CONFIRM_NOTE_DELETE] ?: true,
            strikethroughCompleted = preferences[PreferencesKeys.STRIKETHROUGH_COMPLETED] ?: true,
            moveCheckedToBottom = preferences[PreferencesKeys.MOVE_CHECKED_TO_BOTTOM] ?: false,
            autoFocusNewNote = preferences[PreferencesKeys.AUTO_FOCUS_NEW_NOTE] ?: true
        )
    }

    val themeMode: Flow<ThemeMode> = settingsFlow.map { it.themeMode }
    val dynamicColor: Flow<Boolean> = settingsFlow.map { it.dynamicColor }
    val amoledPureBlackCards: Flow<Boolean> = settingsFlow.map { it.amoledPureBlackCards }
    val showCardBackgrounds: Flow<Boolean> = settingsFlow.map { it.showCardBackgrounds }
    val defaultCardSize: Flow<CardSize> = settingsFlow.map { it.defaultCardSize }
    val defaultNoteColor: Flow<String> = settingsFlow.map { it.defaultNoteColor }
    val defaultBackgroundTheme: Flow<String> = settingsFlow.map { it.defaultBackgroundTheme }
    val gridColumns: Flow<Int> = settingsFlow.map { it.gridColumns }
    val compactCardSpacing: Flow<Boolean> = settingsFlow.map { it.compactCardSpacing }
    val showTaskProgress: Flow<Boolean> = settingsFlow.map { it.showTaskProgress }
    val showNoteMetadata: Flow<Boolean> = settingsFlow.map { it.showNoteMetadata }
    val confirmNoteDelete: Flow<Boolean> = settingsFlow.map { it.confirmNoteDelete }
    val strikethroughCompleted: Flow<Boolean> = settingsFlow.map { it.strikethroughCompleted }
    val moveCheckedToBottom: Flow<Boolean> = settingsFlow.map { it.moveCheckedToBottom }
    val autoFocusNewNote: Flow<Boolean> = settingsFlow.map { it.autoFocusNewNote }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setAmoledPureBlackCards(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AMOLED_PURE_BLACK_CARDS] = enabled }
    }

    suspend fun setShowCardBackgrounds(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_CARD_BACKGROUNDS] = enabled }
    }

    suspend fun setDefaultCardSize(cardSize: CardSize) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_CARD_SIZE] = cardSize.name }
    }

    suspend fun setDefaultNoteColor(colorId: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_NOTE_COLOR] = colorId }
    }

    suspend fun setDefaultBackgroundTheme(themeId: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_BACKGROUND_THEME] = themeId }
    }

    suspend fun setGridColumns(columns: Int) {
        context.dataStore.edit { it[PreferencesKeys.GRID_COLUMNS] = columns }
    }

    suspend fun setCompactCardSpacing(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.COMPACT_CARD_SPACING] = enabled }
    }

    suspend fun setShowTaskProgress(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_TASK_PROGRESS] = enabled }
    }

    suspend fun setShowNoteMetadata(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_NOTE_METADATA] = enabled }
    }

    suspend fun setConfirmNoteDelete(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CONFIRM_NOTE_DELETE] = enabled }
    }

    suspend fun setStrikethroughCompleted(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.STRIKETHROUGH_COMPLETED] = enabled }
    }

    suspend fun setMoveCheckedToBottom(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.MOVE_CHECKED_TO_BOTTOM] = enabled }
    }

    suspend fun setAutoFocusNewNote(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_FOCUS_NEW_NOTE] = enabled }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { it.clear() }
    }
}
