/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zainotes.app.ui.theme.ExpressiveMotion
import com.zainotes.app.ui.theme.GoogleSansFlexFontFamily
import com.zainotes.app.ui.theme.expressivePressable
import com.zainotes.app.util.InstallLaunchResult
import com.zainotes.app.util.OtaCheckResult
import com.zainotes.app.util.OtaDownloadProgress
import com.zainotes.app.util.OtaReleaseInfo
import com.zainotes.app.util.OtaUpdateManager
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Material 3 Expressive OTA Updater & Changelog Bottom Sheet.
 *
 * - Checks GitHub Releases (`hydroxideOH-OMG/Zainotes`) for new versions
 * - Informs the user when a newer release is available
 * - Upon user approval, streams the APK download with MD3 Expressive animations & progress
 * - Presents the downloaded APK to the user to install via Android's Package Installer
 * - Renders multi-version Markdown changelogs into clean Material 3 Expressive typography
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtaUpdaterSheet(
    initialCheckResult: OtaCheckResult?,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onDismissReleaseTag: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isChecking by remember { mutableStateOf(initialCheckResult == null) }
    var checkResult by remember(initialCheckResult) { mutableStateOf(initialCheckResult) }
    var selectedReleaseTag by remember(initialCheckResult) {
        mutableStateOf(initialCheckResult?.latestRelease?.tagName.orEmpty())
    }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf<OtaDownloadProgress?>(null) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var needsInstallPermission by remember { mutableStateOf(false) }
    var activeDownloadJob by remember { mutableStateOf<Job?>(null) }

    fun triggerCheck() {
        coroutineScope.launch {
            isChecking = true
            downloadError = null
            val res = OtaUpdateManager.checkForUpdates(context)
            res.onSuccess { result ->
                checkResult = result
                if (selectedReleaseTag.isBlank() ||
                    result.allReleases.none { it.tagName == selectedReleaseTag }
                ) {
                    selectedReleaseTag = result.latestRelease.tagName
                }
            }
            isChecking = false
        }
    }

    LaunchedEffect(Unit) {
        if (checkResult == null) {
            triggerCheck()
        }
    }

    fun startApprovedDownload(release: OtaReleaseInfo) {
        if (checkResult?.isUpdateAvailable != true) {
            triggerCheck()
            return
        }
        activeDownloadJob?.cancel()
        downloadError = null
        downloadedApkFile = null
        needsInstallPermission = false
        isDownloading = true
        downloadProgress = OtaDownloadProgress(0L, release.apkSizeBytes, 0L)

        activeDownloadJob = coroutineScope.launch {
            val result = OtaUpdateManager.downloadReleaseApk(
                context = context,
                release = release,
                onProgress = { progress ->
                    downloadProgress = progress
                }
            )
            isDownloading = false
            result.onSuccess { apkFile ->
                downloadedApkFile = apkFile
                val installResult = OtaUpdateManager.promptInstallApk(context, apkFile)
                if (installResult == InstallLaunchResult.NEEDS_UNKNOWN_SOURCES_PERMISSION) {
                    needsInstallPermission = true
                } else if (installResult == InstallLaunchResult.ERROR) {
                    downloadError = "APK downloaded (${apkFile.name}), but the system package installer could not be opened."
                }
            }.onFailure { err ->
                downloadError = err.message ?: "Download failed. You can also download the APK directly from GitHub."
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            activeDownloadJob?.cancel()
            onDismissRequest()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Software Updater",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "GitHub OTA Releases & Changelogs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (!isChecking && !isDownloading) {
                            triggerCheck()
                        }
                    },
                    enabled = !isChecking && !isDownloading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Check GitHub for updates",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Hero Status & Action Card
            AnimatedContent(
                targetState = isChecking,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(initialScale = 0.95f)).togetherWith(
                        fadeOut(tween(150)) + scaleOut(targetScale = 0.95f)
                    )
                },
                label = "otaStatusHeroTransition"
            ) { checking ->
                if (checking) {
                    // Checking GitHub Releases Card with MD3 Expressive Morphing & Wave Indicators
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                ExpressiveMorphingIndicator(
                                    size = 44.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                                Column {
                                    Text(
                                        text = "Checking GitHub Releases...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Querying hydroxideOH-OMG/Zainotes for new versions and changelogs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            ExpressiveWaveProgressBar(
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                } else {
                    val result = checkResult
                    if (result != null) {
                        OtaReleaseHeroCard(
                            result = result,
                            isDownloading = isDownloading,
                            downloadProgress = downloadProgress,
                            downloadedApkFile = downloadedApkFile,
                            downloadError = downloadError,
                            needsInstallPermission = needsInstallPermission,
                            onCheckForUpdates = {
                                if (!isChecking && !isDownloading) {
                                    triggerCheck()
                                }
                            },
                            onApproveDownload = {
                                startApprovedDownload(result.latestRelease)
                            },
                            onCancelDownload = {
                                activeDownloadJob?.cancel()
                                isDownloading = false
                            },
                            onInstallDownloadedApk = { apk ->
                                val status = OtaUpdateManager.promptInstallApk(context, apk)
                                needsInstallPermission = (status == InstallLaunchResult.NEEDS_UNKNOWN_SOURCES_PERMISSION)
                            },
                            onOpenUnknownSourcesSettings = {
                                OtaUpdateManager.openUnknownSourcesSettings(context)
                            },
                            onOpenGitHubRelease = { url ->
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (_: Exception) {
                                }
                            },
                            onRemindLater = {
                                onDismissReleaseTag(result.latestRelease.tagName)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }

            // Changelogs Section
            val releases = checkResult?.allReleases.orEmpty()
            if (releases.isNotEmpty()) {
                val activeRelease = releases.find { it.tagName == selectedReleaseTag }
                    ?: releases.first()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Release Changelogs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = "${releases.size} releases",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Version Selector Chips
                    if (releases.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            releases.forEachIndexed { idx, rel ->
                                val isSelected = rel.tagName == activeRelease.tagName
                                val chipCorner by animateDpAsState(
                                    targetValue = if (isSelected) 14.dp else 22.dp,
                                    animationSpec = ExpressiveMotion.spatialFastSpec(),
                                    label = "releaseTabCorner"
                                )
                                val chipBg by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    animationSpec = ExpressiveMotion.snappySpec(),
                                    label = "releaseTabBg"
                                )
                                val chipFg by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    animationSpec = ExpressiveMotion.snappySpec(),
                                    label = "releaseTabFg"
                                )

                                Surface(
                                    onClick = { selectedReleaseTag = rel.tagName },
                                    shape = RoundedCornerShape(chipCorner),
                                    color = chipBg,
                                    contentColor = chipFg,
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                        }
                                        Text(
                                            text = if (idx == 0) "${rel.tagName} (Latest)" else rel.tagName,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Formatted Expressive Changelog Card
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Release Metadata Header inside Changelog Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeRelease.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Tag ${activeRelease.tagName} · ${activeRelease.publishedAt}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    onClick = {
                                        try {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(activeRelease.htmlUrl))
                                            )
                                        } catch (_: Exception) {
                                        }
                                    },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Open release on GitHub",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "GitHub",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            // Structured Markdown Renderer for Release Notes
                            ChangelogMarkdownRenderer(
                                markdown = activeRelease.changelogMarkdown,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtaReleaseHeroCard(
    result: OtaCheckResult,
    isDownloading: Boolean,
    downloadProgress: OtaDownloadProgress?,
    downloadedApkFile: File?,
    downloadError: String?,
    needsInstallPermission: Boolean,
    onCheckForUpdates: () -> Unit,
    onApproveDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstallDownloadedApk: (File) -> Unit,
    onOpenUnknownSourcesSettings: () -> Unit,
    onOpenGitHubRelease: (String) -> Unit,
    onRemindLater: () -> Unit
) {
    val isUpdateAvailable = result.isUpdateAvailable
    val latest = result.latestRelease
    val hasReadyUpdateApk = isUpdateAvailable && downloadedApkFile != null && downloadedApkFile.exists()

    val containerColor = when {
        hasReadyUpdateApk -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
        isUpdateAvailable -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val borderColor = when {
        hasReadyUpdateApk -> MaterialTheme.colorScheme.primary
        isUpdateAvailable -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status Pill + Version Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        hasReadyUpdateApk -> MaterialTheme.colorScheme.primary
                        isUpdateAvailable -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = when {
                        hasReadyUpdateApk -> MaterialTheme.colorScheme.onPrimary
                        isUpdateAvailable -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                hasReadyUpdateApk -> Icons.Default.DownloadDone
                                isUpdateAvailable -> Icons.Default.NewReleases
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                hasReadyUpdateApk -> "Ready to Install"
                                isDownloading -> "Downloading Update..."
                                isUpdateAvailable -> "New Release Available"
                                else -> "Up to Date"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Version Comparison Pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "v${result.currentVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isUpdateAvailable) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "v${latest.cleanVersion}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Title & Description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isUpdateAvailable) {
                        "Zainotes ${latest.tagName} is ready to download"
                    } else {
                        "You're running the latest version (v${result.currentVersion})"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val sizeText = OtaUpdateManager.formatByteSize(latest.apkSizeBytes)
                    .takeIf { it.isNotBlank() }
                    ?.let { " · APK size: $it" }
                    .orEmpty()

                Text(
                    text = if (isUpdateAvailable) {
                        "Approve the update below to download the signed APK from GitHub Releases and launch the Android installer$sizeText."
                    } else {
                        "Signed with the permanent Zainotes repository key for seamless in-place updates$sizeText."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Live Downloading Progress UI
            AnimatedVisibility(
                visible = isDownloading,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val prog = downloadProgress
                val animatedFraction by animateFloatAsState(
                    targetValue = prog?.progressFraction ?: 0f,
                    animationSpec = ExpressiveMotion.spatialFastSpec(),
                    label = "otaDownloadFraction"
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ExpressiveMorphingIndicator(
                                    size = 28.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Downloading ${latest.apkFileName}...",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val downloadedStr = OtaUpdateManager.formatByteSize(prog?.bytesDownloaded ?: 0L)
                                        .ifBlank { "0 KB" }
                                    val totalStr = OtaUpdateManager.formatByteSize(prog?.totalBytes ?: 0L)
                                        .ifBlank { "..." }
                                    val speedStr = OtaUpdateManager.formatByteSize(prog?.speedBytesPerSec ?: 0L)
                                        .takeIf { it.isNotBlank() }
                                        ?.let { " · $it/s" }
                                        .orEmpty()
                                    Text(
                                        text = "$downloadedStr / $totalStr$speedStr",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text(
                                    text = "${prog?.progressPercent ?: 0}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { animatedFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        ExpressiveWaveProgressBar(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = onCancelDownload,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel Download")
                            }
                        }
                    }
                }
            }

            // Unknown Sources Permission Guidance Banner (Android 8+)
            AnimatedVisibility(visible = needsInstallPermission && downloadedApkFile != null) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Allow Installing Updates",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Android requires one-time permission to install downloaded APK updates directly from Zainotes. Tap below to enable it, then tap Install Update Now.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        FilledTonalButton(
                            onClick = onOpenUnknownSourcesSettings,
                            shape = CircleShape
                        ) {
                            Text("Allow in Android Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Error Banner (if download failed)
            if (!downloadError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = downloadError,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        FilledTonalButton(
                            onClick = { onOpenGitHubRelease(latest.htmlUrl) },
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download APK in Browser", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Action Buttons (Download & Install / Check for Updates / Install / Later)
            if (!isDownloading) {
                val readyFile = downloadedApkFile
                if (readyFile != null && readyFile.exists() && isUpdateAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val installInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = { onInstallDownloadedApk(readyFile) },
                            interactionSource = installInteraction,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .expressivePressable(installInteraction, pressedScale = 0.94f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.InstallMobile,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Install Update Now",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        FilledTonalButton(
                            onClick = onApproveDownload,
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Re-download APK",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else if (isUpdateAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val approveInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = onApproveDownload,
                            interactionSource = approveInteraction,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .expressivePressable(approveInteraction, pressedScale = 0.94f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Download & Install",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = onRemindLater,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "Later",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                } else {
                    // Up to Date: provide Check for Updates button instead of Download & Install
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val checkInteraction = remember { MutableInteractionSource() }
                        FilledTonalButton(
                            onClick = onCheckForUpdates,
                            interactionSource = checkInteraction,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .expressivePressable(checkInteraction, pressedScale = 0.94f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Check for Updates",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = { onOpenGitHubRelease(latest.htmlUrl) },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "GitHub",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Structured Material 3 Expressive Markdown Renderer for GitHub Release Changelogs.
 * Renders headings (#, ##, ###, ####), horizontal dividers (---), blockquotes (>),
 * nested and top-level bullet lists (- / *), numbered lists (1.), code blocks (```),
 * and inline styles (**bold**, *italic*, `code`, [link](url)) cleanly with zero raw syntax noise.
 */
@Composable
fun ChangelogMarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val lines = remember(markdown) { markdown.replace("\r\n", "\n").lines() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBgColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var idx = 0
        while (idx < lines.size) {
            val rawLine = lines[idx]
            val trimmed = rawLine.trim()

            when {
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                    idx++
                }

                // Fenced Code Block
                trimmed.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    idx++
                    while (idx < lines.size && !lines[idx].trim().startsWith("```")) {
                        codeLines.add(lines[idx])
                        idx++
                    }
                    if (idx < lines.size) idx++ // skip closing ```
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(
                            0.8.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = codeLines.joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Horizontal Divider
                trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
                    idx++
                }

                // Heading 1 (# )
                trimmed.startsWith("# ") -> {
                    val text = trimmed.removePrefix("# ").trim()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseChangelogInlineMarkdown(
                                text = text,
                                primaryColor = primaryColor,
                                codeBackground = codeBgColor
                            ),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = GoogleSansFlexFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = onSurfaceColor
                            )
                        )
                    }
                    idx++
                }

                // Heading 2 (## )
                trimmed.startsWith("## ") -> {
                    val text = trimmed.removePrefix("## ").trim()
                    Text(
                        text = parseChangelogInlineMarkdown(
                            text = text,
                            primaryColor = primaryColor,
                            codeBackground = codeBgColor
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = GoogleSansFlexFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = onSurfaceColor
                        ),
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                    idx++
                }

                // Heading 3 (### )
                trimmed.startsWith("### ") -> {
                    val text = trimmed.removePrefix("### ").trim()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = parseChangelogInlineMarkdown(
                                text = text,
                                primaryColor = primaryColor,
                                codeBackground = codeBgColor
                            ),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = GoogleSansFlexFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    idx++
                }

                // Heading 4 (#### )
                trimmed.startsWith("#### ") -> {
                    val text = trimmed.removePrefix("#### ").trim()
                    Text(
                        text = parseChangelogInlineMarkdown(
                            text = text,
                            primaryColor = primaryColor,
                            codeBackground = codeBgColor
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = GoogleSansFlexFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    idx++
                }

                // Blockquote (> )
                trimmed.startsWith(">") -> {
                    val quoteLines = mutableListOf<String>()
                    while (idx < lines.size && lines[idx].trim().startsWith(">")) {
                        quoteLines.add(lines[idx].trim().removePrefix(">").trim())
                        idx++
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = parseChangelogInlineMarkdown(
                                    text = quoteLines.joinToString(" "),
                                    primaryColor = primaryColor,
                                    codeBackground = codeBgColor
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }

                // Bullet Item (- or * or •)
                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                    val leadingSpaces = rawLine.takeWhile { it == ' ' || it == '\t' }.length
                    val isNested = leadingSpaces >= 2
                    val bulletContent = trimmed.drop(2).trim()

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = if (isNested) 18.dp else 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(if (isNested) 5.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isNested) onSurfaceVariantColor.copy(alpha = 0.7f)
                                    else primaryColor
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseChangelogInlineMarkdown(
                                text = bulletContent,
                                primaryColor = primaryColor,
                                codeBackground = codeBgColor
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = onSurfaceColor,
                                lineHeight = 21.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    idx++
                }

                // Numbered List Item (1. , 2. , etc.)
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val numberPart = trimmed.substringBefore(".")
                    val bodyPart = trimmed.substringAfter(".").trim()
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = numberPart,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseChangelogInlineMarkdown(
                                text = bodyPart,
                                primaryColor = primaryColor,
                                codeBackground = codeBgColor
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = onSurfaceColor,
                                lineHeight = 21.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    idx++
                }

                // Standard Paragraph
                else -> {
                    Text(
                        text = parseChangelogInlineMarkdown(
                            text = trimmed,
                            primaryColor = primaryColor,
                            codeBackground = codeBgColor
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = onSurfaceVariantColor,
                            lineHeight = 21.sp
                        )
                    )
                    idx++
                }
            }
        }
    }
}

/**
 * Parses inline Markdown spans (`**bold**`, `*italic*`, `` `code` ``, `[label](url)`)
 * into a styled [AnnotatedString] with zero raw markdown markers.
 */
fun parseChangelogInlineMarkdown(
    text: String,
    primaryColor: Color,
    codeBackground: Color
): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val n = text.length
        while (i < n) {
            when {
                // Bold + Italic (***text***)
                text.startsWith("***", i) -> {
                    val end = text.indexOf("***", i + 3)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Italic
                            )
                        ) {
                            append(text.substring(i + 3, end))
                        }
                        i = end + 3
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Bold (**text**)
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(
                                parseChangelogInlineMarkdown(
                                    text = text.substring(i + 2, end),
                                    primaryColor = primaryColor,
                                    codeBackground = codeBackground
                                )
                            )
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Inline Code (`code`)
                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                background = codeBackground
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Markdown Link ([label](url))
                text[i] == '[' -> {
                    val closeBracket = text.indexOf(']', i + 1)
                    val openParen = if (closeBracket != -1 && closeBracket + 1 < n && text[closeBracket + 1] == '(') {
                        closeBracket + 1
                    } else {
                        -1
                    }
                    val closeParen = if (openParen != -1) text.indexOf(')', openParen + 1) else -1
                    if (closeBracket != -1 && openParen != -1 && closeParen != -1) {
                        val label = text.substring(i + 1, closeBracket)
                        withStyle(
                            SpanStyle(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(label)
                        }
                        i = closeParen + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Italic (*text*)
                text[i] == '*' && (i + 1 < n && text[i + 1] != ' ') -> {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
