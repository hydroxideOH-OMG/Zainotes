/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.NoteBlock
import com.zainotes.app.ui.theme.GoogleSansFlexFontFamily
import com.zainotes.app.ui.theme.expressivePressable
import com.zainotes.app.util.MediaAttachmentHelper
import kotlinx.coroutines.delay

/**
 * Material 3 Expressive bottom sheet opened from the leftmost '+' button in the note editor's bottom toolbar.
 * Allows adding Images, Audio files, or Video files into the note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMediaAttachmentSheet(
    sheetState: SheetState,
    onPickImage: () -> Unit,
    onPickAudio: () -> Unit,
    onPickVideo: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add File to Note",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Choose an image, audio recording, or video clip from your device to embed directly in this note.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            MediaPickerOptionCard(
                icon = Icons.Default.Image,
                title = "Add Image",
                subtitle = "Photos, screenshots, or pictures from your gallery",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = {
                    onDismissRequest()
                    onPickImage()
                }
            )

            MediaPickerOptionCard(
                icon = Icons.Default.AudioFile,
                title = "Add Audio File",
                subtitle = "Voice memos, music tracks, or sound recordings",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = {
                    onDismissRequest()
                    onPickAudio()
                }
            )

            MediaPickerOptionCard(
                icon = Icons.Default.Movie,
                title = "Add Video File",
                subtitle = "Video clips or screen recordings with inline playback",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = {
                    onDismissRequest()
                    onPickVideo()
                }
            )
        }
    }
}

@Composable
private fun MediaPickerOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .expressivePressable()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
                contentColor = contentColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Interactive Image Block inside the Note Editor.
 */
