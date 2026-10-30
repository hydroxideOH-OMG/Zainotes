/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.Note
import com.zainotes.app.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HomeFilter {
    ALL,
    TASKS,
    PINNED
}

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: HomeFilter = HomeFilter.ALL,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(HomeFilter.ALL)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.activeNotes,
        _searchQuery,
        _activeFilter
    ) { notes, query, filter ->
        val filtered = notes.filter { note ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                note.title.contains(query, ignoreCase = true) ||
                        note.blocks.any { it.content.contains(query, ignoreCase = true) }
            }

            val matchesFilter = when (filter) {
                HomeFilter.ALL -> true
                HomeFilter.TASKS -> note.totalTasksCount > 0
                HomeFilter.PINNED -> note.isPinned
            }

            matchesQuery && matchesFilter
        }
        HomeUiState(
            notes = filtered,
            searchQuery = query,
            activeFilter = filter,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: HomeFilter) {
        _activeFilter.value = filter
    }

    fun toggleTaskChecked(noteId: Long, blockId: String, isChecked: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskChecked(noteId, blockId, isChecked)
        }
    }

    fun updateCardSize(noteId: Long, cardSize: CardSize) {
        viewModelScope.launch {
            repository.updateCardSize(noteId, cardSize)
        }
    }

    fun updateColor(noteId: Long, colorId: String) {
        viewModelScope.launch {
            repository.updateColor(noteId, colorId)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.togglePin(note.id, !note.isPinned)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}
