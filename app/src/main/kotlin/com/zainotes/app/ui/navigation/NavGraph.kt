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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Box(modifier = modifier.fillMaxSize()) {
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
                            animationSpec = ExpressiveMotion.spatialFastSpec()
                        ) + fadeIn(tween(ExpressiveMotion.DurationShort))
                    } else {
                        fadeIn(tween(ExpressiveMotion.DurationShort)) +
                            scaleIn(initialScale = 0.96f, animationSpec = ExpressiveMotion.spatialFastSpec())
                    }
                },
                exitTransition = {
                    val toOrder = routeTabOrder(targetState.destination.route)
                    if (toOrder > 0) {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = ExpressiveMotion.spatialFastSpec()
                        ) + fadeOut(tween(ExpressiveMotion.DurationShort))
                    } else {
                        fadeOut(tween(ExpressiveMotion.DurationShort)) +
                            scaleOut(targetScale = 0.96f, animationSpec = ExpressiveMotion.spatialFastSpec())
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
                        animationSpec = ExpressiveMotion.spatialFastSpec()
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
                        animationSpec = ExpressiveMotion.spatialFastSpec()
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
                        navController.navigateTopLevel(Screen.Settings.route)
                    }
                )
            }

            composable(
                route = Screen.Settings.route,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = ExpressiveMotion.spatialFastSpec()
                    ) + fadeIn(tween(ExpressiveMotion.DurationShort))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = ExpressiveMotion.spatialFastSpec()
                    ) + fadeOut(tween(ExpressiveMotion.DurationShort))
                }
            ) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    key = "settings_vm",
                    initializer = { SettingsViewModel(settingsRepository, backupManager, notesRepository) }
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.navigateTopLevel(Screen.Home.route) }
                )
            }

            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType; defaultValue = 0L }),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = ExpressiveMotion.spatialDefaultSpec()
                    ) + fadeIn(tween(ExpressiveMotion.DurationShort))
                },
                exitTransition = {
                    slideOutOfContainer(
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

        // Floating Bottom Navigation Dock (Pill + Standalone Circular FAB)
        AnimatedVisibility(
            visible = !isEditor,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = ExpressiveMotion.spatialDefaultSpec()
            ) + fadeIn(tween(ExpressiveMotion.DurationShort)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = ExpressiveMotion.spatialFastSpec()
            ) + fadeOut(tween(ExpressiveMotion.DurationShort)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            FloatingNavDock(
                currentDestination = currentDockDestination,
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