@Composable
fun NoteImageBlockCard(
    block: NoteBlock,
    index: Int,
    totalBlocks: Int,
    isFocused: Boolean,
    contentColor: Color,
    onFocused: () -> Unit,
    onCaptionChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriString = block.mediaUri.orEmpty()
    var showMenu by remember { mutableStateOf(false) }
    var showFullscreen by remember { mutableStateOf(false) }

    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = uriString) {
        value = MediaAttachmentHelper.loadImageBitmap(
            context = context,
            mediaUriString = uriString,
            maxDimensionPx = 1440
        )
    }

    val sizeLabel = remember(block.mediaSizeBytes) {
        MediaAttachmentHelper.formatFileSize(block.mediaSizeBytes)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(contentColor.copy(alpha = 0.06f))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(10.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = block.mediaName?.takeIf { it.isNotBlank() } ?: "Attached Image",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (sizeLabel.isNotBlank()) {
                        Text(
                            text = sizeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (imageBitmap != null) {
                    IconButton(
                        onClick = { showFullscreen = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "View full screen image",
                            tint = contentColor.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Box {
                    IconButton(
                        onClick = {
                            onFocused()
                            showMenu = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Image block options",
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    MediaBlockOptionsMenu(
                        expanded = showMenu,
                        index = index,
                        totalBlocks = totalBlocks,
                        onDismiss = { showMenu = false },
                        onMoveUp = onMoveUp,
                        onMoveDown = onMoveDown,
                        onDelete = onDelete
                    )
                }
            }
        }

        // Image display container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp, max = 340.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(contentColor.copy(alpha = 0.08f))
                .clickable {
                    onFocused()
                    if (imageBitmap != null) {
                        showFullscreen = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val bmp = imageBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = block.mediaName ?: "Attached note image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    ExpressiveMorphingIndicator(
                        size = 28.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Loading image…",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.65f)
                    )
                }
            }
        }

        // Optional Caption Input
        MediaCaptionInput(
            caption = block.content,
            placeholder = "Add a caption for this image...",
            contentColor = contentColor,
            onFocused = onFocused,
            onCaptionChange = onCaptionChange
        )
    }

    if (showFullscreen && imageBitmap != null) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable { showFullscreen = false },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = block.mediaName ?: "Full screen image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )

                FilledTonalIconButton(
                    onClick = { showFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close full screen image"
                    )
                }

                if (block.content.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.72f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = block.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Audio Player Block inside the Note Editor.
 */
@Composable
fun NoteAudioBlockCard(
    block: NoteBlock,
    index: Int,
    totalBlocks: Int,
    isFocused: Boolean,
    contentColor: Color,
    onFocused: () -> Unit,
    onCaptionChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriString = block.mediaUri.orEmpty()
    var showMenu by remember { mutableStateOf(false) }

    var mediaPlayer by remember(uriString) { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember(uriString) { mutableStateOf(false) }
    var isPlaying by remember(uriString) { mutableStateOf(false) }
    var currentPosMs by remember(uriString) { mutableIntStateOf(0) }
    var totalDurationMs by remember(uriString) {
        mutableIntStateOf(block.mediaDurationMs.toInt().coerceAtLeast(0))
    }
    var hasError by remember(uriString) { mutableStateOf(false) }

    DisposableEffect(uriString) {
        val player = MediaPlayer()
        try {
            val parsedUri = Uri.parse(uriString)
            val localFile = MediaAttachmentHelper.resolveLocalFile(parsedUri, uriString)
            if (localFile != null && localFile.exists()) {
                player.setDataSource(localFile.absolutePath)
            } else {
                player.setDataSource(context, parsedUri)
            }
            player.setOnPreparedListener { mp ->
                isPrepared = true
                if (mp.duration > 0) {
                    totalDurationMs = mp.duration
                }
            }
            player.setOnCompletionListener { mp ->
                isPlaying = false
                currentPosMs = mp.duration.coerceAtLeast(0)
            }
            player.setOnErrorListener { _, _, _ ->
                hasError = true
                isPlaying = false
                true
            }
            player.prepareAsync()
            mediaPlayer = player
        } catch (_: Exception) {
            hasError = true
        }

        onDispose {
            try {
                player.stop()
            } catch (_: Exception) {
            }
            try {
                player.release()
            } catch (_: Exception) {
            }
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying, isPrepared) {
        while (isPlaying && isPrepared) {
            val mp = mediaPlayer
            if (mp != null) {
                try {
                    currentPosMs = mp.currentPosition.coerceAtLeast(0)
                } catch (_: Exception) {
                }
            }
            delay(200L)
        }
    }

    val playButtonScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "audioPlayScale"
    )

    val playButtonContainer by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
        label = "audioPlayContainer"
    )
    val playButtonContent by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
        label = "audioPlayContent"
    )

    val sizeLabel = remember(block.mediaSizeBytes) {
        MediaAttachmentHelper.formatFileSize(block.mediaSizeBytes)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(contentColor.copy(alpha = 0.06f))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Play / Pause Circular Button
            Surface(
                onClick = {
                    onFocused()
                    val mp = mediaPlayer
                    if (mp != null && isPrepared && !hasError) {
                        try {
                            if (mp.isPlaying) {
                                mp.pause()
                                isPlaying = false
                            } else {
                                if (currentPosMs >= totalDurationMs - 250 && totalDurationMs > 0) {
                                    mp.seekTo(0)
                                    currentPosMs = 0
                                }
                                mp.start()
                                isPlaying = true
                            }
                        } catch (_: Exception) {
                        }
                    }
                },
                shape = CircleShape,
                color = playButtonContainer,
                contentColor = playButtonContent,
                modifier = Modifier
                    .size(48.dp)
                    .scale(playButtonScale)
                    .expressivePressable()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause audio" else "Play audio",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = block.mediaName?.takeIf { it.isNotBlank() } ?: "Audio Recording",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box {
                        IconButton(
                            onClick = {
                                onFocused()
                                showMenu = true
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Audio block options",
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        MediaBlockOptionsMenu(
                            expanded = showMenu,
                            index = index,
                            totalBlocks = totalBlocks,
                            onDismiss = { showMenu = false },
                            onMoveUp = onMoveUp,
                            onMoveDown = onMoveDown,
                            onDelete = onDelete
                        )
                    }
                }

                val durationForSlider = totalDurationMs.coerceAtLeast(1)
                val sliderFraction = (currentPosMs.toFloat() / durationForSlider.toFloat()).coerceIn(0f, 1f)

                Slider(
                    value = sliderFraction,
                    onValueChange = { fraction ->
                        val targetMs = (fraction * durationForSlider).toInt()
                        currentPosMs = targetMs
                        try {
                            if (isPrepared) {
                                mediaPlayer?.seekTo(targetMs)
                            }
                        } catch (_: Exception) {
                        }
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = contentColor.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasError) {
                            "Unable to play audio file"
                        } else {
                            "${MediaAttachmentHelper.formatDuration(currentPosMs.toLong())} / ${MediaAttachmentHelper.formatDuration(totalDurationMs.toLong())}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasError) MaterialTheme.colorScheme.error else contentColor.copy(alpha = 0.72f)
                    )
                    if (sizeLabel.isNotBlank()) {
                        Text(
                            text = sizeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }

        // Optional Caption Input
        MediaCaptionInput(
            caption = block.content,
            placeholder = "Add a note about this audio...",
            contentColor = contentColor,
            onFocused = onFocused,
            onCaptionChange = onCaptionChange
        )
    }
}

/**
 * Interactive Video Player Block inside the Note Editor.
 * Supports both inline video playback right inside the note and a Fullscreen video player mode.
 */
@Composable
fun NoteVideoBlockCard(
    block: NoteBlock,
    index: Int,
    totalBlocks: Int,
    isFocused: Boolean,
    contentColor: Color,
    onFocused: () -> Unit,
    onCaptionChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriString = block.mediaUri.orEmpty()
    var showMenu by remember { mutableStateOf(false) }
    var isInlinePlayerActive by remember(uriString) { mutableStateOf(false) }
    var isVideoPlaying by remember(uriString) { mutableStateOf(false) }
    var showFullscreenVideo by remember(uriString) { mutableStateOf(false) }

    var videoViewRef by remember(uriString) { mutableStateOf<VideoView?>(null) }
    var currentPosMs by remember(uriString) { mutableIntStateOf(0) }
    var totalDurationMs by remember(uriString) {
        mutableIntStateOf(block.mediaDurationMs.toInt().coerceAtLeast(0))
    }

    val thumbnailBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = uriString) {
        value = MediaAttachmentHelper.loadVideoThumbnail(
            context = context,
            mediaUriString = uriString,
            maxDimensionPx = 960
        )
    }

    LaunchedEffect(isInlinePlayerActive, isVideoPlaying) {
        while (isInlinePlayerActive && isVideoPlaying) {
            val vv = videoViewRef
            if (vv != null) {
                try {
                    if (vv.isPlaying) {
                        currentPosMs = vv.currentPosition.coerceAtLeast(0)
                    }
                    if (vv.duration > 0) {
                        totalDurationMs = vv.duration
                    }
                } catch (_: Exception) {
                }
            }
            delay(200L)
        }
    }

    DisposableEffect(uriString) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {
            }
            videoViewRef = null
        }
    }

    val sizeLabel = remember(block.mediaSizeBytes) {
        MediaAttachmentHelper.formatFileSize(block.mediaSizeBytes)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(contentColor.copy(alpha = 0.06f))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(10.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = block.mediaName?.takeIf { it.isNotBlank() } ?: "Attached Video",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val metaParts = listOfNotNull(
                        if (totalDurationMs > 0) MediaAttachmentHelper.formatDuration(totalDurationMs.toLong()) else null,
                        sizeLabel.takeIf { it.isNotBlank() }
                    )
                    if (metaParts.isNotEmpty()) {
                        Text(
                            text = metaParts.joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        onFocused()
                        try {
                            videoViewRef?.pause()
                            isVideoPlaying = false
                        } catch (_: Exception) {
                        }
                        showFullscreenVideo = true
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen video player",
                        tint = contentColor.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = {
                            onFocused()
                            showMenu = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Video block options",
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    MediaBlockOptionsMenu(
                        expanded = showMenu,
                        index = index,
                        totalBlocks = totalBlocks,
                        onDismiss = { showMenu = false },
                        onMoveUp = onMoveUp,
                        onMoveDown = onMoveDown,
                        onDelete = onDelete
                    )
                }
            }
        }

        // Video Stage (Thumbnail preview OR active inline VideoView)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (!isInlinePlayerActive) {
                val thumb = thumbnailBitmap
                if (thumb != null) {
                    Image(
                        bitmap = thumb,
                        contentDescription = block.mediaName ?: "Video preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Gradient overlay for contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )

                // Center Play FAB
                Surface(
                    onClick = {
                        onFocused()
                        isInlinePlayerActive = true
                        isVideoPlaying = true
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(60.dp)
                        .expressivePressable()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play video",
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                if (totalDurationMs > 0) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = MediaAttachmentHelper.formatDuration(totalDurationMs.toLong()),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val parsedUri = Uri.parse(uriString)
                            val localFile = MediaAttachmentHelper.resolveLocalFile(parsedUri, uriString)
                            if (localFile != null && localFile.exists()) {
                                setVideoPath(localFile.absolutePath)
                            } else {
                                setVideoURI(parsedUri)
                            }
                            setOnPreparedListener { mp ->
                                if (mp.duration > 0) {
                                    totalDurationMs = mp.duration
                                }
                                start()
                                isVideoPlaying = true
                            }
                            setOnCompletionListener {
                                isVideoPlaying = false
                                currentPosMs = totalDurationMs
                            }
                            videoViewRef = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            val vv = videoViewRef
                            if (vv != null) {
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isVideoPlaying = false
                                } else {
                                    vv.start()
                                    isVideoPlaying = true
                                }
                            }
                        }
                )
            }
        }

        // Inline Video Transport Controls when active
        if (isInlinePlayerActive) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val vv = videoViewRef
                        if (vv != null) {
                            if (vv.isPlaying) {
                                vv.pause()
                                isVideoPlaying = false
                            } else {
                                if (currentPosMs >= totalDurationMs - 300 && totalDurationMs > 0) {
                                    vv.seekTo(0)
                                    currentPosMs = 0
                                }
                                vv.start()
                                isVideoPlaying = true
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isVideoPlaying -> Icons.Default.Pause
                            currentPosMs >= totalDurationMs - 300 && totalDurationMs > 0 -> Icons.Default.Replay
                            else -> Icons.Default.PlayArrow
                        },
                        contentDescription = if (isVideoPlaying) "Pause video" else "Play video",
                        modifier = Modifier.size(20.dp)
                    )
                }

                val durationForSlider = totalDurationMs.coerceAtLeast(1)
                val fraction = (currentPosMs.toFloat() / durationForSlider.toFloat()).coerceIn(0f, 1f)

                Slider(
                    value = fraction,
                    onValueChange = { nextFraction ->
                        val targetMs = (nextFraction * durationForSlider).toInt()
                        currentPosMs = targetMs
                        try {
                            videoViewRef?.seekTo(targetMs)
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                )

                Text(
                    text = "${MediaAttachmentHelper.formatDuration(currentPosMs.toLong())} / ${MediaAttachmentHelper.formatDuration(totalDurationMs.toLong())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.75f)
                )
            }
        }

        // Optional Caption Input
        MediaCaptionInput(
            caption = block.content,
            placeholder = "Add a caption for this video...",
            contentColor = contentColor,
            onFocused = onFocused,
            onCaptionChange = onCaptionChange
        )
    }

    if (showFullscreenVideo) {
        FullscreenVideoDialog(
            uriString = uriString,
            title = block.mediaName?.takeIf { it.isNotBlank() } ?: "Video",
            onDismiss = { showFullscreenVideo = false }
        )
    }
}

@Composable
private fun FullscreenVideoDialog(
    uriString: String,
    title: String,
    onDismiss: () -> Unit
) {
    var fsVideoView by remember { mutableStateOf<VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosMs by remember { mutableIntStateOf(0) }
    var totalDurationMs by remember { mutableIntStateOf(1) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val vv = fsVideoView
            if (vv != null) {
                try {
                    if (vv.isPlaying) {
                        currentPosMs = vv.currentPosition.coerceAtLeast(0)
                    }
                    if (vv.duration > 0) {
                        totalDurationMs = vv.duration
                    }
                } catch (_: Exception) {
                }
            }
            delay(200L)
        }
    }

    DisposableEffect(uriString) {
        onDispose {
            try {
                fsVideoView?.stopPlayback()
            } catch (_: Exception) {
            }
            fsVideoView = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val parsedUri = Uri.parse(uriString)
                        val localFile = MediaAttachmentHelper.resolveLocalFile(parsedUri, uriString)
                        if (localFile != null && localFile.exists()) {
                            setVideoPath(localFile.absolutePath)
                        } else {
                            setVideoURI(parsedUri)
                        }
                        setOnPreparedListener { mp ->
                            if (mp.duration > 0) {
                                totalDurationMs = mp.duration
                            }
                            start()
                            isPlaying = true
                        }
                        setOnCompletionListener {
                            isPlaying = false
                            currentPosMs = totalDurationMs
                        }
                        fsVideoView = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilledTonalIconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close fullscreen video"
                    )
                }
            }

            // Bottom transport controls
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.72f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            val vv = fsVideoView
                            if (vv != null) {
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }

                    val safeDuration = totalDurationMs.coerceAtLeast(1)
                    val fraction = (currentPosMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

                    Slider(
                        value = fraction,
                        onValueChange = { next ->
                            val targetMs = (next * safeDuration).toInt()
                            currentPosMs = targetMs
                            try {
                                fsVideoView?.seekTo(targetMs)
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${MediaAttachmentHelper.formatDuration(currentPosMs.toLong())} / ${MediaAttachmentHelper.formatDuration(totalDurationMs.toLong())}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaCaptionInput(
    caption: String,
    placeholder: String,
    contentColor: Color,
    onFocused: () -> Unit,
    onCaptionChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 2.dp)
    ) {
        if (caption.isEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = contentColor.copy(alpha = 0.42f)
                )
            )
        }
        BasicTextField(
            value = caption,
            onValueChange = onCaptionChange,
            textStyle = TextStyle(
                fontSize = 13.5.sp,
                lineHeight = 18.sp,
                fontFamily = GoogleSansFlexFontFamily,
                color = contentColor.copy(alpha = 0.88f)
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) onFocused() }
        )
    }
}

@Composable
private fun MediaBlockOptionsMenu(
    expanded: Boolean,
    index: Int,
    totalBlocks: Int,
    onDismiss: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (index > 0) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = { Text("Move Up") },
                onClick = {
                    onMoveUp()
                    onDismiss()
                }
            )
        }
        if (index < totalBlocks - 1) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = { Text("Move Down") },
                onClick = {
                    onMoveDown()
                    onDismiss()
                }
            )
        }
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            },
            text = { Text("Remove Attachment", color = MaterialTheme.colorScheme.error) },
            onClick = {
                onDelete()
                onDismiss()
            }
        )
    }
}

