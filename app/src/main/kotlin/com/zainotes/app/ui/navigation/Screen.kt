/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AiChat : Screen("ai_chat")
    data object Settings : Screen("settings")
    data object Editor : Screen("editor/{noteId}") {
        fun createRoute(noteId: Long = 0L) = "editor/$noteId"
    }
}
