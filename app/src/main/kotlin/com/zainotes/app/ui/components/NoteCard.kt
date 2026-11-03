/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.components

import android.text.format.DateUtils
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.Note
import com.zainotes.app.data.model.NoteBackgroundTheme
import com.zainotes.app.data.model.NoteBlock
import com.zainotes.app.data.model.getNumberedIndex
import com.zainotes.app.data.preferences.AppSettingsSnapshot
import com.zainotes.app.ui.theme.CardShapeExpressive
import com.zainotes.app.ui.theme.ExpressiveMotion
import com.zainotes.app.ui.theme.GoogleSansFlexFontFamily
import com.zainotes.app.ui.theme.NoteColor

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTaskToggle: (blockId: String, isChecked: Boolean) -> Unit,
    onCardSizeChange: (CardSize) -> Unit,
    onColorChange: (String) -> Unit,
    onBackgroundThemeChange: (String) -> Unit = {},
    onDuplicate: () -> Unit = {},
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    settings: AppSettingsSnapshot = AppSettingsSnapshot(),
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val noteColor = NoteColor.fromId(note.colorId)

    val cardBg = when {
        isDark && settings.amoledPureBlackCards && noteColor == NoteColor.DEFAULT -> Color.Black
        else -> noteColor.containerColor(isDark, MaterialTheme.colorScheme.surfaceContainerHigh)
    }
    val cardContent = noteColor.contentColor(isDark, MaterialTheme.colorScheme.onSurface)

    var showMenu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showSizePicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showFreeformCropDialog by remember { mutableStateOf(false) }
    val sizeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val themeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val maxPreviewItems = when (note.cardSize) {
        CardSize.COMPACT -> 2
        CardSize.STANDARD -> 4
        CardSize.WIDE -> 5
        CardSize.HERO -> 8
        CardSize.FREEFORM -> Int.MAX_VALUE
    }

    val themeEnum = NoteBackgroundTheme.fromId(note.backgroundTheme)

    Card(
        shape = CardShapeExpressive,
        colors = CardDefaults.cardColors(
            containerColor = cardBg,
            contentColor = cardContent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = ExpressiveMotion.spatialDefaultSpec())
            .clip(CardShapeExpressive)
            .then(
                if (isDark && settings.amoledPureBlackCards) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        shape = CardShapeExpressive
                    )
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (settings.showCardBackgrounds && themeEnum != NoteBackgroundTheme.NONE) {
                NoteBackgroundRenderer(
                    theme = themeEnum,
                    tint = cardContent,
                    modifier = Modifier.matchParentSize()
                )
            }

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
                                tint = cardContent,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 2.dp)
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options for ${note.title.ifBlank { "untitled note" }}",
                                    tint = cardContent.copy(alpha = 0.65f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text = { Text(if (note.isPinned) "Unpin note" else "Pin note") },
                                    onClick = {
                                        onTogglePin()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text = { Text("Color & Background Scene...") },
                                    onClick = {
                                        showMenu = false
                                        showThemePicker = true
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.PhotoSizeSelectLarge, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text = { Text("Resize card...") },
                                    onClick = {
                                        showMenu = false
                                        showSizePicker = true
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text = { Text("Interactive Free-form Crop...") },
                                    onClick = {
                                        showMenu = false
                                        showFreeformCropDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    text = { Text("Duplicate note") },
                                    onClick = {
                                        showMenu = false
                                        onDuplicate()
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                    text = { Text("Delete note", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        if (settings.confirmNoteDelete) {
                                            confirmDelete = true
                                        } else {
                                            onDelete()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Task completion progress bar
                if (settings.showTaskProgress && note.totalTasksCount > 0) {
                    val taskProgress by animateFloatAsState(
                        targetValue = note.completedTasksCount.toFloat() / note.totalTasksCount,
                        animationSpec = ExpressiveMotion.spatialFastSpec(),
                        label = "taskProgress"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LinearProgressIndicator(
                            progress = { taskProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = cardContent.copy(alpha = 0.16f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${note.completedTasksCount}/${note.totalTasksCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = cardContent.copy(alpha = 0.78f)
                        )
                    }
                }

                // Content preview items
                val previewBlocks = note.blocks.take(maxPreviewItems)
                if (previewBlocks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        previewBlocks.forEachIndexed { index, block ->
                            NoteBlockPreviewItem(
                                block = block,
                                cardContentColor = cardContent,
                                strikethroughCompleted = settings.strikethroughCompleted,
                                numberedIndex = note.blocks.getNumberedIndex(index),
                                onTaskToggle = { isChecked ->
                                    onTaskToggle(block.id, isChecked)
                                }
                            )
                        }

                        if (note.blocks.size > maxPreviewItems) {
                            Text(
                                text = "+${note.blocks.size - maxPreviewItems} more items",
                                style = MaterialTheme.typography.labelSmall,
                                color = cardContent.copy(alpha = 0.55f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Optional metadata footer (relative time + active theme badge)
                if (settings.showNoteMetadata) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val relTime = remember(note.updatedAt) {
                            DateUtils.getRelativeTimeSpanString(
                                note.updatedAt,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_RELATIVE
                            ).toString()
                        }
                        Text(
                            text = relTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = cardContent.copy(alpha = 0.52f)
                        )

                        if (themeEnum != NoteBackgroundTheme.NONE) {
                            Surface(
                                shape = CircleShape,
                                color = cardContent.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = themeEnum.icon,
                                        contentDescription = null,
                                        tint = cardContent.copy(alpha = 0.75f),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = themeEnum.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cardContent.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showThemePicker) {
        NoteThemePickerSheet(
            selectedColorId = note.colorId,
            onColorSelected = onColorChange,
            selectedThemeId = note.backgroundTheme,
            onThemeSelected = onBackgroundThemeChange,
            sheetState = themeSheetState,
            onDismissRequest = { showThemePicker = false }
        )
    }

    if (showFreeformCropDialog) {
        FreeformCropResizeDialog(
            note = note,
            onConfirmSize = { newSize ->
                onCardSizeChange(newSize)
                showFreeformCropDialog = false
            },
            onDismiss = { showFreeformCropDialog = false }
        )
    }

    if (showSizePicker) {
        CardSizePickerSheet(
            selectedSize = note.cardSize,
            onSizeSelected = { size ->
                onCardSizeChange(size)
                showSizePicker = false
            },
            sheetState = sizeSheetState,
            onDismissRequest = { showSizePicker = false }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this note?") },
            text = { Text("This will permanently delete ${note.title.ifBlank { "this note" }}.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NoteBlockPreviewItem(
    block: NoteBlock,
    cardContentColor: Color,
    strikethroughCompleted: Boolean = true,
    numberedIndex: Int = 1,
    onTaskToggle: (Boolean) -> Unit
) {
    val decorations = mutableListOf<TextDecoration>()
    if (block.isUnderline) decorations.add(TextDecoration.Underline)
    if (block.isStrikethrough || (block.type == BlockType.TASK && block.isChecked && strikethroughCompleted)) {
        decorations.add(TextDecoration.LineThrough)
    }
    val combinedDecoration = if (decorations.isEmpty()) TextDecoration.None else TextDecoration.combine(decorations)

    val itemStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = if (block.isBold || block.type == BlockType.HEADING) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (block.isItalic || block.type == BlockType.QUOTE) FontStyle.Italic else FontStyle.Normal,
        fontFamily = if (block.isMonospace) FontFamily.Monospace else GoogleSansFlexFontFamily,
        textDecoration = combinedDecoration
    )

    val rowModifier = if (block.isHighlighted) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    when (block.type) {
        BlockType.TASK -> {
            val taskTextColor by animateColorAsState(
                targetValue = if (block.isChecked) cardContentColor.copy(alpha = 0.55f) else cardContentColor,
                animationSpec = ExpressiveMotion.snappySpec(),
                label = "taskTextColor"
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = rowModifier
            ) {
                Checkbox(
                    checked = block.isChecked,
                    onCheckedChange = onTaskToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = cardContentColor.copy(alpha = 0.6f),
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = block.content.ifBlank { "Task" },
                    style = itemStyle.copy(color = taskTextColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        BlockType.BULLET -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = rowModifier
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(cardContentColor.copy(alpha = 0.75f), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = block.content,
                    style = itemStyle.copy(color = cardContentColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        BlockType.NUMBERED -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = rowModifier
            ) {
                Text(
                    text = "$numberedIndex.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = cardContentColor.copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.width(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = block.content,
                    style = itemStyle.copy(color = cardContentColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        BlockType.QUOTE -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = rowModifier
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = block.content,
                    style = itemStyle.copy(color = cardContentColor.copy(alpha = 0.9f)),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        BlockType.HEADING -> {
            if (block.content.isNotBlank()) {
                Text(
                    text = block.content,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontStyle = itemStyle.fontStyle,
                        textDecoration = combinedDecoration,
                        color = cardContentColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = rowModifier
                )
            }
        }
        BlockType.TEXT -> {
            if (block.content.isNotBlank()) {
                Text(
                    text = block.content,
                    style = itemStyle.copy(color = cardContentColor),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = rowModifier
                )
            }
        }
    }
}
