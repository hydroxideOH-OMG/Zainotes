/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.screens.aichat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.ai.AiChatMessage
import com.zainotes.app.data.ai.AiMemoryItem
import com.zainotes.app.data.ai.AiProposedNoteAction
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
    val activeNotes by (notesRepository?.activeNotes?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) })
    val settings by (settingsRepository?.settingsFlow?.collectAsState(initial = AppSettingsSnapshot())
        ?: remember { mutableStateOf(AppSettingsSnapshot()) })

    val coroutineScope = rememberCoroutineScope()

    var promptInput by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var showMemoryDialog by remember { mutableStateOf(false) }

    val messages = remember { mutableStateListOf<AiChatMessage>() }

    val totalNotes = activeNotes.size
    val openTasksCount = activeNotes.sumOf { it.totalTasksCount - it.completedTasksCount }
    val memories = settings.aiMemories

    fun sendChatMessage(rawPrompt: String) {
        val cleanPrompt = rawPrompt.trim()
        if (cleanPrompt.isEmpty() || isThinking) return

        val userMsg = AiChatMessage(
            role = "user",
            text = cleanPrompt
        )
        messages.add(userMsg)
        promptInput = ""
        isThinking = true

        coroutineScope.launch {
            val response = AiProviderClient.runAgentChatTurn(
                provider = settings.activeAiProvider,
                apiKey = settings.activeApiKey,
                modelName = settings.activeModelName,
                customBaseUrl = settings.aiCustomBaseUrl,
                userPrompt = cleanPrompt,
                chatHistory = messages.dropLast(1),
                memories = memories,
                activeNotes = activeNotes
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
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Smart Canvas Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = { showMemoryDialog = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "Persistent Memory",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Memory (${memories.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configure AI Provider & API Keys"
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Workspace Status & Prompt Composer Card
            Card(
                shape = CardShapeExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            onClick = onNavigateToSettings,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${settings.activeAiProvider.displayName} · ${settings.activeModelName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Text(
                            text = "$totalNotes notes · ${memories.size} memories",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Ask anything, manage persistent memory & draft or edit notes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chat with your AI assistant using tool calling: save facts to persistent memory, draft rich notes (tasks, bullets, 1 2 3, headings, tables, code) for your approval, or edit any existing note.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ask a question, say 'Remember...', or 'Draft/Edit a note...'") },
                        shape = RoundedCornerShape(22.dp),
                        singleLine = false,
                        maxLines = 4,
                        trailingIcon = {
                            if (isThinking) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                IconButton(
                                    onClick = { sendChatMessage(promptInput) },
                                    enabled = promptInput.isNotBlank()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send message",
                                        tint = if (promptInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(10.dp))

                    // Quick Chip Prompts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = { sendChatMessage("Summarize my notes with a comparison table") },
                            label = { Text("Summarize notes") },
                            shape = CircleShape
                        )
                        SuggestionChip(
                            onClick = { sendChatMessage("Extract all open tasks") },
                            label = { Text("Collect open tasks ($openTasksCount)") },
                            shape = CircleShape
                        )
                        SuggestionChip(
                            onClick = { sendChatMessage("Remember that I prefer concise checklists with tables and code snippets") },
                            label = { Text("Save preference to memory") },
                            shape = CircleShape
                        )
                        SuggestionChip(
                            onClick = { sendChatMessage("Edit Welcome to Zainotes and add a table and code block") },
                            label = { Text("Edit existing note") },
                            shape = CircleShape
                        )
                    }
                }
            }

            // Conversational Chat & Tool Approval Feed
            if (messages.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Conversation & Tool Actions",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { messages.clear() }) {
                        Text("Clear Chat")
                    }
                }

                messages.forEachIndexed { msgIdx, msg ->
                    val isUser = msg.role == "user"
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically(ExpressiveMotion.spatialFastSpec()) + fadeIn()
                    ) {
                        Card(
                            shape = CardShapeExpressive,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isUser) "You" else "Zainotes AI Assistant",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                    )
                                    if (!isUser && msg.providerBadge.isNotBlank()) {
                                        Text(
                                            text = msg.providerBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )

                                // Tool Call Badges (e.g. save_memory, update_memory, draft_note, edit_note)
                                if (msg.toolEvents.isNotEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        msg.toolEvents.forEach { ev ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (ev.toolName.contains("memory")) Icons.Default.Memory else Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = "Tool [${ev.toolName}]: ${ev.summary}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
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
            }

            // Section: 1-Tap Smart Actions
            Text(
                text = "One-Tap Canvas Actions",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            PromptSuggestionCard(
                icon = Icons.Default.Summarize,
                title = "Summarize My Workspace ($totalNotes notes)",
                description = "Compile an instant digest with a table & numbered list of all your active notes.",
                onClick = { sendChatMessage("Summarize my notes with a comparison table") }
            )

            PromptSuggestionCard(
                icon = Icons.Default.Checklist,
                title = "Collect Unchecked Tasks ($openTasksCount open)",
                description = "Gather every unfinished checkbox item across all notes into one master checklist.",
                onClick = { sendChatMessage("Extract all open tasks") }
            )

            PromptSuggestionCard(
                icon = Icons.Default.ShoppingBag,
                title = "Generate Grocery & Errand Scene",
                description = "Draft a categorized grocery checklist styled with the Groceries background illustration.",
                onClick = { sendChatMessage("Draft a weekly grocery run note with a table and checklist") }
            )

            PromptSuggestionCard(
                icon = Icons.Default.Lightbulb,
                title = "Project Sprint & Code Template",
                description = "Draft a full-featured note with headings, tasks, bullets, 1 2 3 list, table, and code.",
                onClick = { sendChatMessage("Draft a project launch sprint note with code and table") }
            )
        }
    }

    // Persistent Memory Inspector & Editor Dialog
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
                        text = "AI Persistent Memory (${memories.size})",
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
                        text = "The AI automatically reads, saves, and edits entries here via tool calling so it remembers your preferences, context, and facts across chats and note drafts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newMemKey,
                        onValueChange = { newMemKey = it },
                        label = { Text(if (editingMemoryId != null) "Edit Memory Topic / Key" else "New Memory Topic / Key") },
                        placeholder = { Text("e.g., Work Schedule, Dietary Preference") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMemContent,
                        onValueChange = { newMemContent = it },
                        label = { Text("Memory Details") },
                        placeholder = { Text("Facts or instructions for the AI to remember...") },
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
                                Text("Cancel Edit")
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
                            Text(if (editingMemoryId != null) "Update Memory" else "Add Memory")
                        }
                    }

                    HorizontalDivider()

                    if (memories.isEmpty()) {
                        Text(
                            text = "No memories stored yet. Tell the AI 'Remember that...' in chat or add one above.",
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

            // Full preview of blocks (Task, Bullet, 1 2 3, Heading, Text, Quote, Code, Table)
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
                            onClick = { proposal.savedNoteId?.let(onOpenNote) }
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
                        OutlinedButton(onClick = onDiscard) {
                            Icon(Icons.Default.Close, contentDescription = null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Discard")
                        }
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(onClick = onApprove) {
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

@Composable
private fun PromptSuggestionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = CardShapeExpressive,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShapeExpressive)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
