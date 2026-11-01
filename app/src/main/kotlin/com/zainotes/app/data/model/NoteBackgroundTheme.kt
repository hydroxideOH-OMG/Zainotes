/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Background Themes & Illustration patterns for Notes.
 * Includes all themes from Google Keep plus extended creative themes.
 */
enum class NoteBackgroundTheme(
    val id: String,
    val displayName: String,
    val icon: ImageVector
) {
    NONE(
        id = "none",
        displayName = "None",
        icon = Icons.Default.Block
    ),
    GROCERIES(
        id = "groceries",
        displayName = "Groceries",
        icon = Icons.Default.ShoppingBag
    ),
    FOOD(
        id = "food",
        displayName = "Food & Cooking",
        icon = Icons.Default.Restaurant
    ),
    MUSIC(
        id = "music",
        displayName = "Music",
        icon = Icons.Default.MusicNote
    ),
    RECIPES(
        id = "recipes",
        displayName = "Recipes",
        icon = Icons.Default.SoupKitchen
    ),
    NOTES(
        id = "notes",
        displayName = "Notes & Pen",
        icon = Icons.Default.Description
    ),
    PLACES(
        id = "places",
        displayName = "Travel & Places",
        icon = Icons.Default.Flight
    ),
    VIDEO(
        id = "video",
        displayName = "Cinema & Film",
        icon = Icons.Default.Movie
    ),
    CELEBRATION(
        id = "celebration",
        displayName = "Celebration",
        icon = Icons.Default.Celebration
    ),
    STORM(
        id = "storm",
        displayName = "Storm & Weather",
        icon = Icons.Default.Thunderstorm
    ),
    DUSK(
        id = "dusk",
        displayName = "Night Sky",
        icon = Icons.Default.Nightlight
    ),
    NATURE(
        id = "nature",
        displayName = "Flora & Nature",
        icon = Icons.Default.Park
    ),
    DOODLES(
        id = "doodles",
        displayName = "Doodles",
        icon = Icons.Default.Brush
    ),
    RULED(
        id = "ruled",
        displayName = "Ruled Lines",
        icon = Icons.Default.ViewHeadline
    ),
    GRID(
        id = "grid",
        displayName = "Graph Grid",
        icon = Icons.Default.GridOn
    ),
    DOTS(
        id = "dots",
        displayName = "Dot Matrix",
        icon = Icons.Default.BlurOn
    );

    companion object {
        fun fromId(id: String?): NoteBackgroundTheme {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}
