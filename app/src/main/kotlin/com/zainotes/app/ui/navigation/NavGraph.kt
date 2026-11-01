/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */
package com.zainotes.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.zainotes.app.data.backup.BackupManager
import com.zainotes.app.data.preferences.SettingsRepository
import com.zainotes.app.data.repository.NotesRepository
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
    val isSettings = route == Screen.Settings.route

    BoxWithConstraints(modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp && !isEditor
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!useRail && !isEditor) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = !isSettings,
                            onClick = { navController.navigateTopLevel(Screen.Home.route) },
                            icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                            label = { Text("Notes") }
                        )
                        NavigationBarItem(
                            selected = isSettings,
                            onClick = { navController.navigateTopLevel(Screen.Settings.route) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        ) { contentPadding ->
            Row(Modifier.fillMaxSize().padding(contentPadding)) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationRailItem(
                            selected = !isSettings,
                            onClick = { navController.navigateTopLevel(Screen.Home.route) },
                            icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                            label = { Text("Notes") },
                            alwaysShowLabel = true
                        )
                        NavigationRailItem(
                            selected = isSettings,
                            onClick = { navController.navigateTopLevel(Screen.Settings.route) },
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Settings") },
                            alwaysShowLabel = true
                        )
                    }
                }
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.weight(1f).fillMaxSize()
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
                        EditorScreen(EditorViewModel(notesRepository, noteId), onNavigateBack = { navController.popBackStack() })
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
                            viewModel = SettingsViewModel(settingsRepository, backupManager),
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
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
