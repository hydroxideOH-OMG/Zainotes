/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */
package com.zainotes.app.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.preferences.AppSettingsSnapshot
import com.zainotes.app.ui.components.HomeTagFilterChip
import com.zainotes.app.ui.components.NoteCard
import com.zainotes.app.ui.theme.ExpressiveMotion
import com.zainotes.app.ui.theme.expressivePressable
import com.zainotes.app.ui.theme.staggeredEntrance

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
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isSearchActive) {
        if (state.isSearchActive) {
            try {
                searchFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // Top Navigation Bar with Enlarged "Zainotes" & Compact Search Bar that Expands to Full Navbar
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = state.isSearchActive,
                        transitionSpec = {
                            (fadeIn(tween(180)) + scaleIn(
                                initialScale = 0.92f,
                                animationSpec = ExpressiveMotion.spatialHeroSpec()
                            )).togetherWith(
                                fadeOut(tween(140)) + scaleOut(
                                    targetScale = 0.92f,
                                    animationSpec = ExpressiveMotion.spatialFastSpec()
                                )
                            )
                        },
                        label = "topBarSearchExpand"
                    ) { isSearchExpanded ->
                        if (isSearchExpanded) {
                            // Expanded Full-Width Top Navigation Search Box
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        shape = CircleShape
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.onSearchQueryChanged("")
                                            viewModel.toggleSearchActive()
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Close search",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    BasicTextField(
                                        value = state.searchQuery,
                                        onValueChange = viewModel::onSearchQueryChanged,
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 6.dp)
                                            .focusRequester(searchFocusRequester),
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.CenterStart) {
                                                if (state.searchQuery.isEmpty()) {
                                                    Text(
                                                        text = "Search notes, tags, tasks, tables...",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )

                                    if (state.searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.onSearchQueryChanged("") },
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Default Top Bar: Enlarged "Zainotes" on Left + Compact Pill Search Bar on Right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Zainotes",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.6).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                val searchPillInteraction = remember { MutableInteractionSource() }
                                Surface(
                                    onClick = viewModel::toggleSearchActive,
                                    interactionSource = searchPillInteraction,
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .height(42.dp)
                                        .expressivePressable(searchPillInteraction, pressedScale = 0.93f)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search notes",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Search...",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { insets ->
        val gridSpacing = if (settings.compactCardSpacing) 8.dp else 12.dp
        val columnsConfig = if (settings.gridColumns == 1) {
            StaggeredGridCells.Fixed(1)
        } else {
            StaggeredGridCells.Fixed(2)
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(insets),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Everything scrolls smoothly together inside LazyVerticalStaggeredGrid!
            LazyVerticalStaggeredGrid(
                columns = columnsConfig,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = 114.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                verticalItemSpacing = gridSpacing,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(insets)
            ) {
                // Full-line Scrollable Header: "Your expressive canvas" + "All | N" Split Button + Tag Filter Chips
                item(
                    key = "home_scrollable_header",
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .staggeredEntrance(index = 0, initialOffsetY = 24f)
                    ) {
                        Text(
                            text = "Your expressive canvas",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                ExpressiveSplitButton(
                                    selected = state.selectedTag == null,
                                    menuExpanded = showSplitMenu,
                                    onLeadingClick = {
                                        viewModel.onFilterChanged(HomeFilter.ALL)
                                        viewModel.onSelectTag(null)
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

                            state.allTags.forEach { tag ->
                                HomeTagFilterChip(
                                    tag = tag,
                                    count = state.tagCounts[tag] ?: 0,
                                    selected = state.selectedTag?.equals(tag, ignoreCase = true) == true,
                                    onClick = { viewModel.onSelectTag(tag) }
                                )
                            }
                        }
                    }
                }

                if (state.notes.isEmpty()) {
                    item(
                        key = "home_empty_state",
                        span = StaggeredGridItemSpan.FullLine
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 64.dp)
                                .staggeredEntrance(index = 1),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val isFiltered = state.searchQuery.isNotBlank() || state.selectedTag != null
                            Icon(
                                imageVector = if (isFiltered) Icons.Default.Search else Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = if (isFiltered) "No matching notes" else "Start with one thought",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isFiltered) {
                                    "Try another search keyword or clear the active tag filter."
                                } else {
                                    "Create notes, checklists, tables, and bulletin cards with vibrant colors and illustrated scenes."
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
                                        viewModel.onSelectTag(null)
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
                    }
                } else {
                    itemsIndexed(
                        items = state.notes,
                        key = { _, note -> note.id },
                        span = { _, note ->
                            if (settings.gridColumns == 1 ||
                                note.cardSize == CardSize.WIDE ||
                                note.cardSize == CardSize.HERO
                            ) {
                                StaggeredGridItemSpan.FullLine
                            } else {
                                StaggeredGridItemSpan.SingleLane
                            }
                        }
                    ) { index, note ->
                        // Free-form mode is always on by default
                        val effectiveNote = if (note.cardSize == CardSize.STANDARD) {
                            note.copy(cardSize = CardSize.FREEFORM)
                        } else {
                            note
                        }

                        NoteCard(
                            note = effectiveNote,
                            availableTags = state.allTags,
                            settings = settings,
                            onClick = { onNoteClick(note.id) },
                            onTaskToggle = { blockId, checked ->
                                viewModel.toggleTaskChecked(note.id, blockId, checked)
                            },
                            onCardSizeChange = { viewModel.updateCardSize(note.id, it) },
                            onColorChange = { viewModel.updateColor(note.id, it) },
                            onBackgroundThemeChange = { viewModel.updateBackgroundTheme(note.id, it) },
                            onTagClick = { clickedTag -> viewModel.onSelectTag(clickedTag) },
                            onUpdateTags = { newTags -> viewModel.updateNoteTags(note.id, newTags) },
                            onDuplicate = { viewModel.duplicateNote(note) },
                            onTogglePin = { viewModel.togglePin(note) },
                            onDelete = { viewModel.deleteNote(note) },
                            modifier = Modifier
                                .animateItem(
                                    fadeInSpec = ExpressiveMotion.snappySpec(),
                                    placementSpec = ExpressiveMotion.spatialDefaultSpec(),
                                    fadeOutSpec = ExpressiveMotion.snappySpec()
                                )
                                .staggeredEntrance(index = index + 1)
                        )
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

