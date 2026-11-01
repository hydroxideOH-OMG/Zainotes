/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.BlockType

/**
 * Editor Floating Bottom Dock.
 *
 * Implements the requested bottom navigation bar UI in the note editor:
 * - At the left: A circular button with a tick button (Done / Save & Finish note).
 * - At the right: A long floating pill containing buttons for text, bulletings,
 *   checkpoints, 1 2 3 list, heading, theme/palette, and card size.
 */
@Composable
fun EditorFloatingDock(
    onDone: () -> Unit,
    onAddBlock: (BlockType) -> Unit,
    onOpenThemePicker: () -> Unit,
    onOpenSizePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. At the left: Circle with tick button (Save & Done)
            FloatingActionButton(
                onClick = onDone,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Done editing note"
                    },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done editing note",
                    modifier = Modifier.size(28.dp)
                )
            }

            // 8px - 12px separation gap
            Spacer(modifier = Modifier.width(10.dp))

            // 2. At the right: Long floating pill with action buttons
            Surface(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(56.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.15f),
                        spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f)
                    ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 3.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Text
                    EditorDockItem(
                        icon = Icons.Default.TextFields,
                        label = "Text",
                        onClick = { onAddBlock(BlockType.TEXT) },
                        contentDescription = "Add text paragraph"
                    )

                    // Bulletings
                    EditorDockItem(
                        icon = Icons.Default.FormatListBulleted,
                        label = "Bullets",
                        onClick = { onAddBlock(BlockType.BULLET) },
                        contentDescription = "Add bullet list item"
                    )

                    // Checkpoints
                    EditorDockItem(
                        icon = Icons.Default.CheckBox,
                        label = "Checklist",
                        onClick = { onAddBlock(BlockType.TASK) },
                        contentDescription = "Add checklist task item"
                    )

                    // 1 2 3 list
                    EditorDockItem(
                        icon = Icons.Default.FormatListNumbered,
                        label = "1 2 3",
                        onClick = { onAddBlock(BlockType.NUMBERED) },
                        contentDescription = "Add numbered 1 2 3 list item"
                    )

                    // Heading
                    EditorDockItem(
                        icon = Icons.Default.Title,
                        label = "Heading",
                        onClick = { onAddBlock(BlockType.HEADING) },
                        contentDescription = "Add heading block"
                    )

                    // Theme & Background images / vibrant colors
                    EditorDockItem(
                        icon = Icons.Default.Palette,
                        label = "Theme",
                        onClick = onOpenThemePicker,
                        contentDescription = "Change background color and image theme",
                        highlight = true
                    )

                    // Card Size
                    EditorDockItem(
                        icon = Icons.Default.AspectRatio,
                        label = "Size",
                        onClick = onOpenSizePicker,
                        contentDescription = "Change card size layout"
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorDockItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    contentDescription: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (highlight) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else Color.Transparent,
        contentColor = if (highlight) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .height(44.dp)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
