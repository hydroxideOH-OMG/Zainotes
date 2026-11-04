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

import com.zainotes.app.ui.components.normalizeTagName

enum class HomeFilter {
    ALL
}

enum class HomeSortOrder(val label: String) {
    RECENTLY_EDITED("Recently edited"),
    TITLE_AZ("Title (A–Z)"),
    OLDEST_FIRST("Oldest first")
}

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val totalCount: Int = 0,
    val tasksCount: Int = 0,
    val pinnedCount: Int = 0,
    val openTasksTotal: Int = 0,
    val allTags: List<String> = emptyList(),
    val tagCounts: Map<String, Int> = emptyMap(),
    val selectedTag: String? = null,
    val activeFilter: HomeFilter = HomeFilter.ALL,
    val sortOrder: HomeSortOrder = HomeSortOrder.RECENTLY_EDITED,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isFreeformGrid: Boolean = true,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    private val _activeFilter = MutableStateFlow(HomeFilter.ALL)
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(HomeSortOrder.RECENTLY_EDITED)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.activeNotes,
        _selectedTag,
        _sortOrder,
        _searchQuery,
        _isSearchActive
    ) { notes, selectedTag, sortOrder, query, searchActive ->
        val total = notes.size
        val tasks = notes.count { it.totalTasksCount > 0 }
        val pinned = notes.count { it.isPinned }
        val openTasks = notes.sumOf { it.totalTasksCount - it.completedTasksCount }

        val countsMap = linkedMapOf<String, Int>()
        val canonicalMap = linkedMapOf<String, String>()
        notes.forEach { note ->
            note.tags.forEach { rawTag ->
                val cleaned = normalizeTagName(rawTag)
                if (cleaned.isNotBlank()) {
                    val key = cleaned.lowercase()
                    val display = canonicalMap.getOrPut(key) { cleaned }
                    countsMap[display] = (countsMap[display] ?: 0) + 1
                }
            }
        }
        val allTagsSorted = countsMap.keys.sortedBy { it.lowercase() }
        val activeSelectedTag = selectedTag?.takeIf { sel ->
            allTagsSorted.any { it.equals(sel, ignoreCase = true) }
        }

        val tagFiltered = if (activeSelectedTag == null) {
            notes
        } else {
            notes.filter { note ->
                note.tags.any { it.equals(activeSelectedTag, ignoreCase = true) }
            }
        }

        val trimmedQuery = query.trim().removePrefix("#")
        val searched = if (trimmedQuery.isEmpty()) {
            tagFiltered
        } else {
            tagFiltered.filter { note ->
                note.title.contains(trimmedQuery, ignoreCase = true) ||
                    note.previewText.contains(trimmedQuery, ignoreCase = true) ||
                    note.tags.any { it.contains(trimmedQuery, ignoreCase = true) } ||
                    note.blocks.any { it.content.contains(trimmedQuery, ignoreCase = true) }
            }
        }

        val sorted = when (sortOrder) {
            HomeSortOrder.RECENTLY_EDITED -> searched.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
            )
            HomeSortOrder.TITLE_AZ -> searched.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase() }
            )
            HomeSortOrder.OLDEST_FIRST -> searched.sortedWith(
                compareByDescending<Note> { it.isPinned }.thenBy { it.createdAt }
            )
        }

        HomeUiState(
            notes = sorted,
            totalCount = total,
            tasksCount = tasks,
            pinnedCount = pinned,
            openTasksTotal = openTasks,
            allTags = allTagsSorted,
            tagCounts = countsMap,
            selectedTag = activeSelectedTag,
            activeFilter = _activeFilter.value,
            sortOrder = sortOrder,
            searchQuery = query,
            isSearchActive = searchActive,
            isFreeformGrid = true,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = false)
    )

    fun onFilterChanged(filter: HomeFilter) {
        _activeFilter.value = filter
        if (filter == HomeFilter.ALL) {
            _selectedTag.value = null
        }
    }

    fun onSelectTag(tag: String?) {
        if (tag == null) {
            _selectedTag.value = null
        } else if (_selectedTag.value?.equals(tag, ignoreCase = true) == true) {
            _selectedTag.value = null
        } else {
            _selectedTag.value = tag
        }
    }

    fun onSortOrderChanged(sortOrder: HomeSortOrder) {
        _sortOrder.value = sortOrder
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

    fun updateNoteTags(noteId: Long, tags: List<String>) {
        viewModelScope.launch {
            repository.updateTags(noteId, tags)
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
