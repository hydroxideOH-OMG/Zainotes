/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.data.ai

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.Note
import com.zainotes.app.data.model.NoteBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

enum class AiProvider(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val suggestedModels: List<String>
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        subtitle = "Google AI Studio · Gemini 2.5 / 3.x",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta",
        defaultModel = "gemini-2.5-flash",
        suggestedModels = listOf(
            "gemini-2.5-flash",
            "gemini-2.5-pro",
            "gemini-3.8-flash",
            "gemini-3.1-flash-lite"
        )
    ),
    OPENAI(
        id = "openai",
        displayName = "ChatGPT (OpenAI)",
        subtitle = "OpenAI Platform · GPT-4o / o3",
        defaultBaseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini",
        suggestedModels = listOf(
            "gpt-4o",
            "gpt-4o-mini",
            "gpt-4.1",
            "gpt-4.1-mini",
            "o3-mini"
        )
    ),
    ANTHROPIC(
        id = "anthropic",
        displayName = "Anthropic Claude",
        subtitle = "Claude 3.7 Sonnet / 3.5 Haiku",
        defaultBaseUrl = "https://api.anthropic.com/v1",
        defaultModel = "claude-3-7-sonnet-latest",
        suggestedModels = listOf(
            "claude-3-7-sonnet-latest",
            "claude-3-5-sonnet-latest",
            "claude-3-5-haiku-latest"
        )
    ),
    OPENROUTER(
        id = "openrouter",
        displayName = "OpenRouter",
        subtitle = "Unified gateway for 200+ LLMs",
        defaultBaseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "google/gemini-2.5-flash",
        suggestedModels = listOf(
            "google/gemini-2.5-flash",
            "openai/gpt-4o-mini",
            "anthropic/claude-3.7-sonnet",
            "deepseek/deepseek-chat"
        )
    ),
    GROQ(
        id = "groq",
        displayName = "Groq Cloud",
        subtitle = "Ultra-fast LPU inference",
        defaultBaseUrl = "https://api.groq.com/openai/v1",
        defaultModel = "llama-3.3-70b-versatile",
        suggestedModels = listOf(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "mixtral-8x7b-32768",
            "qwen-qwq-32b"
        )
    ),
    DEEPSEEK(
        id = "deepseek",
        displayName = "DeepSeek",
        subtitle = "DeepSeek V3 & R1 Reasoning",
        defaultBaseUrl = "https://api.deepseek.com/v1",
        defaultModel = "deepseek-chat",
        suggestedModels = listOf(
            "deepseek-chat",
            "deepseek-reasoner"
        )
    ),
    MISTRAL(
        id = "mistral",
        displayName = "Mistral AI",
        subtitle = "Mistral Large & Codestral",
        defaultBaseUrl = "https://api.mistral.ai/v1",
        defaultModel = "mistral-large-latest",
        suggestedModels = listOf(
            "mistral-large-latest",
            "mistral-small-latest",
            "codestral-latest"
        )
    ),
    CUSTOM(
        id = "custom",
        displayName = "Custom OpenAI API",
        subtitle = "Any OpenAI-compatible endpoint",
        defaultBaseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini",
        suggestedModels = listOf("gpt-4o-mini", "local-model")
    );

    companion object {
        fun fromId(id: String?): AiProvider {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: GEMINI
        }
    }
}

data class AiMemoryItem(
    val id: String = UUID.randomUUID().toString(),
    val key: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class AiToolEvent(
    val id: String = UUID.randomUUID().toString(),
    val toolName: String,
    val summary: String
)

data class AiProposedNoteAction(
    val id: String = UUID.randomUUID().toString(),
    val actionType: String, // "DRAFT_NOTE" or "EDIT_NOTE"
    val targetNoteId: Long? = null,
    val title: String,
    val summary: String,
    val colorId: String = "default",
    val backgroundTheme: String = "none",
    val isPinned: Boolean = false,
    val blocks: List<NoteBlock> = emptyList(),
    val status: String = "PENDING", // "PENDING", "APPROVED", "DISCARDED"
    val savedNoteId: Long? = null
)

data class AiAttachedImage(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val mimeType: String = "image/jpeg",
    val base64Data: String = ""
)

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val text: String,
    val attachedNoteTitles: List<String> = emptyList(),
    val attachedImages: List<AiAttachedImage> = emptyList(),
    val toolEvents: List<AiToolEvent> = emptyList(),
    val proposedNotes: List<AiProposedNoteAction> = emptyList(),
    val providerBadge: String = ""
)

