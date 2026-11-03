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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(highlightBg)
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading indicator based on block type
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
                        .size(44.dp)
                        .scale(checkboxScale)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            BlockType.BULLET -> {
                Box(
                    modifier = Modifier.size(36.dp),
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
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                )
            }
            BlockType.HEADING -> {
                Icon(
                    imageVector = Icons.Default.Title,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 6.dp)
                        .size(20.dp)
                )
            }
            BlockType.QUOTE -> {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 8.dp)
                        .width(4.dp)
                        .height(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            BlockType.TEXT -> {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }

        // Compute combined typography from block type + rich text formatting flags
        val baseFontSize = when (block.type) {
            BlockType.HEADING -> 21f
            BlockType.QUOTE -> 16.5f
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
            lineHeight = (scaledFontSize.value * 1.42f).sp,
            fontWeight = when {
                block.type == BlockType.HEADING || block.isBold -> FontWeight.Bold
                else -> FontWeight.Normal
            },
            fontStyle = if (block.isItalic || block.type == BlockType.QUOTE) FontStyle.Italic else FontStyle.Normal,
            fontFamily = if (block.isMonospace) FontFamily.Monospace else GoogleSansFlexFontFamily,
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
                .padding(vertical = 6.dp)
        ) {
            if (block.content.isEmpty()) {
                val placeholder = when (block.type) {
                    BlockType.TASK -> "Add a task item..."
                    BlockType.BULLET -> "Add a bullet point..."
                    BlockType.NUMBERED -> "Add a numbered item..."
                    BlockType.HEADING -> "Section heading..."
                    BlockType.QUOTE -> "Write a quote or callout..."
                    BlockType.TEXT -> "Write note text..."
                }
                Text(
                    text = placeholder,
                    style = textStyle.copy(color = contentColor.copy(alpha = 0.42f)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BasicTextField(
                value = block.content,
                onValueChange = onContentChange,
                textStyle = textStyle,
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
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Block options",
                    tint = contentColor.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Normal Text") },
                    onClick = {
                        onConvertType(BlockType.TEXT)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Task (Checkbox)") },
                    onClick = {
                        onConvertType(BlockType.TASK)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Bullet Point") },
                    onClick = {
                        onConvertType(BlockType.BULLET)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Numbered List") },
                    onClick = {
                        onConvertType(BlockType.NUMBERED)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Heading") },
                    onClick = {
                        onConvertType(BlockType.HEADING)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Quote Callout") },
                    onClick = {
                        onConvertType(BlockType.QUOTE)
                        showMenu = false
                    }
                )
                if (index > 0) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Move Up") },
                        onClick = {
                            onMoveUp()
                            showMenu = false
                        }
                    )
                }
                if (index < totalBlocks - 1) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Move Down") },
                        onClick = {
                            onMoveDown()
                            showMenu = false
                        }
                    )
                }
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                    text = { Text("Delete Block", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        onDelete()
                        showMenu = false
                    }
                )
            }
        }
    }
}
