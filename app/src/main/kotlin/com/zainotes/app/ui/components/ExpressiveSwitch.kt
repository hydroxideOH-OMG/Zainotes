/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Material 3 Expressive Switch matching the official M3 Expressive specification:
 * - 52dp x 32dp pill track
 * - Checked state: Primary filled track, large 24dp onPrimary circular thumb with ✓ icon
 * - Unchecked state: SurfaceContainerHighest track with 2dp outline stroke, 24dp outline
 *   circular thumb with ✕ icon inside
 * - Interactive states: Press expands thumb to 28dp with a 40dp state-layer halo and bouncy
 *   spatial spring physics; Focus renders an outer expressive focus ring; Disabled dims alpha.
 */
@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val colorScheme = MaterialTheme.colorScheme

    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled && checked -> colorScheme.onSurface.copy(alpha = 0.12f)
            !enabled && !checked -> colorScheme.surfaceVariant.copy(alpha = 0.12f)
            checked -> colorScheme.primary
            else -> colorScheme.surfaceContainerHighest
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switchTrackColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled && !checked -> colorScheme.onSurface.copy(alpha = 0.12f)
            checked -> Color.Transparent
            else -> colorScheme.outline
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switchBorderColor"
    )

    val thumbColor by animateColorAsState(
        targetValue = when {
            !enabled && checked -> colorScheme.surface
            !enabled && !checked -> colorScheme.onSurface.copy(alpha = 0.38f)
            checked && isPressed -> colorScheme.primaryContainer
            checked -> colorScheme.onPrimary
            isPressed -> colorScheme.onSurfaceVariant
            else -> colorScheme.outline
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switchThumbColor"
    )

    val iconTint by animateColorAsState(
        targetValue = when {
            !enabled && checked -> colorScheme.onSurface.copy(alpha = 0.38f)
            !enabled && !checked -> colorScheme.surfaceContainerHighest.copy(alpha = 0.38f)
            checked -> colorScheme.onPrimaryContainer
            else -> colorScheme.surfaceContainerHighest
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switchIconTint"
    )

    // Spatial bouncy spring for thumb size and horizontal travel
    val thumbSize by animateDpAsState(
        targetValue = if (isPressed && enabled) 28.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = 700f
        ),
        label = "switchThumbSize"
    )

    // Track is 52dp wide, 32dp tall.
    // Center of thumb travels from 16dp (left) to 36dp (right).
    val thumbCenterX by animateDpAsState(
        targetValue = if (checked) 36.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 580f
        ),
        label = "switchThumbCenter"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "switchIconScale"
    )

    val haloAlpha by animateFloatAsState(
        targetValue = when {
            !enabled -> 0f
            isPressed -> 0.22f
            isFocused -> 0.16f
            isHovered -> 0.12f
            else -> 0f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switchHaloAlpha"
    )

    val haloColor = if (checked) colorScheme.primary else colorScheme.onSurface

    val clickableModifier = if (onCheckedChange != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Switch,
            onClick = { onCheckedChange(!checked) }
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .padding(2.dp)
            .then(
                if (isFocused && enabled) {
                    Modifier
                        .border(2.dp, colorScheme.secondary, CircleShape)
                        .padding(2.dp)
                } else {
                    Modifier
                }
            )
            .size(width = 52.dp, height = 32.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Pill
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 32.dp)
                .clip(CircleShape)
                .background(trackColor)
                .then(
                    if (!checked) {
                        Modifier.border(
                            width = 2.dp,
                            color = borderColor,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
        )

        // State-layer Halo (40dp circle centered on the thumb)
        if (haloAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .offset {
                        val xPx = (thumbCenterX - 20.dp).toPx().roundToInt()
                        IntOffset(xPx, 0)
                    }
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(haloColor.copy(alpha = haloAlpha))
            )
        }

        // Thumb with ✓ (checked) or ✕ (unchecked) icon
        Box(
            modifier = Modifier
                .offset {
                    val xPx = (thumbCenterX - (thumbSize / 2)).toPx().roundToInt()
                    IntOffset(xPx, 0)
                }
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(if (checked) 16.dp else 14.dp)
                    .scale(iconScale)
                    .alpha(if (enabled) 1f else 0.7f)
            )
        }
    }
}
