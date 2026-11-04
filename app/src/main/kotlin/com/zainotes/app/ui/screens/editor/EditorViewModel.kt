/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.Note
import com.zainotes.app.data.model.NoteBlock
import com.zainotes.app.data.preferences.SettingsRepository
import com.zainotes.app.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.zainotes.app.ui.components.normalizeTagName
import java.util.UUID

data class EditorUiState(
    val noteId: Long = 0,
    val title: String = "",
    val colorId: String = "default",
    val backgroundTheme: String = "none",
    val cardSize: CardSize = CardSize.FREEFORM,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val tags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val blocks: List<NoteBlock> = listOf(NoteBlock(type = BlockType.TEXT)),
    val activeBlockId: String? = null,
    val strikethroughCompleted: Boolean = true,
    val confirmNoteDelete: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleted: Boolean = false
) {
    val activeBlock: NoteBlock?
        get() = blocks.find { it.id == activeBlockId } ?: blocks.lastOrNull()
}

class EditorViewModel(
    private val repository: NotesRepository,
    private val initialNoteId: Long = 0L,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val initialBlock = NoteBlock(type = BlockType.TEXT)
    private val _uiState = MutableStateFlow(
        EditorUiState(
            noteId = initialNoteId,
            blocks = listOf(initialBlock),
            activeBlockId = initialBlock.id
        )
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val saveMutex = Mutex()
    private var moveCheckedToBottom: Boolean = false

    init {
        viewModelScope.launch {
            val snapshot = settingsRepository?.settingsFlow?.first()
            if (snapshot != null) {
                moveCheckedToBottom = snapshot.moveCheckedToBottom
                _uiState.update { state ->
                    if (initialNoteId == 0L && state.noteId == 0L) {
                        state.copy(
                            colorId = snapshot.defaultNoteColor,
                            backgroundTheme = snapshot.defaultBackgroundTheme,
                            cardSize = snapshot.defaultCardSize,
                            strikethroughCompleted = snapshot.strikethroughCompleted,
                            confirmNoteDelete = snapshot.confirmNoteDelete
                        )
                    } else {
                        state.copy(
                            strikethroughCompleted = snapshot.strikethroughCompleted,
                            confirmNoteDelete = snapshot.confirmNoteDelete
                        )
                    }
                }
            }
            if (initialNoteId > 0) {
                loadNote(initialNoteId)
            }
        }
        viewModelScope.launch {
            repository.activeNotes.collect { allNotes ->
                val distinctTags = allNotes
                    .flatMap { it.tags }
                    .map { normalizeTagName(it) }
                    .filter { it.isNotBlank() }
                    .distinctBy { it.lowercase() }
                    .sortedBy { it.lowercase() }
                _uiState.update { it.copy(availableTags = distinctTags) }
            }
        }
    }

    private suspend fun loadNote(id: Long) {
        val note = repository.getNoteByIdDirect(id)
        if (note != null) {
            val loadedBlocks = if (note.blocks.isEmpty()) listOf(NoteBlock(type = BlockType.TEXT)) else note.blocks
            _uiState.update {
                it.copy(
                    noteId = note.id,
                    title = note.title,
                    colorId = note.colorId,
                    backgroundTheme = note.backgroundTheme,
                    cardSize = note.cardSize,
                    isPinned = note.isPinned,
                    isArchived = note.isArchived,
                    tags = note.tags,
                    blocks = loadedBlocks,
                    activeBlockId = loadedBlocks.firstOrNull()?.id
                )
            }
        }
    }

    fun setActiveBlock(blockId: String) {
        _uiState.update { it.copy(activeBlockId = blockId) }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        autoSave()
    }

    fun updateColor(colorId: String) {
        _uiState.update { it.copy(colorId = colorId) }
        autoSave()
    }

    fun updateBackgroundTheme(theme: String) {
        _uiState.update { it.copy(backgroundTheme = theme) }
        autoSave()
    }

    fun updateCardSize(cardSize: CardSize) {
        _uiState.update { it.copy(cardSize = cardSize) }
        autoSave()
    }

    fun togglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned) }
        autoSave()
    }

    fun addTag(rawTag: String) {
        val cleaned = normalizeTagName(rawTag)
        if (cleaned.isBlank()) return
        _uiState.update { state ->
            if (state.tags.any { it.equals(cleaned, ignoreCase = true) }) {
                state
            } else {
                state.copy(tags = state.tags + cleaned)
            }
        }
        autoSave()
    }

    fun removeTag(tag: String) {
        _uiState.update { state ->
            state.copy(tags = state.tags.filterNot { it.equals(tag, ignoreCase = true) })
        }
        autoSave()
    }

    fun toggleTag(rawTag: String) {
        val cleaned = normalizeTagName(rawTag)
        if (cleaned.isBlank()) return
        _uiState.update { state ->
            val exists = state.tags.any { it.equals(cleaned, ignoreCase = true) }
            val nextTags = if (exists) {
                state.tags.filterNot { it.equals(cleaned, ignoreCase = true) }
            } else {
                state.tags + cleaned
            }
            state.copy(tags = nextTags)
        }
        autoSave()
    }

    fun addBlock(type: BlockType, targetIndex: Int = -1) {
        _uiState.update { state ->
            val mutable = state.blocks.toMutableList()
            val defaultTable = if (type == BlockType.TABLE) {
                listOf(
                    listOf("Column 1", "Column 2", "Column 3"),
                    listOf("", "", ""),
                    listOf("", "", "")
                )
            } else {
                emptyList()
            }
            val newBlock = NoteBlock(
                id = UUID.randomUUID().toString(),
                type = type,
                content = if (type == BlockType.TABLE) "Column 1 | Column 2 | Column 3\n |  | \n |  | " else "",
                order = mutable.size,
                isMonospace = type == BlockType.CODE,
                tableData = defaultTable
            )
            if (targetIndex in 0..mutable.size) {
                mutable.add(targetIndex, newBlock)
            } else {
                mutable.add(newBlock)
            }
            state.copy(blocks = mutable, activeBlockId = newBlock.id)
        }
        autoSave()
    }

    fun addMediaBlock(
        type: BlockType,
        mediaUri: String,
        mediaName: String,
        mediaMimeType: String,
        mediaDurationMs: Long,
        mediaSizeBytes: Long
    ) {
        _uiState.update { state ->
            val mutable = state.blocks.toMutableList()
            val mediaBlock = NoteBlock(
                id = UUID.randomUUID().toString(),
                type = type,
                content = "",
                order = mutable.size,
                mediaUri = mediaUri,
                mediaName = mediaName,
                mediaMimeType = mediaMimeType,
                mediaDurationMs = mediaDurationMs,
                mediaSizeBytes = mediaSizeBytes
            )
            if (mutable.size == 1 && mutable[0].type == BlockType.TEXT && mutable[0].content.isBlank() && mutable[0].mediaUri.isNullOrBlank()) {
                mutable.add(0, mediaBlock)
            } else {
                val activeIdx = mutable.indexOfFirst { it.id == state.activeBlockId }
                if (activeIdx in mutable.indices) {
                    mutable.add(activeIdx + 1, mediaBlock)
                } else {
                    mutable.add(mediaBlock)
                }
            }
            state.copy(blocks = mutable, activeBlockId = mediaBlock.id)
        }
        autoSave()
    }

    fun updateBlockContent(blockId: String, content: String) {
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) block.copy(content = content) else block
            }
            state.copy(blocks = updated, activeBlockId = blockId)
        }
        autoSave()
    }

    fun updateTableData(blockId: String, newTable: List<List<String>>) {
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) {
                    val md = newTable.joinToString("\n") { row -> row.joinToString(" | ") }
                    block.copy(tableData = newTable, content = md)
                } else {
                    block
                }
            }
            state.copy(blocks = updated, activeBlockId = blockId)
        }
        autoSave()
    }

    fun toggleTaskChecked(blockId: String) {
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) block.copy(isChecked = !block.isChecked) else block
            }
            val ordered = if (moveCheckedToBottom) {
                val unchecked = updated.filterNot { it.type == BlockType.TASK && it.isChecked }
                val checked = updated.filter { it.type == BlockType.TASK && it.isChecked }
                unchecked + checked
            } else {
                updated
            }
            state.copy(blocks = ordered)
        }
        autoSave()
    }

    fun convertBlockType(blockId: String, newType: BlockType) {
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) {
                    val nextTable = if (newType == BlockType.TABLE && block.tableData.isEmpty()) {
                        block.resolvedTable()
                    } else {
                        block.tableData
                    }
                    block.copy(
                        type = newType,
                        isMonospace = if (newType == BlockType.CODE) true else block.isMonospace,
                        tableData = nextTable
                    )
                } else {
                    block
                }
            }
            state.copy(blocks = updated, activeBlockId = blockId)
        }
        autoSave()
    }

    // Rich text formatting toggles for the upper floating formatting bar
    private fun updateActiveBlock(transform: (NoteBlock) -> NoteBlock) {
        _uiState.update { state ->
            val targetId = state.activeBlock?.id ?: return@update state
            val updated = state.blocks.map { block ->
                if (block.id == targetId) transform(block) else block
            }
            state.copy(blocks = updated, activeBlockId = targetId)
        }
        autoSave()
    }

    fun toggleBold() = updateActiveBlock { it.copy(isBold = !it.isBold) }

    fun toggleItalic() = updateActiveBlock { it.copy(isItalic = !it.isItalic) }

    fun toggleUnderline() = updateActiveBlock { it.copy(isUnderline = !it.isUnderline) }

    fun toggleStrikethrough() = updateActiveBlock { it.copy(isStrikethrough = !it.isStrikethrough) }

    fun toggleHighlight() = updateActiveBlock { it.copy(isHighlighted = !it.isHighlighted) }

    fun toggleMonospace() = updateActiveBlock { it.copy(isMonospace = !it.isMonospace) }

    fun cycleTextSize() = updateActiveBlock { block ->
        val nextScale = when (block.textScale) {
            0 -> 1
            1 -> -1
            else -> 0
        }
        block.copy(textScale = nextScale)
    }

    fun cycleTextAlign() = updateActiveBlock { block ->
        val nextAlign = (block.textAlign + 1) % 3
        block.copy(textAlign = nextAlign)
    }

    fun clearBlockFormatting() = updateActiveBlock { block ->
        block.copy(
            isBold = false,
            isItalic = false,
            isUnderline = false,
            isStrikethrough = false,
            isHighlighted = false,
            isMonospace = false,
            textScale = 0,
            textAlign = 0
        )
    }

    fun deleteBlock(blockId: String) {
        _uiState.update { state ->
            if (state.blocks.size <= 1) {
                val fresh = NoteBlock(type = BlockType.TEXT)
                state.copy(blocks = listOf(fresh), activeBlockId = fresh.id)
            } else {
                val updated = state.blocks.filterNot { it.id == blockId }
                val nextActive = if (state.activeBlockId == blockId) updated.lastOrNull()?.id else state.activeBlockId
                state.copy(blocks = updated, activeBlockId = nextActive)
            }
        }
        autoSave()
    }

    fun moveBlockUp(index: Int) {
        if (index <= 0) return
        _uiState.update { state ->
            val list = state.blocks.toMutableList()
            val item = list.removeAt(index)
            list.add(index - 1, item)
            state.copy(blocks = list)
        }
        autoSave()
    }

    fun moveBlockDown(index: Int) {
        val currentSize = _uiState.value.blocks.size
        if (index >= currentSize - 1) return
        _uiState.update { state ->
            val list = state.blocks.toMutableList()
            val item = list.removeAt(index)
            list.add(index + 1, item)
            state.copy(blocks = list)
        }
        autoSave()
    }

    fun deleteNote(onDeleted: () -> Unit) {
        viewModelScope.launch {
            saveMutex.withLock {
                val id = _uiState.value.noteId
                if (id > 0) {
                    repository.deleteNoteById(id)
                }
                _uiState.update { it.copy(isDeleted = true) }
            }
            onDeleted()
        }
    }

    private fun autoSave() {
        viewModelScope.launch {
            saveMutex.withLock {
                val current = _uiState.value
                if (current.isDeleted) return@withLock

                val hasTextContent = current.title.isNotBlank() ||
                    current.blocks.any { it.content.isNotBlank() || !it.mediaUri.isNullOrBlank() } ||
                    current.tags.isNotEmpty()
                val hasCustomAppearance = current.colorId != "default" || current.backgroundTheme != "none" || current.isPinned
                if (!hasTextContent && !hasCustomAppearance && current.noteId == 0L) return@withLock

                val note = Note(
                    id = current.noteId,
                    title = current.title,
                    colorId = current.colorId,
                    backgroundTheme = current.backgroundTheme,
                    cardSize = current.cardSize,
                    isPinned = current.isPinned,
                    isArchived = current.isArchived,
                    tags = current.tags,
                    blocks = current.blocks
                )
                val savedId = repository.saveNote(note)
                if (current.noteId == 0L && savedId > 0L) {
                    _uiState.update { it.copy(noteId = savedId) }
                }
            }
        }
    }
}
