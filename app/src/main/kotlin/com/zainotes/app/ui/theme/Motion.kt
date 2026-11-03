/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Material 3 Expressive Motion Tokens.
 *
 * Implements M3 Expressive spatial and effects spring physics:
 * - Spatial springs allow slight physical overshoot (low/medium bouncy damping) for
 *   movement, size expansion, thumb travel, and container morphing.
 * - Effects springs are critically damped (no bounce) for color, alpha, and elevation.
 */
object ExpressiveMotion {
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> spatialFastSpec() = spring<T>(
        dampingRatio = 0.68f,
        stiffness = 650f
    )

    fun <T> spatialDefaultSpec() = spring<T>(
        dampingRatio = 0.74f,
        stiffness = 420f
    )

    fun <T> snappySpec() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> bouncySpec() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val EmphasizedSpatial = tween<Float>(
        durationMillis = 400,
        easing = FastOutSlowInEasing
    )

    const val DurationShort = 180
    const val DurationMedium = 280
    const val DurationLong = 420
}
