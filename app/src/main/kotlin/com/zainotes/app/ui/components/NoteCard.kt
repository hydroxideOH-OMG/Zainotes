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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.Note
import com.zainotes.app.data.model.NoteBlock
import com.zainotes.app.ui.theme.CardShapeExpressive
import com.zainotes.app.ui.theme.ExpressiveMotion
import com.zainotes.app.ui.theme.NoteColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTaskToggle: (blockId: String, isChecked: Boolean) -> Unit,
    onCardSizeChange: (CardSize) -> Unit,
    onColorChange: (String) -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val noteColor = NoteColor.fromId(note.colorId)
    val cardBg = noteColor.containerColor(isDark, MaterialTheme.colorScheme.surfaceContainerHigh)
    val cardContent = noteColor.contentColor(isDark, MaterialTheme.colorScheme.onSurface)

    var showMenu by remember { mutableStateOf(false) }

    val maxPreviewItems = when (note.cardSize) {
        CardSize.COMPACT -> 2
        CardSize.STANDARD -> 4
        CardSize.WIDE -> 5
        CardSize.HERO -> 8
    }

    Card(
        shape = CardShapeExpressive,
        colors = CardDefaults.cardColors(
            containerColor = cardBg,
            contentColor = cardContent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShapeExpressive)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title and Pin Icon / Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = when (note.cardSize) {
                            CardSize.HERO -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            else -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = cardContent,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = cardContent.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (note.isPinned) "Unpin Note" else "Pin Note") },
                                onClick = {
                                    onTogglePin()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Standard Size (1x1)") },
                                onClick = {
                                    onCardSizeChange(CardSize.STANDARD)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Wide Banner (2x1)") },
                                onClick = {
                                    onCardSizeChange(CardSize.WIDE)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Compact Tile") },
                                onClick = {
                                    onCardSizeChange(CardSize.COMPACT)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hero Spotlight") },
                                onClick = {
                                    onCardSizeChange(CardSize.HERO)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Note", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Task completion progress bar
            if (note.totalTasksCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LinearProgressIndicator(
                        progress = { note.completedTasksCount.toFloat() / note.totalTasksCount },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = cardContent.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${note.completedTasksCount}/${note.totalTasksCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = cardContent.copy(alpha = 0.7f)
                    )
                }
            }

            // Content preview items
            val previewBlocks = note.blocks.take(maxPreviewItems)
            if (previewBlocks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    previewBlocks.forEach { block ->
                        NoteBlockPreviewItem(
                            block = block,
                            cardContentColor = cardContent,
                            onTaskToggle = { isChecked ->
                                onTaskToggle(block.id, isChecked)
                            }
                        )
                    }

                    if (note.blocks.size > maxPreviewItems) {
                        Text(
                            text = "+${note.blocks.size - maxPreviewItems} more items",
                            style = MaterialTheme.typography.labelSmall,
                            color = cardContent.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteBlockPreviewItem(
    block: NoteBlock,
    cardContentColor: Color,
    onTaskToggle: (Boolean) -> Unit
) {
    when (block.type) {
        BlockType.TASK -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = block.isChecked,
                    onCheckedChange = onTaskToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = cardContentColor.copy(alpha = 0.6f),
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = block.content.ifBlank { "Task" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (block.isChecked) cardContentColor.copy(alpha = 0.45f) else cardContentColor,
                        textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        BlockType.BULLET -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(cardContentColor.copy(alpha = 0.7f), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = block.content,
                    style = MaterialTheme.typography.bodyMedium.copy(color = cardContentColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        BlockType.HEADING -> {
            Text(
                text = block.content,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = cardContentColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        BlockType.TEXT -> {
            if (block.content.isNotBlank()) {
                Text(
                    text = block.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = cardContentColor.copy(alpha = 0.85f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
