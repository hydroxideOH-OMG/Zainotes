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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zainotes.app.data.ai.AiChatSession
import com.zainotes.app.data.ai.AiMemoryItem
import com.zainotes.app.data.ai.AiProvider
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zainotes_settings")

data class AppSettingsSnapshot(
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
    val aiProviderId: String = AiProvider.GEMINI.id,
    val aiApiKeys: Map<String, String> = emptyMap(),
    val aiSelectedModels: Map<String, String> = emptyMap(),
    val aiFetchedModels: Map<String, List<String>> = emptyMap(),
    val aiCustomBaseUrl: String = "https://api.openai.com/v1",
    val aiMemories: List<AiMemoryItem> = emptyList(),
    val aiChatSessions: List<AiChatSession> = emptyList(),
    val activeAiChatSessionId: String = ""
) {
    val activeAiProvider: AiProvider
        get() = AiProvider.fromId(aiProviderId)

    val activeApiKey: String
        get() = aiApiKeys[activeAiProvider.id].orEmpty()

    val activeModelName: String
        get() = aiSelectedModels[activeAiProvider.id]?.takeIf { it.isNotBlank() }
            ?: activeAiProvider.defaultModel

    val activeAvailableModels: List<String>
        get() {
            val fetched = aiFetchedModels[activeAiProvider.id].orEmpty()
            return if (fetched.isNotEmpty()) fetched else activeAiProvider.suggestedModels
        }
}

class SettingsRepository(private val context: Context) {

