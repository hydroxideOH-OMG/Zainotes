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
val AmoledSurface = Color(0xFF000000)
val AmoledSurfaceDim = Color(0xFF000000)
val AmoledSurfaceBright = Color(0xFF141414)
val AmoledSurfaceVariant = Color(0xFF141416)
val AmoledSurfaceContainerLowest = Color(0xFF000000)
val AmoledSurfaceContainerLow = Color(0xFF080808)
val AmoledSurfaceContainer = Color(0xFF101010)
val AmoledSurfaceContainerHigh = Color(0xFF161616)
val AmoledSurfaceContainerHighest = Color(0xFF1C1C1E)

/**
 * Vibrant Expressive Note Colors.
 * Provides rich, vibrant, high-contrast hues for light and AMOLED dark modes.
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
        lightContainer = Color.Unspecified,
        onLightContainer = Color.Unspecified,
        darkContainer = Color.Unspecified,
        onDarkContainer = Color.Unspecified
    ),
    CORAL(
        id = "coral",
        displayName = "Coral Red",
        lightContainer = Color(0xFFFF8A80),
        onLightContainer = Color(0xFF380705),
        darkContainer = Color(0xFF6B1E19),
        onDarkContainer = Color(0xFFFFDAD6)
    ),
    PEACH(
        id = "peach",
        displayName = "Peach",
        lightContainer = Color(0xFFFFAB91),
        onLightContainer = Color(0xFF360F00),
        darkContainer = Color(0xFF6E3310),
        onDarkContainer = Color(0xFFFFDCC7)
    ),
    AMBER(
        id = "amber",
        displayName = "Amber",
        lightContainer = Color(0xFFFFD54F),
        onLightContainer = Color(0xFF2E2000),
        darkContainer = Color(0xFF5E4200),
        onDarkContainer = Color(0xFFFFEFA8)
    ),
    LIME(
        id = "lime",
        displayName = "Lime",
        lightContainer = Color(0xFFD4E157),
        onLightContainer = Color(0xFF222900),
        darkContainer = Color(0xFF3E4E0C),
        onDarkContainer = Color(0xFFEAF5A0)
    ),
    SAGE(
        id = "sage",
        displayName = "Emerald",
        lightContainer = Color(0xFF81C784),
        onLightContainer = Color(0xFF002E0E),
        darkContainer = Color(0xFF1B532C),
        onDarkContainer = Color(0xFFB9F6CA)
    ),
    MINT(
        id = "mint",
        displayName = "Teal Mint",
        lightContainer = Color(0xFF4DB6AC),
        onLightContainer = Color(0xFF002824),
        darkContainer = Color(0xFF104E48),
        onDarkContainer = Color(0xFFA7FFEB)
    ),
    SKY(
        id = "sky",
        displayName = "Sky Blue",
        lightContainer = Color(0xFF4FC3F7),
        onLightContainer = Color(0xFF002336),
        darkContainer = Color(0xFF124A75),
        onDarkContainer = Color(0xFFBBDEFB)
    ),
    INDIGO(
        id = "indigo",
        displayName = "Indigo",
        lightContainer = Color(0xFF7986CB),
        onLightContainer = Color(0xFF0E1642),
        darkContainer = Color(0xFF252D68),
        onDarkContainer = Color(0xFFC5CAE9)
    ),
    LAVENDER(
        id = "lavender",
        displayName = "Purple",
        lightContainer = Color(0xFFBA68C8),
        onLightContainer = Color(0xFF2B0038),
        darkContainer = Color(0xFF53165E),
        onDarkContainer = Color(0xFFF3E5F5)
    ),
    ROSE(
        id = "rose",
        displayName = "Hot Rose",
        lightContainer = Color(0xFFF06292),
        onLightContainer = Color(0xFF3B001B),
        darkContainer = Color(0xFF661034),
        onDarkContainer = Color(0xFFFCE4EC)
    ),
    CLAY(
        id = "clay",
        displayName = "Warm Mocha",
        lightContainer = Color(0xFFA1887F),
        onLightContainer = Color(0xFF231510),
        darkContainer = Color(0xFF4B342C),
        onDarkContainer = Color(0xFFEFEBE9)
    ),
    SLATE(
        id = "slate",
        displayName = "Slate",
        lightContainer = Color(0xFF90A4AE),
        onLightContainer = Color(0xFF101B20),
        darkContainer = Color(0xFF293941),
        onDarkContainer = Color(0xFFECEFF1)
    );

    companion object {
        fun fromId(id: String?): NoteColor {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }

    fun containerColor(isDark: Boolean, defaultContainer: Color): Color {
        return if (this == DEFAULT) {
            defaultContainer
        } else if (isDark) {
            darkContainer
        } else {
            lightContainer
        }
    }

    fun contentColor(isDark: Boolean, defaultContent: Color): Color {
        return if (this == DEFAULT) {
            defaultContent
        } else if (isDark) {
            onDarkContainer
        } else {
            onLightContainer
        }
    }
}
