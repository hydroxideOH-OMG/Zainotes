/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.data.repository

import com.zainotes.app.data.local.NoteDao
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class NotesRepository(private val noteDao: NoteDao) {

    val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes().flowOn(Dispatchers.IO)
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes().flowOn(Dispatchers.IO)

    fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id).flowOn(Dispatchers.IO)

    suspend fun getNoteByIdDirect(id: Long): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteByIdDirect(id)
    }

    suspend fun getAllNotesDirect(): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getAllNotesDirect()
    }

    suspend fun saveNote(note: Note): Long = withContext(Dispatchers.IO) {
        if (note.id == 0L) {
            noteDao.insertNote(note.copy(createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
        } else {
            noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }
    }

    suspend fun insertNotes(notes: List<Note>) = withContext(Dispatchers.IO) {
        noteDao.insertNotes(notes)
    }

    suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteById(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }

    suspend fun deleteAllNotes() = withContext(Dispatchers.IO) {
        noteDao.deleteAllNotes()
    }

    suspend fun toggleTaskChecked(noteId: Long, blockId: String, isChecked: Boolean) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteByIdDirect(noteId) ?: return@withContext
        val updatedBlocks = note.blocks.map { block ->
            if (block.id == blockId) block.copy(isChecked = isChecked) else block
        }
        noteDao.updateNote(note.copy(blocks = updatedBlocks, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateCardSize(id: Long, cardSize: CardSize) = withContext(Dispatchers.IO) {
        noteDao.updateCardSize(id, cardSize)
    }

    suspend fun updateColor(id: Long, colorId: String) = withContext(Dispatchers.IO) {
        noteDao.updateColor(id, colorId)
    }

    suspend fun updateBackgroundTheme(id: Long, backgroundTheme: String) = withContext(Dispatchers.IO) {
        noteDao.updateBackgroundTheme(id, backgroundTheme)
    }

    suspend fun duplicateNote(note: Note): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val copy = note.copy(
            id = 0L,
            title = if (note.title.isBlank()) "Copied Note" else "${note.title} (Copy)",
            createdAt = now,
            updatedAt = now
        )
        noteDao.insertNote(copy)
    }

    suspend fun togglePin(id: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        noteDao.updatePinStatus(id, isPinned)
    }

    suspend fun toggleArchive(id: Long, isArchived: Boolean) = withContext(Dispatchers.IO) {
        noteDao.updateArchiveStatus(id, isArchived)
    }

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query).flowOn(Dispatchers.IO)
}
