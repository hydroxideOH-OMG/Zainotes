/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.theme

import androidx.compose.ui.graphics.Color

// Expressive Seed Palette
val PrimaryLight = Color(0xFF6750A4)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFEADDFF)
val OnPrimaryContainerLight = Color(0xFF21005D)

val SecondaryLight = Color(0xFF625B71)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE8DEF8)
val OnSecondaryContainerLight = Color(0xFF1D192B)

val TertiaryLight = Color(0xFF7D5260)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFD8E4)
val OnTertiaryContainerLight = Color(0xFF31111D)

val BackgroundLight = Color(0xFFFEF7FF)
val OnBackgroundLight = Color(0xFF1D1B20)
val SurfaceLight = Color(0xFFFEF7FF)
val OnSurfaceLight = Color(0xFF1D1B20)
val SurfaceVariantLight = Color(0xFFE7E0EC)
val OnSurfaceVariantLight = Color(0xFF49454F)

// Dark Theme Palette
val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF381E72)
val PrimaryContainerDark = Color(0xFF4F378B)
val OnPrimaryContainerDark = Color(0xFFEADDFF)

val SecondaryDark = Color(0xFFCCC2DC)
val OnSecondaryDark = Color(0xFF332D41)
val SecondaryContainerDark = Color(0xFF4A4458)
val OnSecondaryContainerDark = Color(0xFFE8DEF8)

val TertiaryDark = Color(0xFFEFB8C8)
val OnTertiaryDark = Color(0xFF492532)
val TertiaryContainerDark = Color(0xFF633B48)
val OnTertiaryContainerDark = Color(0xFFFFD8E4)

val BackgroundDark = Color(0xFF141218)
val OnBackgroundDark = Color(0xFFE6E0E9)
val SurfaceDark = Color(0xFF141218)
val OnSurfaceDark = Color(0xFFE6E0E9)
val SurfaceVariantDark = Color(0xFF49454F)
val OnSurfaceVariantDark = Color(0xFFCAC4D0)

// Pure AMOLED Black Palette
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF080808)
val AmoledSurfaceVariant = Color(0xFF18181A)
val AmoledSurfaceContainer = Color(0xFF121212)

/**
 * Curated Expressive Note Colors.
 * Provides distinct, aesthetically pleasing pastel tones for light and dark/AMOLED modes.
 */
enum class NoteColor(
    val id: String,
    val displayName: String,
    val lightContainer: Color,
    val onLightContainer: Color,
    val darkContainer: Color,
    val onDarkContainer: Color
) {
    DEFAULT(
        id = "default",
        displayName = "Default",
        lightContainer = Color(0xFFF3EDF7),
        onLightContainer = Color(0xFF1D1B20),
        darkContainer = Color(0xFF211F26),
        onDarkContainer = Color(0xFFE6E0E9)
    ),
    CORAL(
        id = "coral",
        displayName = "Coral",
        lightContainer = Color(0xFFFFDAD6),
        onLightContainer = Color(0xFF410002),
        darkContainer = Color(0xFF3B1E1E),
        onDarkContainer = Color(0xFFFFDAD6)
    ),
    AMBER(
        id = "amber",
        displayName = "Amber",
        lightContainer = Color(0xFFFFE08B),
        onLightContainer = Color(0xFF241A00),
        darkContainer = Color(0xFF352B11),
        onDarkContainer = Color(0xFFFFE08B)
    ),
    SAGE(
        id = "sage",
        displayName = "Sage",
        lightContainer = Color(0xFFC7EBD1),
        onLightContainer = Color(0xFF002110),
        darkContainer = Color(0xFF1C2B20),
        onDarkContainer = Color(0xFFC7EBD1)
    ),
    MINT(
        id = "mint",
        displayName = "Mint",
        lightContainer = Color(0xFFBAF4EB),
        onLightContainer = Color(0xFF00201D),
        darkContainer = Color(0xFF142B28),
        onDarkContainer = Color(0xFFBAF4EB)
    ),
    SKY(
        id = "sky",
        displayName = "Sky",
        lightContainer = Color(0xFFC8E6FF),
        onLightContainer = Color(0xFF001E30),
        darkContainer = Color(0xFF162635),
        onDarkContainer = Color(0xFFC8E6FF)
    ),
    LAVENDER(
        id = "lavender",
        displayName = "Lavender",
        lightContainer = Color(0xFFEADBFF),
        onLightContainer = Color(0xFF22005C),
        darkContainer = Color(0xFF281E38),
        onDarkContainer = Color(0xFFEADBFF)
    ),
    ROSE(
        id = "rose",
        displayName = "Rose",
        lightContainer = Color(0xFFFFD8EB),
        onLightContainer = Color(0xFF370B24),
        darkContainer = Color(0xFF371D2D),
        onDarkContainer = Color(0xFFFFD8EB)
    ),
    SLATE(
        id = "slate",
        displayName = "Slate",
        lightContainer = Color(0xFFDEE2EC),
        onLightContainer = Color(0xFF141C24),
        darkContainer = Color(0xFF21252D),
        onDarkContainer = Color(0xFFDEE2EC)
    );

    companion object {
        fun fromId(id: String?): NoteColor {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
