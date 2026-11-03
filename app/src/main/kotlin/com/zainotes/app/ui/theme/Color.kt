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
    val shortName: String,
    val lightContainer: Color,
    val onLightContainer: Color,
    val darkContainer: Color,
    val onDarkContainer: Color
) {
    DEFAULT(
        id = "default",
        displayName = "Default",
        shortName = "Default",
        lightContainer = Color(0xFFECE6F0),
        onLightContainer = Color(0xFF1D1B20),
        darkContainer = Color(0xFF161616),
        onDarkContainer = Color(0xFFE6E0E9)
    ),
    CORAL(
        id = "coral",
        displayName = "Coral Red",
        shortName = "Coral",
        lightContainer = Color(0xFFFF8A80),
        onLightContainer = Color(0xFF380705),
        darkContainer = Color(0xFF6B1E19),
        onDarkContainer = Color(0xFFFFDAD6)
    ),
    PEACH(
        id = "peach",
        displayName = "Sunset Peach",
        shortName = "Sunset",
        lightContainer = Color(0xFFFFAB91),
        onLightContainer = Color(0xFF371406),
        darkContainer = Color(0xFF6B2C16),
        onDarkContainer = Color(0xFFFFDBCF)
    ),
    SAND(
        id = "sand",
        displayName = "Golden Sand",
        shortName = "Golden",
        lightContainer = Color(0xFFFFE082),
        onLightContainer = Color(0xFF332600),
        darkContainer = Color(0xFF594300),
        onDarkContainer = Color(0xFFFFEFB8)
    ),
    MINT(
        id = "mint",
        displayName = "Fresh Mint",
        shortName = "Fresh",
        lightContainer = Color(0xFFA5D6A7),
        onLightContainer = Color(0xFF0B2E13),
        darkContainer = Color(0xFF1E4620),
        onDarkContainer = Color(0xFFC8E6C9)
    ),
    SAGE(
        id = "sage",
        displayName = "Botanical Sage",
        shortName = "Botanical",
        lightContainer = Color(0xFF80CBC4),
        onLightContainer = Color(0xFF002B26),
        darkContainer = Color(0xFF164A45),
        onDarkContainer = Color(0xFFB2DFDB)
    ),
    SKY(
        id = "sky",
        displayName = "Sky Blue",
        shortName = "Sky",
        lightContainer = Color(0xFF81D4FA),
        onLightContainer = Color(0xFF00273B),
        darkContainer = Color(0xFF104563),
        onDarkContainer = Color(0xFFB3E5FC)
    ),
    COBALT(
        id = "cobalt",
        displayName = "Deep Cobalt",
        shortName = "Deep",
        lightContainer = Color(0xFF9FA8DA),
        onLightContainer = Color(0xFF101840),
        darkContainer = Color(0xFF283566),
        onDarkContainer = Color(0xFFC5CAE9)
    ),
    LAVENDER(
        id = "lavender",
        displayName = "Soft Lavender",
        shortName = "Soft",
        lightContainer = Color(0xFFCE93D8),
        onLightContainer = Color(0xFF2F0A38),
        darkContainer = Color(0xFF4E2457),
        onDarkContainer = Color(0xFFE1BEE7)
    ),
    BLOSSOM(
        id = "blossom",
        displayName = "Cherry Blossom",
        shortName = "Cherry",
        lightContainer = Color(0xFFF48FB1),
        onLightContainer = Color(0xFF3B081B),
        darkContainer = Color(0xFF631D35),
        onDarkContainer = Color(0xFFF8BBD0)
    ),
    CLAY(
        id = "clay",
        displayName = "Warm Clay",
        shortName = "Warm",
        lightContainer = Color(0xFFBCAAA4),
        onLightContainer = Color(0xFF291D19),
        darkContainer = Color(0xFF4A3833),
        onDarkContainer = Color(0xFFD7CCC8)
    ),
    SLATE(
        id = "slate",
        displayName = "Storm Slate",
        shortName = "Storm",
        lightContainer = Color(0xFFB0BEC5),
        onLightContainer = Color(0xFF172329),
        darkContainer = Color(0xFF293941),
        onDarkContainer = Color(0xFFECEFF1)
    );

    companion object {
        fun fromId(id: String?): NoteColor {
            if (id == null) return DEFAULT
            return when (id.lowercase()) {
                "amber" -> SAND
                "lime" -> MINT
                "indigo" -> COBALT
                "rose" -> BLOSSOM
                else -> entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
            }
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
