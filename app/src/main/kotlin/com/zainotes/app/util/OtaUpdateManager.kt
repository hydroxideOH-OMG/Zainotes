/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class OtaReleaseInfo(
    val tagName: String,
    val title: String,
    val changelogMarkdown: String,
    val publishedAt: String,
    val htmlUrl: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val isPreRelease: Boolean = false
) {
    val cleanVersion: String
        get() = tagName.trim().removePrefix("v").removePrefix("V").ifBlank { "2.0.0" }
}

data class OtaCheckResult(
    val currentVersion: String,
    val latestRelease: OtaReleaseInfo,
    val allReleases: List<OtaReleaseInfo>,
    val isUpdateAvailable: Boolean,
    val checkedAtMillis: Long = System.currentTimeMillis(),
    val isOfflineFallback: Boolean = false
)

data class OtaDownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long
) {
    val progressFraction: Float
        get() = if (totalBytes > 0L) {
            (bytesDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val progressPercent: Int
        get() = (progressFraction * 100f).toInt().coerceIn(0, 100)
}

enum class InstallLaunchResult {
    LAUNCHED,
    NEEDS_UNKNOWN_SOURCES_PERMISSION,
    ERROR
}

object OtaUpdateManager {

    const val GITHUB_OWNER = "hydroxideOH-OMG"
    const val GITHUB_REPO = "Zainotes"
    const val GITHUB_RELEASES_API = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases"
    const val GITHUB_RELEASES_WEB = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases"
    private const val DEFAULT_LATEST_APK_URL =
        "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/latest/download/Zainotes-signed.apk"

    fun getInstalledVersionName(context: Context): String {
        return try {
            val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pkgInfo.versionName?.trim()?.ifBlank { "2.0.0" } ?: "2.0.0"
        } catch (_: Exception) {
            "2.0.0"
        }
    }

    /**
     * Compares two semantic version strings (e.g., "v2.1.0" vs "2.0.0").
     * Returns > 0 if [versionA] is newer than [versionB], < 0 if older, 0 if equal.
     */
    fun compareVersions(versionA: String, versionB: String): Int {
        val partsA = parseVersionNumbers(versionA)
        val partsB = parseVersionNumbers(versionB)
        val maxLen = maxOf(partsA.size, partsB.size, 3)
        for (i in 0 until maxLen) {
            val a = partsA.getOrElse(i) { 0 }
            val b = partsB.getOrElse(i) { 0 }
            if (a != b) return a.compareTo(b)
        }
        return 0
    }

    private fun parseVersionNumbers(raw: String): List<Int> {
        val cleaned = raw.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .substringBefore("+")
        return cleaned.split(".")
            .mapNotNull { segment ->
                segment.filter { it.isDigit() }.toIntOrNull()
            }
            .ifEmpty { listOf(0, 0, 0) }
    }

    /**
     * Queries GitHub Releases API for all releases of Zainotes, parses changelogs and APK assets,
     * and determines whether a newer version is available.
     */
    suspend fun checkForUpdates(context: Context): Result<OtaCheckResult> = withContext(Dispatchers.IO) {
        val currentVersion = getInstalledVersionName(context)
        try {
            val url = URL(GITHUB_RELEASES_API)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 15_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Zainotes-Android-OTA/$currentVersion")
            }

            val code = conn.responseCode
            if (code !in 200..299) {
                conn.disconnect()
                return@withContext Result.success(
                    buildFallbackResult(currentVersion = currentVersion, isOffline = true)
                )
            }

            val responseBody = BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                reader.readText()
            }
            conn.disconnect()

            val jsonArray = JSONArray(responseBody)
            val parsedReleases = mutableListOf<OtaReleaseInfo>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                if (obj.optBoolean("draft", false)) continue
                parsedReleases.add(parseReleaseObject(obj))
            }

            // Ensure bundled changelogs are also available if not already in the GitHub response
            val fallbackReleases = getBundledReleaseHistory()
            fallbackReleases.forEach { bundled ->
                if (parsedReleases.none { compareVersions(it.tagName, bundled.tagName) == 0 }) {
                    parsedReleases.add(bundled)
                }
            }

            val sortedReleases = parsedReleases.sortedWith { a, b ->
                compareVersions(b.tagName, a.tagName)
            }

            val latest = sortedReleases.firstOrNull { !it.isPreRelease }
                ?: sortedReleases.firstOrNull()
                ?: fallbackReleases.first()

            val updateAvailable = compareVersions(latest.tagName, currentVersion) > 0

            Result.success(
                OtaCheckResult(
                    currentVersion = currentVersion,
                    latestRelease = latest,
                    allReleases = sortedReleases,
                    isUpdateAvailable = updateAvailable,
                    isOfflineFallback = false
                )
            )
        } catch (_: Exception) {
            Result.success(
                buildFallbackResult(currentVersion = currentVersion, isOffline = true)
            )
        }
    }

    private fun parseReleaseObject(obj: JSONObject): OtaReleaseInfo {
        val tagName = obj.optString("tag_name", "v2.0.0").ifBlank { "v2.0.0" }
        val name = obj.optString("name", "").ifBlank { "Zainotes $tagName" }
        val rawBody = obj.optString("body", "").trim()
        val publishedRaw = obj.optString("published_at", "")
        val htmlUrl = obj.optString("html_url", GITHUB_RELEASES_WEB).ifBlank { GITHUB_RELEASES_WEB }
        val isPreRelease = obj.optBoolean("prerelease", false)

        var apkUrl = ""
        var apkName = "Zainotes-$tagName.apk"
        var apkSize = 0L

        val assets = obj.optJSONArray("assets")
        if (assets != null) {
            for (j in 0 until assets.length()) {
                val asset = assets.optJSONObject(j) ?: continue
                val assetName = asset.optString("name", "")
                val contentType = asset.optString("content_type", "")
                val downloadUrl = asset.optString("browser_download_url", "")
                if (assetName.endsWith(".apk", ignoreCase = true) ||
                    contentType.contains("android.package-archive", ignoreCase = true)
                ) {
                    apkUrl = downloadUrl
                    apkName = assetName.ifBlank { apkName }
                    apkSize = asset.optLong("size", 0L)
                    break
                }
            }
        }

        if (apkUrl.isBlank()) {
            apkUrl = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/download/$tagName/Zainotes-signed.apk"
        }

        val resolvedChangelog = rawBody.ifBlank {
            getBundledReleaseHistory()
                .firstOrNull { compareVersions(it.tagName, tagName) == 0 }
                ?.changelogMarkdown
                ?: "### Highlights in $name\n- Performance improvements, bug fixes, and Material 3 Expressive refinements."
        }

        return OtaReleaseInfo(
            tagName = tagName,
            title = name,
            changelogMarkdown = resolvedChangelog,
            publishedAt = formatPublishedDate(publishedRaw),
            htmlUrl = htmlUrl,
            apkDownloadUrl = apkUrl,
            apkFileName = apkName,
            apkSizeBytes = apkSize,
            isPreRelease = isPreRelease
        )
    }

    private fun formatPublishedDate(isoString: String): String {
        if (isoString.isBlank()) return "Official GitHub Release"
        val datePart = isoString.substringBefore("T")
        val pieces = datePart.split("-")
        if (pieces.size != 3) return datePart
        val year = pieces[0]
        val month = when (pieces[1]) {
            "01" -> "Jan"
            "02" -> "Feb"
            "03" -> "Mar"
            "04" -> "Apr"
            "05" -> "May"
            "06" -> "Jun"
            "07" -> "Jul"
            "08" -> "Aug"
            "09" -> "Sep"
            "10" -> "Oct"
            "11" -> "Nov"
            "12" -> "Dec"
            else -> pieces[1]
        }
        val day = pieces[2].trimStart('0').ifEmpty { "1" }
        return "$month $day, $year"
    }

    /**
     * Downloads the release APK from GitHub into the app's OTA cache folder with real-time progress.
     */
    suspend fun downloadReleaseApk(
        context: Context,
        release: OtaReleaseInfo,
        onProgress: (OtaDownloadProgress) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val otaDir = File(context.cacheDir, "ota_updates").apply {
            if (!exists()) mkdirs()
        }
        val safeName = release.apkFileName
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .ifBlank { "Zainotes-${release.cleanVersion}.apk" }
        val destinationFile = File(otaDir, safeName)

        val candidateUrls = listOf(
            release.apkDownloadUrl,
            DEFAULT_LATEST_APK_URL,
            "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/download/${release.tagName}/app-release.apk"
        ).filter { it.isNotBlank() }.distinct()

        var lastError: Exception? = null

        for (candidateUrl in candidateUrls) {
            try {
                val downloaded = streamDownloadWithRedirects(
                    urlString = candidateUrl,
                    destFile = destinationFile,
                    fallbackSize = release.apkSizeBytes,
                    onProgress = onProgress
                )
                if (downloaded.exists() && downloaded.length() > 10_240L) {
                    return@withContext Result.success(downloaded)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                destinationFile.delete()
                throw e
            } catch (e: Exception) {
                lastError = e
                destinationFile.delete()
            }
        }

        Result.failure(
            lastError ?: Exception("Could not download APK from GitHub Releases. Please check your internet connection.")
        )
    }

    private suspend fun streamDownloadWithRedirects(
        urlString: String,
        destFile: File,
        fallbackSize: Long,
        onProgress: (OtaDownloadProgress) -> Unit
    ): File = withContext(Dispatchers.IO) {
        var currentUrl = urlString
        var redirects = 0
        var connection: HttpURLConnection? = null

        while (redirects < 6) {
            val url = URL(currentUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 30_000
                setRequestProperty("Accept", "application/octet-stream, */*")
                setRequestProperty("User-Agent", "Zainotes-Android-OTA")
            }
            val status = conn.responseCode
            if (status in listOf(
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_SEE_OTHER,
                    307,
                    308
                )
            ) {
                val location = conn.getHeaderField("Location")
                conn.disconnect()
                if (location.isNullOrBlank()) break
                currentUrl = location
                redirects++
            } else {
                connection = conn
                break
            }
        }

        val activeConn = connection ?: throw Exception("Too many redirects while connecting to GitHub")
        try {
            val code = activeConn.responseCode
            if (code !in 200..299) {
                throw Exception("GitHub returned HTTP $code for APK download")
            }

            val totalBytes = activeConn.contentLengthLong.takeIf { it > 0L } ?: fallbackSize
            var downloadedBytes = 0L
            val startTime = System.currentTimeMillis()
            var lastEmitTime = 0L

            onProgress(OtaDownloadProgress(0L, totalBytes, 0L))

            BufferedInputStream(activeConn.inputStream, 32 * 1024).use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var read = input.read(buffer)
                    while (read != -1) {
                        coroutineContext.ensureActive()
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val now = System.currentTimeMillis()
                        if (now - lastEmitTime >= 80L || downloadedBytes == totalBytes) {
                            val elapsedSec = ((now - startTime) / 1000.0).coerceAtLeast(0.15)
                            val speed = (downloadedBytes / elapsedSec).toLong()
                            onProgress(
                                OtaDownloadProgress(
                                    bytesDownloaded = downloadedBytes,
                                    totalBytes = maxOf(totalBytes, downloadedBytes),
                                    speedBytesPerSec = speed
                                )
                            )
                            lastEmitTime = now
                        }
                        read = input.read(buffer)
                    }
                    output.flush()
                }
            }
            destFile
        } finally {
            activeConn.disconnect()
        }
    }

    /**
     * Checks whether the app has permission to request APK package installation on Android 8.0+.
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens the system "Install unknown apps" settings screen for Zainotes on Android 8.0+.
     */
    fun openUnknownSourcesSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Launches the Android system package installer to install the downloaded [apkFile].
     */
    fun promptInstallApk(context: Context, apkFile: File): InstallLaunchResult {
        if (!apkFile.exists()) return InstallLaunchResult.ERROR
        if (!canRequestPackageInstalls(context)) {
            return InstallLaunchResult.NEEDS_UNKNOWN_SOURCES_PERMISSION
        }
        return try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            InstallLaunchResult.LAUNCHED
        } catch (_: Exception) {
            InstallLaunchResult.ERROR
        }
    }

    fun formatByteSize(bytes: Long): String {
        if (bytes <= 0L) return ""
        val kb = bytes / 1024.0
        if (kb < 1024.0) {
            return String.format(Locale.US, "%.0f KB", kb.coerceAtLeast(1.0))
        }
        val mb = kb / 1024.0
        return String.format(Locale.US, "%.1f MB", mb)
    }

    private fun buildFallbackResult(currentVersion: String, isOffline: Boolean): OtaCheckResult {
        val history = getBundledReleaseHistory()
        val latest = history.first()
        val updateAvailable = compareVersions(latest.tagName, currentVersion) > 0
        return OtaCheckResult(
            currentVersion = currentVersion,
            latestRelease = latest,
            allReleases = history,
            isUpdateAvailable = updateAvailable,
            isOfflineFallback = isOffline
        )
    }

    fun getBundledReleaseHistory(): List<OtaReleaseInfo> {
        return listOf(
            OtaReleaseInfo(
                tagName = "v2.1.0",
                title = "Zainotes v2.1.0 — Embedded Media, Accent Palettes & OTA Updater",
                publishedAt = "Official Release",
                htmlUrl = "$GITHUB_RELEASES_WEB/tag/v2.1.0",
                apkDownloadUrl = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/download/v2.1.0/Zainotes-signed.apk",
                apkFileName = "Zainotes-v2.1.0.apk",
                apkSizeBytes = 8_650_000L,
                changelogMarkdown = """
# Zainotes v2.1.0

Welcome to **Zainotes v2.1.0**! This release introduces rich multimedia note attachments, customizable theme accent color palettes, note tagging, an in-app GitHub OTA updater with formatted changelogs, AI smart session titles, and refined Material 3 Expressive UI components.

---

## What is New in Version 2.1.0

### 1. Note Tagging & Filtering
- Create, assign, and remove custom `#tags` on notes from the Editor tags bar, floating dock, or Home card long-press menu.
- Filter notes on the Home screen using Material 3 Expressive tag filter chips with live note counts.

### 2. GitHub OTA Updater & Changelog Viewer
- Automatically checks GitHub Releases (`hydroxideOH-OMG/Zainotes`) for new versions on startup (when enabled) and manually from Settings.
- Informs you when a new release is available, streams the APK download upon your approval with real-time progress, and launches the Android package installer.
- Built-in multi-version Markdown changelog renderer with Material 3 Expressive typography, callouts, code blocks, and version switcher chips.

### 3. Embedded Media Attachments
- **Image Blocks**: Embed photos and pictures with high-resolution rendering, rounded corner framing, and full-screen preview.
- **Audio Blocks**: Attach voice notes and audio clips with an interactive waveform player, play/pause controls, and duration scrubbing slider.
- **Video Blocks**: Attach video files with integrated playback and full-screen viewing support.
- **Persistent Internal Storage**: Media attachments are safely copied and indexed in the app private internal storage to prevent broken links.

### 4. Theme Accent Color Palettes
- Choose from 8 handcrafted accent palettes when Dynamic Wallpaper Color (Monet) is disabled:
  - Purple
  - Ocean Blue
  - Emerald
  - Rose Pink
  - Sunset Gold
  - Coral Red
  - Aqua Teal
  - Indigo
- Seamlessly adapts across Light and pure `#000000` AMOLED modes.

### 5. Smart AI Chat Session Titles & Streaming
- Real-time Server-Sent Events (SSE) token streaming across supported AI providers.
- Automatic generation of concise, descriptive topic titles for AI chat threads with inline session renaming.
                """.trimIndent()
            ),
            OtaReleaseInfo(
                tagName = "v2.0.0",
                title = "Zainotes v2.0.0 — Expressive AI, Media & Tagging Milestone",
                publishedAt = "Previous Release",
                htmlUrl = "$GITHUB_RELEASES_WEB/tag/v2.0.0",
                apkDownloadUrl = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/download/v2.0.0/Zainotes-signed.apk",
                apkFileName = "Zainotes-v2.0.0.apk",
                apkSizeBytes = 8_420_000L,
                changelogMarkdown = """
# Zainotes v2.0.0

A major milestone release for **Zainotes (`com.zainotes.app`)**. This release introduces full R8 code shrinking, real-time streaming AI assistant chat with session history, media file attachments, note tagging, OTA GitHub updates, and Material 3 Expressive components.

---

### Key Highlights in Version 2.0.0

#### 1. Note Tagging & Media Attachments
- Organize notes with custom `#tags`, filter your Home canvas by tag chips, and manage tags from the editor or card menu.
- Attach **Images**, **Audio**, and **Video** files directly inside notes using the `+` button on the left of the bottom editor toolbar.
- Built-in waveform audio player and interactive video preview cards.

#### 2. Native AI Assistant Chat & Streaming
- Dedicated Material 3 Expressive AI chat workspace with real-time Server-Sent Events (SSE) token streaming.
- Multi-provider AI support (Google Gemini, OpenAI, Groq, OpenRouter, and Custom/Local HTTP endpoints).
- Persistent chat session history with smart titles and inline session renaming.
- Contextual note drafting, task checklists, and persistent AI memory.

#### 3. Enhanced Rich Note Blocks
- Extended note block system with rich formatting, code blocks, quote callouts, and interactive tables.
- Sequential `1 2 3` numbered list clustering that automatically counts within runs and resets cleanly.
- Real-time task progress tracking with animated expressive progress indicators.

#### 4. Custom Theme Accents & Pure AMOLED Mode
- Choose from vibrant custom theme accent colors whenever dynamic wallpaper colors are disabled.
- Pure `#000000` AMOLED black compliance across the canvas, floating docks, sheets, and system bars.
- 15 native vector background themes with adaptive bounding for both card previews and full-screen editor views.

---

### In-Place Upgrade Compatibility
- Signed with the permanent repository release keystore valid through **2054**.
- In-place upgrades from `v1.2` install seamlessly without signature mismatch errors.
                """.trimIndent()
            ),
            OtaReleaseInfo(
                tagName = "v1.2.0",
                title = "Zainotes v1.2 — First Initial Clean Release",
                publishedAt = "Previous Release",
                htmlUrl = "$GITHUB_RELEASES_WEB/tag/v1.2.0",
                apkDownloadUrl = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/download/v1.2.0/Zainotes-signed.apk",
                apkFileName = "Zainotes-v1.2.0.apk",
                apkSizeBytes = 7_150_000L,
                changelogMarkdown = """
# Zainotes v1.2 — First Initial Clean Release

Welcome to the official **first initial clean release** of **Zainotes (`com.zainotes.app`)**, a modern, native Android notes, tasks, and bulletin application built with Jetpack Compose and Material 3 Expressive.

---

## Permanent Release Signing Key & Seamless In-Place Updates
- **Why Previous Version Updates Failed**:
  Previously, releases were signed dynamically in GitHub Actions with ephemeral keystores. Because Android enforces cryptographic signature parity across package updates (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`), attempting to install one version over an earlier version resulted in a signature mismatch error.
- **The Permanent Fix**:
  Starting with this **v1.2 clean release**, Zainotes is built and signed with a permanent repository keystore valid through **2054**.
  > **Notice for previous testers**: Please uninstall any older test builds of Zainotes (`v1.0.x` / `v1.1.x`) once, then install this release. From this release onward, all future updates install seamlessly in-place!

---

## What's New & Refined in v1.2

### 1. Interactive Free-Form Sizing (Image-Crop Style)
- Long-pressing any note card presents a dedicated **Free-form sizing** option.
- Interactive crop canvas overlay with 4 corner drag handles and 3x3 rule-of-thirds composition guidelines.

### 2. Strict Pure AMOLED `#000000` Black
- Complete overhaul of AMOLED Dark Mode across all UI surfaces with pitch black `#000000` backgrounds and floating docks.

### 3. Google Keep Background Themes & Vector Art Bounding
- Fixed vector illustrations across all 15 themes (**Groceries**, **Food & Cooking**, **Music**, **Recipes**, **Notes & Pen**, **Travel & Places**, **Cinema & Film**, **Celebration**, **Storm & Weather**, **Night Sky & Dusk**, **Flora & Nature**, **Abstract Doodles**, **Ruled Lines**, **Graph Grid**, and **Dot Matrix**).

### 4. Smart 1 2 3 Numbered Lists
- Numbered lists automatically calculate sequence based on consecutive numbered blocks and support enter-to-continue flow while editing.
                """.trimIndent()
            )
        )
    }
}
