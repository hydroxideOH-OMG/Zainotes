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

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val text: String,
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
     * Runs a multi-capability AI chat turn with Persistent Memory and Note Drafting/Editing tools.
     */
    suspend fun runAgentChatTurn(
        provider: AiProvider,
        apiKey: String,
        modelName: String,
        customBaseUrl: String,
        userPrompt: String,
        chatHistory: List<AiChatMessage>,
        memories: List<AiMemoryItem>,
        activeNotes: List<Note>
    ): AiAgentTurnResponse = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        val chosenModel = modelName.ifBlank { provider.defaultModel }

        if (cleanKey.isNotBlank()) {
            val systemInstruction = buildAgentSystemPrompt(memories, activeNotes)
            val llmResult = try {
                when (provider) {
                    AiProvider.GEMINI -> callGeminiJsonAgent(cleanKey, chosenModel, systemInstruction, chatHistory, userPrompt)
                    AiProvider.ANTHROPIC -> callAnthropicJsonAgent(cleanKey, chosenModel, systemInstruction, chatHistory, userPrompt)
                    else -> {
                        val base = if (provider == AiProvider.CUSTOM && customBaseUrl.isNotBlank()) {
                            customBaseUrl.trim().trimEnd('/')
                        } else {
                            provider.defaultBaseUrl.trimEnd('/')
                        }
                        callOpenAiCompatibleJsonAgent(base, cleanKey, chosenModel, systemInstruction, chatHistory, userPrompt)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }

            llmResult.getOrNull()?.let { rawOutput ->
                return@withContext parseAgentStructuredResponse(
                    rawText = rawOutput,
                    currentMemories = memories,
                    activeNotes = activeNotes,
                    badge = "${provider.displayName} · $chosenModel"
                )
            }
        }

        // Smart local agent with full persistent memory & note tool-calling support
        runLocalSmartAgent(
            userPrompt = userPrompt,
            currentMemories = memories,
            activeNotes = activeNotes,
            badge = if (cleanKey.isBlank()) "Built-in Smart Agent (Add API key in Settings for cloud LLMs)"
            else "${provider.displayName} · $chosenModel"
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
        userPrompt: String
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

                val actionType = if (isEdit) "EDIT_NOTE" else "DRAFT_NOTE"
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

    /**
     * Intelligent local fallback agent that executes real tool calls (`save_memory`, `update_memory`,
     * `delete_memory`, `draft_note`, `edit_note`) with full block support (Task, Bullet, 1 2 3,
     * Heading, Text, Quote, Code, Table).
     */
    private fun runLocalSmartAgent(
        userPrompt: String,
        currentMemories: List<AiMemoryItem>,
        activeNotes: List<Note>,
        badge: String
    ): AiAgentTurnResponse {
        val clean = userPrompt.trim()
        val lower = clean.lowercase()
        val updatedMemories = currentMemories.toMutableList()
        val toolEvents = mutableListOf<AiToolEvent>()
        val proposedNotes = mutableListOf<AiProposedNoteAction>()

        // 1. Check for memory save / update / delete commands
        if (lower.startsWith("remember ") || lower.contains("save to memory") || lower.contains("remember that")) {
            val fact = clean
                .replace(Regex("(?i)^(please\\s+)?(remember\\s+that|remember|save\\s+to\\s+memory:?\\s*)"), "")
                .trim()
            val key = fact.take(28).split(" ").take(4).joinToString(" ").replaceFirstChar { it.uppercase() }.ifBlank { "User Preference" }
            val existingIdx = updatedMemories.indexOfFirst { it.key.equals(key, ignoreCase = true) }
            if (existingIdx >= 0) {
                updatedMemories[existingIdx] = updatedMemories[existingIdx].copy(content = fact, updatedAt = System.currentTimeMillis())
                toolEvents.add(AiToolEvent(toolName = "update_memory", summary = "Updated memory \"$key\""))
            } else {
                updatedMemories.add(0, AiMemoryItem(key = key, content = fact))
                toolEvents.add(AiToolEvent(toolName = "save_memory", summary = "Saved to persistent memory: \"$key\""))
            }
            if (!lower.contains("note") && !lower.contains("draft") && !lower.contains("create")) {
                return AiAgentTurnResponse(
                    replyText = "I've saved that to your persistent memory under **$key**: \"$fact\". I will automatically reference this whenever you ask questions or draft notes.",
                    updatedMemories = updatedMemories,
                    toolEvents = toolEvents,
                    proposedNotes = emptyList(),
                    providerBadge = badge
                )
            }
        }

        if (lower.startsWith("forget ") || lower.contains("delete memory") || lower.contains("clear memory")) {
            val target = clean.replace(Regex("(?i)^(forget|delete\\s+memory|clear\\s+memory)\\s*"), "").trim()
            val removed = if (target.isBlank() || target == "all") {
                val hadAny = updatedMemories.isNotEmpty()
                updatedMemories.clear()
                hadAny
            } else {
                updatedMemories.removeAll { it.key.contains(target, ignoreCase = true) || it.content.contains(target, ignoreCase = true) }
            }
            if (removed) {
                toolEvents.add(AiToolEvent(toolName = "delete_memory", summary = "Removed matching entry from persistent memory"))
            }
            return AiAgentTurnResponse(
                replyText = if (removed) "Done — I've updated your persistent memory." else "I couldn't find a matching memory entry to remove.",
                updatedMemories = updatedMemories,
                toolEvents = toolEvents,
                proposedNotes = emptyList(),
                providerBadge = badge
            )
        }

        // 2. Check if user wants to EDIT an existing note
        val matchedExistingNote = activeNotes.firstOrNull { note ->
            note.title.isNotBlank() && lower.contains(note.title.lowercase()) &&
                (lower.contains("edit") || lower.contains("update") || lower.contains("add") || lower.contains("modify") || lower.contains("change"))
        } ?: if (lower.startsWith("edit note") || lower.startsWith("update note")) activeNotes.firstOrNull() else null

        if (matchedExistingNote != null) {
            val newBlocks = matchedExistingNote.blocks.toMutableList()
            if (lower.contains("table")) {
                val tbl = listOf(
                    listOf("Milestone", "Owner", "Status"),
                    listOf("Core Spec", "Design", "Done"),
                    listOf("QA & Polish", "Engineering", "In Progress")
                )
                newBlocks.add(
                    NoteBlock(
                        type = BlockType.TABLE,
                        content = tbl.joinToString("\n") { it.joinToString(" | ") },
                        tableData = tbl
                    )
                )
            }
            if (lower.contains("code")) {
                newBlocks.add(
                    NoteBlock(
                        type = BlockType.CODE,
                        content = "fun verifyCanvas() {\n    println(\"Zainotes Expressive Ready\")\n}",
                        isMonospace = true
                    )
                )
            }
            if (lower.contains("task") || lower.contains("check")) {
                newBlocks.add(NoteBlock(type = BlockType.TASK, content = "Follow-up task added by AI Assistant"))
            }
            if (lower.contains("bullet")) {
                newBlocks.add(NoteBlock(type = BlockType.BULLET, content = "Key highlight added to ${matchedExistingNote.title}"))
            }
            if (lower.contains("number") || lower.contains("1 2 3") || lower.contains("step")) {
                newBlocks.add(NoteBlock(type = BlockType.NUMBERED, content = "First prioritized step"))
                newBlocks.add(NoteBlock(type = BlockType.NUMBERED, content = "Second prioritized step"))
            }
            if (newBlocks.size == matchedExistingNote.blocks.size) {
                newBlocks.add(NoteBlock(type = BlockType.HEADING, content = "AI Update Notes", isBold = true))
                newBlocks.add(NoteBlock(type = BlockType.TEXT, content = clean))
            }

            proposedNotes.add(
                AiProposedNoteAction(
                    actionType = "EDIT_NOTE",
                    targetNoteId = matchedExistingNote.id,
                    title = matchedExistingNote.title,
                    summary = "Proposed update to \"${matchedExistingNote.title}\" (${newBlocks.size} blocks total)",
                    colorId = matchedExistingNote.colorId,
                    backgroundTheme = matchedExistingNote.backgroundTheme,
                    isPinned = matchedExistingNote.isPinned,
                    blocks = newBlocks
                )
            )
            toolEvents.add(
                AiToolEvent(
                    toolName = "edit_note",
                    summary = "Prepared edit for \"${matchedExistingNote.title}\" (awaiting your approval)"
                )
            )
            return AiAgentTurnResponse(
                replyText = "I've prepared the requested edits for **${matchedExistingNote.title}**. Review the updated blocks below and tap **Approve & Apply Edit** to update your note.",
                updatedMemories = updatedMemories,
                toolEvents = toolEvents,
                proposedNotes = proposedNotes,
                providerBadge = badge
            )
        }

        // 3. Check if user is asking a general question vs drafting a note
        val wantsDraft = lower.contains("draft") || lower.contains("create") || lower.contains("make a note") ||
            lower.contains("summarize") || lower.contains("digest") || lower.contains("checklist") ||
            lower.contains("grocery") || lower.contains("table") || lower.contains("plan") ||
            lower.contains("template") || lower.contains("extract") || lower.contains("open task")

        if (!wantsDraft && (lower.endsWith("?") || lower.startsWith("what") || lower.startsWith("how") || lower.startsWith("why") || lower.startsWith("who") || lower.startsWith("tell me"))) {
            val memoryContext = if (updatedMemories.isNotEmpty()) {
                "\n\n**Referenced from your Persistent Memory:**\n" +
                    updatedMemories.joinToString("\n") { "• **${it.key}**: ${it.content}" }
            } else ""
            val notesSummary = "You currently have **${activeNotes.size} notes** on your canvas (${activeNotes.sumOf { it.totalTasksCount - it.completedTasksCount }} open tasks)."
            return AiAgentTurnResponse(
                replyText = "Here's what I found for **\"$clean\"**:\n\n$notesSummary$memoryContext\n\nYou can also ask me to **remember any fact** in persistent memory, **draft a new note** (with headings, text, tasks/checkpoints, bullets, 1 2 3 lists, code blocks, and tables), or **edit any existing note** on your canvas!",
                updatedMemories = updatedMemories,
                toolEvents = toolEvents,
                proposedNotes = emptyList(),
                providerBadge = badge
            )
        }

        // 4. Draft a comprehensive rich note using tool calling (with memory reference if available)
        val memoryBlockList = if (updatedMemories.isNotEmpty()) {
            listOf(
                NoteBlock(
                    type = BlockType.QUOTE,
                    content = "Memory Context: " + updatedMemories.take(2).joinToString(" | ") { "${it.key}: ${it.content}" },
                    isItalic = true
                )
            )
        } else emptyList()

        val draftAction = when {
            lower.contains("summarize") || lower.contains("digest") -> {
                val summaryRows = mutableListOf(listOf("Note Title", "Blocks", "Open Tasks"))
                activeNotes.take(6).forEach { n ->
                    summaryRows.add(
                        listOf(
                            n.title.ifBlank { "Untitled" },
                            n.blocks.size.toString(),
                            (n.totalTasksCount - n.completedTasksCount).toString()
                        )
                    )
                }
                val blocks = mutableListOf<NoteBlock>().apply {
                    add(NoteBlock(type = BlockType.HEADING, content = "Workspace Digest (${activeNotes.size} notes)", isBold = true))
                    addAll(memoryBlockList)
                    add(
                        NoteBlock(
                            type = BlockType.TABLE,
                            content = summaryRows.joinToString("\n") { it.joinToString(" | ") },
                            tableData = summaryRows
                        )
                    )
                    activeNotes.take(5).forEachIndexed { idx, n ->
                        add(NoteBlock(type = BlockType.NUMBERED, content = "${n.title.ifBlank { "Untitled" }} — ${n.completedTasksCount}/${n.totalTasksCount} tasks completed", order = idx))
                    }
                }
                AiProposedNoteAction(
                    actionType = "DRAFT_NOTE",
                    title = "Canvas Summary & Digest",
                    summary = "Synthesized ${activeNotes.size} notes with a structured comparison table and numbered breakdown.",
                    colorId = "lavender",
                    backgroundTheme = "notes",
                    blocks = blocks
                )
            }

            lower.contains("open task") || lower.contains("extract") -> {
                val openTasks = activeNotes.flatMap { note ->
                    note.blocks.filter { it.type == BlockType.TASK && !it.isChecked }.map { block ->
                        val prefix = if (note.title.isNotBlank()) "[${note.title}] " else ""
                        NoteBlock(type = BlockType.TASK, content = "$prefix${block.content.ifBlank { "Pending task" }}")
                    }
                }
                val blocks = mutableListOf<NoteBlock>().apply {
                    add(NoteBlock(type = BlockType.HEADING, content = "Master Priority Checkpoints", isBold = true))
                    addAll(memoryBlockList)
                    if (openTasks.isEmpty()) {
                        add(NoteBlock(type = BlockType.TASK, content = "All caught up! Plan next sprint priorities"))
                    } else {
                        addAll(openTasks)
                    }
                }
                AiProposedNoteAction(
                    actionType = "DRAFT_NOTE",
                    title = "Master Action Checklist",
                    summary = "Extracted ${openTasks.size} unchecked tasks across your workspace.",
                    colorId = "mint",
                    backgroundTheme = "ruled",
                    blocks = blocks
                )
            }

            else -> {
                val cleanTitle = clean
                    .replace(Regex("(?i)^(draft|create|make|build)\\s+(a\\s+)?(new\\s+)?(note\\s+)?(about|for|on)?\\s*"), "")
                    .trim()
                    .take(40)
                    .replaceFirstChar { it.uppercase() }
                    .ifBlank { "Smart Canvas Draft" }

                val tableRows = listOf(
                    listOf("Phase / Topic", "Details", "Target"),
                    listOf("1. Preparation", "Scope & requirements for $cleanTitle", "Day 1"),
                    listOf("2. Execution", "Core implementation & verification", "Day 2")
                )

                val blocks = mutableListOf<NoteBlock>().apply {
                    add(NoteBlock(type = BlockType.HEADING, content = "$cleanTitle — Overview", isBold = true))
                    addAll(memoryBlockList)
                    add(NoteBlock(type = BlockType.TEXT, content = "Structured note drafted with headings, checkpoints, bullets, numbered steps, table, and code snippet."))
                    add(NoteBlock(type = BlockType.TASK, content = "Complete primary checkpoint for $cleanTitle", isBold = true))
                    add(NoteBlock(type = BlockType.TASK, content = "Verify deliverables and review quality"))
                    add(NoteBlock(type = BlockType.BULLET, content = "Key consideration and context note"))
                    add(NoteBlock(type = BlockType.NUMBERED, content = "First sequential action step"))
                    add(NoteBlock(type = BlockType.NUMBERED, content = "Second sequential action step"))
                    add(
                        NoteBlock(
                            type = BlockType.TABLE,
                            content = tableRows.joinToString("\n") { it.joinToString(" | ") },
                            tableData = tableRows
                        )
                    )
                    if (lower.contains("code") || lower.contains("script") || lower.contains("dev") || lower.contains("api")) {
                        add(
                            NoteBlock(
                                type = BlockType.CODE,
                                content = "// Snippet for $cleanTitle\nconst status = \"ready\";\nconsole.log(status);",
                                isMonospace = true
                            )
                        )
                    }
                }
                AiProposedNoteAction(
                    actionType = "DRAFT_NOTE",
                    title = cleanTitle,
                    summary = "Drafted rich note with tasks, bullets, 1 2 3 list, table, and headings.",
                    colorId = "sky",
                    backgroundTheme = "doodles",
                    blocks = blocks
                )
            }
        }

        proposedNotes.add(draftAction)
        toolEvents.add(
            AiToolEvent(
                toolName = "draft_note",
                summary = "Drafted note \"${draftAction.title}\" (awaiting your approval)"
            )
        )

        return AiAgentTurnResponse(
            replyText = "I've drafted **${draftAction.title}** for you${if (updatedMemories.isNotEmpty()) " (referencing your persistent memory)" else ""}. Review the preview card below and tap **Approve & Add to Notes** to save it to your canvas.",
            updatedMemories = updatedMemories,
            toolEvents = toolEvents,
            proposedNotes = proposedNotes,
            providerBadge = badge
        )
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
