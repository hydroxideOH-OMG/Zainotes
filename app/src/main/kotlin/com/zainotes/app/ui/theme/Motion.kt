/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Material 3 Expressive Motion Tokens & Reusable Physics Modifiers.
 *
 * Implements M3 Expressive spatial and effects spring physics:
 * - Spatial Hero springs (`dampingRatio = 0.64f`, `stiffness = 300f`) for large screen/dock/bar choreography.
 * - Spatial Component springs (`dampingRatio = 0.62f`, `stiffness = 380f`) for cards, buttons, and shape morphing.
 * - Spatial Micro springs (`dampingRatio = 0.54f`, `stiffness = 460f`) for checkboxes, icons, and badges.
 * - Effects springs are critically damped (no bounce) for color, alpha, and elevation.
 */
object ExpressiveMotion {
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    val BouncySpring = spring<Float>(
        dampingRatio = 0.56f,
        stiffness = 380f
    )

    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 460f
    )

    fun <T> spatialHeroSpec() = spring<T>(
        dampingRatio = 0.64f,
        stiffness = 300f
    )

    fun <T> spatialFastSpec() = spring<T>(
        dampingRatio = 0.62f,
        stiffness = 380f
    )

    fun <T> spatialDefaultSpec() = spring<T>(
        dampingRatio = 0.64f,
        stiffness = 330f
    )

    fun <T> spatialMicroSpec() = spring<T>(
        dampingRatio = 0.54f,
        stiffness = 460f
    )

    fun <T> snappySpec() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 480f
    )

    fun <T> colorSpec() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 500f
    )

    fun <T> bouncySpec() = spring<T>(
        dampingRatio = 0.55f,
        stiffness = 340f
    )

    val EmphasizedSpatial = tween<Float>(
        durationMillis = 420,
        easing = FastOutSlowInEasing
    )

    const val DurationShort = 220
    const val DurationMedium = 320
    const val DurationLong = 460
}

/**
 * Gives any interactive surface Material 3 Expressive tactile press squash & bouncy release overshoot.
 */
fun Modifier.expressivePressable(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.94f,
    pressedRotationZ: Float = 0f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = ExpressiveMotion.spatialMicroSpec(),
        label = "expressivePressScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) pressedRotationZ else 0f,
        animationSpec = ExpressiveMotion.spatialMicroSpec(),
        label = "expressivePressRotation"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationZ = rotation
    }
}

/**
 * Self-contained overload of [expressivePressable] that observes pointer press events without
 * consuming them, so any Button, Surface, or clickable composable gets M3 Expressive press squash
 * and spring release bounce automatically.
 */
fun Modifier.expressivePressable(
    pressedScale: Float = 0.94f,
    pressedRotationZ: Float = 0f
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = ExpressiveMotion.spatialMicroSpec(),
        label = "expressivePointerPressScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) pressedRotationZ else 0f,
        animationSpec = ExpressiveMotion.spatialMicroSpec(),
        label = "expressivePointerPressRotation"
    )
    this
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    isPressed = event.changes.any { it.pressed }
                }
            }
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            rotationZ = rotation
        }
}

/**
 * Staggered Material 3 Expressive entrance animation (scale + vertical spring slide + fade).
 */
fun Modifier.staggeredEntrance(
    index: Int,
    baseDelayMs: Long = 28L,
    maxDelayMs: Long = 220L,
    initialOffsetY: Float = 42f,
    initialScale: Float = 0.91f
): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        val delayTime = (index * baseDelayMs).coerceAtMost(maxDelayMs)
        if (delayTime > 0) delay(delayTime)
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.64f,
                    stiffness = 340f
                )
            )
        }
    }
    this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        val currentScale = initialScale + (1f - initialScale) * p
        scaleX = currentScale
        scaleY = currentScale
        translationY = (1f - p) * initialOffsetY
    }
}

