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
    val providerBadge: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val isCustomTitle: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<AiChatMessage> = emptyList()
)

data class AiStreamUpdate(
    val partialReplyText: String,
    val statusPhase: String,
    val suggestedSessionTitle: String? = null
)

data class AiAgentTurnResponse(
    val replyText: String,
    val updatedMemories: List<AiMemoryItem>,
    val toolEvents: List<AiToolEvent>,
    val proposedNotes: List<AiProposedNoteAction>,
    val providerBadge: String,
    val suggestedSessionTitle: String? = null
)

object AiProviderClient {
    private val gson = Gson()

    fun normalizeCustomBaseUrl(rawUrl: String, fallback: String = "https://api.openai.com/v1"): String {
        var u = rawUrl.trim()
        if (u.isBlank()) return fallback.trimEnd('/')
        if (!u.startsWith("http://", ignoreCase = true) && !u.startsWith("https://", ignoreCase = true)) {
            val isLocal = u.startsWith("localhost", ignoreCase = true) ||
                u.startsWith("127.") ||
                u.startsWith("10.") ||
                u.startsWith("192.168.") ||
                u.startsWith("0.0.0.0")
            u = (if (isLocal) "http://" else "https://") + u
        }
        u = u.trimEnd('/')
        if (u.endsWith("/chat/completions", ignoreCase = true)) {
            u = u.substring(0, u.length - "/chat/completions".length).trimEnd('/')
        } else if (u.endsWith("/models", ignoreCase = true)) {
            u = u.substring(0, u.length - "/models".length).trimEnd('/')
        }
        return u
    }

    fun isLocalEndpoint(url: String): Boolean {
        val lower = url.trim().lowercase()
        return lower.contains("127.0.0.1") ||
            lower.contains("localhost") ||
            lower.contains("10.0.2.2") ||
            lower.contains("192.168.") ||
            lower.contains("0.0.0.0")
    }