/**
 * Compact media preview used inside Home screen [NoteCard]s.
 */
@Composable
fun NoteMediaPreviewItem(
    block: NoteBlock,
    cardContentColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriString = block.mediaUri.orEmpty()

    when (block.type) {
        BlockType.IMAGE -> {
            val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = uriString) {
                value = MediaAttachmentHelper.loadImageBitmap(
                    context = context,
                    mediaUriString = uriString,
                    maxDimensionPx = 720
                )
            }
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 165.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardContentColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = bitmap
                    if (bmp != null) {
                        Image(
                            bitmap = bmp,
                            contentDescription = block.mediaName ?: "Image attachment",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = block.mediaName ?: "Image",
                                style = MaterialTheme.typography.labelMedium,
                                color = cardContentColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (block.content.isNotBlank()) {
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.labelSmall,
                        color = cardContentColor.copy(alpha = 0.78f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        BlockType.AUDIO -> {
            val durationLabel = remember(block.mediaDurationMs) {
                if (block.mediaDurationMs > 0L) {
                    MediaAttachmentHelper.formatDuration(block.mediaDurationMs)
                } else {
                    ""
                }
            }
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardContentColor.copy(alpha = 0.09f))
                        .border(0.6.dp, cardContentColor.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = block.mediaName?.takeIf { it.isNotBlank() } ?: "Audio Clip",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = cardContentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (durationLabel.isNotBlank()) {
                        Text(
                            text = durationLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = cardContentColor.copy(alpha = 0.65f)
                        )
                    }
                }
                if (block.content.isNotBlank()) {
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.labelSmall,
                        color = cardContentColor.copy(alpha = 0.78f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        BlockType.VIDEO -> {
            val thumb by produceState<ImageBitmap?>(initialValue = null, key1 = uriString) {
                value = MediaAttachmentHelper.loadVideoThumbnail(
                    context = context,
                    mediaUriString = uriString,
                    maxDimensionPx = 640
                )
            }
            val durationLabel = remember(block.mediaDurationMs) {
                if (block.mediaDurationMs > 0L) {
                    MediaAttachmentHelper.formatDuration(block.mediaDurationMs)
                } else {
                    ""
                }
            }
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = thumb
                    if (bmp != null) {
                        Image(
                            bitmap = bmp,
                            contentDescription = block.mediaName ?: "Video attachment",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.32f))
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (durationLabel.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.72f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                        ) {
                            Text(
                                text = durationLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (block.content.isNotBlank()) {
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.labelSmall,
                        color = cardContentColor.copy(alpha = 0.78f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        else -> {}
    }
}
