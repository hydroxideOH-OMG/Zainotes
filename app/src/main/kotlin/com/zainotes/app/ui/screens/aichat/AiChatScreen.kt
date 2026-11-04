/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.screens.aichat

import android.text.format.DateUtils
import android.util.Base64
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.ai.AiAttachedImage
import com.zainotes.app.data.ai.AiChatMessage
import com.zainotes.app.data.ai.AiChatSession
import com.zainotes.app.data.ai.AiMemoryItem
import com.zainotes.app.data.ai.AiProposedNoteAction
import com.zainotes.app.data.ai.AiProvider
import com.zainotes.app.data.ai.AiProviderClient
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.Note
import com.zainotes.app.data.model.getNumberedIndex
import com.zainotes.app.data.preferences.AppSettingsSnapshot
import com.zainotes.app.data.preferences.SettingsRepository
import com.zainotes.app.data.repository.NotesRepository
import com.zainotes.app.ui.components.ExpressiveAiThinkingCard
import com.zainotes.app.ui.components.ExpressiveMorphingIndicator
import com.zainotes.app.ui.components.ExpressiveWaveProgressBar
import com.zainotes.app.ui.theme.ExpressiveMotion
import com.zainotes.app.ui.theme.expressivePressable
import com.zainotes.app.ui.theme.staggeredEntrance
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    notesRepository: NotesRepository? = null,
    settingsRepository: SettingsRepository? = null,
    onOpenNote: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeNotes by (notesRepository?.activeNotes?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) })
    val settings by (settingsRepository?.settingsFlow?.collectAsState(initial = AppSettingsSnapshot())
        ?: remember { mutableStateOf(AppSettingsSnapshot()) })

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var promptInput by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var streamingReplyText by remember { mutableStateOf("") }
    var streamingPhase by remember { mutableStateOf("") }
    var showMemoryDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showMissingApiKeyDialog by remember { mutableStateOf(false) }
    var showModelSelectorDialog by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    val attachedNotes = remember { mutableStateListOf<Note>() }
    val attachedImages = remember { mutableStateListOf<AiAttachedImage>() }
    val messages = remember { mutableStateListOf<AiChatMessage>() }

    var currentSessionId by remember { mutableStateOf("") }
    var hasInitializedSession by remember { mutableStateOf(false) }

    val memories = settings.aiMemories
    val chatSessions = settings.aiChatSessions

    // Load active session or most recent session when DataStore emits
    LaunchedEffect(settings.aiChatSessions, settings.activeAiChatSessionId) {
        if (!hasInitializedSession) {
            val preferred = settings.aiChatSessions.find { it.id == settings.activeAiChatSessionId }
                ?: settings.aiChatSessions.firstOrNull()
            if (preferred != null) {
                currentSessionId = preferred.id
                messages.clear()
                messages.addAll(preferred.messages)
                hasInitializedSession = true
            } else if (currentSessionId.isBlank()) {
                currentSessionId = UUID.randomUUID().toString()
            }
        }
    }

    fun persistSessionState(
        updatedMessages: List<AiChatMessage>,
        aiSuggestedTitle: String? = null
    ) {
        if (settingsRepository == null) return
        val sessionId = currentSessionId.ifBlank {
            UUID.randomUUID().toString().also { currentSessionId = it }
        }
        val existing = chatSessions.find { it.id == sessionId }
        val firstUserPrompt = updatedMessages.firstOrNull { it.role == "user" }?.text.orEmpty().trim()
        val firstProposedNoteTitle = updatedMessages
            .flatMap { it.proposedNotes }
            .firstOrNull()?.title

        val computedTitle = when {
            existing != null && existing.isCustomTitle && existing.title.isNotBlank() -> {
                existing.title
            }
            !aiSuggestedTitle.isNullOrBlank() -> {
                AiProviderClient.generateSmartSessionTitle(
                    userPrompt = firstUserPrompt,
                    proposedNoteTitle = firstProposedNoteTitle,
                    aiSuggestedTitle = aiSuggestedTitle
                )
            }
            existing != null && existing.title.isNotBlank() &&
                existing.title != "New Chat" &&
                !existing.title.endsWith("…") &&
                existing.title != firstUserPrompt -> {
                existing.title
            }
            else -> {
                AiProviderClient.generateSmartSessionTitle(
                    userPrompt = firstUserPrompt,
                    proposedNoteTitle = firstProposedNoteTitle
                )
            }
        }

        val now = System.currentTimeMillis()
        val updatedSession = AiChatSession(
            id = sessionId,
            title = computedTitle,
            isCustomTitle = existing?.isCustomTitle ?: false,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            messages = updatedMessages.toList()
        )
        val nextSessions = listOf(updatedSession) + chatSessions.filterNot { it.id == sessionId }
        coroutineScope.launch {
            settingsRepository.setAiChatSessions(nextSessions.take(40), activeSessionId = sessionId)
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        val cleanTitle = newTitle.trim()
        if (cleanTitle.isEmpty() || settingsRepository == null) return
        val nextSessions = chatSessions.map { s ->
            if (s.id == sessionId) {
                s.copy(
                    title = cleanTitle,
                    isCustomTitle = true,
                    updatedAt = System.currentTimeMillis()
                )
            } else s
        }
        coroutineScope.launch {
            settingsRepository.setAiChatSessions(nextSessions, activeSessionId = currentSessionId)
        }
    }

    fun startNewChatSession() {
        if (isThinking) return
        val newId = UUID.randomUUID().toString()
        currentSessionId = newId
        hasInitializedSession = true
        messages.clear()
        promptInput = ""
        attachedNotes.clear()
        attachedImages.clear()
        if (settingsRepository != null) {
            coroutineScope.launch {
                settingsRepository.setActiveAiChatSessionId(newId)
            }
        }
    }

    fun switchToSession(session: AiChatSession) {
        if (isThinking) return
        currentSessionId = session.id
        hasInitializedSession = true
        messages.clear()
        messages.addAll(session.messages)
        promptInput = ""
        attachedNotes.clear()
        attachedImages.clear()
        if (settingsRepository != null) {
            coroutineScope.launch {
                settingsRepository.setActiveAiChatSessionId(session.id)
            }
        }
    }

    fun deleteSession(sessionId: String) {
        val remaining = chatSessions.filterNot { it.id == sessionId }
        if (sessionId == currentSessionId) {
            val nextActive = remaining.firstOrNull()
            if (nextActive != null) {
                currentSessionId = nextActive.id
                messages.clear()
                messages.addAll(nextActive.messages)
            } else {
                currentSessionId = UUID.randomUUID().toString()
                messages.clear()
            }
        }
        if (settingsRepository != null) {
            coroutineScope.launch {
                settingsRepository.setAiChatSessions(remaining, activeSessionId = currentSessionId)
            }
        }
    }

    // Track IME (soft keyboard) height so the composer bar rises smoothly above the keyboard
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val navBottomPx = WindowInsets.navigationBars.getBottom(density)
    val isImeOpen = imeBottomPx > 0
    val targetBottomPadding = with(density) {
        if (isImeOpen) {
            imeBottomPx.toDp() + 6.dp
        } else {
            navBottomPx.toDp() + 92.dp
        }
    }

    // Automatically scroll to the bottom of the conversation when keyboard opens or tokens stream in
    val streamScrollBucket = streamingReplyText.length / 32
    LaunchedEffect(isImeOpen, messages.size, isThinking, streamScrollBucket) {
        if (messages.isNotEmpty()) {
            val targetIdx = if (isThinking) messages.size else messages.lastIndex
            runCatching { listState.animateScrollToItem(targetIdx.coerceAtLeast(0)) }
        }
    }

    // System image picker for the '+' attachment menu
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                val base64 = if (bytes != null && bytes.size <= 3_500_000) {
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                } else ""
                val label = uri.lastPathSegment?.substringAfterLast('/')?.take(22) ?: "Image"
                attachedImages.add(
                    AiAttachedImage(
                        label = label,
                        mimeType = mimeType,
                        base64Data = base64
                    )
                )
            } catch (_: Exception) {
                attachedImages.add(
                    AiAttachedImage(
                        label = "Attached photo.jpg",
                        mimeType = "image/jpeg"
                    )
                )
            }
        }
    }

    fun sendChatMessage(rawPrompt: String) {
        val cleanPrompt = rawPrompt.trim()
        if ((cleanPrompt.isEmpty() && attachedNotes.isEmpty() && attachedImages.isEmpty()) || isThinking) return

        val isLocalCustom = settings.activeAiProvider == AiProvider.CUSTOM &&
            AiProviderClient.isLocalEndpoint(settings.aiCustomBaseUrl)

        // Enforce live API key check (unless using a local custom endpoint on 127.0.0.1 / localhost)
        if (settings.activeApiKey.isBlank() && !isLocalCustom) {
            showMissingApiKeyDialog = true
            return
        }

        hasInitializedSession = true
        if (currentSessionId.isBlank()) {
            currentSessionId = UUID.randomUUID().toString()
        }

        val currentAttachedNotes = attachedNotes.toList()
        val currentAttachedImages = attachedImages.toList()
        val finalPromptText = cleanPrompt.ifBlank {
            if (currentAttachedImages.isNotEmpty()) "Analyze the attached image and help me with my notes."
            else "Review the attached note."
        }

        val userMsg = AiChatMessage(
            role = "user",
            text = finalPromptText,
            attachedNoteTitles = currentAttachedNotes.map { it.title.ifBlank { "Untitled Note" } },
            attachedImages = currentAttachedImages
        )
        messages.add(userMsg)
        persistSessionState(messages)
        promptInput = ""
        attachedNotes.clear()
        attachedImages.clear()
        streamingReplyText = ""
        streamingPhase = "Connecting to stream..."
        isThinking = true

        coroutineScope.launch {
            val response = AiProviderClient.runAgentChatTurn(
                provider = settings.activeAiProvider,
                apiKey = settings.activeApiKey,
                modelName = settings.activeModelName,
                customBaseUrl = settings.aiCustomBaseUrl,
                userPrompt = finalPromptText,
                chatHistory = messages.dropLast(1),
                memories = memories,
                activeNotes = activeNotes,
                attachedNotes = currentAttachedNotes,
                attachedImages = currentAttachedImages,
                onStreamUpdate = { streamUpdate ->
                    coroutineScope.launch(Dispatchers.Main.immediate) {
                        streamingReplyText = streamUpdate.partialReplyText
                        streamingPhase = streamUpdate.statusPhase
                    }
                }
            )

            if (response.updatedMemories != memories && settingsRepository != null) {
                settingsRepository.setAiMemories(response.updatedMemories)
            }

            messages.add(
                AiChatMessage(
                    role = "assistant",
                    text = response.replyText,
                    toolEvents = response.toolEvents,
                    proposedNotes = response.proposedNotes,
                    providerBadge = response.providerBadge
                )
            )
            persistSessionState(
                updatedMessages = messages,
                aiSuggestedTitle = response.suggestedSessionTitle
            )
            streamingReplyText = ""
            streamingPhase = ""
            isThinking = false
        }
    }

    // Display whatever AI model name is currently in use
    val activeModelDisplayName = settings.activeModelName.ifBlank {
        settings.activeAiProvider.defaultModel
    }

    // Concise, human-friendly one-line starter chips at the top
    val starterChips = listOf(
        "Summarize my notes" to "Summarize my notes with a quick overview",
        "List open tasks" to "Find and list all open tasks from my notes",
        "Draft a checklist" to "Draft a clean daily checklist note",
        "Save a memory" to "Remember that I like concise bullet points"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Top Bar Row: Left Active Model Selector & Right New Chat + History + Memory ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Model Selector Pill named with whatever AI model name is used
                    Surface(
                        onClick = { showModelSelectorDialog = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .expressivePressable(pressedScale = 0.95f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = activeModelDisplayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Select AI model",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // New Chat Session Button
                        FilledTonalIconButton(
                            onClick = { startNewChatSession() },
                            modifier = Modifier
                                .size(38.dp)
                                .expressivePressable(pressedScale = 0.9f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddComment,
                                contentDescription = "New Chat Session",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Chat History (Sessions) Button
                        Surface(
                            onClick = { showHistoryDialog = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.expressivePressable(pressedScale = 0.93f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Chat History",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                if (chatSessions.isNotEmpty()) {
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${chatSessions.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Right Memory Pill Button in Material 3 Expressive style
                        Surface(
                            onClick = { showMemoryDialog = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.expressivePressable(pressedScale = 0.93f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = if (memories.isEmpty()) "Memory" else "Memory (${memories.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // ── Concise One-Line Starter Options at the Top (Hidden while keyboard is open & chatting to save vertical room) ──
                AnimatedVisibility(
                    visible = !isImeOpen || messages.isEmpty(),
                    enter = expandVertically(ExpressiveMotion.spatialFastSpec()) + fadeIn(),
                    exit = shrinkVertically(ExpressiveMotion.spatialFastSpec()) + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        starterChips.forEachIndexed { idx, (shortLabel, fullPrompt) ->
                            Surface(
                                onClick = { sendChatMessage(fullPrompt) },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                ),
                                modifier = Modifier
                                    .staggeredEntrance(index = idx)
                                    .expressivePressable(pressedScale = 0.94f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = shortLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = targetBottomPadding)
        ) {
            // ── Center Conversation Area ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !isThinking) {
                    // Material 3 Expressive Empty State Card
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .staggeredEntrance(index = 1),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(if (isImeOpen) 16.dp else 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(if (isImeOpen) 44.dp else 56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(if (isImeOpen) 22.dp else 28.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Ask anything or draft notes",
                                    style = if (isImeOpen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "Ask questions, save facts to persistent memory, or draft and edit notes with tasks, bullets, and tables.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(
                            items = messages,
                            key = { _, m -> m.id }
                        ) { msgIdx, msg ->
                            val isUser = msg.role == "user"
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(
                                        fadeInSpec = ExpressiveMotion.spatialFastSpec(),
                                        placementSpec = ExpressiveMotion.spatialDefaultSpec(),
                                        fadeOutSpec = ExpressiveMotion.spatialFastSpec()
                                    )
                                    .staggeredEntrance(index = msgIdx),
                                contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 26.dp,
                                        topEnd = 26.dp,
                                        bottomStart = if (isUser) 26.dp else 8.dp,
                                        bottomEnd = if (isUser) 8.dp else 26.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerLow
                                        }
                                    ),
                                    border = if (!isUser) {
                                        androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                        )
                                    } else null,
                                    modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 0.96f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Attached notes / images pills on user message
                                        if (msg.attachedNoteTitles.isNotEmpty() || msg.attachedImages.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState())
                                                    .padding(bottom = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                msg.attachedNoteTitles.forEach { title ->
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Description,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text(
                                                                text = title,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                                msg.attachedImages.forEach { img ->
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Image,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text(
                                                                text = img.label,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        MarkdownMessageContent(
                                            text = msg.text,
                                            textColor = if (isUser) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            accentColor = if (isUser) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        )

                                        // Tool Call Badges
                                        if (msg.toolEvents.isNotEmpty()) {
                                            Spacer(Modifier.height(10.dp))
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                msg.toolEvents.forEach { ev ->
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (ev.toolName.contains("memory")) Icons.Default.Memory else Icons.Default.AutoAwesome,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                            Spacer(Modifier.width(6.dp))
                                                            Text(
                                                                text = ev.summary,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Proposed Note Drafts / Edits Requiring User Approval
                                        if (msg.proposedNotes.isNotEmpty()) {
                                            Spacer(Modifier.height(12.dp))
                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                msg.proposedNotes.forEachIndexed { pIdx, prop ->
                                                    ProposedNoteApprovalCard(
                                                        proposal = prop,
                                                        onApprove = {
                                                            if (notesRepository != null) {
                                                                coroutineScope.launch {
                                                                    val targetId = if (prop.actionType == "EDIT_NOTE" && prop.targetNoteId != null) {
                                                                        prop.targetNoteId
                                                                    } else 0L
                                                                    val savedId = notesRepository.saveNote(
                                                                        Note(
                                                                            id = targetId,
                                                                            title = prop.title,
                                                                            colorId = prop.colorId,
                                                                            backgroundTheme = prop.backgroundTheme,
                                                                            cardSize = CardSize.FREEFORM,
                                                                            isPinned = prop.isPinned,
                                                                            blocks = prop.blocks
                                                                        )
                                                                    )
                                                                    val finalId = if (targetId > 0L) targetId else savedId
                                                                    val updatedList = msg.proposedNotes.toMutableList()
                                                                    updatedList[pIdx] = prop.copy(status = "APPROVED", savedNoteId = finalId)
                                                                    messages[msgIdx] = msg.copy(proposedNotes = updatedList)
                                                                    persistSessionState(messages)
                                                                }
                                                            }
                                                        },
                                                        onDiscard = {
                                                            val updatedList = msg.proposedNotes.toMutableList()
                                                            updatedList[pIdx] = prop.copy(status = "DISCARDED")
                                                            messages[msgIdx] = msg.copy(proposedNotes = updatedList)
                                                            persistSessionState(messages)
                                                        },
                                                        onOpenNote = onOpenNote
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isThinking) {
                            item(key = "m3_expressive_thinking_indicator") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(
                                            fadeInSpec = ExpressiveMotion.spatialFastSpec(),
                                            placementSpec = ExpressiveMotion.spatialDefaultSpec(),
                                            fadeOutSpec = ExpressiveMotion.spatialFastSpec()
                                        ),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (streamingReplyText.isNotBlank()) {
                                        Card(
                                            shape = RoundedCornerShape(
                                                topStart = 26.dp,
                                                topEnd = 26.dp,
                                                bottomStart = 8.dp,
                                                bottomEnd = 26.dp
                                            ),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                            ),
                                            modifier = Modifier.fillMaxWidth(0.96f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        ExpressiveMorphingIndicator(
                                                            size = 22.dp,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                                        )
                                                        Text(
                                                            text = activeModelDisplayName,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                    ) {
                                                        Text(
                                                            text = streamingPhase.ifBlank { "Streaming..." },
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }

                                                MarkdownMessageContent(
                                                    text = streamingReplyText,
                                                    textColor = MaterialTheme.colorScheme.onSurface,
                                                    accentColor = MaterialTheme.colorScheme.primary
                                                )

                                                ExpressiveWaveProgressBar(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                                )
                                            }
                                        }
                                    } else {
                                        ExpressiveAiThinkingCard(
                                            modelDisplayName = activeModelDisplayName,
                                            statusOverride = streamingPhase
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Bottom Material 3 Expressive Composer Card ──
            val hasAttachments = attachedNotes.isNotEmpty() || attachedImages.isNotEmpty()
            val canSend = (promptInput.isNotBlank() || hasAttachments) && !isThinking
            val pillCornerRadius by animateDpAsState(
                targetValue = if (hasAttachments) 24.dp else 32.dp,
                animationSpec = ExpressiveMotion.spatialFastSpec(),
                label = "InputPillRadius"
            )
            val sendButtonScale by animateFloatAsState(
                targetValue = if (canSend || isThinking) 1f else 0.92f,
                animationSpec = ExpressiveMotion.spatialMicroSpec(),
                label = "SendBtnScale"
            )
            val sendButtonColor by animateColorAsState(
                targetValue = if (canSend) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                animationSpec = ExpressiveMotion.colorSpec(),
                label = "SendBtnColor"
            )
            val sendIconColor by animateColorAsState(
                targetValue = if (canSend) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = ExpressiveMotion.colorSpec(),
                label = "SendIconColor"
            )

            Surface(
                shape = RoundedCornerShape(pillCornerRadius),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Removable attachment chips (Notes & Images)
                    AnimatedVisibility(
                        visible = hasAttachments,
                        enter = expandVertically(ExpressiveMotion.spatialFastSpec()) + fadeIn(),
                        exit = shrinkVertically(ExpressiveMotion.spatialFastSpec()) + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            attachedNotes.forEach { note ->
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = note.title.ifBlank { "Untitled Note" },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { attachedNotes.remove(note) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove attached note",
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            attachedImages.forEach { img ->
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = img.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { attachedImages.remove(img) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove attached image",
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left '+' tonal button to add images or attach an existing note
                        Surface(
                            onClick = { showAttachmentSheet = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .size(42.dp)
                                .expressivePressable(pressedScale = 0.88f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add image or attach existing note",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        // Center Material 3 text field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (promptInput.isEmpty()) {
                                Text(
                                    text = "Ask AI or draft a note...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        // Right Material 3 Expressive primary send button (morphs into M3 Expressive Loader while thinking)
                        Surface(
                            onClick = { sendChatMessage(promptInput) },
                            shape = CircleShape,
                            color = sendButtonColor,
                            contentColor = sendIconColor,
                            modifier = Modifier
                                .size(44.dp)
                                .graphicsLayer {
                                    scaleX = sendButtonScale
                                    scaleY = sendButtonScale
                                }
                                .expressivePressable(pressedScale = 0.88f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isThinking) {
                                    ExpressiveMorphingIndicator(
                                        size = 24.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send prompt",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Chat History (Multi-Session) Dialog with Full Session Names & Inline Rename ──
    if (showHistoryDialog) {
        var editingSessionId by remember { mutableStateOf<String?>(null) }
        var editingSessionTitle by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Chat Sessions (${chatSessions.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start a New Chat Session CTA
                    Surface(
                        onClick = {
                            startNewChatSession()
                            showHistoryDialog = false
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .expressivePressable()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddComment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Start New Chat Session",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    HorizontalDivider()

                    if (chatSessions.isEmpty()) {
                        Text(
                            text = "No saved chat sessions yet. Send a message to automatically save your conversation history.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        chatSessions.forEach { session ->
                            val isCurrent = session.id == currentSessionId
                            val isEditingThis = editingSessionId == session.id
                            val lastMsgPreview = session.messages.lastOrNull()?.text
                                .orEmpty()
                                .replace("**", "")
                                .replace("`", "")
                                .replace(Regex("\\s+"), " ")
                                .trim()
                            val relativeTime = DateUtils.getRelativeTimeSpanString(
                                session.updatedAt,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_RELATIVE
                            ).toString()

                            Surface(
                                onClick = {
                                    if (!isEditingThis) {
                                        switchToSession(session)
                                        showHistoryDialog = false
                                    }
                                },
                                shape = RoundedCornerShape(18.dp),
                                color = if (isCurrent) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                border = if (isCurrent) {
                                    androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.primary
                                    )
                                } else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .expressivePressable()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isCurrent) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                                },
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.ChatBubbleOutline,
                                                        contentDescription = null,
                                                        tint = if (isCurrent) {
                                                            MaterialTheme.colorScheme.onPrimary
                                                        } else {
                                                            MaterialTheme.colorScheme.primary
                                                        },
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                // Complete chat session name shown across up to 3 lines without inline badge truncation
                                                Text(
                                                    text = session.title.ifBlank { "New Chat" },
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (lastMsgPreview.isNotBlank()) {
                                                    Text(
                                                        text = lastMsgPreview,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (isCurrent) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = MaterialTheme.colorScheme.primary
                                                        ) {
                                                            Text(
                                                                text = "Active",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onPrimary,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "${session.messages.size} messages · $relativeTime",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (isEditingThis) {
                                                        editingSessionId = null
                                                    } else {
                                                        editingSessionId = session.id
                                                        editingSessionTitle = session.title.ifBlank { "New Chat" }
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Rename chat session",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { deleteSession(session.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete chat session",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Inline Rename Editor when Edit button is tapped
                                    AnimatedVisibility(
                                        visible = isEditingThis,
                                        enter = expandVertically(ExpressiveMotion.spatialFastSpec()) + fadeIn(),
                                        exit = shrinkVertically(ExpressiveMotion.spatialFastSpec()) + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = editingSessionTitle,
                                                onValueChange = { editingSessionTitle = it },
                                                label = { Text("Chat Name") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(
                                                    onClick = { editingSessionId = null }
                                                ) {
                                                    Text("Cancel")
                                                }
                                                Spacer(Modifier.width(6.dp))
                                                FilledTonalButton(
                                                    onClick = {
                                                        val trimmed = editingSessionTitle.trim()
                                                        if (trimmed.isNotBlank()) {
                                                            renameSession(session.id, trimmed)
                                                        }
                                                        editingSessionId = null
                                                    },
                                                    enabled = editingSessionTitle.trim().isNotBlank(),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("Save Name", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ── Missing API Key Dialog -> Deep-links directly to Settings API Key Input ──
    if (showMissingApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showMissingApiKeyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "API Key Required",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "API key is not set. Set an API key in the settings",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        showMissingApiKeyDialog = false
                        onNavigateToSettings()
                    },
                    modifier = Modifier.expressivePressable()
                ) {
                    Text("Go to Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMissingApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── "+" Attachment Dialog: Add Image or Attach Existing Note ──
    if (showAttachmentSheet) {
        AlertDialog(
            onDismissRequest = { showAttachmentSheet = false },
            title = {
                Text(
                    text = "Add to Prompt",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option 1: Add Image from Device Gallery
                    Surface(
                        onClick = {
                            showAttachmentSheet = false
                            imagePickerLauncher.launch("image/*")
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .expressivePressable()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Add Image",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Pick an image or screenshot to include with your prompt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Option 2: Attach an Existing Note
                    Text(
                        text = "Attach an Existing Note (${activeNotes.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (activeNotes.isEmpty()) {
                        Text(
                            text = "No notes available to attach yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        activeNotes.forEach { note ->
                            val isAlreadyAttached = attachedNotes.any { it.id == note.id }
                            Surface(
                                onClick = {
                                    if (!isAlreadyAttached) {
                                        attachedNotes.add(note)
                                    }
                                    showAttachmentSheet = false
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isAlreadyAttached) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .expressivePressable()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = note.title.ifBlank { "Untitled Note" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${note.blocks.size} blocks",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isAlreadyAttached) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Attached",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentSheet = false }) {
                    Text("Close")
                }
            }
        )
    }

    // ── Model & Provider Selector Dialog (Shows Fetched/Available Models + Custom Model Input) ──
    if (showModelSelectorDialog) {
        var customModelDraft by remember(settings.activeModelName) {
            mutableStateOf(activeModelDisplayName)
        }
        val candidateModels = settings.activeAvailableModels.ifEmpty {
            settings.activeAiProvider.suggestedModels
        }
        AlertDialog(
            onDismissRequest = { showModelSelectorDialog = false },
            title = {
                Text(
                    text = "Select AI Model",
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
                        text = "Provider: ${settings.activeAiProvider.displayName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = customModelDraft,
                        onValueChange = { customModelDraft = it },
                        label = { Text("Active or Custom Model Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    candidateModels.forEach { candidateModel ->
                        val isSelected = activeModelDisplayName == candidateModel
                        Surface(
                            onClick = {
                                customModelDraft = candidateModel
                                if (settingsRepository != null) {
                                    coroutineScope.launch {
                                        settingsRepository.setAiSelectedModel(settings.activeAiProvider.id, candidateModel)
                                    }
                                }
                                showModelSelectorDialog = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .expressivePressable()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = candidateModel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            showModelSelectorDialog = false
                            onNavigateToSettings()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Configure API Key & Providers")
                    }
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        val clean = customModelDraft.trim()
                        if (clean.isNotBlank() && settingsRepository != null) {
                            coroutineScope.launch {
                                settingsRepository.setAiSelectedModel(settings.activeAiProvider.id, clean)
                            }
                        }
                        showModelSelectorDialog = false
                    }
                ) {
                    Text("Use Model")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModelSelectorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Persistent Memory Inspector & Editor Dialog ──
    if (showMemoryDialog) {
        var newMemKey by remember { mutableStateOf("") }
        var newMemContent by remember { mutableStateOf("") }
        var editingMemoryId by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showMemoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Persistent Memory (${memories.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Saved facts and preferences that the AI remembers across chats and note drafts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newMemKey,
                        onValueChange = { newMemKey = it },
                        label = { Text(if (editingMemoryId != null) "Edit Topic" else "Topic") },
                        placeholder = { Text("e.g., Work Schedule") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMemContent,
                        onValueChange = { newMemContent = it },
                        label = { Text("Details") },
                        placeholder = { Text("What should the AI remember?") },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (editingMemoryId != null) {
                            TextButton(
                                onClick = {
                                    editingMemoryId = null
                                    newMemKey = ""
                                    newMemContent = ""
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                        FilledTonalButton(
                            onClick = {
                                val k = newMemKey.trim()
                                val c = newMemContent.trim()
                                if (k.isNotBlank() && c.isNotBlank() && settingsRepository != null) {
                                    coroutineScope.launch {
                                        val next = memories.toMutableList()
                                        val targetId = editingMemoryId
                                        if (targetId != null) {
                                            val idx = next.indexOfFirst { it.id == targetId }
                                            if (idx >= 0) {
                                                next[idx] = next[idx].copy(key = k, content = c, updatedAt = System.currentTimeMillis())
                                            }
                                        } else {
                                            next.add(0, AiMemoryItem(key = k, content = c))
                                        }
                                        settingsRepository.setAiMemories(next)
                                        editingMemoryId = null
                                        newMemKey = ""
                                        newMemContent = ""
                                    }
                                }
                            },
                            enabled = newMemKey.isNotBlank() && newMemContent.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (editingMemoryId != null) "Update" else "Add")
                        }
                    }

                    HorizontalDivider()

                    if (memories.isEmpty()) {
                        Text(
                            text = "No memories stored yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        memories.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.key,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = item.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            editingMemoryId = item.id
                                            newMemKey = item.key
                                            newMemContent = item.content
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit memory", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            if (settingsRepository != null) {
                                                coroutineScope.launch {
                                                    settingsRepository.setAiMemories(memories.filterNot { it.id == item.id })
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete memory",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMemoryDialog = false }) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProposedNoteApprovalCard(
    proposal: AiProposedNoteAction,
    onApprove: () -> Unit,
    onDiscard: () -> Unit,
    onOpenNote: (Long) -> Unit
) {
    val isEdit = proposal.actionType == "EDIT_NOTE"
    val cleanSummary = proposal.summary.trim()
    val showSummary = cleanSummary.length > 6 &&
        !cleanSummary.equals("blocks", ignoreCase = true) &&
        !cleanSummary.equals("note", ignoreCase = true) &&
        !cleanSummary.equals(proposal.title.trim(), ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isEdit) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (isEdit) "Proposed Note Edit" else "Proposed Note Draft",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isEdit) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "${proposal.colorId} · ${proposal.backgroundTheme}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = proposal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (showSummary) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = cleanSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                proposal.blocks.forEachIndexed { idx, b ->
                    when (b.type) {
                        BlockType.TABLE -> {
                            val rows = b.resolvedTable()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            ) {
                                rows.forEachIndexed { rIdx, rCells ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (rIdx == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                else Color.Transparent
                                            )
                                    ) {
                                        rCells.forEach { cell ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                    .padding(6.dp)
                                            ) {
                                                Text(
                                                    text = rememberMarkdownAnnotatedString(
                                                        raw = cell,
                                                        textColor = MaterialTheme.colorScheme.onSurface,
                                                        accentColor = MaterialTheme.colorScheme.primary,
                                                        codeBgColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (rIdx == 0) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        BlockType.CODE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = b.content,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                )
                            }
                        }

                        else -> {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (b.type) {
                                    BlockType.TASK -> {
                                        Icon(
                                            imageVector = if (b.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(16.dp)
                                        )
                                    }
                                    BlockType.BULLET -> {
                                        Icon(
                                            imageVector = Icons.Default.FiberManualRecord,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .padding(top = 6.dp)
                                                .size(7.dp)
                                        )
                                    }
                                    BlockType.NUMBERED -> {
                                        Text(
                                            text = "${proposal.blocks.getNumberedIndex(idx)}.",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    else -> {}
                                }
                                Text(
                                    text = rememberMarkdownAnnotatedString(
                                        raw = b.content,
                                        textColor = if (b.type == BlockType.HEADING) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        accentColor = MaterialTheme.colorScheme.primary,
                                        codeBgColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                    ),
                                    style = when (b.type) {
                                        BlockType.HEADING -> MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        BlockType.QUOTE -> MaterialTheme.typography.bodySmall.copy(
                                            fontStyle = FontStyle.Italic
                                        )
                                        else -> MaterialTheme.typography.bodyMedium
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Balanced single-line action row that never wraps into a tall circular blob
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (proposal.status) {
                    "APPROVED" -> {
                        FilledTonalButton(
                            onClick = { proposal.savedNoteId?.let(onOpenNote) },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .expressivePressable()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isEdit) "Edit Applied · Open Note" else "Saved · Open Note",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    "DISCARDED" -> {
                        Text(
                            text = "Proposal Discarded",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        OutlinedButton(
                            onClick = onDiscard,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(0.42f)
                                .height(44.dp)
                                .expressivePressable()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Discard",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        FilledTonalButton(
                            onClick = onApprove,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(0.58f)
                                .height(44.dp)
                                .expressivePressable()
                        ) {
                            Icon(Icons.Default.AddTask, contentDescription = null, Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isEdit) "Apply Edit" else "Add to Notes",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders AI or user message text with rich Markdown support:
 * - Headings (#, ##, ###)
 * - Bullet lists (•, -, *, +) with hanging indent and bold inline spans
 * - Numbered lists (1., 2.) with hanging indent
 * - Task checklists (- [ ], - [x])
 * - Fenced code blocks (```)
 * - Blockquotes (>)
 * - Inline bold (**text**), italic (*text*), inline code (`code`), and strikethrough (~~text~~)
 */
@Composable
private fun MarkdownMessageContent(
    text: String,
    textColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val codeBgColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownUiBlock.Heading -> {
                    val headingStyle = when (block.level) {
                        1 -> MaterialTheme.typography.titleMedium
                        2 -> MaterialTheme.typography.titleSmall
                        else -> MaterialTheme.typography.labelLarge
                    }
                    Text(
                        text = rememberMarkdownAnnotatedString(
                            raw = block.content,
                            textColor = accentColor,
                            accentColor = accentColor,
                            codeBgColor = codeBgColor
                        ),
                        style = headingStyle,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                is MarkdownUiBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            modifier = Modifier
                                .padding(start = (block.indentLevel * 10).dp)
                                .width(18.dp)
                        )
                        Text(
                            text = rememberMarkdownAnnotatedString(
                                raw = block.content,
                                textColor = textColor,
                                accentColor = accentColor,
                                codeBgColor = codeBgColor
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownUiBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.width(26.dp)
                        )
                        Text(
                            text = rememberMarkdownAnnotatedString(
                                raw = block.content,
                                textColor = textColor,
                                accentColor = accentColor,
                                codeBgColor = codeBgColor
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownUiBlock.TaskItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (block.checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(17.dp)
                        )
                        Text(
                            text = rememberMarkdownAnnotatedString(
                                raw = block.content,
                                textColor = textColor,
                                accentColor = accentColor,
                                codeBgColor = codeBgColor
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownUiBlock.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.7f))
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = rememberMarkdownAnnotatedString(
                                raw = block.content,
                                textColor = textColor.copy(alpha = 0.9f),
                                accentColor = accentColor,
                                codeBgColor = codeBgColor
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }
                }

                is MarkdownUiBlock.CodeBlock -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = codeBgColor,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (block.language.isNotBlank()) {
                                Text(
                                    text = block.language.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Text(
                                text = block.code,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                is MarkdownUiBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                is MarkdownUiBlock.Paragraph -> {
                    Text(
                        text = rememberMarkdownAnnotatedString(
                            raw = block.content,
                            textColor = textColor,
                            accentColor = accentColor,
                            codeBgColor = codeBgColor
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }
            }
        }
    }
}

private sealed class MarkdownUiBlock {
    data class Heading(val level: Int, val content: String) : MarkdownUiBlock()
    data class BulletItem(val indentLevel: Int, val content: String) : MarkdownUiBlock()
    data class NumberedItem(val number: String, val content: String) : MarkdownUiBlock()
    data class TaskItem(val checked: Boolean, val content: String) : MarkdownUiBlock()
    data class Quote(val content: String) : MarkdownUiBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownUiBlock()
    data object Divider : MarkdownUiBlock()
    data class Paragraph(val content: String) : MarkdownUiBlock()
}

private fun parseMarkdownBlocks(rawText: String): List<MarkdownUiBlock> {
    if (rawText.isBlank()) return emptyList()
    val result = mutableListOf<MarkdownUiBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val rawLine = lines[i]
        val trimmed = rawLine.trim()

        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // Fenced code block ```
        if (trimmed.startsWith("```")) {
            val lang = trimmed.removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                if (codeBuilder.isNotEmpty()) codeBuilder.append('\n')
                codeBuilder.append(lines[i])
                i++
            }
            result.add(MarkdownUiBlock.CodeBlock(language = lang, code = codeBuilder.toString()))
            i++ // skip closing ```
            continue
        }

        // Horizontal divider
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            result.add(MarkdownUiBlock.Divider)
            i++
            continue
        }

        // Headings (#, ##, ###, ####)
        val headingMatch = Regex("^(#{1,4})\\s+(.+)$").find(trimmed)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val content = headingMatch.groupValues[2].trim()
            result.add(MarkdownUiBlock.Heading(level = level, content = content))
            i++
            continue
        }

        // Task checklist (- [ ] or - [x] or • [ ])
        val taskMatch = Regex("^(?:[-*•+])\\s+\\[([ xX])]\\s+(.+)$").find(trimmed)
        if (taskMatch != null) {
            val checked = taskMatch.groupValues[1].equals("x", ignoreCase = true)
            val content = taskMatch.groupValues[2].trim()
            result.add(MarkdownUiBlock.TaskItem(checked = checked, content = content))
            i++
            continue
        }

        // Bullet list item (•, -, *, +)
        val leadingSpaces = rawLine.takeWhile { it == ' ' || it == '\t' }.length
        val bulletMatch = Regex("^(?:[•\\-*+])\\s+(.+)$").find(trimmed)
        if (bulletMatch != null) {
            val indent = (leadingSpaces / 2).coerceIn(0, 3)
            result.add(MarkdownUiBlock.BulletItem(indentLevel = indent, content = bulletMatch.groupValues[1].trim()))
            i++
            continue
        }

        // Numbered list item (1. or 1))
        val numMatch = Regex("^(\\d+)[.)]\\s+(.+)$").find(trimmed)
        if (numMatch != null) {
            result.add(
                MarkdownUiBlock.NumberedItem(
                    number = numMatch.groupValues[1],
                    content = numMatch.groupValues[2].trim()
                )
            )
            i++
            continue
        }

        // Blockquote (> ...)
        if (trimmed.startsWith(">")) {
            val quoteText = trimmed.removePrefix(">").trim()
            if (quoteText.isNotEmpty()) {
                result.add(MarkdownUiBlock.Quote(content = quoteText))
            }
            i++
            continue
        }

        result.add(MarkdownUiBlock.Paragraph(content = trimmed))
        i++
    }

    return result
}

@Composable
private fun rememberMarkdownAnnotatedString(
    raw: String,
    textColor: Color,
    accentColor: Color,
    codeBgColor: Color
): AnnotatedString {
    return remember(raw, textColor, accentColor, codeBgColor) {
        buildMarkdownAnnotatedString(
            raw = raw,
            textColor = textColor,
            accentColor = accentColor,
            codeBgColor = codeBgColor
        )
    }
}

private fun buildMarkdownAnnotatedString(
    raw: String,
    textColor: Color,
    accentColor: Color,
    codeBgColor: Color
): AnnotatedString = buildAnnotatedString {
    var i = 0
    val len = raw.length

    while (i < len) {
        when {
            // Bold + Italic ***text***
            raw.startsWith("***", i) -> {
                val close = raw.indexOf("***", i + 3)
                if (close > i + 3) {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            color = textColor
                        )
                    ) {
                        append(raw.substring(i + 3, close))
                    }
                    i = close + 3
                } else {
                    // Unclosed *** at the end of live stream
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            color = textColor
                        )
                    ) {
                        append(raw.substring(i + 3))
                    }
                    break
                }
            }

            // Bold **text** or __text__
            raw.startsWith("**", i) || raw.startsWith("__", i) -> {
                val delim = raw.substring(i, i + 2)
                val close = raw.indexOf(delim, i + 2)
                if (close > i + 2) {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    ) {
                        append(raw.substring(i + 2, close))
                    }
                    i = close + 2
                } else {
                    // Unclosed ** during live token streaming -> render bold without raw asterisks
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    ) {
                        append(raw.substring(i + 2))
                    }
                    break
                }
            }

            // Strikethrough ~~text~~
            raw.startsWith("~~", i) -> {
                val close = raw.indexOf("~~", i + 2)
                if (close > i + 2) {
                    withStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough,
                            color = textColor.copy(alpha = 0.75f)
                        )
                    ) {
                        append(raw.substring(i + 2, close))
                    }
                    i = close + 2
                } else {
                    append("~~")
                    i += 2
                }
            }

            // Inline code `code`
            raw[i] == '`' -> {
                val close = raw.indexOf('`', i + 1)
                if (close > i + 1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            background = codeBgColor,
                            color = accentColor
                        )
                    ) {
                        append(" ${raw.substring(i + 1, close)} ")
                    }
                    i = close + 1
                } else {
                    append('`')
                    i++
                }
            }

            // Markdown link [label](url)
            raw[i] == '[' -> {
                val closeBracket = raw.indexOf(']', i + 1)
                val openParen = if (closeBracket > i) raw.indexOf('(', closeBracket + 1) else -1
                val closeParen = if (openParen == closeBracket + 1) raw.indexOf(')', openParen + 1) else -1
                if (closeBracket > i + 1 && openParen == closeBracket + 1 && closeParen > openParen + 1) {
                    val label = raw.substring(i + 1, closeBracket)
                    withStyle(
                        SpanStyle(
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(label)
                    }
                    i = closeParen + 1
                } else {
                    append(raw[i])
                    i++
                }
            }

            // Italic *text* (only when not followed by space)
            raw[i] == '*' && i + 1 < len && raw[i + 1] != ' ' && raw[i + 1] != '*' -> {
                val close = raw.indexOf('*', i + 1)
                if (close > i + 1 && raw[close - 1] != ' ') {
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = textColor
                        )
                    ) {
                        append(raw.substring(i + 1, close))
                    }
                    i = close + 1
                } else {
                    append(raw[i])
                    i++
                }
            }

            else -> {
                append(raw[i])
                i++
            }
        }
    }
}

