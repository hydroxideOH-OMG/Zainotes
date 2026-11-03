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
 * Custom Theme Accent Colors used when Dynamic Wallpaper Color is turned off.
 */
enum class ThemeAccentColor(
    val id: String,
    val displayName: String,
    val swatchColor: Color,
    // Light Mode Scheme Tokens
    val primaryLight: Color,
    val onPrimaryLight: Color,
    val primaryContainerLight: Color,
    val onPrimaryContainerLight: Color,
    val secondaryLight: Color,
    val onSecondaryLight: Color,
    val secondaryContainerLight: Color,
    val onSecondaryContainerLight: Color,
    val tertiaryLight: Color,
    val onTertiaryLight: Color,
    val tertiaryContainerLight: Color,
    val onTertiaryContainerLight: Color,
    // Dark / AMOLED Mode Scheme Tokens
    val primaryDark: Color,
    val onPrimaryDark: Color,
    val primaryContainerDark: Color,
    val onPrimaryContainerDark: Color,
    val secondaryDark: Color,
    val onSecondaryDark: Color,
    val secondaryContainerDark: Color,
    val onSecondaryContainerDark: Color,
    val tertiaryDark: Color,
    val onTertiaryDark: Color,
    val tertiaryContainerDark: Color,
    val onTertiaryContainerDark: Color
) {
    VIOLET(
        id = "violet",
        displayName = "Purple",
        swatchColor = Color(0xFF9A82DB),
        primaryLight = Color(0xFF6750A4),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFEADDFF),
        onPrimaryContainerLight = Color(0xFF21005D),
        secondaryLight = Color(0xFF625B71),
        onSecondaryLight = Color(0xFFFFFFFF),
        secondaryContainerLight = Color(0xFFE8DEF8),
        onSecondaryContainerLight = Color(0xFF1D192B),
        tertiaryLight = Color(0xFF7D5260),
        onTertiaryLight = Color(0xFFFFFFFF),
        tertiaryContainerLight = Color(0xFFFFD8E4),
        onTertiaryContainerLight = Color(0xFF31111D),
        primaryDark = Color(0xFFD0BCFF),
        onPrimaryDark = Color(0xFF381E72),
        primaryContainerDark = Color(0xFF4F378B),
        onPrimaryContainerDark = Color(0xFFEADDFF),
        secondaryDark = Color(0xFFCCC2DC),
        onSecondaryDark = Color(0xFF332D41),
        secondaryContainerDark = Color(0xFF4A4458),
        onSecondaryContainerDark = Color(0xFFE8DEF8),
        tertiaryDark = Color(0xFFEFB8C8),
        onTertiaryDark = Color(0xFF492532),
        tertiaryContainerDark = Color(0xFF633B48),
        onTertiaryContainerDark = Color(0xFFFFD8E4)
    ),
    OCEAN(
        id = "ocean",
        displayName = "Ocean Blue",
        swatchColor = Color(0xFF42A5F5),
        primaryLight = Color(0xFF0061A4),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFD1E4FF),
        onPrimaryContainerLight = Color(0xFF001D36),
        secondaryLight = Color(0xFF535F70),
        onSecondaryLight = Color(0xFFFFFFFF),
        secondaryContainerLight = Color(0xFFD7E3F7),
        onSecondaryContainerLight = Color(0xFF101C2B),
        tertiaryLight = Color(0xFF6B5778),
        onTertiaryLight = Color(0xFFFFFFFF),
        tertiaryContainerLight = Color(0xFFF2DAFF),
        onTertiaryContainerLight = Color(0xFF251431),
        primaryDark = Color(0xFF9ECAFF),
        onPrimaryDark = Color(0xFF003258),
        primaryContainerDark = Color(0xFF00497D),
        onPrimaryContainerDark = Color(0xFFD1E4FF),
        secondaryDark = Color(0xFFBBC7DB),
        onSecondaryDark = Color(0xFF253140),
        secondaryContainerDark = Color(0xFF3B4858),
        onSecondaryContainerDark = Color(0xFFD7E3F7),
        tertiaryDark = Color(0xFFD6BEE4),
        onTertiaryDark = Color(0xFF3B2948),
        tertiaryContainerDark = Color(0xFF523F5F),
        onTertiaryContainerDark = Color(0xFFF2DAFF)
    ),
    EMERALD(
        id = "emerald",
        displayName = "Emerald",
        swatchColor = Color(0xFF26A69A),
        primaryLight = Color(0xFF006C4C),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFF89F8C7),
        onPrimaryContainerLight = Color(0xFF002114),
        secondaryLight = Color(0xFF4D6357),
        onSecondaryLight = Color(0xFFFFFFFF),
        secondaryContainerLight = Color(0xFFCFE9D9),
        onSecondaryContainerLight = Color(0xFF092016),
        tertiaryLight = Color(0xFF3D6373),
        onTertiaryLight = Color(0xFFFFFFFF),
        tertiaryContainerLight = Color(0xFFC1E8FB),
        onTertiaryContainerLight = Color(0xFF001F29),
        primaryDark = Color(0xFF6CDBAC),
        onPrimaryDark = Color(0xFF003826),
        primaryContainerDark = Color(0xFF005138),
        onPrimaryContainerDark = Color(0xFF89F8C7),
        secondaryDark = Color(0xFFB3CCBE),
        onSecondaryDark = Color(0xFF1F352A),
        secondaryContainerDark = Color(0xFF354B40),
        onSecondaryContainerDark = Color(0xFFCFE9D9),
        tertiaryDark = Color(0xFFA5CCDF),
        onTertiaryDark = Color(0xFF073543),
        tertiaryContainerDark = Color(0xFF244C5B),
        onTertiaryContainerDark = Color(0xFFC1E8FB)
    ),
    ROSE(
        id = "rose",
        displayName = "Rose Pink",
        swatchColor = Color(0xFFEC407A),
        primaryLight = Color(0xFF984061),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFFFD9E2),
        onPrimaryContainerLight = Color(0xFF3E001D),
        secondaryLight = Color(0xFF74565F),
        onSecondaryLight = Color(0xFFFFFFFF),
        secondaryContainerLight = Color(0xFFFFD9E2),
        onSecondaryContainerLight = Color(0xFF2B151C),
        tertiaryLight = Color(0xFF7C5635),
        onTertiaryLight = Color(0xFFFFFFFF),
        tertiaryContainerLight = Color(0xFFFFDCC1),
        onTertiaryContainerLight = Color(0xFF2E1500),
        primaryDark = Color(0xFFFFB0C8),
        onPrimaryDark = Color(0xFF5E1133),
        primaryContainerDark = Color(0xFF7B2949),
        onPrimaryContainerDark = Color(0xFFFFD9E2),
        secondaryDark = Color(0xFFE3BDC6),
        onSecondaryDark = Color(0xFF422931),
        secondaryContainerDark = Color(0xFF5A3F47),
        onSecondaryContainerDark = Color(0xFFFFD9E2),
        tertiaryDark = Color(0xFFEFBD94),
        onTertiaryDark = Color(0xFF48290B),
        tertiaryContainerDark = Color(0xFF623F20),
        onTertiaryContainerDark = Color(0xFFFFDCC1)
    ),
    AMBER(
        id = "amber",
        displayName = "Sunset Gold",
        swatchColor = Color(0xFFFFB300),
        primaryLight = Color(0xFF825500),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFFFDDB3),
        onPrimaryContainerLight = Color(0xFF291800),
        secondaryLight = Color(0xFF6F5B40),
        onSecondaryLight = Color(0xFFFFFFFF),
        secondaryContainerLight = Color(0xFFFBDEBC),
        onSecondaryContainerLight = Color(0xFF271904),
        tertiaryLight = Color(0xFF51643F),
        onTertiaryLight = Color(0xFFFFFFFF),
        tertiaryContainerLight = Color(0xFFD4EABB),
        onTertiaryContainerLight = Color(0xFF102004),
        primaryDark = Color(0xFFFFB951),
        onPrimaryDark = Color(0xFF452B00),
        primaryContainerDark = Color(0xFF633F00),
        onPrimaryContainerDark = Color(0xFFFFDDB3),
        secondaryDark = Color(0xFFDDC2A1),
        onSecondaryDark = Color(0xFF3E2D16),
        secondaryContainerDark = Color(0xFF56442A),
        onSecondaryContainerDark = Color(0xFFFBDEBC),
        tertiaryDark = Color(0xFFB8CEA1),
        onTertiaryDark = Color(0xFF243515),
        tertiaryContainerDark = Color(0xFF3A4C2A),
        onTertiaryContainerDark = Color(0xFFD4EABB)
    ),
    CORAL(
        id = "coral",
        displayName = "Coral Red",
        swatchColor = Color(0xFFFF7043),
        primaryLight = Color(0xFF9C413D),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFFFDAD6),
        onPrimaryContainerLight = Color(0xFF410003),
        secondaryLight = Color(0xFF775654),
        onSecondaryLight = Color(0xFFFFFFFF),
        secondaryContainerLight = Color(0xFFFFDAD6),
        onSecondaryContainerLight = Color(0xFF2C1513),
        tertiaryLight = Color(0xFF725B2E),
        onTertiaryLight = Color(0xFFFFFFFF),
        tertiaryContainerLight = Color(0xFFFFDEA6),
        onTertiaryContainerLight = Color(0xFF261900),
        primaryDark = Color(0xFFFFB3AE),
        onPrimaryDark = Color(0xFF5F1414),
        primaryContainerDark = Color(0xFF7E2A28),
        onPrimaryContainerDark = Color(0xFFFFDAD6),
        secondaryDark = Color(0xFFE7BDB9),
        onSecondaryDark = Color(0xFF442927),
        secondaryContainerDark = Color(0xFF5D3F3D),
        onSecondaryContainerDark = Color(0xFFFFDAD6),
        tertiaryDark = Color(0xFFE1C38C),
        onTertiaryDark = Color(0xFF402D04),
        tertiaryContainerDark = Color(0xFF584419),
        onTertiaryContainerDark = Color(0xFFFFDEA6)
    ),
    TEAL(
        id = "teal",
        displayName = "Aqua Teal",
        swatchColor = Color(0xFF26C6DA),
        primaryLight = Color(0xFF006874),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFF97F0FF),
        onPrimaryContainerLight = Color(0xFF001F24),
        secondaryLight = Color(0xFF4A6267),
        onSecondaryLight = Color(0xFFFFFFFF),
        secondaryContainerLight = Color(0xFFCDE7EC),
        onSecondaryContainerLight = Color(0xFF051F23),
        tertiaryLight = Color(0xFF525E7D),
        onTertiaryLight = Color(0xFFFFFFFF),
        tertiaryContainerLight = Color(0xFFDAE2FF),
        onTertiaryContainerLight = Color(0xFF0E1B37),
        primaryDark = Color(0xFF4FD8EB),
        onPrimaryDark = Color(0xFF00363D),
        primaryContainerDark = Color(0xFF004F58),
        onPrimaryContainerDark = Color(0xFF97F0FF),
        secondaryDark = Color(0xFFB1CBD0),
        onSecondaryDark = Color(0xFF1C3438),
        secondaryContainerDark = Color(0xFF334B4F),
        onSecondaryContainerDark = Color(0xFFCDE7EC),
        tertiaryDark = Color(0xFFBAC6EA),
        onTertiaryDark = Color(0xFF24304D),
        tertiaryContainerDark = Color(0xFF3B4664),
        onTertiaryContainerDark = Color(0xFFDAE2FF)
    ),
    INDIGO(
        id = "indigo",
        displayName = "Indigo",
        swatchColor = Color(0xFF5C6BC0),
        primaryLight = Color(0xFF4355B9),
        onPrimaryLight = Color(0xFFFFFFFF),
        primaryContainerLight = Color(0xFFDEE0FF),
        onPrimaryContainerLight = Color(0xFF00105C),
        secondaryLight = Color(0xFF5B5D72),
        onSecondaryLight = Color(0xFFFFFFFF),
        secondaryContainerLight = Color(0xFFE0E1F9),
        onSecondaryContainerLight = Color(0xFF181A2C),
        tertiaryLight = Color(0xFF77536D),
        onTertiaryLight = Color(0xFFFFFFFF),
        tertiaryContainerLight = Color(0xFFFFD7F1),
        onTertiaryContainerLight = Color(0xFF2D1228),
        primaryDark = Color(0xFFBAC3FF),
        onPrimaryDark = Color(0xFF08218A),
        primaryContainerDark = Color(0xFF293CA0),
        onPrimaryContainerDark = Color(0xFFDEE0FF),
        secondaryDark = Color(0xFFC3C5DD),
        onSecondaryDark = Color(0xFF2D2F42),
        secondaryContainerDark = Color(0xFF434659),
        onSecondaryContainerDark = Color(0xFFE0E1F9),
        tertiaryDark = Color(0xFFE6BAD7),
        onTertiaryDark = Color(0xFF44263D),
        tertiaryContainerDark = Color(0xFF5D3C55),
        onTertiaryContainerDark = Color(0xFFFFD7F1)
    );

    companion object {
        fun fromId(id: String?): ThemeAccentColor {
            if (id.isNullOrBlank()) return VIOLET
            return entries.find { it.id.equals(id.trim(), ignoreCase = true) } ?: VIOLET
        }
    }
}

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
