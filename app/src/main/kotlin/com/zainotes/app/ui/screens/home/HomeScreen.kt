/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */
package com.zainotes.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.preferences.AppSettingsSnapshot
import com.zainotes.app.ui.components.NoteCard
import com.zainotes.app.ui.theme.ExpressiveMotion

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNoteClick: (Long) -> Unit,
    onNewNoteClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    settings: AppSettingsSnapshot = AppSettingsSnapshot(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showSplitMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Zainotes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (state.totalCount > 0) {
                            Spacer(Modifier.width(10.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${state.totalCount} notes",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::toggleSearchActive,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (state.isSearchActive) "Close search" else "Search notes",
                            tint = if (state.isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            // Header, Collapsible Expressive Search Bar & Material 3 Expressive Split Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Your expressive canvas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        state.totalCount == 0 -> "A quiet, 100% offline space for your thoughts and tasks."
                        state.openTasksTotal > 0 -> "${state.openTasksTotal} open tasks across ${state.tasksCount} checklists · tap to complete"
                        else -> "Tap a task to check off · hold any card for colors & scenes"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                AnimatedVisibility(
                    visible = state.isSearchActive,
                    enter = expandVertically(ExpressiveMotion.spatialFastSpec()) + fadeIn(),
                    exit = shrinkVertically(ExpressiveMotion.snappySpec()) + fadeOut()
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        placeholder = { Text("Search titles, tasks, or note blocks...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape
                    )
                }

                // Material 3 Expressive Split Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        ExpressiveSplitButton(
                            selected = true,
                            menuExpanded = showSplitMenu,
                            onLeadingClick = {
                                viewModel.onFilterChanged(HomeFilter.ALL)
                                viewModel.onSearchQueryChanged("")
                            },
                            onTrailingClick = { showSplitMenu = !showSplitMenu },
                            icon = Icons.Default.GridView,
                            label = "All",
                            count = state.totalCount
                        )

                        DropdownMenu(
                            expanded = showSplitMenu,
                            onDismissRequest = { showSplitMenu = false }
                        ) {
                            HomeSortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        if (state.sortOrder == order) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Spacer(Modifier.size(18.dp))
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = "Sort: ${order.label}",
                                            fontWeight = if (state.sortOrder == order) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        viewModel.onSortOrderChanged(order)
                                        showSplitMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Canvas Content (Always Free-Form Mode)
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
                    val isFiltered = state.searchQuery.isNotBlank()
                    Icon(
                        imageVector = if (state.searchQuery.isNotBlank()) Icons.Default.Search else Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (state.searchQuery.isNotBlank()) "No matching notes" else "Start with one thought",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (state.searchQuery.isNotBlank()) {
                            "Try another search keyword or clear the search filter."
                        } else {
                            "Create notes, checklists, and bulletin cards with vibrant colors and illustrated scenes."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                    )
                    if (isFiltered) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.onFilterChanged(HomeFilter.ALL)
                                viewModel.onSearchQueryChanged("")
                            }
                        ) {
                            Text("Show all notes")
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
                    val gridSpacing = if (settings.compactCardSpacing) 8.dp else 12.dp
                    val columnsConfig = if (settings.gridColumns == 1) {
                        StaggeredGridCells.Fixed(1)
                    } else {
                        StaggeredGridCells.Fixed(2)
                    }

                    LazyVerticalStaggeredGrid(
                        columns = columnsConfig,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = 104.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                        verticalItemSpacing = gridSpacing,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = state.notes,
                            key = { it.id },
                            span = { note ->
                                if (settings.gridColumns == 1 ||
                                    note.cardSize == CardSize.WIDE ||
                                    note.cardSize == CardSize.HERO
                                ) {
                                    StaggeredGridItemSpan.FullLine
                                } else {
                                    StaggeredGridItemSpan.SingleLane
                                }
                            }
                        ) { note ->
                            // Free-form mode is always on by default
                            val effectiveNote = if (note.cardSize == CardSize.STANDARD) {
                                note.copy(cardSize = CardSize.FREEFORM)
                            } else {
                                note
                            }

                            NoteCard(
                                note = effectiveNote,
                                settings = settings,
                                onClick = { onNoteClick(note.id) },
                                onTaskToggle = { blockId, checked ->
                                    viewModel.toggleTaskChecked(note.id, blockId, checked)
                                },
                                onCardSizeChange = { viewModel.updateCardSize(note.id, it) },
                                onColorChange = { viewModel.updateColor(note.id, it) },
                                onBackgroundThemeChange = { viewModel.updateBackgroundTheme(note.id, it) },
                                onDuplicate = { viewModel.duplicateNote(note) },
                                onTogglePin = { viewModel.togglePin(note) },
                                onDelete = { viewModel.deleteNote(note) },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = ExpressiveMotion.snappySpec(),
                                    placementSpec = ExpressiveMotion.spatialDefaultSpec(),
                                    fadeOutSpec = ExpressiveMotion.snappySpec()
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Expressive Split Button:
 * Two-segment split silhouette with a 2dp split seam, asymmetric corner radii
 * (50% outer pill corners, 4dp inner split corners that morph on selection),
 * leading icon + label segment, and trailing count + chevron segment.
 */
@Composable
private fun ExpressiveSplitButton(
    selected: Boolean,
    menuExpanded: Boolean = false,
    onLeadingClick: () -> Unit,
    onTrailingClick: () -> Unit,
    icon: ImageVector,
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "splitBtnScale"
    )

    val innerCorner by animateDpAsState(
        targetValue = if (selected) 6.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "splitInnerCorner"
    )

    val chevronRotation by animateFloatAsState(
        targetValue = if (menuExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "splitChevronRot"
    )

    val leadingBg by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "splitLeadingBg"
    )

    val leadingFg by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "splitLeadingFg"
    )

    val trailingBg by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "splitTrailingBg"
    )

    val trailingFg by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "splitTrailingFg"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .scale(scale)
            .semantics {
                role = Role.Tab
                this.selected = selected
            }
    ) {
        // Leading Split Segment (Icon + Label)
        Surface(
            onClick = onLeadingClick,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                bottomStart = 20.dp,
                topEnd = innerCorner,
                bottomEnd = innerCorner
            ),
            color = leadingBg,
            contentColor = leadingFg,
            modifier = Modifier.height(38.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 14.dp, end = 10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                )
            }
        }

        // Trailing Split Segment (Count + Expressive Split Chevron)
        Surface(
            onClick = onTrailingClick,
            shape = RoundedCornerShape(
                topStart = innerCorner,
                bottomStart = innerCorner,
                topEnd = 20.dp,
                bottomEnd = 20.dp
            ),
            color = trailingBg,
            contentColor = trailingFg,
            modifier = Modifier.height(38.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 9.dp, end = 11.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Sort notes options",
                    modifier = Modifier
                        .size(15.dp)
                        .rotate(chevronRotation)
                )
            }
        }
    }
}