    /**
     * Generates a clean, descriptive topic title for a chat session instead of using the raw user message.
     */
    fun generateSmartSessionTitle(
        userPrompt: String,
        proposedNoteTitle: String? = null,
        aiSuggestedTitle: String? = null
    ): String {
        val cleanSuggested = aiSuggestedTitle?.trim()?.removeSurrounding("\"")?.trim().orEmpty()
        if (cleanSuggested.length in 3..48 && !cleanSuggested.equals("New Chat", ignoreCase = true)) {
            return cleanSuggested
        }
        val cleanNoteTitle = proposedNoteTitle?.trim().orEmpty()
        if (cleanNoteTitle.isNotBlank() && !cleanNoteTitle.equals("Untitled Note", ignoreCase = true)) {
            return cleanNoteTitle.take(45)
        }

        val normalized = userPrompt.trim().replace(Regex("\\s+"), " ")
        if (normalized.isBlank()) return "New Chat"

        val lower = normalized.lowercase()
        when {
            lower.startsWith("summarize my notes") -> return "Workspace Notes Summary"
            lower.startsWith("find and list all open tasks") || lower.startsWith("list open tasks") -> return "Open Tasks Overview"
            lower.startsWith("draft a clean daily checklist") || lower.startsWith("draft a checklist") -> return "Daily Checklist Draft"
            lower.startsWith("remember that ") -> {
                val fact = normalized.substring("remember that ".length).trim().trimEnd('.', '!', '?')
                val shortFact = fact.split(" ").take(5).joinToString(" ")
                return "Memory: ${shortFact.replaceFirstChar { it.uppercase() }}"
            }
        }

        // Strip conversational filler prefixes to extract the topic
        val stripped = normalized
            .replace(
                Regex(
                    "^(please\\s+|can you\\s+|could you\\s+|would you\\s+|help me\\s+|i want to\\s+|i need to\\s+|draft a clean\\s+|draft a\\s+|create a\\s+|write a\\s+|make a\\s+|give me a\\s+|tell me about\\s+)+",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )
            .trim()
            .trimEnd('.', '!', '?', ',', ';', ':')

        val candidate = stripped.ifBlank { normalized }
        val words = candidate.split(" ").filter { it.isNotBlank() }
        val concise = if (words.size > 6) {
            words.take(6).joinToString(" ")
        } else {
            words.joinToString(" ")
        }
        return concise.replaceFirstChar { it.uppercase() }.take(48)
    }

    /**
     * Fetches the available model names directly from the selected AI provider's API.
     */
    suspend fun fetchModelsFromApi(
        provider: AiProvider,
        apiKey: String,
        customBaseUrl: String = ""
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        val resolvedCustomBase = normalizeCustomBaseUrl(customBaseUrl, provider.defaultBaseUrl)
        val allowBlankKey = provider == AiProvider.CUSTOM && isLocalEndpoint(resolvedCustomBase)
        if (cleanKey.isBlank() && !allowBlankKey) {
            return@withContext Result.failure(IllegalArgumentException("Please enter an API key first."))
        }

        try {
            when (provider) {
                AiProvider.GEMINI -> {
                    val endpoint = "https://generativelanguage.googleapis.com/v1beta/models?key=$cleanKey"
                    val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 25000
                        readTimeout = 45000
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
                        connectTimeout = 25000
                        readTimeout = 45000
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
                    val base = if (provider == AiProvider.CUSTOM) {
                        resolvedCustomBase
                    } else {
                        provider.defaultBaseUrl.trimEnd('/')
                    }
                    val endpoint = "$base/models"
                    val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        if (cleanKey.isNotBlank()) {
                            setRequestProperty("Authorization", "Bearer $cleanKey")
                        }
                        setRequestProperty("Accept", "application/json")
                        connectTimeout = 25000
                        readTimeout = 45000
                    }
                    val code = conn.responseCode
                    val body = readStream(conn, code in 200..299)
                    if (code !in 200..299) {
                        return@withContext Result.failure(Exception("${provider.displayName} HTTP $code: ${extractErrorMsg(body)}"))
                    }
                    val root = JsonParser.parseString(body).asJsonObject
                    val arr = root.getAsJsonArray("data") ?: root.getAsJsonArray("models") ?: JsonArray()
                    val names = arr.mapNotNull {
                        val obj = it.asJsonObject
                        obj.get("id")?.asString ?: obj.get("name")?.asString?.removePrefix("models/")
                    }.distinct()
                    Result.success(if (names.isNotEmpty()) names else provider.suggestedModels)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Runs a live AI chat turn with Server-Sent Events (SSE) token streaming,
     * Persistent Memory, and Note Drafting/Editing tools.
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
        attachedImages: List<AiAttachedImage> = emptyList(),
        onStreamUpdate: (AiStreamUpdate) -> Unit = {}
    ): AiAgentTurnResponse = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        val chosenModel = modelName.ifBlank { provider.defaultModel }
        val resolvedCustomBase = normalizeCustomBaseUrl(customBaseUrl, provider.defaultBaseUrl)
        val allowBlankKey = provider == AiProvider.CUSTOM && isLocalEndpoint(resolvedCustomBase)

        if (cleanKey.isBlank() && !allowBlankKey) {
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
                AiProvider.GEMINI -> callGeminiStreamingAgent(
                    apiKey = cleanKey,
                    model = chosenModel,
                    systemInstruction = systemInstruction,
                    history = chatHistory,
                    userPrompt = enrichedPrompt,
                    attachedImages = attachedImages,
                    onStreamUpdate = onStreamUpdate
                )
                AiProvider.ANTHROPIC -> callAnthropicStreamingAgent(
                    apiKey = cleanKey,
                    model = chosenModel,
                    systemInstruction = systemInstruction,
                    history = chatHistory,
                    userPrompt = enrichedPrompt,
                    onStreamUpdate = onStreamUpdate
                )
                else -> {
                    val base = if (provider == AiProvider.CUSTOM) {
                        resolvedCustomBase
                    } else {
                        provider.defaultBaseUrl.trimEnd('/')
                    }
                    callOpenAiCompatibleStreamingAgent(
                        baseUrl = base,
                        apiKey = cleanKey,
                        model = chosenModel,
                        systemInstruction = systemInstruction,
                        history = chatHistory,
                        userPrompt = enrichedPrompt,
                        onStreamUpdate = onStreamUpdate
                    )
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
            "None."
        } else {
            memories.take(20).joinToString("\n") { "- [id=${it.id}] ${it.key}: ${it.content}" }
        }

        val notesText = if (notes.isEmpty()) {
            "None."
        } else {
            notes.take(15).joinToString("\n") { n ->
                val blockPreview = n.blocks.take(8).joinToString("; ") { b ->
                    when (b.type) {
                        BlockType.TASK -> "[${if (b.isChecked) "x" else " "}] ${b.content.take(80)}"
                        BlockType.TABLE -> "[TABLE: ${b.resolvedTable().take(3).joinToString(" / ") { r -> r.joinToString("|") }}]"
                        else -> "(${b.type.name}) ${b.content.take(80)}"
                    }
                }
                "- Note #${n.id} (title=\"${n.title}\", color=\"${n.colorId}\", theme=\"${n.backgroundTheme}\"): $blockPreview"
            }
        }

        return """
You are Zainotes Smart AI Assistant with Persistent Memory and Note Canvas Tools.
Respond ONLY with a valid JSON object (no markdown fences) matching this schema:
{
  "reply": "Your helpful, well-formatted conversational response (supports Markdown bold, bullets, headings, code).",
  "session_title": "Short 2 to 5 word topic title for this chat session",
  "memory_actions": [
    {
      "action": "save" | "update" | "delete",
      "key": "Memory topic",
      "content": "Details to remember"
    }
  ],
  "note_actions": [
    {
      "action": "draft_note" | "edit_note",
      "targetNoteId": 1,
      "title": "Note Title",
      "summary": "Concise 1-sentence description of the note draft or edits",
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
          "tableData": [["Header 1", "Header 2"], ["Row 1", "Row 2"]]
        }
      ]
    }
  ]
}

PERSISTENT MEMORY:
$memText

WORKSPACE NOTES:
$notesText
""".trimIndent()
    }

    /**
     * Extracts the live human-readable `"reply"` value while the JSON response is still streaming via SSE.
     */
    internal fun extractLiveStreamState(
        rawAccumulated: String,
        hasReasoningStream: Boolean = false
    ): AiStreamUpdate {
        val openThink = rawAccumulated.indexOf("<think>", ignoreCase = true)
        val closeThink = rawAccumulated.indexOf("</think>", ignoreCase = true)
        if (openThink >= 0 && closeThink < 0) {
            return AiStreamUpdate(
                partialReplyText = "",
                statusPhase = "Reasoning through request..."
            )
        }

        val withoutThink = rawAccumulated
            .replace(Regex("<think>[\\s\\S]*?</think>", RegexOption.IGNORE_CASE), "")
            .trim()

        if (withoutThink.isBlank()) {
            return AiStreamUpdate(
                partialReplyText = "",
                statusPhase = if (hasReasoningStream) "Reasoning through request..." else "Receiving stream..."
            )
        }

        val stripped = withoutThink
            .removePrefix("```json")
            .removePrefix("```")
            .trimStart()

        // If the model isn't outputting JSON at all (e.g., a raw markdown response from a custom model)
        if (!stripped.startsWith("{") && !stripped.contains("\"reply\"")) {
            return AiStreamUpdate(
                partialReplyText = stripped.removeSuffix("```").trim(),
                statusPhase = "Streaming response..."
            )
        }

        val replyKeyMatch = Regex("\"reply\"\\s*:\\s*\"").find(stripped)
            ?: return AiStreamUpdate(
                partialReplyText = "",
                statusPhase = "Preparing response..."
            )

        val valueStart = replyKeyMatch.range.last + 1
        val sb = StringBuilder()
        var i = valueStart
        var replyFinished = false

        while (i < stripped.length) {
            val c = stripped[i]
            if (c == '\\') {
                if (i + 1 >= stripped.length) break // wait for next chunk to complete escape
                val next = stripped[i + 1]
                when (next) {
                    'n' -> { sb.append('\n'); i += 2 }
                    'r' -> { i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    '"' -> { sb.append('"'); i += 2 }
                    '\\' -> { sb.append('\\'); i += 2 }
                    '/' -> { sb.append('/'); i += 2 }
                    'u' -> {
                        if (i + 5 < stripped.length) {
                            val hex = stripped.substring(i + 2, i + 6)
                            val codePoint = hex.toIntOrNull(16)
                            if (codePoint != null) sb.append(codePoint.toChar())
                            i += 6
                        } else {
                            break
                        }
                    }
                    else -> {
                        sb.append(next)
                        i += 2
                    }
                }
            } else if (c == '"') {
                replyFinished = true
                break
            } else {
                sb.append(c)
                i++
            }
        }

        val tailAfterReply = if (replyFinished && i < stripped.length) stripped.substring(i) else ""
        val phase = when {
            !replyFinished -> "Streaming response..."
            tailAfterReply.contains("\"note_actions\"") -> "Drafting note blocks..."
            tailAfterReply.contains("\"memory_actions\"") -> "Updating memory..."
            else -> "Finalizing response..."
        }

        val titleMatch = Regex("\"session_title\"\\s*:\\s*\"([^\"]+)\"").find(stripped)
        val extractedTitle = titleMatch?.groupValues?.getOrNull(1)?.trim()

        return AiStreamUpdate(
            partialReplyText = sb.toString(),
            statusPhase = phase,
            suggestedSessionTitle = extractedTitle
        )
    }

    private fun callGeminiStreamingAgent(
        apiKey: String,
        model: String,
        systemInstruction: String,
        history: List<AiChatMessage>,
        userPrompt: String,
        attachedImages: List<AiAttachedImage> = emptyList(),
        onStreamUpdate: (AiStreamUpdate) -> Unit = {}
    ): Result<String> {
        val streamEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
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

        val conn = (URL(streamEndpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "text/event-stream")
            connectTimeout = 45000
            readTimeout = 300000
        }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(gson.toJson(payload)) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val errBody = readStream(conn, false)
            return Result.failure(Exception("Gemini HTTP $code: ${extractErrorMsg(errBody)}"))
        }

        val accumulated = StringBuilder()
        val rawFallback = StringBuilder()

        BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                rawFallback.append(currentLine).append('\n')
                val trimmed = currentLine.trim()
                if (trimmed.startsWith("data:")) {
                    val dataStr = trimmed.removePrefix("data:").trim()
                    if (dataStr.isEmpty() || dataStr == "[DONE]") continue
                    runCatching {
                        val chunkObj = JsonParser.parseString(dataStr).asJsonObject
                        val parts = chunkObj.getAsJsonArray("candidates")
                            ?.firstOrNull()?.asJsonObject
                            ?.getAsJsonObject("content")
                            ?.getAsJsonArray("parts")
                        parts?.forEach { p ->
                            val pObj = p.asJsonObject
                            val isThought = pObj.get("thought")?.asBoolean == true
                            val chunkText = pObj.get("text")?.asString.orEmpty()
                            if (isThought) {
                                onStreamUpdate(
                                    AiStreamUpdate(
                                        partialReplyText = "",
                                        statusPhase = "Reasoning through request..."
                                    )
                                )
                            } else if (chunkText.isNotEmpty()) {
                                accumulated.append(chunkText)
                                onStreamUpdate(extractLiveStreamState(accumulated.toString()))
                            }
                        }
                    }
                }
            }
        }

        val finalStr = accumulated.toString().trim()
        if (finalStr.isNotEmpty()) {
            return Result.success(finalStr)
        }

        // Fallback if server returned non-SSE JSON response
        val fallbackBody = rawFallback.toString().trim()
        if (fallbackBody.isNotEmpty()) {
            runCatching {
                val root = JsonParser.parseString(fallbackBody).asJsonObject
                val parts = root.getAsJsonArray("candidates")
                    ?.firstOrNull()?.asJsonObject
                    ?.getAsJsonObject("content")
                    ?.getAsJsonArray("parts")
                val text = parts
                    ?.mapNotNull { it.asJsonObject.get("text")?.asString }
                    ?.lastOrNull { it.isNotBlank() }
                if (!text.isNullOrBlank()) return Result.success(text)
            }
        }

        return Result.failure(Exception("Empty response from Gemini"))
    }

    private fun callOpenAiCompatibleStreamingAgent(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemInstruction: String,
        history: List<AiChatMessage>,
        userPrompt: String,
        onStreamUpdate: (AiStreamUpdate) -> Unit = {}
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
            addProperty("stream", true)
        }

        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            val effectiveKey = apiKey.ifBlank { "local-key" }
            setRequestProperty("Authorization", "Bearer $effectiveKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "text/event-stream, application/json")
            connectTimeout = 45000
            readTimeout = 300000
        }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(gson.toJson(payload)) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val errBody = readStream(conn, false)
            return Result.failure(Exception("API HTTP $code: ${extractErrorMsg(errBody)}"))
        }

