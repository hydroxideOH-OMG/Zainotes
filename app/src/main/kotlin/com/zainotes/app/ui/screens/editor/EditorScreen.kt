/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.screens.editor

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.ui.components.BlockItem
import com.zainotes.app.ui.components.BlockToolbar
import com.zainotes.app.ui.components.CardSizePickerSheet
import com.zainotes.app.ui.components.ColorPickerSheet
import com.zainotes.app.ui.theme.ExpressiveMotion
import com.zainotes.app.ui.theme.NoteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val noteColor = NoteColor.fromId(state.colorId)
    val targetBgColor = if (isDark) noteColor.darkContainer else noteColor.lightContainer
    val targetContentColor = if (isDark) noteColor.onDarkContainer else noteColor.onLightContainer

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "bgColor"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "contentColor"
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showSizePicker by remember { mutableStateOf(false) }

    val colorSheetState = rememberModalBottomSheetState()
    val sizeSheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = animatedContentColor
                        )
                    }
                },
                actions = {
                    // Pin Button
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (state.isPinned) MaterialTheme.colorScheme.primary else animatedContentColor.copy(alpha = 0.7f)
                        )
                    }

                    // Delete Button
                    if (state.noteId > 0) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Note",
                                tint = animatedContentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            BlockToolbar(
                onAddBlock = { type -> viewModel.addBlock(type) },
                onOpenColorPicker = { showColorPicker = true },
                onOpenSizePicker = { showSizePicker = true }
            )
        },
        containerColor = animatedBgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Note Title Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (state.title.isEmpty()) {
                    Text(
                        text = "Note Title",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = animatedContentColor.copy(alpha = 0.45f)
                        )
                    )
                }
                BasicTextField(
                    value = state.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = animatedContentColor
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Blocks List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(
                    items = state.blocks,
                    key = { _, block -> block.id }
                ) { index, block ->
                    BlockItem(
                        block = block,
                        index = index,
                        totalBlocks = state.blocks.size,
                        onContentChange = { newText -> viewModel.updateBlockContent(block.id, newText) },
                        onTaskToggle = { viewModel.toggleTaskChecked(block.id) },
                        onConvertType = { newType -> viewModel.convertBlockType(block.id, newType) },
                        onDelete = { viewModel.deleteBlock(block.id) },
                        onMoveUp = { viewModel.moveBlockUp(index) },
                        onMoveDown = { viewModel.moveBlockDown(index) },
                        onEnterPressed = { viewModel.addBlock(BlockType.TEXT, index + 1) },
                        contentColor = animatedContentColor
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteNote(onNavigateBack)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Color Picker Sheet
    if (showColorPicker) {
        ColorPickerSheet(
            selectedColorId = state.colorId,
            onColorSelected = { selectedId ->
                viewModel.updateColor(selectedId)
                showColorPicker = false
            },
            sheetState = colorSheetState,
            onDismissRequest = { showColorPicker = false }
        )
    }

    // Card Size Picker Sheet
    if (showSizePicker) {
        CardSizePickerSheet(
            selectedSize = state.cardSize,
            onSizeSelected = { selectedSize ->
                viewModel.updateCardSize(selectedSize)
                showSizePicker = false
            },
            sheetState = sizeSheetState,
            onDismissRequest = { showSizePicker = false }
        )
    }
}
