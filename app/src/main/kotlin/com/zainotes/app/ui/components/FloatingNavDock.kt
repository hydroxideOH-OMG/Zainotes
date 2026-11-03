/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    modifier: Modifier = Modifier
) {
    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
    val fabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabPressScale"
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
            // 1. Primary Navigation Bar (M3 Expressive Floating Navigation Pill)
            Surface(
                modifier = Modifier
                    .height(60.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                        spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f)
                    ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
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

            // 2. Standalone Action Button (Circular M3 Expressive FAB matching dock height)
            FloatingActionButton(
                onClick = onAddNote,
                interactionSource = fabInteractionSource,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .scale(fabScale)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Create new note"
                    },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new note",
                    modifier = Modifier.size(26.dp)
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
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dockPillColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dockContentColor"
    )

    val itemScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.93f
            isSelected -> 1.0f
            else -> 0.98f
        },
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 620f
        ),
        label = "dockItemScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = pillColor,
        contentColor = contentColor,
        modifier = modifier
            .height(48.dp)
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
                modifier = Modifier.size(22.dp)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = 540f
                    ),
                    expandFrom = Alignment.Start
                ) + fadeIn(tween(140)),
                exit = shrinkHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = 600f
                    ),
                    shrinkTowards = Alignment.Start
                ) + fadeOut(tween(100))
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
