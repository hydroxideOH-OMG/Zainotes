/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.screens.aichat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.Note
import com.zainotes.app.data.model.NoteBlock
import com.zainotes.app.data.repository.NotesRepository
import com.zainotes.app.ui.theme.CardShapeExpressive
import com.zainotes.app.ui.theme.ExpressiveMotion
import kotlinx.coroutines.launch

private data class AssistantDraftResult(
    val id: Long = System.currentTimeMillis(),
    val prompt: String,
    val generatedTitle: String,
    val summaryText: String,
    val colorId: String,
    val backgroundTheme: String,
    val blocks: List<NoteBlock>,
    val savedNoteId: Long? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    notesRepository: NotesRepository? = null,
    onOpenNote: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeNotes by (notesRepository?.activeNotes?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) })
    val coroutineScope = rememberCoroutineScope()

    var promptInput by remember { mutableStateOf("") }
    val results = remember { mutableStateListOf<AssistantDraftResult>() }

    val totalNotes = activeNotes.size
    val openTasksCount = activeNotes.sumOf { it.totalTasksCount - it.completedTasksCount }

    fun runSmartAction(promptText: String) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isEmpty()) return

        val lower = cleanPrompt.lowercase()
        val draft = when {
            lower.contains("summarize") || lower.contains("digest") -> {
                val titles = activeNotes.take(6).mapIndexed { i, n ->
                    "${i + 1}. ${n.title.ifBlank { "Untitled Note" }} (${n.completedTasksCount}/${n.totalTasksCount} tasks)"
                }
                val blocks = mutableListOf(
                    NoteBlock(type = BlockType.HEADING, content = "Workspace Digest ($totalNotes notes)", isBold = true),
                    NoteBlock(type = BlockType.QUOTE, content = "$openTasksCount open tasks remaining across your active checklists.")
                )
                if (titles.isEmpty()) {
                    blocks.add(NoteBlock(type = BlockType.TEXT, content = "No notes yet — create your first note or generate a template below."))
                } else {
                    titles.forEach { line ->
                        blocks.add(NoteBlock(type = BlockType.BULLET, content = line))
                    }
                }
                AssistantDraftResult(
                    prompt = cleanPrompt,
                    generatedTitle = "Canvas Summary & Digest",
                    summaryText = "Synthesized $totalNotes active notes and $openTasksCount open tasks.",
                    colorId = "lavender",
                    backgroundTheme = "notes",
                    blocks = blocks
                )
            }

            lower.contains("open task") || lower.contains("extract") || lower.contains("action item") -> {
                val openTasks = activeNotes.flatMap { note ->
                    note.blocks.filter { it.type == BlockType.TASK && !it.isChecked }.map { block ->
                        val prefix = if (note.title.isNotBlank()) "[${note.title}] " else ""
                        NoteBlock(type = BlockType.TASK, content = "$prefix${block.content.ifBlank { "Pending task" }}")
                    }
                }
                val blocks = if (openTasks.isEmpty()) {
                    listOf(
                        NoteBlock(type = BlockType.HEADING, content = "All Caught Up!", isBold = true),
                        NoteBlock(type = BlockType.TASK, content = "Plan priorities for tomorrow"),
                        NoteBlock(type = BlockType.TASK, content = "Review pinned notes on home canvas")
                    )
                } else {
                    listOf(NoteBlock(type = BlockType.HEADING, content = "Priority Action Items", isBold = true)) + openTasks
                }
                AssistantDraftResult(
                    prompt = cleanPrompt,
                    generatedTitle = "Master Action Checklist",
                    summaryText = "Extracted ${openTasks.size} unchecked tasks across your workspace.",
                    colorId = "mint",
                    backgroundTheme = "ruled",
                    blocks = blocks
                )
            }

            lower.contains("grocery") || lower.contains("groceries") || lower.contains("shopping") -> {
                AssistantDraftResult(
                    prompt = cleanPrompt,
                    generatedTitle = "Fresh Grocery Run",
                    summaryText = "Structured a categorized grocery checklist with the Groceries scene.",
                    colorId = "sage",
                    backgroundTheme = "groceries",
                    blocks = listOf(
                        NoteBlock(type = BlockType.HEADING, content = "Produce & Fresh", isBold = true),
                        NoteBlock(type = BlockType.TASK, content = "Avocados, baby spinach & cherry tomatoes"),
                        NoteBlock(type = BlockType.TASK, content = "Honeycrisp apples & lemons"),
                        NoteBlock(type = BlockType.HEADING, content = "Pantry & Dairy", isBold = true),
                        NoteBlock(type = BlockType.TASK, content = "Oat milk, Greek yogurt & pasture eggs"),
                        NoteBlock(type = BlockType.TASK, content = "Sourdough loaf & extra virgin olive oil")
                    )
                )
            }

            lower.contains("travel") || lower.contains("trip") || lower.contains("packing") -> {
                AssistantDraftResult(
                    prompt = cleanPrompt,
                    generatedTitle = "Trip Packing & Itinerary",
                    summaryText = "Created a travel checklist paired with the Travel & Places scene.",
                    colorId = "sky",
                    backgroundTheme = "places",
                    blocks = listOf(
                        NoteBlock(type = BlockType.HEADING, content = "Essentials & Documents", isBold = true),
                        NoteBlock(type = BlockType.TASK, content = "ID / Passport, boarding passes & wallet"),
                        NoteBlock(type = BlockType.TASK, content = "Phone charger, power bank & travel adapter"),
                        NoteBlock(type = BlockType.HEADING, content = "Wardrobe & Gear", isBold = true),
                        NoteBlock(type = BlockType.TASK, content = "Comfortable walking shoes & weather jacket"),
                        NoteBlock(type = BlockType.TASK, content = "Toiletries kit & sunglasses")
                    )
                )
            }

            else -> {
                val rawItems = cleanPrompt
                    .split(",", ";", "\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val blocks = mutableListOf(
                    NoteBlock(type = BlockType.HEADING, content = cleanPrompt.replaceFirstChar { it.uppercase() }, isBold = true)
                )
                if (rawItems.size > 1) {
                    rawItems.forEach { item ->
                        blocks.add(NoteBlock(type = BlockType.TASK, content = item.replaceFirstChar { it.uppercase() }))
                    }
                } else {
                    blocks.add(NoteBlock(type = BlockType.QUOTE, content = "Key objective: $cleanPrompt"))
                    blocks.add(NoteBlock(type = BlockType.TASK, content = "Define first actionable milestone for $cleanPrompt", isBold = true))
                    blocks.add(NoteBlock(type = BlockType.TASK, content = "Outline core steps and resources needed"))
                    blocks.add(NoteBlock(type = BlockType.TASK, content = "Review progress and refine checklist"))
                }
                AssistantDraftResult(
                    prompt = cleanPrompt,
                    generatedTitle = cleanPrompt.take(36).replaceFirstChar { it.uppercase() },
                    summaryText = "Generated a structured note & checklist ready to save to your canvas.",
                    colorId = "amber",
                    backgroundTheme = "doodles",
                    blocks = blocks
                )
            }
        }

        results.add(0, draft)
        promptInput = ""
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
                                    text = "100% On-Device Smart Synthesis",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Text(
                            text = "$totalNotes notes · $openTasksCount tasks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Turn thoughts into structured notes & checklists",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instantly summarize your existing notes, gather unchecked tasks across cards, or draft a themed checklist from any topic.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type a topic or comma-separated tasks...") },
                        shape = CircleShape,
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { runSmartAction(promptInput) },
                                enabled = promptInput.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Generate note",
                                    tint = if (promptInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                            onClick = { runSmartAction("Summarize my notes") },
                            label = { Text("Summarize notes") },
                            shape = CircleShape
                        )
                        SuggestionChip(
                            onClick = { runSmartAction("Extract all open tasks") },
                            label = { Text("Collect open tasks ($openTasksCount)") },
                            shape = CircleShape
                        )
                        SuggestionChip(
                            onClick = { runSmartAction("Weekly grocery run") },
                            label = { Text("Grocery checklist") },
                            shape = CircleShape
                        )
                        SuggestionChip(
                            onClick = { runSmartAction("Weekend trip packing") },
                            label = { Text("Travel packing list") },
                            shape = CircleShape
                        )
                    }
                }
            }

            // Generated Drafts Feed
            if (results.isNotEmpty()) {
                Text(
                    text = "Generated Workspace Drafts",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                results.forEachIndexed { idx, item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically(ExpressiveMotion.spatialFastSpec()) + fadeIn()
                    ) {
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.generatedTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.summaryText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    item.blocks.take(6).forEach { b ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            when (b.type) {
                                                BlockType.TASK -> {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckBoxOutlineBlank,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                BlockType.BULLET -> {
                                                    Icon(
                                                        imageVector = Icons.Default.FiberManualRecord,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(7.dp)
                                                    )
                                                }
                                                else -> {}
                                            }
                                            Text(
                                                text = b.content,
                                                style = if (b.type == BlockType.HEADING) {
                                                    MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                } else {
                                                    MaterialTheme.typography.bodyMedium
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (item.savedNoteId != null) {
                                        FilledTonalButton(
                                            onClick = { onOpenNote(item.savedNoteId) }
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Saved · Open in Editor")
                                        }
                                    } else {
                                        FilledTonalButton(
                                            onClick = {
                                                if (notesRepository != null) {
                                                    coroutineScope.launch {
                                                        val newId = notesRepository.saveNote(
                                                            Note(
                                                                title = item.generatedTitle,
                                                                colorId = item.colorId,
                                                                backgroundTheme = item.backgroundTheme,
                                                                cardSize = CardSize.WIDE,
                                                                blocks = item.blocks
                                                            )
                                                        )
                                                        results[idx] = item.copy(savedNoteId = newId)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.AddTask, contentDescription = null, Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Save to Canvas")
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
                description = "Compile an instant digest of all your active notes and task progress.",
                onClick = { runSmartAction("Summarize my notes") }
            )

            PromptSuggestionCard(
                icon = Icons.Default.Checklist,
                title = "Collect Unchecked Tasks ($openTasksCount open)",
                description = "Gather every unfinished checkbox item across all notes into one master checklist.",
                onClick = { runSmartAction("Extract all open tasks") }
            )

            PromptSuggestionCard(
                icon = Icons.Default.ShoppingBag,
                title = "Generate Grocery & Errand Scene",
                description = "Create a categorized grocery checklist styled with the Groceries background illustration.",
                onClick = { runSmartAction("Weekly grocery run") }
            )

            PromptSuggestionCard(
                icon = Icons.Default.Lightbulb,
                title = "Project Sprint & Brainstorm Template",
                description = "Spin up a structured project card with objectives, milestones, and action items.",
                onClick = { runSmartAction("Project launch milestones, Design review, QA checklist, Release notes") }
            )
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
