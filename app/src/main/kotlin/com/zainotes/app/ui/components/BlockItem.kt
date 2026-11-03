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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.NoteBlock
import com.zainotes.app.ui.theme.GoogleSansFlexFontFamily

@Composable
fun BlockItem(
    block: NoteBlock,
    index: Int,
    totalBlocks: Int,
    isFocused: Boolean = false,
    strikethroughCompleted: Boolean = true,
    onFocused: () -> Unit = {},
    onContentChange: (String) -> Unit,
    onTableChange: (List<List<String>>) -> Unit = {},
    onTaskToggle: () -> Unit,
    onConvertType: (BlockType) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEnterPressed: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier,
    numberedIndex: Int = 1
) {
    var showMenu by remember { mutableStateOf(false) }

    val highlightBg by animateColorAsState(
        targetValue = when {
            block.isHighlighted -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
            block.type == BlockType.CODE -> contentColor.copy(alpha = 0.08f)
            isFocused -> contentColor.copy(alpha = 0.05f)
            else -> Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "blockHighlightBg"
    )

    val checkboxScale by animateFloatAsState(
        targetValue = if (block.isChecked) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkboxScale"
    )

    when (block.type) {
        BlockType.IMAGE -> {
            NoteImageBlockCard(
                block = block,
                index = index,
                totalBlocks = totalBlocks,
                isFocused = isFocused,
                contentColor = contentColor,
                onFocused = onFocused,
                onCaptionChange = onContentChange,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onDelete = onDelete,
                modifier = modifier
            )
            return
        }
        BlockType.AUDIO -> {
            NoteAudioBlockCard(
                block = block,
                index = index,
                totalBlocks = totalBlocks,
                isFocused = isFocused,
                contentColor = contentColor,
                onFocused = onFocused,
                onCaptionChange = onContentChange,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onDelete = onDelete,
                modifier = modifier
            )
            return
        }
        BlockType.VIDEO -> {
            NoteVideoBlockCard(
                block = block,
                index = index,
                totalBlocks = totalBlocks,
                isFocused = isFocused,
                contentColor = contentColor,
                onFocused = onFocused,
                onCaptionChange = onContentChange,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onDelete = onDelete,
                modifier = modifier
            )
            return
        }
        else -> {}
    }

    if (block.type == BlockType.TABLE) {
        val tableRows = block.resolvedTable()
        val rowCount = tableRows.size.coerceAtLeast(1)
        val colCount = tableRows.firstOrNull()?.size?.coerceAtLeast(1) ?: 3

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(contentColor.copy(alpha = 0.05f))
                .border(
                    width = 1.dp,
                    color = if (isFocused) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(10.dp)
        ) {
            // Table Header Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Table ($rowCount×$colCount)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.85f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TableControlChip(
                        label = "+ Row",
                        contentColor = contentColor,
                        onClick = {
                            onFocused()
                            val updated = tableRows.toMutableList()
                            updated.add(List(colCount) { "" })
                            onTableChange(updated)
                        }
                    )
                    TableControlChip(
                        label = "+ Col",
                        contentColor = contentColor,
                        onClick = {
                            onFocused()
                            val updated = tableRows.map { it + "" }
                            onTableChange(updated)
                        }
                    )
                    if (rowCount > 1) {
                        TableControlChip(
                            label = "- Row",
                            contentColor = contentColor,
                            onClick = {
                                onFocused()
                                onTableChange(tableRows.dropLast(1))
                            }
                        )
                    }
                    if (colCount > 1) {
                        TableControlChip(
                            label = "- Col",
                            contentColor = contentColor,
                            onClick = {
                                onFocused()
                                onTableChange(tableRows.map { it.dropLast(1) })
                            }
                        )
                    }

                    Box {
                        IconButton(
                            onClick = {
                                onFocused()
                                showMenu = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Table block options",
                                tint = contentColor.copy(alpha = 0.65f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        BlockOptionsDropdownMenu(
                            expanded = showMenu,
                            index = index,
                            totalBlocks = totalBlocks,
                            onDismiss = { showMenu = false },
                            onConvertType = onConvertType,
                            onMoveUp = onMoveUp,
                            onMoveDown = onMoveDown,
                            onDelete = onDelete
                        )
                    }
                }
            }

            // Table Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, contentColor.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
            ) {
                tableRows.forEachIndexed { rIdx, rowCells ->
                    val isHeader = rIdx == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isHeader) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                else Color.Transparent
                            ),
                        verticalAlignment = Alignment.Top
                    ) {
                        rowCells.forEachIndexed { cIdx, cellValue ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(0.5.dp, contentColor.copy(alpha = 0.16f))
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                if (cellValue.isEmpty()) {
                                    Text(
                                        text = if (isHeader) "Header ${cIdx + 1}" else "Cell",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = contentColor.copy(alpha = 0.38f),
                                            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                                BasicTextField(
                                    value = cellValue,
                                    onValueChange = { newVal ->
                                        val nextTable = tableRows.mapIndexed { ri, r ->
                                            if (ri == rIdx) {
                                                r.mapIndexed { ci, c -> if (ci == cIdx) newVal else c }
                                            } else r
                                        }
                                        onTableChange(nextTable)
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 13.5.sp,
                                        lineHeight = 18.sp,
                                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = GoogleSansFlexFontFamily,
                                        color = contentColor
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { if (it.isFocused) onFocused() }
                                )
                            }
                        }
                    }
                    if (rIdx < tableRows.size - 1) {
                        HorizontalDivider(color = contentColor.copy(alpha = 0.15f), thickness = 0.5.dp)
                    }
                }
            }
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(highlightBg)
            .then(
                if (isFocused || block.type == BlockType.CODE) {
                    Modifier.border(
                        width = 1.dp,
                        color = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        else contentColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Leading indicator aligned to the top of the first line so wrapped lines flow top-to-bottom cleanly
        when (block.type) {
            BlockType.TASK -> {
                Checkbox(
                    checked = block.isChecked,
                    onCheckedChange = { onTaskToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = contentColor.copy(alpha = 0.65f),
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .scale(checkboxScale)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            BlockType.BULLET -> {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(contentColor.copy(alpha = 0.85f), shape = CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            BlockType.NUMBERED -> {
                Text(
                    text = "$numberedIndex.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 8.dp)
                )
            }
            BlockType.HEADING -> {
                Icon(
                    imageVector = Icons.Default.Title,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 6.dp, top = 10.dp)
                        .size(20.dp)
                )
            }
            BlockType.QUOTE -> {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 8.dp, top = 8.dp)
                        .width(4.dp)
                        .height(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            BlockType.CODE -> {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 6.dp, top = 9.dp)
                        .size(18.dp)
                )
            }
            BlockType.TEXT, BlockType.TABLE, BlockType.IMAGE, BlockType.AUDIO, BlockType.VIDEO -> {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }

        // Compute combined typography from block type + rich text formatting flags
        val baseFontSize = when (block.type) {
            BlockType.HEADING -> 21f
            BlockType.QUOTE -> 16.5f
            BlockType.CODE -> 14.5f
            else -> 16f
        }
        val scaledFontSize = when (block.textScale) {
            1 -> (baseFontSize + 3.5f).sp
            -1 -> (baseFontSize - 2.5f).sp
            else -> baseFontSize.sp
        }

        val decorations = mutableListOf<TextDecoration>()
        if (block.isUnderline) decorations.add(TextDecoration.Underline)
        if (block.isStrikethrough || (block.type == BlockType.TASK && block.isChecked && strikethroughCompleted)) {
            decorations.add(TextDecoration.LineThrough)
        }
        val combinedDecoration = if (decorations.isEmpty()) TextDecoration.None else TextDecoration.combine(decorations)

        val resolvedTextAlign = when (block.textAlign) {
            1 -> TextAlign.Center
            2 -> TextAlign.End
            else -> TextAlign.Start
        }

        val textStyle = TextStyle(
            fontSize = scaledFontSize,
            lineHeight = (scaledFontSize.value * 1.44f).sp,
            fontWeight = when {
                block.type == BlockType.HEADING || block.isBold -> FontWeight.Bold
                else -> FontWeight.Normal
            },
            fontStyle = if (block.isItalic || block.type == BlockType.QUOTE) FontStyle.Italic else FontStyle.Normal,
            fontFamily = if (block.isMonospace || block.type == BlockType.CODE) FontFamily.Monospace else GoogleSansFlexFontFamily,
            textDecoration = combinedDecoration,
            textAlign = resolvedTextAlign,
            color = if (block.type == BlockType.TASK && block.isChecked) {
                contentColor.copy(alpha = 0.50f)
            } else {
                contentColor
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            if (block.content.isEmpty()) {
                val placeholder = when (block.type) {
                    BlockType.TASK -> "Add a task item..."
                    BlockType.BULLET -> "Add a bullet point..."
                    BlockType.NUMBERED -> "Add a numbered item..."
                    BlockType.HEADING -> "Section heading..."
                    BlockType.QUOTE -> "Write a quote or callout..."
                    BlockType.CODE -> "Write code snippet..."
                    BlockType.TEXT, BlockType.TABLE, BlockType.IMAGE, BlockType.AUDIO, BlockType.VIDEO -> "Write note text..."
                }
                Text(
                    text = placeholder,
                    style = textStyle.copy(color = contentColor.copy(alpha = 0.42f)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BasicTextField(
                value = block.content,
                onValueChange = { incoming ->
                    // For list/heading items, pressing Enter advances to a new block automatically
                    if (
                        (block.type == BlockType.TASK ||
                            block.type == BlockType.BULLET ||
                            block.type == BlockType.NUMBERED ||
                            block.type == BlockType.HEADING) &&
                        incoming.endsWith("\n") &&
                        !block.content.endsWith("\n")
                    ) {
                        onContentChange(incoming.removeSuffix("\n"))
                        onEnterPressed()
                    } else {
                        onContentChange(incoming)
                    }
                },
                textStyle = textStyle,
                singleLine = false,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                keyboardActions = KeyboardActions(onDone = { onEnterPressed() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onFocused()
                        }
                    }
                    .semantics {
                        contentDescription = "${block.type.name.lowercase().replaceFirstChar { it.uppercase() }} block"
                    }
            )
        }

        // Quick block actions / type switcher
        Box {
            IconButton(
                onClick = {
                    onFocused()
                    showMenu = true
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Block options",
                    tint = contentColor.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
            }

            BlockOptionsDropdownMenu(
                expanded = showMenu,
                index = index,
                totalBlocks = totalBlocks,
                onDismiss = { showMenu = false },
                onConvertType = onConvertType,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun TableControlChip(
    label: String,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = contentColor.copy(alpha = 0.12f),
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BlockOptionsDropdownMenu(
    expanded: Boolean,
    index: Int,
    totalBlocks: Int,
    onDismiss: () -> Unit,
    onConvertType: (BlockType) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Normal Text") },
            onClick = {
                onConvertType(BlockType.TEXT)
                onDismiss()
            }
        )
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Task (Checkbox)") },
            onClick = {
                onConvertType(BlockType.TASK)
                onDismiss()
            }
        )
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Bullet Point") },
            onClick = {
                onConvertType(BlockType.BULLET)
                onDismiss()
            }
        )
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Numbered List") },
            onClick = {
                onConvertType(BlockType.NUMBERED)
                onDismiss()
            }
        )
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Heading") },
            onClick = {
                onConvertType(BlockType.HEADING)
                onDismiss()
            }
        )
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Quote Callout") },
            onClick = {
                onConvertType(BlockType.QUOTE)
                onDismiss()
            }
        )
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Table Grid") },
            onClick = {
                onConvertType(BlockType.TABLE)
                onDismiss()
            }
        )
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(18.dp)) },
            text = { Text("Code Block") },
            onClick = {
                onConvertType(BlockType.CODE)
                onDismiss()
            }
        )
        if (index > 0) {
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = { Text("Move Up") },
                onClick = {
                    onMoveUp()
                    onDismiss()
                }
            )
        }
        if (index < totalBlocks - 1) {
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = { Text("Move Down") },
                onClick = {
                    onMoveDown()
                    onDismiss()
                }
            )
        }
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
            text = { Text("Delete Block", color = MaterialTheme.colorScheme.error) },
            onClick = {
                onDelete()
                onDismiss()
            }
        )
    }
}
