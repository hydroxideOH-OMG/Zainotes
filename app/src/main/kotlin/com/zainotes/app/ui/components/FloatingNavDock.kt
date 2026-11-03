/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zainotes.app.ui.theme.ExpressiveMotion

enum class DockDestination(
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val contentDescription: String
) {
    HOME(
        label = "Notes",
        activeIcon = Icons.Filled.Home,
        inactiveIcon = Icons.Outlined.Home,
        contentDescription = "Navigate to Home notes"
    ),
    AI_CHAT(
        label = "Smart AI",
        activeIcon = Icons.Filled.AutoAwesome,
        inactiveIcon = Icons.Outlined.AutoAwesome,
        contentDescription = "Navigate to Smart AI assistant"
    ),
    SETTINGS(
        label = "Settings",
        activeIcon = Icons.Filled.Settings,
        inactiveIcon = Icons.Outlined.Settings,
        contentDescription = "Navigate to Settings"
    )
}

/**
 * Floating Bottom Navigation Dock for Zainotes.
 *
 * Uses Material 3 Expressive spatial spring physics for zero-lag, silky-smooth tab expansion
 * and tactile press scaling without expensive multi-pass Crossfade/animateContentSize thrashing.
 */
@Composable
fun FloatingNavDock(
    currentDestination: DockDestination,
    onDestinationSelected: (DockDestination) -> Unit,
    onAddNote: () -> Unit,
    isCollapsed: Boolean = false,
    modifier: Modifier = Modifier
) {
    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabPressed by fabInteractionSource.collectIsPressedAsState()

    val fabScale by animateFloatAsState(
        targetValue = when {
            isFabPressed -> 0.88f
            isCollapsed -> 1.05f
            else -> 1f
        },
        animationSpec = ExpressiveMotion.spatialMicroSpec(),
        label = "fabPressScale"
    )

    val fabIconRotation by animateFloatAsState(
        targetValue = when {
            isFabPressed -> 45f
            isCollapsed -> 90f
            else -> 0f
        },
        animationSpec = ExpressiveMotion.spatialHeroSpec(),
        label = "fabIconRotation"
    )

    val fabCornerPercent by animateFloatAsState(
        targetValue = when {
            isFabPressed -> 32f
            isCollapsed -> 50f
            else -> 36f
        },
        animationSpec = ExpressiveMotion.spatialFastSpec(),
        label = "fabCornerMorph"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 1. Primary Navigation Bar (Slides completely off-screen when scrolling down,
            //    allowing the + button to smoothly glide to the exact horizontal center)
            AnimatedVisibility(
                visible = !isCollapsed,
                enter = expandHorizontally(
                    animationSpec = ExpressiveMotion.spatialHeroSpec(),
                    expandFrom = Alignment.CenterHorizontally
                ) + slideInVertically(
                    initialOffsetY = { it * 2 },
                    animationSpec = ExpressiveMotion.spatialHeroSpec()
                ) + scaleIn(
                    initialScale = 0.82f,
                    animationSpec = ExpressiveMotion.spatialHeroSpec()
                ) + fadeIn(tween(180)),
                exit = shrinkHorizontally(
                    animationSpec = ExpressiveMotion.spatialHeroSpec(),
                    shrinkTowards = Alignment.CenterHorizontally
                ) + slideOutVertically(
                    targetOffsetY = { it * 2 },
                    animationSpec = ExpressiveMotion.spatialHeroSpec()
                ) + scaleOut(
                    targetScale = 0.82f,
                    animationSpec = ExpressiveMotion.spatialHeroSpec()
                ) + fadeOut(tween(150))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .height(62.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = CircleShape,
                                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.22f),
                                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
                            ),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 3.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .selectableGroup()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DockDestination.entries.forEach { destination ->
                                val isSelected = currentDestination == destination

                                FloatingNavDockItem(
                                    destination = destination,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (!isSelected) {
                                            onDestinationSelected(destination)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }
            }

            // 2. Standalone + Note Action Button (Glides to horizontal center when nav pill collapses)
            FloatingActionButton(
                onClick = onAddNote,
                interactionSource = fabInteractionSource,
                shape = RoundedCornerShape(percent = fabCornerPercent.toInt().coerceIn(24, 50)),
                modifier = Modifier
                    .size(62.dp)
                    .scale(fabScale)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Create new note"
                    },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new note",
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer { rotationZ = fabIconRotation }
                )
            }
        }
    }
}

@Composable
private fun FloatingNavDockItem(
    destination: DockDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pillColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "dockPillColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "dockContentColor"
    )

    val itemScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.90f
            isSelected -> 1.0f
            else -> 0.96f
        },
        animationSpec = ExpressiveMotion.spatialMicroSpec(),
        label = "dockItemScale"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = ExpressiveMotion.spatialMicroSpec(),
        label = "dockIconScale"
    )

    val iconRotation by animateFloatAsState(
        targetValue = when {
            isPressed -> -10f
            isSelected && destination == DockDestination.AI_CHAT -> 12f
            isSelected && destination == DockDestination.SETTINGS -> 45f
            else -> 0f
        },
        animationSpec = ExpressiveMotion.spatialMicroSpec(),
        label = "dockIconRotation"
    )

    val cornerPercent by animateFloatAsState(
        targetValue = if (isPressed) 32f else 50f,
        animationSpec = ExpressiveMotion.spatialFastSpec(),
        label = "dockCornerPercent"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(percent = cornerPercent.toInt().coerceIn(25, 50)),
        color = pillColor,
        contentColor = contentColor,
        modifier = modifier
            .height(50.dp)
            .scale(itemScale)
            .semantics {
                selected = isSelected
                role = Role.Tab
                contentDescription = destination.contentDescription
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = if (isSelected) 16.dp else 14.dp,
                end = if (isSelected) 18.dp else 14.dp
            )
        ) {
            Icon(
                imageVector = if (isSelected) destination.activeIcon else destination.inactiveIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        rotationZ = iconRotation
                    }
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(
                    animationSpec = ExpressiveMotion.spatialFastSpec(),
                    expandFrom = Alignment.Start
                ) + fadeIn(tween(160)),
                exit = shrinkHorizontally(
                    animationSpec = ExpressiveMotion.snappySpec(),
                    shrinkTowards = Alignment.Start
                ) + fadeOut(tween(110))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

