/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */
package com.zainotes.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.zainotes.app.data.backup.BackupManager
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

@Composable
fun ZainotesNavGraph(
    navController: NavHostController,
    notesRepository: NotesRepository,
    settingsRepository: SettingsRepository,
    backupManager: BackupManager,
    modifier: Modifier = Modifier
) {
    val entry = navController.currentBackStackEntryAsState().value
    val route = entry?.destination?.route
    val isEditor = route?.startsWith("editor/") == true

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
                enterTransition = { fadeIn(tween(ExpressiveMotion.DurationShort)) },
                exitTransition = { fadeOut(tween(ExpressiveMotion.DurationShort)) }
            ) {
                HomeScreen(
                    viewModel = HomeViewModel(notesRepository),
                    onNoteClick = { navController.navigate(Screen.Editor.createRoute(it)) },
                    onNewNoteClick = { navController.navigate(Screen.Editor.createRoute()) },
                    onNavigateToSettings = { navController.navigateTopLevel(Screen.Settings.route) }
                )
            }
            composable(
                route = Screen.AiChat.route,
                enterTransition = { fadeIn(tween(ExpressiveMotion.DurationShort)) },
                exitTransition = { fadeOut(tween(ExpressiveMotion.DurationShort)) }
            ) {
                AiChatScreen()
            }
            composable(
                route = Screen.Settings.route,
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ExpressiveMotion.DurationMedium)) +
                        fadeIn(tween(ExpressiveMotion.DurationShort))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ExpressiveMotion.DurationMedium)) +
                        fadeOut(tween(ExpressiveMotion.DurationShort))
                }
            ) {
                SettingsScreen(
                    viewModel = SettingsViewModel(settingsRepository, backupManager, notesRepository),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType; defaultValue = 0L }),
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(ExpressiveMotion.DurationMedium)) +
                        fadeIn(tween(ExpressiveMotion.DurationShort))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(ExpressiveMotion.DurationMedium)) +
                        fadeOut(tween(ExpressiveMotion.DurationShort))
                }
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                EditorScreen(
                    viewModel = EditorViewModel(notesRepository, noteId),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Floating Bottom Navigation Dock (Pill + Standalone Circular FAB)
        if (!isEditor) {
            FloatingNavDock(
                currentDestination = currentDockDestination,
                onDestinationSelected = { destination ->
                    when (destination) {
                        DockDestination.HOME -> navController.navigateTopLevel(Screen.Home.route)
                        DockDestination.AI_CHAT -> navController.navigateTopLevel(Screen.AiChat.route)
                        DockDestination.SETTINGS -> navController.navigateTopLevel(Screen.Settings.route)
                    }
                },
                onAddNote = {
                    navController.navigate(Screen.Editor.createRoute())
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
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
