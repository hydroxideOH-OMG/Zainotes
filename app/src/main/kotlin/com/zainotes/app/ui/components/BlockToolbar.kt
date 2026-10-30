/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.ui.theme.ChipShapeExpressive

@Composable
fun BlockToolbar(
    onAddBlock: (BlockType) -> Unit,
    onOpenColorPicker: () -> Unit,
    onOpenSizePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add Task
            AssistChip(
                onClick = { onAddBlock(BlockType.TASK) },
                label = { Text("Task") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CheckBox,
                        contentDescription = "Task",
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = ChipShapeExpressive,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )

            // Add Bullet
            AssistChip(
                onClick = { onAddBlock(BlockType.BULLET) },
                label = { Text("Bulletin") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Bulletin",
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = ChipShapeExpressive,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )

            // Add Text
            AssistChip(
                onClick = { onAddBlock(BlockType.TEXT) },
                label = { Text("Text") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "Text",
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = ChipShapeExpressive,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )

            // Add Heading
            AssistChip(
                onClick = { onAddBlock(BlockType.HEADING) },
                label = { Text("Heading") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = "Heading",
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = ChipShapeExpressive,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )

            // Note Color
            IconButton(
                onClick = onOpenColorPicker,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = "Change color",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Note Card Size
            IconButton(
                onClick = onOpenSizePicker,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Change card size",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