    private val gson = Gson()

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val THEME_ACCENT_COLOR = stringPreferencesKey("theme_accent_color")
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
        val AI_PROVIDER_ID = stringPreferencesKey("ai_provider_id")
        val AI_API_KEYS_JSON = stringPreferencesKey("ai_api_keys_json")
        val AI_SELECTED_MODELS_JSON = stringPreferencesKey("ai_selected_models_json")
        val AI_FETCHED_MODELS_JSON = stringPreferencesKey("ai_fetched_models_json")
        val AI_CUSTOM_BASE_URL = stringPreferencesKey("ai_custom_base_url")
        val AI_MEMORIES_JSON = stringPreferencesKey("ai_memories_json")
        val AI_CHAT_SESSIONS_JSON = stringPreferencesKey("ai_chat_sessions_json")
        val ACTIVE_AI_CHAT_SESSION_ID = stringPreferencesKey("active_ai_chat_session_id")
    }

    val settingsFlow: Flow<AppSettingsSnapshot> = context.dataStore.data.map { preferences ->
        val modeName = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val resolvedTheme = try {
            if (modeName.equals("DARK", ignoreCase = true)) ThemeMode.AMOLED
            else ThemeMode.valueOf(modeName)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        val sizeName = preferences[PreferencesKeys.DEFAULT_CARD_SIZE] ?: CardSize.FREEFORM.name

        val keysMap: Map<String, String> = try {
            val json = preferences[PreferencesKeys.AI_API_KEYS_JSON]
            if (json.isNullOrBlank()) emptyMap()
            else gson.fromJson(json, object : TypeToken<Map<String, String>>() {}.type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }

        val selectedModelsMap: Map<String, String> = try {
            val json = preferences[PreferencesKeys.AI_SELECTED_MODELS_JSON]
            if (json.isNullOrBlank()) emptyMap()
            else gson.fromJson(json, object : TypeToken<Map<String, String>>() {}.type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }

        val fetchedModelsMap: Map<String, List<String>> = try {
            val json = preferences[PreferencesKeys.AI_FETCHED_MODELS_JSON]
            if (json.isNullOrBlank()) emptyMap()
            else gson.fromJson(json, object : TypeToken<Map<String, List<String>>>() {}.type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }

        val memoriesList: List<AiMemoryItem> = try {
            val json = preferences[PreferencesKeys.AI_MEMORIES_JSON]
            if (json.isNullOrBlank()) emptyList()
            else gson.fromJson(json, object : TypeToken<List<AiMemoryItem>>() {}.type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val chatSessionsList: List<AiChatSession> = try {
            val json = preferences[PreferencesKeys.AI_CHAT_SESSIONS_JSON]
            if (json.isNullOrBlank()) emptyList()
            else gson.fromJson(json, object : TypeToken<List<AiChatSession>>() {}.type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        AppSettingsSnapshot(
            themeMode = resolvedTheme,
            dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true,
            themeAccentColor = preferences[PreferencesKeys.THEME_ACCENT_COLOR] ?: "violet",
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
            autoFocusNewNote = preferences[PreferencesKeys.AUTO_FOCUS_NEW_NOTE] ?: true,
            aiProviderId = preferences[PreferencesKeys.AI_PROVIDER_ID] ?: AiProvider.GEMINI.id,
            aiApiKeys = keysMap,
            aiSelectedModels = selectedModelsMap,
            aiFetchedModels = fetchedModelsMap,
            aiCustomBaseUrl = preferences[PreferencesKeys.AI_CUSTOM_BASE_URL] ?: "https://api.openai.com/v1",
            aiMemories = memoriesList,
            aiChatSessions = chatSessionsList,
            activeAiChatSessionId = preferences[PreferencesKeys.ACTIVE_AI_CHAT_SESSION_ID] ?: ""
        )
    }

    val themeMode: Flow<ThemeMode> = settingsFlow.map { it.themeMode }
    val dynamicColor: Flow<Boolean> = settingsFlow.map { it.dynamicColor }
    val themeAccentColor: Flow<String> = settingsFlow.map { it.themeAccentColor }
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

    suspend fun setThemeAccentColor(colorId: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME_ACCENT_COLOR] = colorId }
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

    suspend fun setAiProviderId(providerId: String) {
        context.dataStore.edit { it[PreferencesKeys.AI_PROVIDER_ID] = providerId }
    }

    suspend fun setAiApiKey(providerId: String, apiKey: String) {
        context.dataStore.edit { prefs ->
            val current: MutableMap<String, String> = try {
                val json = prefs[PreferencesKeys.AI_API_KEYS_JSON]
                if (json.isNullOrBlank()) mutableMapOf()
                else gson.fromJson<Map<String, String>>(json, object : TypeToken<Map<String, String>>() {}.type)?.toMutableMap() ?: mutableMapOf()
            } catch (_: Exception) {
                mutableMapOf()
            }
            current[providerId] = apiKey.trim()
            prefs[PreferencesKeys.AI_API_KEYS_JSON] = gson.toJson(current)
        }
    }

    suspend fun setAiSelectedModel(providerId: String, modelName: String) {
        context.dataStore.edit { prefs ->
            val current: MutableMap<String, String> = try {
                val json = prefs[PreferencesKeys.AI_SELECTED_MODELS_JSON]
                if (json.isNullOrBlank()) mutableMapOf()
                else gson.fromJson<Map<String, String>>(json, object : TypeToken<Map<String, String>>() {}.type)?.toMutableMap() ?: mutableMapOf()
            } catch (_: Exception) {
                mutableMapOf()
            }
            current[providerId] = modelName.trim()
            prefs[PreferencesKeys.AI_SELECTED_MODELS_JSON] = gson.toJson(current)
        }
    }

    suspend fun setAiFetchedModels(providerId: String, models: List<String>) {
        context.dataStore.edit { prefs ->
            val current: MutableMap<String, List<String>> = try {
                val json = prefs[PreferencesKeys.AI_FETCHED_MODELS_JSON]
                if (json.isNullOrBlank()) mutableMapOf()
                else gson.fromJson<Map<String, List<String>>>(json, object : TypeToken<Map<String, List<String>>>() {}.type)?.toMutableMap() ?: mutableMapOf()
            } catch (_: Exception) {
                mutableMapOf()
            }
            current[providerId] = models
            prefs[PreferencesKeys.AI_FETCHED_MODELS_JSON] = gson.toJson(current)
        }
    }

    suspend fun setAiCustomBaseUrl(baseUrl: String) {
        context.dataStore.edit { it[PreferencesKeys.AI_CUSTOM_BASE_URL] = baseUrl.trim() }
    }

    suspend fun setAiMemories(memories: List<AiMemoryItem>) {
        context.dataStore.edit { it[PreferencesKeys.AI_MEMORIES_JSON] = gson.toJson(memories) }
    }

    suspend fun setAiChatSessions(sessions: List<AiChatSession>, activeSessionId: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AI_CHAT_SESSIONS_JSON] = gson.toJson(sessions)
            if (activeSessionId != null) {
                prefs[PreferencesKeys.ACTIVE_AI_CHAT_SESSION_ID] = activeSessionId
            }
        }
    }

    suspend fun setActiveAiChatSessionId(sessionId: String) {
        context.dataStore.edit { it[PreferencesKeys.ACTIVE_AI_CHAT_SESSION_ID] = sessionId }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { it.clear() }
    }
}