data class AiAgentTurnResponse(
    val replyText: String,
    val updatedMemories: List<AiMemoryItem>,
    val toolEvents: List<AiToolEvent>,
    val proposedNotes: List<AiProposedNoteAction>,
    val providerBadge: String
)

object AiProviderClient {
    private val gson = Gson()

    /**
     * Fetches the available model names directly from the selected AI provider's API.
     */
    suspend fun fetchModelsFromApi(
        provider: AiProvider,
        apiKey: String,
        customBaseUrl: String = ""
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter an API key first."))
        }

        try {
            when (provider) {
                AiProvider.GEMINI -> {
                    val endpoint = "https://generativelanguage.googleapis.com/v1beta/models?key=$cleanKey"
                    val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 12000
                        readTimeout = 12000
                    }
                    val code = conn.responseCode
                    val body = readStream(conn, code in 200..299)
                    if (code !in 200..299) {
                        return@withContext Result.failure(Exception("Gemini API HTTP $code: ${extractErrorMsg(body)}"))
                    }
                    val root = JsonParser.parseString(body).asJsonObject
                    val arr = root.getAsJsonArray("models") ?: JsonArray()
                    val names = mutableListOf<String>()
                    for (el in arr) {
                        val obj = el.asJsonObject
                        val rawName = obj.get("name")?.asString ?: continue
                        val methods = obj.getAsJsonArray("supportedGenerationMethods")
                        val supportsGen = methods == null || methods.any { it.asString == "generateContent" }
                        if (supportsGen) {
                            val shortName = rawName.removePrefix("models/")
                            if (shortName.startsWith("gemini")) {
                                names.add(shortName)
                            }
                        }
                    }
                    val sorted = names.distinct().sortedByDescending {
                        when {
                            it.contains("2.5") || it.contains("3.") -> 2
                            it.contains("flash") || it.contains("pro") -> 1
                            else -> 0
                        }
                    }
                    Result.success(if (sorted.isNotEmpty()) sorted else provider.suggestedModels)
                }

                AiProvider.ANTHROPIC -> {
                    val endpoint = "https://api.anthropic.com/v1/models"
                    val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("x-api-key", cleanKey)
                        setRequestProperty("anthropic-version", "2023-06-01")
                        connectTimeout = 12000
                        readTimeout = 12000
                    }
                    val code = conn.responseCode
                    val body = readStream(conn, code in 200..299)
                    if (code !in 200..299) {
                        return@withContext Result.failure(Exception("Anthropic API HTTP $code: ${extractErrorMsg(body)}"))
                    }
                    val root = JsonParser.parseString(body).asJsonObject
                    val arr = root.getAsJsonArray("data") ?: JsonArray()
                    val names = arr.mapNotNull { it.asJsonObject.get("id")?.asString }.distinct()
                    Result.success(if (names.isNotEmpty()) names else provider.suggestedModels)
                }

