/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    AMOLED
}

private fun buildCustomLightColorScheme(accent: ThemeAccentColor) = lightColorScheme(
    primary = accent.primaryLight,
    onPrimary = accent.onPrimaryLight,
    primaryContainer = accent.primaryContainerLight,
    onPrimaryContainer = accent.onPrimaryContainerLight,
    secondary = accent.secondaryLight,
    onSecondary = accent.onSecondaryLight,
    secondaryContainer = accent.secondaryContainerLight,
    onSecondaryContainer = accent.onSecondaryContainerLight,
    tertiary = accent.tertiaryLight,
    onTertiary = accent.onTertiaryLight,
    tertiaryContainer = accent.tertiaryContainerLight,
    onTertiaryContainer = accent.onTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

private fun buildCustomAmoledColorScheme(accent: ThemeAccentColor) = darkColorScheme(
    primary = accent.primaryDark,
    onPrimary = accent.onPrimaryDark,
    primaryContainer = accent.primaryContainerDark,
    onPrimaryContainer = accent.onPrimaryContainerDark,
    secondary = accent.secondaryDark,
    onSecondary = accent.onSecondaryDark,
    secondaryContainer = accent.secondaryContainerDark,
    onSecondaryContainer = accent.onSecondaryContainerDark,
    tertiary = accent.tertiaryDark,
    onTertiary = accent.onTertiaryDark,
    tertiaryContainer = accent.tertiaryContainerDark,
    onTertiaryContainer = accent.onTertiaryContainerDark,
    background = AmoledBackground,
    onBackground = OnBackgroundDark,
    surface = AmoledSurface,
    onSurface = OnSurfaceDark,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceDim = AmoledSurfaceDim,
    surfaceBright = AmoledSurfaceBright,
    surfaceContainerLowest = AmoledSurfaceContainerLowest,
    surfaceContainerLow = AmoledSurfaceContainerLow,
    surfaceContainer = AmoledSurfaceContainer,
    surfaceContainerHigh = AmoledSurfaceContainerHigh,
    surfaceContainerHighest = AmoledSurfaceContainerHighest
)

@Composable
fun ZainotesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    accentColor: ThemeAccentColor = ThemeAccentColor.VIOLET,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.AMOLED -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dynamic = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isDark) {
                dynamic.copy(
                    background = AmoledBackground,
                    surface = AmoledSurface,
                    surfaceVariant = AmoledSurfaceVariant,
                    surfaceDim = AmoledSurfaceDim,
                    surfaceBright = AmoledSurfaceBright,
                    surfaceContainerLowest = AmoledSurfaceContainerLowest,
                    surfaceContainerLow = AmoledSurfaceContainerLow,
                    surfaceContainer = AmoledSurfaceContainer,
                    surfaceContainerHigh = AmoledSurfaceContainerHigh,
                    surfaceContainerHighest = AmoledSurfaceContainerHighest
                )
            } else {
                dynamic
            }
        }
        isDark -> buildCustomAmoledColorScheme(accentColor)
        else -> buildCustomLightColorScheme(accentColor)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExpressiveTypography,
        shapes = ExpressiveShapes,
        content = content
    )
}
