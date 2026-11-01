/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */
package com.zainotes.app.ui.screens.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.ui.components.NoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNoteClick: (Long) -> Unit,
    onNewNoteClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Zainotes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.totalCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "· ${state.totalCount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::toggleFreeformGrid,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isFreeformGrid) Icons.Default.ViewAgenda else Icons.Default.DashboardCustomize,
                            contentDescription = if (state.isFreeformGrid) "Switch to compact masonry" else "Switch to freeform canvas",
                            tint = if (state.isFreeformGrid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewNoteClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.large,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 6.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create note",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            // Header & Expressive Filter Chips Row (No Search bar)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Your canvas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (state.totalCount == 0) "A quiet, offline space for your thoughts."
                    else if (state.isFreeformGrid) "Freeform sizing active · tap any checklist to complete"
                    else "Tap a task to check off · tap or hold card to resize",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Expressive Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = state.activeFilter == HomeFilter.ALL,
                        onClick = { viewModel.onFilterChanged(HomeFilter.ALL) },
                        label = { Text("All (${state.totalCount})") },
                        leadingIcon = {
                            Icon(Icons.Default.GridView, contentDescription = null, Modifier.size(16.dp))
                        },
                        shape = MaterialTheme.shapes.medium
                    )

                    FilterChip(
                        selected = state.activeFilter == HomeFilter.TASKS,
                        onClick = { viewModel.onFilterChanged(HomeFilter.TASKS) },
                        label = { Text("Checklists (${state.tasksCount})") },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, Modifier.size(16.dp))
                        },
                        shape = MaterialTheme.shapes.medium
                    )

                    FilterChip(
                        selected = state.activeFilter == HomeFilter.PINNED,
                        onClick = { viewModel.onFilterChanged(HomeFilter.PINNED) },
                        label = { Text("Pinned (${state.pinnedCount})") },
                        leadingIcon = {
                            Icon(Icons.Default.PushPin, contentDescription = null, Modifier.size(16.dp))
                        },
                        shape = MaterialTheme.shapes.medium
                    )

                    FilterChip(
                        selected = state.isFreeformGrid,
                        onClick = viewModel::toggleFreeformGrid,
                        label = { Text(if (state.isFreeformGrid) "Freeform: On" else "Freeform") },
                        leadingIcon = {
                            Icon(Icons.Default.DashboardCustomize, contentDescription = null, Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            // Canvas Content
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                state.notes.isEmpty() -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val isFiltered = state.activeFilter != HomeFilter.ALL
                    Icon(
                        imageVector = when (state.activeFilter) {
                            HomeFilter.TASKS -> Icons.Default.CheckCircleOutline
                            HomeFilter.PINNED -> Icons.Default.PushPin
                            HomeFilter.ALL -> Icons.Default.EditNote
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (isFiltered) "No notes in this view" else "Start with one thought",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isFiltered) "Switch back to All notes or create a new note for this section."
                        else "Create notes, checklists, and bulletin cards with flexible organic sizing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                    )
                    if (isFiltered) {
                        FilledTonalButton(
                            onClick = { viewModel.onFilterChanged(HomeFilter.ALL) }
                        ) {
                            Text("View all notes")
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onNewNoteClick
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create note")
                        }
                    }
                }

                else -> {
                    val minCellWidth = if (state.isFreeformGrid) 240.dp else 176.dp
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = minCellWidth),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = state.notes,
                            key = { it.id },
                            span = { note ->
                                if (state.isFreeformGrid) {
                                    if (note.cardSize == CardSize.WIDE || note.cardSize == CardSize.HERO || note.blocks.size > 5) {
                                        StaggeredGridItemSpan.FullLine
                                    } else {
                                        StaggeredGridItemSpan.SingleLane
                                    }
                                } else {
                                    if (note.cardSize == CardSize.WIDE || note.cardSize == CardSize.HERO) {
                                        StaggeredGridItemSpan.FullLine
                                    } else {
                                        StaggeredGridItemSpan.SingleLane
                                    }
                                }
                            }
                        ) { note ->
                            val effectiveNote = if (state.isFreeformGrid && note.cardSize == CardSize.STANDARD) {
                                note.copy(cardSize = CardSize.FREEFORM)
                            } else {
                                note
                            }

                            NoteCard(
                                note = effectiveNote,
                                onClick = { onNoteClick(note.id) },
                                onTaskToggle = { blockId, checked ->
                                    viewModel.toggleTaskChecked(note.id, blockId, checked)
                                },
                                onCardSizeChange = { viewModel.updateCardSize(note.id, it) },
                                onColorChange = { viewModel.updateColor(note.id, it) },
                                onTogglePin = { viewModel.togglePin(note) },
                                onDelete = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}