                else -> {
                    val base = if (provider == AiProvider.CUSTOM && customBaseUrl.isNotBlank()) {
                        customBaseUrl.trim().trimEnd('/')
                    } else {
                        provider.defaultBaseUrl.trimEnd('/')
                    }
                    val endpoint = "$base/models"
                    val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("Authorization", "Bearer $cleanKey")
                        setRequestProperty("Accept", "application/json")
                        connectTimeout = 12000
                        readTimeout = 12000
                    }
                    val code = conn.responseCode
                    val body = readStream(conn, code in 200..299)
                    if (code !in 200..299) {
                        return@withContext Result.failure(Exception("${provider.displayName} HTTP $code: ${extractErrorMsg(body)}"))
                    }
                    val root = JsonParser.parseString(body).asJsonObject
                    val arr = root.getAsJsonArray("data") ?: JsonArray()
                    val names = arr.mapNotNull { it.asJsonObject.get("id")?.asString }.distinct()
                    Result.success(if (names.isNotEmpty()) names else provider.suggestedModels)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Runs a live AI chat turn with Persistent Memory and Note Drafting/Editing tools.
     * Offline fallback is removed: requires a valid API key configured in Settings.
     */
    suspend fun runAgentChatTurn(
        provider: AiProvider,
        apiKey: String,
        modelName: String,
        customBaseUrl: String,
        userPrompt: String,
        chatHistory: List<AiChatMessage>,
        memories: List<AiMemoryItem>,
        activeNotes: List<Note>,
        attachedNotes: List<Note> = emptyList(),
        attachedImages: List<AiAttachedImage> = emptyList()
    ): AiAgentTurnResponse = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        val chosenModel = modelName.ifBlank { provider.defaultModel }

        if (cleanKey.isBlank()) {
            return@withContext AiAgentTurnResponse(
                replyText = "API key is not set. Set an API key in the settings.",
                updatedMemories = memories,
                toolEvents = emptyList(),
                proposedNotes = emptyList(),
                providerBadge = "${provider.displayName} · Missing Key"
            )
        }

        val attachmentContext = buildString {
            if (attachedNotes.isNotEmpty()) {
                append("\n\n[ATTACHED NOTES FOR CONTEXT]:\n")
                attachedNotes.forEach { n ->
                    val blocksDump = n.blocks.joinToString("\n") { b ->
                        "  - [${b.type.name}] ${b.content}"
                    }
                    append("Note #${n.id} \"${n.title}\":\n$blocksDump\n")
                }
            }
            if (attachedImages.isNotEmpty()) {
                append("\n\n[ATTACHED IMAGES]: ${attachedImages.joinToString(", ") { it.label }}")
            }
        }

        val enrichedPrompt = userPrompt + attachmentContext
        val systemInstruction = buildAgentSystemPrompt(memories, activeNotes)

