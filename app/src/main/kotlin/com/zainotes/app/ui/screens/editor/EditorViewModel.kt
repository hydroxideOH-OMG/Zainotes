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
import com.zainotes.app.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EditorUiState(
    val noteId: Long = 0,
    val title: String = "",
    val colorId: String = "default",
    val backgroundTheme: String = "none",
    val cardSize: CardSize = CardSize.STANDARD,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val blocks: List<NoteBlock> = listOf(NoteBlock(type = BlockType.TEXT)),
    val isSaving: Boolean = false,
    val isDeleted: Boolean = false
)

class EditorViewModel(
    private val repository: NotesRepository,
    private val initialNoteId: Long = 0L
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState(noteId = initialNoteId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        if (initialNoteId > 0) {
            loadNote(initialNoteId)
        }
    }

    private fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = repository.getNoteByIdDirect(id)
            if (note != null) {
                _uiState.update {
                    it.copy(
                        noteId = note.id,
                        title = note.title,
                        colorId = note.colorId,
                        backgroundTheme = note.backgroundTheme,
                        cardSize = note.cardSize,
                        isPinned = note.isPinned,
                        isArchived = note.isArchived,
                        blocks = if (note.blocks.isEmpty()) listOf(NoteBlock(type = BlockType.TEXT)) else note.blocks
                    )
                }
            }
        }
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

    fun addBlock(type: BlockType, targetIndex: Int = -1) {
        _uiState.update { state ->
            val mutable = state.blocks.toMutableList()
            val newBlock = NoteBlock(
                id = UUID.randomUUID().toString(),
                type = type,
                content = "",
                order = mutable.size
            )
            if (targetIndex in 0..mutable.size) {
                mutable.add(targetIndex, newBlock)
            } else {
                mutable.add(newBlock)
            }
            state.copy(blocks = mutable)
        }
        autoSave()
    }

    fun updateBlockContent(blockId: String, content: String) {
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) block.copy(content = content) else block
            }
            state.copy(blocks = updated)
        }
        autoSave()
    }

    fun toggleTaskChecked(blockId: String) {
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) block.copy(isChecked = !block.isChecked) else block
            }
            state.copy(blocks = updated)
        }
        autoSave()
    }

    fun convertBlockType(blockId: String, newType: BlockType) {
        _uiState.update { state ->
            val updated = state.blocks.map { block ->
                if (block.id == blockId) block.copy(type = newType) else block
            }
            state.copy(blocks = updated)
        }
        autoSave()
    }

    fun deleteBlock(blockId: String) {
        _uiState.update { state ->
            if (state.blocks.size <= 1) {
                // If it's the last block, just reset it to empty text block
                state.copy(blocks = listOf(NoteBlock(type = BlockType.TEXT)))
            } else {
                val updated = state.blocks.filterNot { it.id == blockId }
                state.copy(blocks = updated)
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
            val id = _uiState.value.noteId
            if (id > 0) {
                repository.deleteNoteById(id)
            }
            _uiState.update { it.copy(isDeleted = true) }
            onDeleted()
        }
    }

    private fun autoSave() {
        val current = _uiState.value
        // Don't save empty notes that haven't been persisted yet
        val hasContent = current.title.isNotBlank() || current.blocks.any { it.content.isNotBlank() }
        if (!hasContent && current.noteId == 0L) return

        viewModelScope.launch {
            val note = Note(
                id = current.noteId,
                title = current.title,
                colorId = current.colorId,
                backgroundTheme = current.backgroundTheme,
                cardSize = current.cardSize,
                isPinned = current.isPinned,
                isArchived = current.isArchived,
                blocks = current.blocks
            )
            val savedId = repository.saveNote(note)
            if (current.noteId == 0L) {
                _uiState.update { it.copy(noteId = savedId) }
            }
        }
    }
}
