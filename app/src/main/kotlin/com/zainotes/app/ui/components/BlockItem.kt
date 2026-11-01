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
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.NoteBlock
import com.zainotes.app.ui.theme.ExpressiveMotion

@Composable
fun BlockItem(
    block: NoteBlock,
    index: Int,
    totalBlocks: Int,
    onContentChange: (String) -> Unit,
    onTaskToggle: () -> Unit,
    onConvertType: (BlockType) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEnterPressed: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                        uncheckedColor = contentColor.copy(alpha = 0.6f),
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            BlockType.BULLET -> {
                Box(
                    modifier = Modifier
                        .size(36.dp),
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
            BlockType.HEADING -> {
                Text(
                    text = "H",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(start = 10.dp, end = 6.dp)
                )
            }
            BlockType.TEXT -> {
                // Gentle indent matching layout rhythm
                Spacer(modifier = Modifier.width(12.dp))
            }
        }

        // Editable Content
        val textStyle: TextStyle = when (block.type) {
            BlockType.HEADING -> MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            BlockType.TASK -> {
                if (block.isChecked) {
                    MaterialTheme.typography.bodyLarge.copy(
                        color = contentColor.copy(alpha = 0.45f),
                        textDecoration = TextDecoration.LineThrough
                    )
                } else {
                    MaterialTheme.typography.bodyLarge.copy(color = contentColor)
                }
            }
            BlockType.BULLET, BlockType.TEXT -> MaterialTheme.typography.bodyLarge.copy(
                color = contentColor,
                lineHeight = 22.sp
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            if (block.content.isEmpty()) {
                val placeholder = when (block.type) {
                    BlockType.TASK -> "Add a task item..."
                    BlockType.BULLET -> "Add a bullet point..."
                    BlockType.HEADING -> "Heading..."
                    BlockType.TEXT -> "Write note text..."
                }
                Text(
                    text = placeholder,
                    style = textStyle.copy(color = contentColor.copy(alpha = 0.4f))
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
                    .semantics { contentDescription = "${block.type.name.lowercase().replaceFirstChar { it.uppercase() }} block" }
            )
        }

        // Quick block actions / type switcher
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Block options",
                    tint = contentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Normal Text") },
                    onClick = {
                        onConvertType(BlockType.TEXT)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Task (Checkbox)") },
                    onClick = {
                        onConvertType(BlockType.TASK)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Bulletin Point") },
                    onClick = {
                        onConvertType(BlockType.BULLET)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Heading") },
                    onClick = {
                        onConvertType(BlockType.HEADING)
                        showMenu = false
                    }
                )
                if (index > 0) {
                    DropdownMenuItem(
                        text = { Text("Move Up") },
                        onClick = {
                            onMoveUp()
                            showMenu = false
                        }
                    )
                }
                if (index < totalBlocks - 1) {
                    DropdownMenuItem(
                        text = { Text("Move Down") },
                        onClick = {
                            onMoveDown()
                            showMenu = false
                        }
                    )
                }
                DropdownMenuItem(
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
