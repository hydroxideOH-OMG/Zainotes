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
    val totalCount: Int = 0,
    val tasksCount: Int = 0,
    val pinnedCount: Int = 0,
    val openTasksTotal: Int = 0,
    val activeFilter: HomeFilter = HomeFilter.ALL,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isFreeformGrid: Boolean = false,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    private val _activeFilter = MutableStateFlow(HomeFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _isFreeformGrid = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.activeNotes,
        _activeFilter,
        _searchQuery,
        _isSearchActive,
        _isFreeformGrid
    ) { notes, filter, query, searchActive, freeform ->
        val total = notes.size
        val tasks = notes.count { it.totalTasksCount > 0 }
        val pinned = notes.count { it.isPinned }
        val openTasks = notes.sumOf { it.totalTasksCount - it.completedTasksCount }

        val byCategory = when (filter) {
            HomeFilter.ALL -> notes
            HomeFilter.TASKS -> notes.filter { it.totalTasksCount > 0 }
            HomeFilter.PINNED -> notes.filter { it.isPinned }
        }

        val trimmedQuery = query.trim()
        val filtered = if (trimmedQuery.isEmpty()) {
            byCategory
        } else {
            byCategory.filter { note ->
                note.title.contains(trimmedQuery, ignoreCase = true) ||
                    note.blocks.any { it.content.contains(trimmedQuery, ignoreCase = true) }
            }
        }

        HomeUiState(
            notes = filtered,
            totalCount = total,
            tasksCount = tasks,
            pinnedCount = pinned,
            openTasksTotal = openTasks,
            activeFilter = filter,
            searchQuery = query,
            isSearchActive = searchActive,
            isFreeformGrid = freeform,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = false)
    )

    fun onFilterChanged(filter: HomeFilter) {
        _activeFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearchActive() {
        val next = !_isSearchActive.value
        _isSearchActive.value = next
        if (!next) {
            _searchQuery.value = ""
        }
    }

    fun toggleFreeformGrid() {
        _isFreeformGrid.value = !_isFreeformGrid.value
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

    fun updateBackgroundTheme(noteId: Long, themeId: String) {
        viewModelScope.launch {
            repository.updateBackgroundTheme(noteId, themeId)
        }
    }

    fun duplicateNote(note: Note) {
        viewModelScope.launch {
            repository.duplicateNote(note)
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
