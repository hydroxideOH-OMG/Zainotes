/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.screens.editor

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.NoteBackgroundTheme
import com.zainotes.app.data.model.getNumberedIndex
import com.zainotes.app.ui.components.BlockItem
import com.zainotes.app.ui.components.CardSizePickerSheet
import com.zainotes.app.ui.components.EditorFloatingDock
import com.zainotes.app.ui.components.NoteBackgroundRenderer
import com.zainotes.app.ui.components.NoteThemePickerSheet
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val noteColor = NoteColor.fromId(state.colorId)
    val targetBgColor = noteColor.containerColor(isDark, MaterialTheme.colorScheme.background)
    val targetContentColor = noteColor.contentColor(isDark, MaterialTheme.colorScheme.onBackground)

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
    var showThemePicker by remember { mutableStateOf(false) }
    var showSizePicker by remember { mutableStateOf(false) }

    val themeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sizeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeTheme = NoteBackgroundTheme.fromId(state.backgroundTheme)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.title.isBlank()) "New note" else "Edit note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        color = animatedContentColor
                    )
                },
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
                    // Quick Theme & Scene Button in Top Bar
                    IconButton(onClick = { showThemePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Canvas Color & Scene",
                            tint = animatedContentColor.copy(alpha = 0.85f)
                        )
                    }

                    // Pin Button
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (state.isPinned) MaterialTheme.colorScheme.primary else animatedContentColor.copy(alpha = 0.75f)
                        )
                    }

                    // Delete Button
                    if (state.noteId > 0) {
                        IconButton(
                            onClick = {
                                if (state.confirmNoteDelete) {
                                    showDeleteDialog = true
                                } else {
                                    viewModel.deleteNote(onNavigateBack)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Note",
                                tint = animatedContentColor.copy(alpha = 0.75f)
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
            // Dual-tier Floating Bottom Navigation Dock for the note editor:
            // Upper floating pill: Rich Text Formatting (Bold, Italic, Underline, Strike, Highlight, Code, Size, Align, Clear)
            // Lower floating dock: Left circle tick button (Done & save) + right long pill with block/canvas buttons
            EditorFloatingDock(
                activeBlock = state.activeBlock,
                onDone = onNavigateBack,
                onAddBlock = { type -> viewModel.addBlock(type) },
                onOpenSizePicker = { showSizePicker = true },
                onToggleBold = viewModel::toggleBold,
                onToggleItalic = viewModel::toggleItalic,
                onToggleUnderline = viewModel::toggleUnderline,
                onToggleStrikethrough = viewModel::toggleStrikethrough,
                onToggleHighlight = viewModel::toggleHighlight,
                onToggleMonospace = viewModel::toggleMonospace,
                onCycleTextSize = viewModel::cycleTextSize,
                onCycleTextAlign = viewModel::cycleTextAlign,
                onClearFormatting = viewModel::clearBlockFormatting,
                modifier = Modifier.navigationBarsPadding()
            )
        },
        containerColor = animatedBgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Render native vector background theme/illustration or pattern across the entire editor canvas
            NoteBackgroundRenderer(
                theme = activeTheme,
                tint = animatedContentColor,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::updateTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = animatedContentColor
                    ),
                    label = { Text("Note title", color = animatedContentColor.copy(alpha = 0.75f)) },
                    placeholder = { Text("Give this note a name...", color = animatedContentColor.copy(alpha = 0.45f)) },
                    singleLine = false,
                    maxLines = 3,
                    shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = animatedContentColor.copy(alpha = 0.25f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

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
                            isFocused = state.activeBlock?.id == block.id,
                            strikethroughCompleted = state.strikethroughCompleted,
                            onFocused = { viewModel.setActiveBlock(block.id) },
                            onContentChange = { newText -> viewModel.updateBlockContent(block.id, newText) },
                            onTableChange = { newTable -> viewModel.updateTableData(block.id, newTable) },
                            onTaskToggle = { viewModel.toggleTaskChecked(block.id) },
                            onConvertType = { newType -> viewModel.convertBlockType(block.id, newType) },
                            onDelete = { viewModel.deleteBlock(block.id) },
                            onMoveUp = { viewModel.moveBlockUp(index) },
                            onMoveDown = { viewModel.moveBlockDown(index) },
                            onEnterPressed = {
                                val nextType = when (block.type) {
                                    BlockType.NUMBERED -> BlockType.NUMBERED
                                    BlockType.BULLET -> BlockType.BULLET
                                    BlockType.TASK -> BlockType.TASK
                                    else -> BlockType.TEXT
                                }
                                viewModel.addBlock(nextType, index + 1)
                            },
                            contentColor = animatedContentColor,
                            numberedIndex = state.blocks.getNumberedIndex(index)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(148.dp))
                    }
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
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Unified Note Theme & Styling Sheet (Vibrant Colors + Google Keep Background Images)
    if (showThemePicker) {
        NoteThemePickerSheet(
            selectedColorId = state.colorId,
            onColorSelected = { selectedId ->
                viewModel.updateColor(selectedId)
            },
            selectedThemeId = state.backgroundTheme,
            onThemeSelected = { selectedTheme ->
                viewModel.updateBackgroundTheme(selectedTheme)
            },
            sheetState = themeSheetState,
            onDismissRequest = { showThemePicker = false }
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
