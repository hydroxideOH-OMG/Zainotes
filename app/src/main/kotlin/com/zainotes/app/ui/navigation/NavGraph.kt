/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */
package com.zainotes.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.zainotes.app.data.backup.BackupManager
import com.zainotes.app.data.preferences.AppSettingsSnapshot
import com.zainotes.app.data.preferences.SettingsRepository
import com.zainotes.app.data.repository.NotesRepository
import com.zainotes.app.ui.components.DockDestination
import com.zainotes.app.ui.components.FloatingNavDock
import com.zainotes.app.ui.screens.aichat.AiChatScreen
import com.zainotes.app.ui.screens.editor.EditorScreen
import com.zainotes.app.ui.screens.editor.EditorViewModel
import com.zainotes.app.ui.screens.home.HomeScreen
import com.zainotes.app.ui.screens.home.HomeViewModel
import com.zainotes.app.ui.screens.settings.SettingsScreen
import com.zainotes.app.ui.screens.settings.SettingsViewModel
import com.zainotes.app.ui.theme.ExpressiveMotion

private fun routeTabOrder(route: String?): Int = when (route) {
    Screen.Home.route -> 0
    Screen.AiChat.route -> 1
    Screen.Settings.route -> 2
    else -> -1
}

@Composable
fun ZainotesNavGraph(
    navController: NavHostController,
    notesRepository: NotesRepository,
    settingsRepository: SettingsRepository,
    backupManager: BackupManager,
    modifier: Modifier = Modifier
) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val isEditor = route?.startsWith("editor/") == true

    val settings by settingsRepository.settingsFlow.collectAsState(initial = AppSettingsSnapshot())

    val currentDockDestination = when (route) {
        Screen.AiChat.route -> DockDestination.AI_CHAT
        Screen.Settings.route -> DockDestination.SETTINGS
        else -> DockDestination.HOME
    }

    var isBottomDockCollapsed by remember { mutableStateOf(false) }
    var accumulatedScrollDelta by remember { mutableFloatStateOf(0f) }
    var focusApiKeyInSettings by remember { mutableStateOf(false) }

    // Reset collapsed dock state whenever route changes
    LaunchedEffect(route) {
        isBottomDockCollapsed = false
        accumulatedScrollDelta = 0f
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy < -1f) {
                    // User scrolling down (finger moving up)
                    accumulatedScrollDelta = (accumulatedScrollDelta + dy).coerceAtMost(0f)
                    if (accumulatedScrollDelta < -42f) {
                        isBottomDockCollapsed = true
                        accumulatedScrollDelta = 0f
                    }
                } else if (dy > 1f) {
                    // User scrolling up (finger moving down)
                    accumulatedScrollDelta = (accumulatedScrollDelta + dy).coerceAtLeast(0f)
                    if (accumulatedScrollDelta > 24f) {
                        isBottomDockCollapsed = false
                        accumulatedScrollDelta = 0f
                    }
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    val fromOrder = routeTabOrder(initialState.destination.route)
                    if (fromOrder > 0) {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = ExpressiveMotion.spatialHeroSpec()
                        ) + scaleIn(
                            initialScale = 0.92f,
                            animationSpec = ExpressiveMotion.spatialHeroSpec()
                        ) + fadeIn(tween(ExpressiveMotion.DurationShort))
                    } else {
                        fadeIn(tween(ExpressiveMotion.DurationShort)) +
                            scaleIn(initialScale = 0.92f, animationSpec = ExpressiveMotion.spatialHeroSpec())
                    }
                },
                exitTransition = {
                    val toOrder = routeTabOrder(targetState.destination.route)
                    if (toOrder > 0) {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = ExpressiveMotion.spatialHeroSpec()
                        ) + scaleOut(
                            targetScale = 0.92f,
                            animationSpec = ExpressiveMotion.spatialHeroSpec()
                        ) + fadeOut(tween(ExpressiveMotion.DurationShort))
                    } else {
                        fadeOut(tween(ExpressiveMotion.DurationShort)) +
                            scaleOut(targetScale = 0.92f, animationSpec = ExpressiveMotion.spatialHeroSpec())
                    }
                }
            ) {
                val homeViewModel: HomeViewModel = viewModel(
                    key = "home_vm",
                    initializer = { HomeViewModel(notesRepository) }
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onNoteClick = { navController.navigate(Screen.Editor.createRoute(it)) },
                    onNewNoteClick = { navController.navigate(Screen.Editor.createRoute()) },
                    onNavigateToSettings = { navController.navigateTopLevel(Screen.Settings.route) },
                    settings = settings
                )
            }

            composable(
                route = Screen.AiChat.route,
                enterTransition = {
                    val fromOrder = routeTabOrder(initialState.destination.route)
                    val direction = if (fromOrder < 1) {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.End
                    }
                    slideIntoContainer(
                        towards = direction,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + fadeIn(tween(ExpressiveMotion.DurationShort))
                },
                exitTransition = {
                    val toOrder = routeTabOrder(targetState.destination.route)
                    val direction = if (toOrder > 1) {
                        AnimatedContentTransitionScope.SlideDirection.Start
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.End
                    }
                    slideOutOfContainer(
                        towards = direction,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + scaleOut(
                        targetScale = 0.92f,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + fadeOut(tween(ExpressiveMotion.DurationShort))
                }
            ) {
                AiChatScreen(
                    notesRepository = notesRepository,
                    settingsRepository = settingsRepository,
                    onOpenNote = { noteId ->
                        navController.navigate(Screen.Editor.createRoute(noteId))
                    },
                    onNavigateToSettings = {
                        focusApiKeyInSettings = true
                        navController.navigateTopLevel(Screen.Settings.route)
                    }
                )
            }

            composable(
                route = Screen.Settings.route,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + fadeIn(tween(ExpressiveMotion.DurationShort))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + scaleOut(
                        targetScale = 0.92f,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + fadeOut(tween(ExpressiveMotion.DurationShort))
                }
            ) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    key = "settings_vm",
                    initializer = { SettingsViewModel(settingsRepository, backupManager, notesRepository) }
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.navigateTopLevel(Screen.Home.route) },
                    focusApiKeyOnOpen = focusApiKeyInSettings,
                    onApiKeyFocusConsumed = { focusApiKeyInSettings = false }
                )
            }

            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType; defaultValue = 0L }),
                enterTransition = {
                    scaleIn(
                        initialScale = 0.86f,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = ExpressiveMotion.spatialHeroSpec()
                    ) + fadeIn(tween(ExpressiveMotion.DurationShort))
                },
                exitTransition = {
                    scaleOut(
                        targetScale = 0.88f,
                        animationSpec = ExpressiveMotion.spatialFastSpec()
                    ) + slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = ExpressiveMotion.spatialFastSpec()
                    ) + fadeOut(tween(ExpressiveMotion.DurationShort))
                }
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                val editorViewModel: EditorViewModel = viewModel(
                    key = "editor_vm_${backStackEntry.id}_$noteId",
                    initializer = { EditorViewModel(notesRepository, noteId, settingsRepository) }
                )
                EditorScreen(
                    viewModel = editorViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

        // Floating Bottom Navigation Dock (Pill + Standalone Circular/Morphing + FAB)
        AnimatedVisibility(
            visible = !isEditor && !isImeVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = ExpressiveMotion.spatialHeroSpec()
            ) + scaleIn(
                initialScale = 0.85f,
                animationSpec = ExpressiveMotion.spatialHeroSpec()
            ) + fadeIn(tween(ExpressiveMotion.DurationShort)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = ExpressiveMotion.spatialFastSpec()
            ) + scaleOut(
                targetScale = 0.85f,
                animationSpec = ExpressiveMotion.spatialFastSpec()
            ) + fadeOut(tween(ExpressiveMotion.DurationShort)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            FloatingNavDock(
                currentDestination = currentDockDestination,
                isCollapsed = isBottomDockCollapsed,
                onDestinationSelected = { destination ->
                    val targetRoute = when (destination) {
                        DockDestination.HOME -> Screen.Home.route
                        DockDestination.AI_CHAT -> Screen.AiChat.route
                        DockDestination.SETTINGS -> Screen.Settings.route
                    }
                    if (route != targetRoute) {
                        navController.navigateTopLevel(targetRoute)
                    }
                },
                onAddNote = {
                    navController.navigate(Screen.Editor.createRoute())
                }
            )
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(Screen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