        val llmResult = try {
            when (provider) {
                AiProvider.GEMINI -> callGeminiJsonAgent(cleanKey, chosenModel, systemInstruction, chatHistory, enrichedPrompt, attachedImages)
                AiProvider.ANTHROPIC -> callAnthropicJsonAgent(cleanKey, chosenModel, systemInstruction, chatHistory, enrichedPrompt)
                else -> {
                    val base = if (provider == AiProvider.CUSTOM && customBaseUrl.isNotBlank()) {
                        customBaseUrl.trim().trimEnd('/')
                    } else {
                        provider.defaultBaseUrl.trimEnd('/')
                    }
                    callOpenAiCompatibleJsonAgent(base, cleanKey, chosenModel, systemInstruction, chatHistory, enrichedPrompt)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

        llmResult.fold(
            onSuccess = { rawOutput ->
                parseAgentStructuredResponse(
                    rawText = rawOutput,
                    currentMemories = memories,
                    activeNotes = activeNotes,
                    badge = "${provider.displayName} · $chosenModel"
                )
            },
            onFailure = { err ->
                AiAgentTurnResponse(
                    replyText = "Could not complete request with ${provider.displayName} ($chosenModel): ${err.message ?: "Network or API key error"}",
                    updatedMemories = memories,
                    toolEvents = emptyList(),
                    proposedNotes = emptyList(),
                    providerBadge = "${provider.displayName} · Error"
                )
            }
        )
    }

    private fun buildAgentSystemPrompt(
        memories: List<AiMemoryItem>,
        notes: List<Note>
    ): String {
        val memText = if (memories.isEmpty()) {
            "No persistent memories saved yet."
        } else {
            memories.joinToString("\n") { "- [id=${it.id}] ${it.key}: ${it.content}" }
        }

        val notesText = if (notes.isEmpty()) {
            "No notes on canvas yet."
        } else {
            notes.take(25).joinToString("\n") { n ->
                val blockPreview = n.blocks.joinToString("; ") { b ->
                    when (b.type) {
                        BlockType.TASK -> "[${if (b.isChecked) "x" else " "}] ${b.content}"
                        BlockType.TABLE -> "[TABLE: ${b.resolvedTable().joinToString(" / ") { r -> r.joinToString("|") }}]"
                        else -> "(${b.type.name}) ${b.content}"
                    }
                }
                "- Note #${n.id} (title=\"${n.title}\", color=\"${n.colorId}\", theme=\"${n.backgroundTheme}\"): $blockPreview"
            }
        }

        return """
You are Zainotes Smart AI Assistant with Persistent Memory and Full Note Canvas Tool-Calling.
You can answer any general question, manage persistent memory, draft new notes, and edit existing notes.

CURRENT PERSISTENT MEMORY:
$memText

CURRENT WORKSPACE NOTES:
$notesText

Always respond with a single valid JSON object (no markdown fences around the JSON) matching this schema:
{
  "reply": "Your helpful, natural conversational response to the user.",
  "memory_actions": [
    {
      "action": "save" | "update" | "delete",
      "key": "Memory topic or key",
      "content": "Detailed information to remember"
    }
  ],
  "note_actions": [
    {
      "action": "draft_note" | "edit_note",
      "targetNoteId": 1,
      "title": "Note Title",
      "summary": "Brief description of what was drafted or changed",
      "colorId": "default" | "coral" | "peach" | "sand" | "mint" | "sage" | "fog" | "storm" | "dusk" | "blossom" | "clay" | "chalk" | "lavender" | "sky" | "amber" | "rose",
      "backgroundTheme": "none" | "groceries" | "food" | "music" | "recipes" | "notes" | "places" | "travel" | "video" | "celebration" | "dusk" | "storm" | "nature" | "doodles" | "ruled" | "grid" | "dots",
      "isPinned": false,
      "blocks": [
        {
          "type": "TEXT" | "HEADING" | "TASK" | "BULLET" | "NUMBERED" | "QUOTE" | "CODE" | "TABLE",
          "content": "Block text content",
          "isChecked": false,
          "isBold": false,
          "isItalic": false,
          "isUnderline": false,
          "isStrikethrough": false,
          "isHighlighted": false,
          "isMonospace": false,
          "tableData": [["Header 1", "Header 2"], ["Row 1 Col 1", "Row 1 Col 2"]]
        }
      ]
    }
  ]
}

Rules for Tool Actions:
1. If the user shares personal preferences, facts, dates, goals, or asks you to remember/update/forget something, emit a `memory_actions` entry.
2. When drafting or editing notes, reference relevant facts from CURRENT PERSISTENT MEMORY whenever helpful.
3. For `draft_note` or `edit_note`, use rich blocks: HEADING, TEXT, TASK (checkboxes/checkpoints), BULLET (bulletins), NUMBERED (1 2 3 lists), QUOTE, CODE (code snippets), and TABLE (with 2D `tableData` rows and columns).
4. When editing an existing note (`edit_note`), provide `targetNoteId` matching the existing Note ID and include the complete updated `blocks` list for that note.
""".trimIndent()
    }

    private fun callGeminiJsonAgent(
        apiKey: String,
        model: String,
        systemInstruction: String,
        history: List<AiChatMessage>,
        userPrompt: String,
        attachedImages: List<AiAttachedImage> = emptyList()
    ): Result<String> {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val payload = JsonObject().apply {
            add("systemInstruction", JsonObject().apply {
                add("parts", JsonArray().apply {
                    add(JsonObject().apply { addProperty("text", systemInstruction) })
                })
            })
            val contentsArr = JsonArray()
            history.takeLast(8).forEach { msg ->
                contentsArr.add(JsonObject().apply {
                    addProperty("role", if (msg.role == "assistant") "model" else "user")
                    add("parts", JsonArray().apply {
                        add(JsonObject().apply { addProperty("text", msg.text) })
                    })
                })
            }
            contentsArr.add(JsonObject().apply {
                addProperty("role", "user")
                add("parts", JsonArray().apply {
                    attachedImages.forEach { img ->
                        if (img.base64Data.isNotBlank()) {
                            add(JsonObject().apply {
                                add("inlineData", JsonObject().apply {
                                    addProperty("mimeType", img.mimeType)
                                    addProperty("data", img.base64Data)
                                })
                            })
                        }
                    }
                    add(JsonObject().apply { addProperty("text", userPrompt) })
                })
            })
            add("contents", contentsArr)
            add("generationConfig", JsonObject().apply {
                addProperty("responseMimeType", "application/json")
            })
        }

        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 20000
            readTimeout = 25000
        }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(gson.toJson(payload)) }
        val code = conn.responseCode
        val body = readStream(conn, code in 200..299)
        if (code !in 200..299) {
            return Result.failure(Exception("Gemini HTTP $code: ${extractErrorMsg(body)}"))
        }
        val root = JsonParser.parseString(body).asJsonObject
        val text = root.getAsJsonArray("candidates")
            ?.firstOrNull()?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?.firstOrNull()?.asJsonObject
            ?.get("text")?.asString
            ?: return Result.failure(Exception("Empty response from Gemini"))
        return Result.success(text)
    }

