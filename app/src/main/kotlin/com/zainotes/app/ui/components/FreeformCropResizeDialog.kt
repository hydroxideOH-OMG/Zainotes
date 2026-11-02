/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.Note
import kotlin.math.roundToInt

/**
 * Interactive Free-form Cropping & Resizing Overlay.
 *
 * Provides an image-crop style interactive canvas overlay with drag handles (top-left,
 * top-right, bottom-left, bottom-right) to resize a note card interactively.
 */
@Composable
fun FreeformCropResizeDialog(
    note: Note,
    onConfirmSize: (CardSize) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        var containerWidth by remember { mutableFloatStateOf(0f) }
        var containerHeight by remember { mutableFloatStateOf(0f) }

        // Crop rect normalized coordinates relative to container
        var cropLeft by remember { mutableFloatStateOf(0f) }
        var cropTop by remember { mutableFloatStateOf(0f) }
        var cropRight by remember { mutableFloatStateOf(0f) }
        var cropBottom by remember { mutableFloatStateOf(0f) }
        var isInitialized by remember { mutableStateOf(false) }

        // Live computed resulting size
        val currentAspect = remember(cropLeft, cropRight, cropTop, cropBottom) {
            val width = (cropRight - cropLeft).coerceAtLeast(1f)
            val height = (cropBottom - cropTop).coerceAtLeast(1f)
            width / height
        }

        val computedCardSize = remember(currentAspect, containerWidth, cropRight, cropLeft) {
            val width = cropRight - cropLeft
            val widthRatio = if (containerWidth > 0) width / containerWidth else 0.5f
            when {
                widthRatio > 0.75f && currentAspect > 1.25f -> CardSize.WIDE
                widthRatio > 0.75f && currentAspect <= 1.25f -> CardSize.HERO
                currentAspect < 0.8f -> CardSize.FREEFORM
                currentAspect < 1.15f -> CardSize.STANDARD
                else -> CardSize.COMPACT
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
                .onGloballyPositioned { coords ->
                    containerWidth = coords.size.width.toFloat()
                    containerHeight = coords.size.height.toFloat()
                    if (!isInitialized && containerWidth > 0f && containerHeight > 0f) {
                        val initPaddingX = containerWidth * 0.08f
                        val initPaddingY = containerHeight * 0.22f
                        cropLeft = initPaddingX
                        cropTop = initPaddingY
                        cropRight = containerWidth - initPaddingX
                        cropBottom = containerHeight - initPaddingY
                        isInitialized = true
                    }
                }
        ) {
            // Note Card Preview in Center
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                NoteCard(
                    note = note.copy(cardSize = computedCardSize),
                    onClick = {},
                    onTaskToggle = { _, _ -> },
                    onCardSizeChange = {},
                    onColorChange = {},
                    onTogglePin = {},
                    onDelete = {}
                )
            }

            // Interactive Crop Box & Dimming Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (!isInitialized) return@Canvas

                val rect = Rect(cropLeft, cropTop, cropRight, cropBottom)

                // Corner brackets / crop rectangle outline
                drawRect(
                    color = Color.White,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f))
                    )
                )

                // 3x3 Grid Guidelines
                val thirdW = rect.width / 3f
                val thirdH = rect.height / 3f
                val guideColor = Color.White.copy(alpha = 0.35f)
                val guideStroke = 1.dp.toPx()

                drawLine(guideColor, Offset(rect.left + thirdW, rect.top), Offset(rect.left + thirdW, rect.bottom), strokeWidth = guideStroke)
                drawLine(guideColor, Offset(rect.left + thirdW * 2, rect.top), Offset(rect.left + thirdW * 2, rect.bottom), strokeWidth = guideStroke)
                drawLine(guideColor, Offset(rect.left, rect.top + thirdH), Offset(rect.right, rect.top + thirdH), strokeWidth = guideStroke)
                drawLine(guideColor, Offset(rect.left, rect.top + thirdH * 2), Offset(rect.right, rect.top + thirdH * 2), strokeWidth = guideStroke)

                // Thicker Solid Crop Corners
                val cornerLen = 28.dp.toPx()
                val cornerWidth = 4.dp.toPx()
                val cornerColor = Color(0xFF4FC3F7)

                // Top-Left
                drawLine(cornerColor, Offset(rect.left - 2, rect.top), Offset(rect.left + cornerLen, rect.top), cornerWidth, StrokeCap.Round)
                drawLine(cornerColor, Offset(rect.left, rect.top - 2), Offset(rect.left, rect.top + cornerLen), cornerWidth, StrokeCap.Round)

                // Top-Right
                drawLine(cornerColor, Offset(rect.right - cornerLen, rect.top), Offset(rect.right + 2, rect.top), cornerWidth, StrokeCap.Round)
                drawLine(cornerColor, Offset(rect.right, rect.top - 2), Offset(rect.right, rect.top + cornerLen), cornerWidth, StrokeCap.Round)

                // Bottom-Left
                drawLine(cornerColor, Offset(rect.left - 2, rect.bottom), Offset(rect.left + cornerLen, rect.bottom), cornerWidth, StrokeCap.Round)
                drawLine(cornerColor, Offset(rect.left, rect.bottom - cornerLen), Offset(rect.left, rect.bottom + 2), cornerWidth, StrokeCap.Round)

                // Bottom-Right
                drawLine(cornerColor, Offset(rect.right - cornerLen, rect.bottom), Offset(rect.right + 2, rect.bottom), cornerWidth, StrokeCap.Round)
                drawLine(cornerColor, Offset(rect.right, rect.bottom - cornerLen), Offset(rect.right, rect.bottom + 2), cornerWidth, StrokeCap.Round)
            }

            // Draggable Handles
            if (isInitialized) {
                // Top-Left Drag Handle
                CropHandle(
                    x = cropLeft,
                    y = cropTop,
                    onDrag = { dx, dy ->
                        cropLeft = (cropLeft + dx).coerceIn(16f, cropRight - 80f)
                        cropTop = (cropTop + dy).coerceIn(70f, cropBottom - 80f)
                    }
                )

                // Top-Right Drag Handle
                CropHandle(
                    x = cropRight,
                    y = cropTop,
                    onDrag = { dx, dy ->
                        cropRight = (cropRight + dx).coerceIn(cropLeft + 80f, containerWidth - 16f)
                        cropTop = (cropTop + dy).coerceIn(70f, cropBottom - 80f)
                    }
                )

                // Bottom-Left Drag Handle
                CropHandle(
                    x = cropLeft,
                    y = cropBottom,
                    onDrag = { dx, dy ->
                        cropLeft = (cropLeft + dx).coerceIn(16f, cropRight - 80f)
                        cropBottom = (cropBottom + dy).coerceIn(cropTop + 80f, containerHeight - 110f)
                    }
                )

                // Bottom-Right Drag Handle
                CropHandle(
                    x = cropRight,
                    y = cropBottom,
                    onDrag = { dx, dy ->
                        cropRight = (cropRight + dx).coerceIn(cropLeft + 80f, containerWidth - 16f)
                        cropBottom = (cropBottom + dy).coerceIn(cropTop + 80f, containerHeight - 110f)
                    }
                )
            }

            // Top Header: Label showing active size & instructions
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                Text(
                    text = "Free-form Sizing: ${computedCardSize.label}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }

            // Bottom Action Controls (Cancel & Confirm buttons)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 32.dp, end = 32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cancel FAB
                    FloatingActionButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel resizing"
                        )
                    }

                    // Confirm FAB
                    FloatingActionButton(
                        onClick = { onConfirmSize(computedCardSize) },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Apply free-form size"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CropHandle(
    x: Float,
    y: Float,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    val handleRadius = 18.dp
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (x - handleRadius.toPx()).roundToInt(),
                    (y - handleRadius.toPx()).roundToInt()
                )
            }
            .size(handleRadius * 2)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF4FC3F7))
        )
    }
}
