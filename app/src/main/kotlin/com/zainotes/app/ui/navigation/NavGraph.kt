/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(ExpressiveMotion.DurationShort)) },
            exitTransition = { fadeOut(animationSpec = tween(ExpressiveMotion.DurationShort)) }
        ) {
            val homeViewModel = HomeViewModel(notesRepository)
            HomeScreen(
                viewModel = homeViewModel,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.Editor.createRoute(noteId))
                },
                onNewNoteClick = {
                    navController.navigate(Screen.Editor.createRoute(0L))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(ExpressiveMotion.DurationMedium)
                ) + fadeIn(animationSpec = tween(ExpressiveMotion.DurationShort))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(ExpressiveMotion.DurationMedium)
                ) + fadeOut(animationSpec = tween(ExpressiveMotion.DurationShort))
            }
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val editorViewModel = EditorViewModel(notesRepository, noteId)
            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ExpressiveMotion.DurationMedium)
                ) + fadeIn(animationSpec = tween(ExpressiveMotion.DurationShort))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ExpressiveMotion.DurationMedium)
                ) + fadeOut(animationSpec = tween(ExpressiveMotion.DurationShort))
            }
        ) {
            val settingsViewModel = SettingsViewModel(settingsRepository, backupManager)
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