    private fun callOpenAiCompatibleJsonAgent(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemInstruction: String,
        history: List<AiChatMessage>,
        userPrompt: String
    ): Result<String> {
        val endpoint = "$baseUrl/chat/completions"
        val messages = JsonArray().apply {
            add(JsonObject().apply {
                addProperty("role", "system")
                addProperty("content", systemInstruction)
            })
            history.takeLast(8).forEach { msg ->
                add(JsonObject().apply {
                    addProperty("role", if (msg.role == "assistant") "assistant" else "user")
                    addProperty("content", msg.text)
                })
            }
            add(JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", userPrompt)
            })
        }
        val payload = JsonObject().apply {
            addProperty("model", model)
            add("messages", messages)
        }

        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 20000
            readTimeout = 25000
        }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(gson.toJson(payload)) }
        val code = conn.responseCode
        val body = readStream(conn, code in 200..299)
        if (code !in 200..299) {
            return Result.failure(Exception("API HTTP $code: ${extractErrorMsg(body)}"))
        }
        val root = JsonParser.parseString(body).asJsonObject
        val text = root.getAsJsonArray("choices")
            ?.firstOrNull()?.asJsonObject
            ?.getAsJsonObject("message")
            ?.get("content")?.asString
            ?: return Result.failure(Exception("Empty response from provider"))
        return Result.success(text)
    }

    private fun callAnthropicJsonAgent(
        apiKey: String,
        model: String,
        systemInstruction: String,
        history: List<AiChatMessage>,
        userPrompt: String
    ): Result<String> {
        val endpoint = "https://api.anthropic.com/v1/messages"
        val messages = JsonArray().apply {
            history.takeLast(8).forEach { msg ->
                add(JsonObject().apply {
                    addProperty("role", if (msg.role == "assistant") "assistant" else "user")
                    addProperty("content", msg.text)
                })
            }
            add(JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", userPrompt)
            })
        }
        val payload = JsonObject().apply {
            addProperty("model", model)
            addProperty("max_tokens", 2048)
            addProperty("system", systemInstruction)
            add("messages", messages)
        }

        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("x-api-key", apiKey)
            setRequestProperty("anthropic-version", "2023-06-01")
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 20000
            readTimeout = 25000
        }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(gson.toJson(payload)) }
        val code = conn.responseCode
        val body = readStream(conn, code in 200..299)
        if (code !in 200..299) {
            return Result.failure(Exception("Anthropic HTTP $code: ${extractErrorMsg(body)}"))
        }
        val root = JsonParser.parseString(body).asJsonObject
        val text = root.getAsJsonArray("content")
            ?.firstOrNull()?.asJsonObject
            ?.get("text")?.asString
            ?: return Result.failure(Exception("Empty response from Anthropic"))
        return Result.success(text)
    }

    private fun parseAgentStructuredResponse(
        rawText: String,
        currentMemories: List<AiMemoryItem>,
        activeNotes: List<Note>,
        badge: String
    ): AiAgentTurnResponse {
        val cleaned = rawText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val updatedMemories = currentMemories.toMutableList()
        val toolEvents = mutableListOf<AiToolEvent>()
        val proposedNotes = mutableListOf<AiProposedNoteAction>()

        return try {
            val obj = JsonParser.parseString(cleaned).asJsonObject
            val reply = obj.get("reply")?.asString ?: cleaned

            obj.getAsJsonArray("memory_actions")?.forEach { el ->
                val mObj = el.asJsonObject
                val action = mObj.get("action")?.asString?.lowercase() ?: "save"
                val key = mObj.get("key")?.asString?.trim().orEmpty().ifBlank { "Note Context" }
                val content = mObj.get("content")?.asString?.trim().orEmpty()

                when (action) {
                    "delete" -> {
                        val removed = updatedMemories.removeAll {
                            it.key.equals(key, ignoreCase = true) || it.id == key
                        }
                        if (removed) {
                            toolEvents.add(AiToolEvent(toolName = "delete_memory", summary = "Deleted memory: $key"))
                        }
                    }
                    "update" -> {
                        val idx = updatedMemories.indexOfFirst { it.key.equals(key, ignoreCase = true) || it.id == key }
                        if (idx >= 0) {
                            updatedMemories[idx] = updatedMemories[idx].copy(content = content, updatedAt = System.currentTimeMillis())
                            toolEvents.add(AiToolEvent(toolName = "update_memory", summary = "Updated memory: $key"))
                        } else if (content.isNotBlank()) {
                            updatedMemories.add(0, AiMemoryItem(key = key, content = content))
                            toolEvents.add(AiToolEvent(toolName = "save_memory", summary = "Saved to memory: $key"))
                        }
                    }
                    else -> {
                        if (content.isNotBlank()) {
                            val idx = updatedMemories.indexOfFirst { it.key.equals(key, ignoreCase = true) }
                            if (idx >= 0) {
                                updatedMemories[idx] = updatedMemories[idx].copy(content = content, updatedAt = System.currentTimeMillis())
                                toolEvents.add(AiToolEvent(toolName = "update_memory", summary = "Updated memory: $key"))
                            } else {
                                updatedMemories.add(0, AiMemoryItem(key = key, content = content))
                                toolEvents.add(AiToolEvent(toolName = "save_memory", summary = "Saved to memory: $key"))
                            }
                        }
                    }
                }
            }

            obj.getAsJsonArray("note_actions")?.forEach { el ->
                val nObj = el.asJsonObject
                val actionRaw = nObj.get("action")?.asString?.lowercase() ?: "draft_note"
                val isEdit = actionRaw.contains("edit")
                val targetId = if (nObj.has("targetNoteId") && !nObj.get("targetNoteId").isJsonNull) {
                    nObj.get("targetNoteId").asLong
                } else null
                val title = nObj.get("title")?.asString ?: "Untitled Note"
                val summary = nObj.get("summary")?.asString
                    ?: if (isEdit) "Proposed edits to \"$title\"" else "Drafted new note \"$title\""
                val colorId = nObj.get("colorId")?.asString ?: "default"
                val bgTheme = nObj.get("backgroundTheme")?.asString ?: "none"
                val isPinned = if (nObj.has("isPinned") && !nObj.get("isPinned").isJsonNull) nObj.get("isPinned").asBoolean else false

                val parsedBlocks = mutableListOf<NoteBlock>()
                nObj.getAsJsonArray("blocks")?.forEachIndexed { bIdx, bEl ->
                    val bObj = bEl.asJsonObject
                    val typeStr = bObj.get("type")?.asString?.uppercase() ?: "TEXT"
                    val bType = try { BlockType.valueOf(typeStr) } catch (_: Exception) { BlockType.TEXT }
                    val bContent = bObj.get("content")?.asString ?: ""
                    val isChecked = bObj.get("isChecked")?.asBoolean ?: false
                    val isBold = bObj.get("isBold")?.asBoolean ?: (bType == BlockType.HEADING)
                    val isItalic = bObj.get("isItalic")?.asBoolean ?: false
                    val isUnderline = bObj.get("isUnderline")?.asBoolean ?: false
                    val isStrike = bObj.get("isStrikethrough")?.asBoolean ?: false
                    val isHighlight = bObj.get("isHighlighted")?.asBoolean ?: false
                    val isMono = bObj.get("isMonospace")?.asBoolean ?: (bType == BlockType.CODE)

                    val tData = mutableListOf<List<String>>()
                    bObj.getAsJsonArray("tableData")?.forEach { rEl ->
                        if (rEl.isJsonArray) {
                            tData.add(rEl.asJsonArray.map { cEl -> cEl.asString })
                        }
                    }

                    parsedBlocks.add(
                        NoteBlock(
                            type = bType,
                            content = if (bType == BlockType.TABLE && bContent.isBlank() && tData.isNotEmpty()) {
                                tData.joinToString("\n") { it.joinToString(" | ") }
                            } else bContent,
                            isChecked = isChecked,
                            order = bIdx,
                            isBold = isBold,
                            isItalic = isItalic,
                            isUnderline = isUnderline,
                            isStrikethrough = isStrike,
                            isHighlighted = isHighlight,
                            isMonospace = isMono,
                            tableData = tData
                        )
                    )
                }

                val resolvedTarget = targetId ?: activeNotes.find { it.title.equals(title, ignoreCase = true) }?.id
                proposedNotes.add(
                    AiProposedNoteAction(
                        actionType = if (isEdit && resolvedTarget != null) "EDIT_NOTE" else "DRAFT_NOTE",
                        targetNoteId = resolvedTarget,
                        title = title,
                        summary = summary,
                        colorId = colorId,
                        backgroundTheme = bgTheme,
                        isPinned = isPinned,
                        blocks = parsedBlocks
                    )
                )
                toolEvents.add(
                    AiToolEvent(
                        toolName = if (isEdit && resolvedTarget != null) "edit_note" else "draft_note",
                        summary = if (isEdit && resolvedTarget != null) "Proposed edit for \"$title\" (awaiting approval)"
                        else "Drafted note \"$title\" (awaiting approval)"
                    )
                )
            }

            AiAgentTurnResponse(
                replyText = reply,
                updatedMemories = updatedMemories,
                toolEvents = toolEvents,
                proposedNotes = proposedNotes,
                providerBadge = badge
            )
        } catch (_: Exception) {
            AiAgentTurnResponse(
                replyText = rawText,
                updatedMemories = updatedMemories,
                toolEvents = emptyList(),
                proposedNotes = emptyList(),
                providerBadge = badge
            )
        }
    }

    private fun readStream(conn: HttpURLConnection, success: Boolean): String {
        val stream = if (success) conn.inputStream else conn.errorStream
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
    }

    private fun extractErrorMsg(rawBody: String): String {
        return try {
            val obj = JsonParser.parseString(rawBody).asJsonObject
            obj.getAsJsonObject("error")?.get("message")?.asString ?: rawBody.take(160)
        } catch (_: Exception) {
            rawBody.take(160)
        }
    }
}
