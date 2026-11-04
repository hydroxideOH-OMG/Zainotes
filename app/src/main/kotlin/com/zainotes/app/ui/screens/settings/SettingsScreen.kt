/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zainotes.app.R
import com.zainotes.app.data.ai.AiProvider
import com.zainotes.app.data.model.NoteBackgroundTheme
import com.zainotes.app.ui.components.CardSizePickerSheet
import com.zainotes.app.ui.components.ExpressiveSwitch
import com.zainotes.app.ui.components.NoteBackgroundRenderer
import com.zainotes.app.ui.components.NoteThemePickerSheet
import com.zainotes.app.ui.components.OtaUpdaterSheet
import com.zainotes.app.ui.theme.CardShapeExpressive
import com.zainotes.app.ui.theme.ExpressiveMotion
import com.zainotes.app.ui.theme.NoteColor
import com.zainotes.app.ui.theme.ThemeAccentColor
import com.zainotes.app.ui.theme.ThemeMode
import com.zainotes.app.ui.theme.expressivePressable
import com.zainotes.app.ui.theme.staggeredEntrance
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    focusApiKeyOnOpen: Boolean = false,
    onApiKeyFocusConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val apiKeyFocusRequester = remember { FocusRequester() }

    var apiKeyCardOffsetY by remember { mutableIntStateOf(0) }
    var highlightApiKeyField by remember { mutableStateOf(false) }

    var showSizePicker by remember { mutableStateOf(false) }
    var showDefaultThemePicker by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showOtaUpdaterSheet by remember { mutableStateOf(false) }

    val sizeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val themeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val otaSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.2f

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importBackup(it) }
    }

    LaunchedEffect(state.backupMessage) {
        state.backupMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Deep-link handler: scroll directly to the AI API Key section and highlight + focus the input field
    LaunchedEffect(focusApiKeyOnOpen, apiKeyCardOffsetY) {
        if (focusApiKeyOnOpen && apiKeyCardOffsetY > 0) {
            delay(120)
            scrollState.animateScrollTo(
                value = (apiKeyCardOffsetY - 36).coerceAtLeast(0),
                animationSpec = ExpressiveMotion.spatialHeroSpec()
            )
            highlightApiKeyField = true
            try {
                apiKeyFocusRequester.requestFocus()
            } catch (_: Exception) {}
            onApiKeyFocusConsumed()
            delay(2200)
            highlightApiKeyField = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Personalize how Zainotes looks and works",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 110.dp)
                .widthIn(max = 720.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Look & Feel
            SettingsSectionHeader(title = "Look & Feel")

            Card(
                shape = CardShapeExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(index = 0)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose automatic, daytime light, or battery-saving pure black dark mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Expressive 3-column Theme Selector Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExpressiveThemeModeCard(
                            title = "Auto",
                            subtitle = "Match phone",
                            icon = Icons.Default.BrightnessAuto,
                            selected = state.themeMode == ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        ExpressiveThemeModeCard(
                            title = "Light",
                            subtitle = "Bright & clean",
                            icon = Icons.Default.LightMode,
                            selected = state.themeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ExpressiveThemeModeCard(
                            title = "Dark",
                            subtitle = "Pure black",
                            icon = Icons.Default.NightlightRound,
                            selected = state.themeMode == ThemeMode.AMOLED,
                            onClick = { viewModel.setThemeMode(ThemeMode.AMOLED) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    SettingsSwitchRow(
                        title = "Match Wallpaper Colors",
                        subtitle = if (supportsDynamicColor)
                            "Automatically pick button and highlight colors from your phone's wallpaper"
                        else "Requires Android 12 or newer",
                        checked = state.dynamicColor && supportsDynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) },
                        enabled = supportsDynamicColor
                    )

                    // Custom Theme Accent Color Selector when Dynamic Wallpaper Color is OFF
                    AnimatedVisibility(
                        visible = !state.dynamicColor || !supportsDynamicColor,
                        enter = expandVertically(ExpressiveMotion.spatialFastSpec()) + fadeIn(),
                        exit = shrinkVertically(ExpressiveMotion.spatialFastSpec()) + fadeOut()
                    ) {
                        val selectedAccent = ThemeAccentColor.fromId(state.themeAccentColor)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Accent Color",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Pick your favorite highlight color for buttons, switches, and badges",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Text(
                                        text = selectedAccent.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThemeAccentColor.entries.forEach { accent ->
                                    val isSelected = selectedAccent == accent
                                    val swatchCorner by animateDpAsState(
                                        targetValue = if (isSelected) 16.dp else 24.dp,
                                        animationSpec = ExpressiveMotion.spatialFastSpec(),
                                        label = "AccentSwatchCorner"
                                    )
                                    val swatchScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.06f else 1f,
                                        animationSpec = ExpressiveMotion.spatialMicroSpec(),
                                        label = "AccentSwatchScale"
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .scale(swatchScale)
                                            .expressivePressable(pressedScale = 0.9f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { viewModel.setThemeAccentColor(accent.id) }
                                            .padding(horizontal = 4.dp, vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(swatchCorner))
                                                .background(accent.swatchColor)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.onSurface
                                                    } else {
                                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                    },
                                                    shape = RoundedCornerShape(swatchCorner)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = accent.displayName,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = accent.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Pure Black Note Cards
                    SettingsSwitchRow(
                        title = "Pure Black Note Cards",
                        subtitle = "Make uncolored notes completely black in Dark mode for a sleek, high-contrast look",
                        checked = state.amoledPureBlackCards,
                        onCheckedChange = { viewModel.setAmoledPureBlackCards(it) },
                        enabled = state.themeMode == ThemeMode.AMOLED || state.themeMode == ThemeMode.SYSTEM
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Show Artwork on Note Cards
                    SettingsSwitchRow(
                        title = "Show Artwork on Note Cards",
                        subtitle = "Display subtle themed illustrations (like Music, Travel, or Nature) on your home screen notes",
                        checked = state.showCardBackgrounds,
                        onCheckedChange = { viewModel.setShowCardBackgrounds(it) }
                    )
                }
            }

            // 1B. Smart AI Assistant
            SettingsSectionHeader(title = "Smart AI Assistant")

            val apiCardHighlightScale by animateFloatAsState(
                targetValue = if (highlightApiKeyField) 1.02f else 1.0f,
                animationSpec = ExpressiveMotion.spatialMicroSpec(),
                label = "apiCardHighlightScale"
            )
            val apiCardBorderWidth by animateDpAsState(
                targetValue = if (highlightApiKeyField) 2.5.dp else 0.dp,
                animationSpec = ExpressiveMotion.spatialFastSpec(),
                label = "apiCardBorderWidth"
            )

            Card(
                shape = CardShapeExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = if (highlightApiKeyField) {
                    BorderStroke(apiCardBorderWidth, MaterialTheme.colorScheme.primary)
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        apiKeyCardOffsetY = coords.positionInParent().y.toInt()
                    }
                    .scale(apiCardHighlightScale)
                    .staggeredEntrance(index = 1)
            ) {
                var apiKeyDraft by remember(state.aiProvider, state.activeApiKey) {
                    mutableStateOf(state.activeApiKey)
                }
                var customBaseUrlDraft by remember(state.aiCustomBaseUrl) {
                    mutableStateOf(state.aiCustomBaseUrl)
                }
                var showApiKeyText by remember { mutableStateOf(false) }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI Service & Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pick which AI service you want to chat with, then paste your key to get started.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AiProvider.entries.forEach { provider ->
                            val selected = state.aiProvider == provider
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setAiProvider(provider) },
                                label = {
                                    Text(
                                        text = provider.displayName,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                shape = CircleShape
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.aiProvider == AiProvider.CUSTOM) {
                        OutlinedTextField(
                            value = customBaseUrlDraft,
                            onValueChange = {
                                customBaseUrlDraft = it
                                viewModel.setAiCustomBaseUrl(it)
                            },
                            label = { Text("Server Address (URL)") },
                            placeholder = { Text("http://127.0.0.1:11434/v1 or https://api.openai.com/v1") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = apiKeyDraft,
                        onValueChange = { apiKeyDraft = it },
                        label = { Text("${state.aiProvider.displayName} Key") },
                        placeholder = { Text("Paste your ${state.aiProvider.displayName} key here...") },
                        singleLine = true,
                        visualTransformation = if (showApiKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = if (highlightApiKeyField) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showApiKeyText = !showApiKeyText }) {
                                Icon(
                                    imageVector = if (showApiKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKeyText) "Hide Key" else "Show Key"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (highlightApiKeyField) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(apiKeyFocusRequester)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (state.activeApiKey.isNotBlank()) {
                            "✓ Key saved for ${state.aiProvider.displayName}"
                        } else if (state.aiProvider == AiProvider.CUSTOM) {
                            "Local AI servers on your phone or computer don't need a key"
                        } else {
                            "No key saved yet for ${state.aiProvider.displayName}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (state.activeApiKey.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    val saveBtnInteraction = remember { MutableInteractionSource() }
                    FilledTonalButton(
                        onClick = {
                            viewModel.saveApiKeyAndFetchModels(
                                provider = state.aiProvider,
                                apiKey = apiKeyDraft,
                                customBaseUrl = customBaseUrlDraft
                            )
                        },
                        interactionSource = saveBtnInteraction,
                        enabled = !state.isFetchingModels,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .expressivePressable(saveBtnInteraction, pressedScale = 0.95f)
                    ) {
                        if (state.isFetchingModels) {
                            com.zainotes.app.ui.components.ExpressiveMorphingIndicator(
                                size = 22.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isFetchingModels) "Loading AI Models..." else "Save & Load AI Models",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Active Model Selection Row
                    val modelRowInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .expressivePressable(modelRowInteraction, pressedScale = 0.96f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = modelRowInteraction,
                                indication = androidx.compose.foundation.LocalIndication.current
                            ) { viewModel.openModelSelectorPrompt() }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Model",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Currently using ${state.activeModelName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 2. Writing & Checklists
            SettingsSectionHeader(title = "Writing & Checklists")

            Card(
                shape = CardShapeExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val defaultThemeObj = NoteBackgroundTheme.fromId(state.defaultBackgroundTheme)
                    val defaultColorObj = NoteColor.fromId(state.defaultNoteColor)
                    val previewBg = defaultColorObj.containerColor(
                        isDark = isDark,
                        defaultContainer = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    val previewTint = defaultColorObj.contentColor(
                        isDark = isDark,
                        defaultContent = MaterialTheme.colorScheme.onSurface
                    )

                    // Default Note Color & Background Theme Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showDefaultThemePicker = true }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(previewBg)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            NoteBackgroundRenderer(
                                theme = defaultThemeObj,
                                tint = previewTint,
                                modifier = Modifier.fillMaxSize()
                            )
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Default Style for New Notes",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${defaultColorObj.displayName} color · ${defaultThemeObj.displayName} background",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Default Card Size Picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showSizePicker = true }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ViewCarousel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Default Card Size on Home Screen",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "New notes start as ${state.defaultCardSize.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Change",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Cross Out Finished Tasks
                    SettingsSwitchRow(
                        title = "Cross Out Finished Tasks",
                        subtitle = "Draw a line through checklist items when you check them off",
                        checked = state.strikethroughCompleted,
                        onCheckedChange = { viewModel.setStrikethroughCompleted(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Move Finished Tasks to the Bottom
                    SettingsSwitchRow(
                        title = "Move Finished Tasks to the Bottom",
                        subtitle = "Keep unfinished tasks at the top by moving checked items down automatically",
                        checked = state.moveCheckedToBottom,
                        onCheckedChange = { viewModel.setMoveCheckedToBottom(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Open Keyboard Immediately for New Notes
                    SettingsSwitchRow(
                        title = "Open Keyboard Immediately for New Notes",
                        subtitle = "Start typing right away as soon as you tap the + button",
                        checked = state.autoFocusNewNote,
                        onCheckedChange = { viewModel.setAutoFocusNewNote(it) }
                    )
                }
            }

            // 3. Home Screen Layout
            SettingsSectionHeader(title = "Home Screen Layout")

            Card(
                shape = CardShapeExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(index = 3)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How Notes Are Arranged",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "View your notes side-by-side in two columns or stacked in a single list",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExpressiveGridDensityOption(
                            label = "2 Columns",
                            icon = Icons.Default.GridView,
                            selected = state.gridColumns == 2,
                            isLeft = true,
                            onClick = { viewModel.setGridColumns(2) },
                            modifier = Modifier.weight(1f)
                        )
                        ExpressiveGridDensityOption(
                            label = "Single List",
                            icon = Icons.Default.ViewAgenda,
                            selected = state.gridColumns == 1,
                            isLeft = false,
                            onClick = { viewModel.setGridColumns(1) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Fit More Notes on Screen
                    SettingsSwitchRow(
                        title = "Fit More Notes on Screen",
                        subtitle = "Use smaller gaps between notes so you can see more at once",
                        checked = state.compactCardSpacing,
                        onCheckedChange = { viewModel.setCompactCardSpacing(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Show Task Progress on Notes
                    SettingsSwitchRow(
                        title = "Show Task Progress on Notes",
                        subtitle = "Show a progress bar and how many tasks are done (like 3/5) on checklist notes",
                        checked = state.showTaskProgress,
                        onCheckedChange = { viewModel.setShowTaskProgress(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Show Last Edited Time
                    SettingsSwitchRow(
                        title = "Show Last Edited Time",
                        subtitle = "Display when each note was last updated at the bottom of the card",
                        checked = state.showNoteMetadata,
                        onCheckedChange = { viewModel.setShowNoteMetadata(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Ask Before Deleting a Note
                    SettingsSwitchRow(
                        title = "Ask Before Deleting a Note",
                        subtitle = "Show a confirmation prompt so you never delete a note by accident",
                        checked = state.confirmNoteDelete,
                        onCheckedChange = { viewModel.setConfirmNoteDelete(it) }
                    )
                }
            }

            // 4. Backup & Your Data
            SettingsSectionHeader(title = "Backup & Your Data")

            Card(
                shape = CardShapeExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Friendly Storage Summary Banner
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Stored Privately on Your Phone",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${state.totalNotesCount} notes · ${state.checklistsCount} checklists · ${state.pinnedCount} pinned",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Export
                    SettingsActionRow(
                        icon = Icons.Default.Download,
                        title = "Save a Backup File",
                        subtitle = "Save a copy of all your notes to your phone or cloud drive",
                        onClick = {
                            exportLauncher.launch("zainotes_backup_${System.currentTimeMillis()}.json")
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Import
                    SettingsActionRow(
                        icon = Icons.Default.Upload,
                        title = "Restore from a Backup File",
                        subtitle = "Bring back your notes from a saved backup file",
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "text/plain"))
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Reset Preferences
                    SettingsActionRow(
                        icon = Icons.Default.RestartAlt,
                        title = "Reset App Settings",
                        subtitle = "Change all settings back to default (your notes will stay safe)",
                        onClick = { viewModel.resetSettingsToDefaults() }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Delete All Notes
                    SettingsActionRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete All Notes",
                        subtitle = "Permanently erase every note and checklist from this phone",
                        iconTint = MaterialTheme.colorScheme.error,
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showClearConfirmation = true }
                    )
                }
            }

            // 5. Software Updates & About Zainotes
            SettingsSectionHeader(title = "Software Updates & About Zainotes")

            Card(
                shape = CardShapeExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zainotes v${stringResource(R.string.app_version)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Simple, private notes, checklists, and smart AI assistant",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = "v${stringResource(R.string.app_version)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val otaBtnInteraction = remember { MutableInteractionSource() }
                    FilledTonalButton(
                        onClick = { showOtaUpdaterSheet = true },
                        interactionSource = otaBtnInteraction,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .expressivePressable(otaBtnInteraction, pressedScale = 0.95f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Check for Updates & View Changelogs",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    SettingsSwitchRow(
                        title = "Automatically Check for Updates",
                        subtitle = "Check GitHub for new Zainotes releases when the app opens and notify you with the changelog",
                        checked = state.autoCheckUpdates,
                        onCheckedChange = { viewModel.setAutoCheckUpdates(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Free & Open-Source App",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "No ads, no tracking, and open-source under the AGPLv3 license",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/hydroxideOH-OMG/Zainotes")
                                )
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "View Source Code on GitHub",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "hydroxideOH-OMG/Zainotes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showOtaUpdaterSheet) {
        OtaUpdaterSheet(
            initialCheckResult = null,
            sheetState = otaSheetState,
            onDismissRequest = { showOtaUpdaterSheet = false },
            onDismissReleaseTag = { tag -> viewModel.setDismissedUpdateTag(tag) }
        )
    }

    if (showSizePicker) {
        CardSizePickerSheet(
            selectedSize = state.defaultCardSize,
            onSizeSelected = { selected ->
                viewModel.setDefaultCardSize(selected)
                showSizePicker = false
            },
            sheetState = sizeSheetState,
            onDismissRequest = { showSizePicker = false }
        )
    }

    if (showDefaultThemePicker) {
        NoteThemePickerSheet(
            selectedColorId = state.defaultNoteColor,
            selectedThemeId = state.defaultBackgroundTheme,
            onColorSelected = { viewModel.setDefaultNoteColor(it) },
            onThemeSelected = { viewModel.setDefaultBackgroundTheme(it) },
            sheetState = themeSheetState,
            onDismissRequest = { showDefaultThemePicker = false }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete all notes?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all ${state.totalNotesCount} notes and checklists from your phone. You cannot undo this.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        viewModel.clearAllNotes()
                    }
                ) {
                    Text(
                        text = "Delete Everything",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.promptModelSelector) {
        var customModelDraft by remember(state.activeModelName) {
            mutableStateOf(state.activeModelName)
        }
        AlertDialog(
            onDismissRequest = { viewModel.dismissModelSelectorPrompt() },
            title = {
                Text(
                    text = "Choose AI Model",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pick an AI model from the list below, or type any model name:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = customModelDraft,
                        onValueChange = { customModelDraft = it },
                        label = { Text("Model Name") },
                        placeholder = { Text(state.aiProvider.defaultModel) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Available Models (${state.availableModels.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    state.availableModels.forEach { modelId ->
                        val isSelected = customModelDraft.trim() == modelId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { customModelDraft = modelId }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { customModelDraft = modelId }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = modelId,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        viewModel.selectAiModel(
                            provider = state.aiProvider,
                            modelName = customModelDraft.ifBlank { state.aiProvider.defaultModel }
                        )
                    }
                ) {
                    Text("Save Model", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissModelSelectorPrompt() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
    )
}

@Composable
private fun ExpressiveGridDensityOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    isLeft: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val innerCorner by animateDpAsState(
        targetValue = if (selected) 24.dp else 10.dp,
        animationSpec = ExpressiveMotion.spatialFastSpec(),
        label = "gridDensityInnerCorner"
    )
    val outerCorner = 24.dp

    val shape = if (isLeft) {
        RoundedCornerShape(
            topStart = outerCorner,
            bottomStart = outerCorner,
            topEnd = innerCorner,
            bottomEnd = innerCorner
        )
    } else {
        RoundedCornerShape(
            topStart = innerCorner,
            bottomStart = innerCorner,
            topEnd = outerCorner,
            bottomEnd = outerCorner
        )
    }

    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "gridDensityBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "gridDensityFg"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = if (selected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        },
        modifier = modifier
            .height(48.dp)
            .expressivePressable(interactionSource, pressedScale = 0.94f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.Check else icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpressiveThemeModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cornerRadius by animateDpAsState(
        targetValue = if (selected) 24.dp else 16.dp,
        animationSpec = ExpressiveMotion.spatialFastSpec(),
        label = "themeCardCorner"
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "theme_card_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = ExpressiveMotion.snappySpec(),
        label = "theme_card_fg"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.expressivePressable(interactionSource, pressedScale = 0.93f),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (selected) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.75f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        ExpressiveSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .expressivePressable(interactionSource, pressedScale = 0.96f)
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = iconTint.copy(alpha = 0.12f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
