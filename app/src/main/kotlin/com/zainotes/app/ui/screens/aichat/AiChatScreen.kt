/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.screens.aichat

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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zainotes.app.data.ai.AiAttachedImage
import com.zainotes.app.data.ai.AiChatMessage
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
import com.zainotes.app.ui.theme.CardShapeExpressive
import com.zainotes.app.ui.theme.ExpressiveMotion
import com.zainotes.app.ui.theme.expressivePressable
import com.zainotes.app.ui.theme.staggeredEntrance
import kotlinx.coroutines.launch

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
    var showMemoryDialog by remember { mutableStateOf(false) }
    var showMissingApiKeyDialog by remember { mutableStateOf(false) }
    var showModelSelectorDialog by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    val attachedNotes = remember { mutableStateListOf<Note>() }
    val attachedImages = remember { mutableStateListOf<AiAttachedImage>() }
    val messages = remember { mutableStateListOf<AiChatMessage>() }

    val memories = settings.aiMemories

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

        // Enforce live API key check — never run an offline AI chat fallback
        if (settings.activeApiKey.isBlank()) {
            showMissingApiKeyDialog = true
            return
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
        promptInput = ""
        attachedNotes.clear()
        attachedImages.clear()
        isThinking = true

        coroutineScope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
            }
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
                attachedImages = currentAttachedImages
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
            isThinking = false
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
            }
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
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Top Bar Row: Left Active Model Selector & Right Memory Pill ──
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
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = activeModelDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Select AI model",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (messages.isNotEmpty()) {
                            FilledTonalIconButton(
                                onClick = { messages.clear() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .expressivePressable(pressedScale = 0.9f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear chat",
                                    modifier = Modifier.size(20.dp)
                                )
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
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
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

                // ── Concise One-Line Starter Options at the Top ──
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
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 90.dp)
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
                            .padding(horizontal = 20.dp)
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
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Ask anything or draft notes",
                                    style = MaterialTheme.typography.titleLarge,
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
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
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

                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (isUser) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
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
                                                                }
                                                            }
                                                        },
                                                        onDiscard = {
                                                            val updatedList = msg.proposedNotes.toMutableList()
                                                            updatedList[pIdx] = prop.copy(status = "DISCARDED")
                                                            messages[msgIdx] = msg.copy(proposedNotes = updatedList)
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
                            item(key = "thinking_indicator") {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = "Thinking with $activeModelDisplayName...",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                targetValue = if (canSend) 1f else 0.92f,
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
                    .padding(horizontal = 20.dp, vertical = 8.dp)
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

                        // Right Material 3 Expressive primary send button
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
                fontWeight = FontWeight.Bold
            )
            Text(
                text = proposal.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                                                    text = cell,
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
                                    text = b.content,
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

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (proposal.status) {
                    "APPROVED" -> {
                        FilledTonalButton(
                            onClick = { proposal.savedNoteId?.let(onOpenNote) },
                            modifier = Modifier.expressivePressable()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isEdit) "Edit Applied · Open in Editor" else "Saved · Open in Editor")
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
                            modifier = Modifier.expressivePressable()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Discard")
                        }
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = onApprove,
                            modifier = Modifier.expressivePressable()
                        ) {
                            Icon(Icons.Default.AddTask, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isEdit) "Approve & Apply Edit" else "Approve & Add to Notes")
                        }
                    }
                }
            }
        }
    }
}