        val accumulated = StringBuilder()
        val rawFallback = StringBuilder()
        var sawReasoningDelta = false

        BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                rawFallback.append(currentLine).append('\n')
                val trimmed = currentLine.trim()
                if (trimmed.startsWith("data:")) {
                    val dataStr = trimmed.removePrefix("data:").trim()
                    if (dataStr.isEmpty() || dataStr == "[DONE]") continue
                    runCatching {
                        val chunkObj = JsonParser.parseString(dataStr).asJsonObject
                        val choiceObj = chunkObj.getAsJsonArray("choices")
                            ?.firstOrNull()?.asJsonObject
                        val deltaObj = choiceObj?.getAsJsonObject("delta")
                            ?: choiceObj?.getAsJsonObject("message")

                        if (deltaObj != null) {
                            val reasoningChunk = deltaObj.get("reasoning_content")?.takeIf { !it.isJsonNull }?.asString
                                ?: deltaObj.get("reasoning")?.takeIf { !it.isJsonNull }?.asString
                            if (!reasoningChunk.isNullOrEmpty()) {
                                sawReasoningDelta = true
                            }
                            val contentChunk = deltaObj.get("content")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
                            if (contentChunk.isNotEmpty()) {
                                accumulated.append(contentChunk)
                            }
                            onStreamUpdate(
                                extractLiveStreamState(
                                    rawAccumulated = accumulated.toString(),
                                    hasReasoningStream = sawReasoningDelta && accumulated.isEmpty()
                                )
                            )
                        }
                    }
                }
            }
        }

        val finalStr = accumulated.toString().trim()
        if (finalStr.isNotEmpty()) {
            return Result.success(finalStr)
        }

        // Fallback if endpoint returned a standard non-SSE JSON payload
        val fallbackBody = rawFallback.toString().trim()
        if (fallbackBody.isNotEmpty()) {
            runCatching {
                val root = JsonParser.parseString(fallbackBody).asJsonObject
                val text = root.getAsJsonArray("choices")
                    ?.firstOrNull()?.asJsonObject
                    ?.getAsJsonObject("message")
                    ?.get("content")?.asString
                if (!text.isNullOrBlank()) {
                    onStreamUpdate(extractLiveStreamState(text))
                    return Result.success(text)
                }
            }
        }

        return Result.failure(Exception("Empty response from provider"))
    }

    private fun callAnthropicStreamingAgent(
        apiKey: String,
        model: String,
        systemInstruction: String,
        history: List<AiChatMessage>,
        userPrompt: String,
        onStreamUpdate: (AiStreamUpdate) -> Unit = {}
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
            addProperty("stream", true)
        }

        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("x-api-key", apiKey)
            setRequestProperty("anthropic-version", "2023-06-01")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "text/event-stream")
            connectTimeout = 45000
            readTimeout = 300000
        }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(gson.toJson(payload)) }
        val code = conn.responseCode
        if (code !in 200..299) {
            val errBody = readStream(conn, false)
            return Result.failure(Exception("Anthropic HTTP $code: ${extractErrorMsg(errBody)}"))
        }

        val accumulated = StringBuilder()
        val rawFallback = StringBuilder()

        BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                rawFallback.append(currentLine).append('\n')
                val trimmed = currentLine.trim()
                if (trimmed.startsWith("data:")) {
                    val dataStr = trimmed.removePrefix("data:").trim()
                    if (dataStr.isEmpty() || dataStr == "[DONE]") continue
                    runCatching {
                        val eventObj = JsonParser.parseString(dataStr).asJsonObject
                        val type = eventObj.get("type")?.asString.orEmpty()
                        if (type == "content_block_delta") {
                            val delta = eventObj.getAsJsonObject("delta")
                            val deltaType = delta?.get("type")?.asString.orEmpty()
                            if (deltaType == "thinking_delta") {
                                onStreamUpdate(
                                    AiStreamUpdate(
                                        partialReplyText = "",
                                        statusPhase = "Reasoning through request..."
                                    )
                                )
                            } else {
                                val chunkText = delta?.get("text")?.asString.orEmpty()
                                if (chunkText.isNotEmpty()) {
                                    accumulated.append(chunkText)
                                    onStreamUpdate(extractLiveStreamState(accumulated.toString()))
                                }
                            }
                        }
                    }
                }
            }
        }

        val finalStr = accumulated.toString().trim()
        if (finalStr.isNotEmpty()) {
            return Result.success(finalStr)
        }

        val fallbackBody = rawFallback.toString().trim()
        if (fallbackBody.isNotEmpty()) {
            runCatching {
                val root = JsonParser.parseString(fallbackBody).asJsonObject
                val text = root.getAsJsonArray("content")
                    ?.firstOrNull()?.asJsonObject
                    ?.get("text")?.asString
                if (!text.isNullOrBlank()) return Result.success(text)
            }
        }

        return Result.failure(Exception("Empty response from Anthropic"))
    }

    private fun parseAgentStructuredResponse(
        rawText: String,
        currentMemories: List<AiMemoryItem>,
        activeNotes: List<Note>,
        badge: String
    ): AiAgentTurnResponse {
        // Strip <think>...</think> blocks emitted by reasoning models
        val withoutThink = rawText.replace(Regex("<think>[\\s\\S]*?</think>", RegexOption.IGNORE_CASE), "").trim()
        val fenceStripped = withoutThink
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val firstBrace = fenceStripped.indexOf('{')
        val lastBrace = fenceStripped.lastIndexOf('}')
        val cleaned = if (firstBrace >= 0 && lastBrace > firstBrace) {
            fenceStripped.substring(firstBrace, lastBrace + 1)
        } else {
            fenceStripped
        }

        val updatedMemories = currentMemories.toMutableList()
        val toolEvents = mutableListOf<AiToolEvent>()
        val proposedNotes = mutableListOf<AiProposedNoteAction>()

        return try {
            val obj = JsonParser.parseString(cleaned).asJsonObject
            val reply = obj.get("reply")?.takeIf { !it.isJsonNull }?.asString ?: withoutThink
            val suggestedTitle = obj.get("session_title")?.takeIf { !it.isJsonNull }?.asString?.trim()

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
                val rawSummary = nObj.get("summary")?.takeIf { !it.isJsonNull }?.asString?.trim().orEmpty()
                val summary = if (
                    rawSummary.length <= 6 ||
                    rawSummary.equals("blocks", ignoreCase = true) ||
                    rawSummary.equals("note", ignoreCase = true) ||
                    rawSummary.equals(title, ignoreCase = true)
                ) {
                    if (isEdit) "Proposed edits to \"$title\"" else "Drafted note \"$title\""
                } else {
                    rawSummary
                }
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
                providerBadge = badge,
                suggestedSessionTitle = suggestedTitle
            )
        } catch (_: Exception) {
            // Even if full JSON parsing fails (e.g. truncated or non-JSON reply), extract partial "reply" if present
            val fallbackExtracted = extractLiveStreamState(withoutThink).partialReplyText.ifBlank { withoutThink }
            AiAgentTurnResponse(
                replyText = fallbackExtracted,
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
