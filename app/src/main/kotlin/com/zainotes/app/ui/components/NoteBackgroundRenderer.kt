/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.NoteBackgroundTheme

/**
 * Renders native vector background themes and patterns for notes,
 * including Google Keep themes (Groceries, Food, Music, Recipes, Notes, Places, Cinema,
 * Celebration, Storm, Dusk) and extended patterns (Nature, Doodles, Ruled, Grid, Dots).
 *
 * Uses proper density-aware dp-to-px scaling so illustrations render crisply at any size:
 * miniature theme picker cards, homepage masonry cards, and the full-screen note editor.
 */
@Composable
fun NoteBackgroundRenderer(
    theme: NoteBackgroundTheme,
    tint: Color,
    modifier: Modifier = Modifier
) {
    if (theme == NoteBackgroundTheme.NONE) return

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width <= 1f || size.height <= 1f) return@Canvas
        clipRect {
            // Subtle ambient tonal wash at bottom-right for illustrated themes
            if (theme != NoteBackgroundTheme.RULED &&
                theme != NoteBackgroundTheme.GRID &&
                theme != NoteBackgroundTheme.DOTS
            ) {
                val radius = size.width.coerceAtLeast(size.height) * 0.65f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tint.copy(alpha = 0.10f),
                            tint.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.85f),
                        radius = radius.coerceAtLeast(10f)
                    ),
                    radius = radius,
                    center = Offset(size.width * 0.85f, size.height * 0.85f)
                )
            }

            when (theme) {
                NoteBackgroundTheme.NONE -> {}
                NoteBackgroundTheme.RULED -> drawRuledPattern(tint)
                NoteBackgroundTheme.GRID -> drawGridPattern(tint)
                NoteBackgroundTheme.DOTS -> drawDotsPattern(tint)
                NoteBackgroundTheme.GROCERIES -> drawGroceriesIllustration(tint)
                NoteBackgroundTheme.FOOD -> drawFoodIllustration(tint)
                NoteBackgroundTheme.MUSIC -> drawMusicIllustration(tint)
                NoteBackgroundTheme.RECIPES -> drawRecipesIllustration(tint)
                NoteBackgroundTheme.NOTES -> drawNotesIllustration(tint)
                NoteBackgroundTheme.PLACES -> drawPlacesIllustration(tint)
                NoteBackgroundTheme.VIDEO -> drawVideoIllustration(tint)
                NoteBackgroundTheme.CELEBRATION -> drawCelebrationIllustration(tint)
                NoteBackgroundTheme.STORM -> drawStormIllustration(tint)
                NoteBackgroundTheme.DUSK -> drawDuskIllustration(tint)
                NoteBackgroundTheme.NATURE -> drawNatureIllustration(tint)
                NoteBackgroundTheme.DOODLES -> drawDoodlesIllustration(tint)
            }
        }
    }
}

// 1. Ruled Lines Pattern
private fun DrawScope.drawRuledPattern(tint: Color) {
    val lineColor = tint.copy(alpha = 0.24f)
    val marginColor = tint.copy(alpha = 0.16f)
    val strokeWidth = 1.25.dp.toPx()
    val isMini = size.height < 110.dp.toPx()
    val step = if (isMini) 14.dp.toPx() else 28.dp.toPx()
    var y = if (isMini) 16.dp.toPx() else 36.dp.toPx()

    // Left notebook margin line
    val marginX = if (isMini) 14.dp.toPx() else 28.dp.toPx()
    if (size.width > marginX * 2) {
        drawLine(
            color = marginColor,
            start = Offset(marginX, 0f),
            end = Offset(marginX, size.height),
            strokeWidth = strokeWidth
        )
    }

    while (y < size.height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth
        )
        y += step
    }
}

// 2. Graph Grid Pattern
private fun DrawScope.drawGridPattern(tint: Color) {
    val gridColor = tint.copy(alpha = 0.20f)
    val majorColor = tint.copy(alpha = 0.30f)
    val strokeWidth = 1.dp.toPx()
    val isMini = size.height < 110.dp.toPx()
    val step = if (isMini) 12.dp.toPx() else 22.dp.toPx()

    var x = step
    var col = 1
    while (x < size.width) {
        drawLine(
            color = if (col % 4 == 0) majorColor else gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = strokeWidth
        )
        x += step
        col++
    }

    var y = step
    var row = 1
    while (y < size.height) {
        drawLine(
            color = if (row % 4 == 0) majorColor else gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth
        )
        y += step
        row++
    }
}

// 3. Dot Matrix Pattern
private fun DrawScope.drawDotsPattern(tint: Color) {
    val dotColor = tint.copy(alpha = 0.32f)
    val isMini = size.height < 110.dp.toPx()
    val dotRadius = if (isMini) 1.4.dp.toPx() else 2.dp.toPx()
    val step = if (isMini) 12.dp.toPx() else 22.dp.toPx()

    var x = step
    while (x < size.width) {
        var y = step
        while (y < size.height) {
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(x, y)
            )
            y += step
        }
        x += step
    }
}

/**
 * Density-aware bounds calculation so illustrations scale cleanly across:
 * - Miniature theme picker swatches (~64dp)
 * - Compact/Standard homepage cards (~120-220dp)
 * - Full-screen editor canvas (~400-800dp)
 */
private data class IllustrationBounds(val originX: Float, val originY: Float, val scale: Float)

private fun DrawScope.calculateIllustrationBounds(
    targetWidthDp: Float = 140f,
    targetHeightDp: Float = 135f
): IllustrationBounds {
    val targetW = targetWidthDp.dp.toPx()
    val targetH = targetHeightDp.dp.toPx()
    val maxAllowedW = size.width * 0.72f
    val maxAllowedH = size.height * 0.78f

    val rawScale = (maxAllowedW / targetW).coerceAtMost(maxAllowedH / targetH)
    val scale = rawScale.coerceIn(0.28f, 1.55f)

    val actualWidth = targetW * scale
    val actualHeight = targetH * scale
    val pad = (6.dp.toPx() * scale).coerceAtLeast(2.dp.toPx())
    val originX = (size.width - actualWidth - pad).coerceAtLeast(0f)
    val originY = (size.height - actualHeight - pad).coerceAtLeast(0f)
    return IllustrationBounds(originX, originY, scale)
}

// 4. Groceries (Shopping bag, baguette, leafy greens, citrus)
private fun DrawScope.drawGroceriesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.20f)
    val accentFill = tint.copy(alpha = 0.12f)
    val (originX, originY, scale) = calculateIllustrationBounds(140f, 135f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Ambient floating leaves in top-right
    drawCircle(accentFill, radius = 8.dp.toPx() * scale, center = Offset(size.width * 0.82f, size.height * 0.18f))
    drawCircle(accentFill, radius = 5.dp.toPx() * scale, center = Offset(size.width * 0.18f, size.height * 0.85f))

    // Baguette & leafy stalks peeking out
    val baguette = RoundRect(
        left = originX + 34.dp.toPx() * scale,
        top = originY + 12.dp.toPx() * scale,
        right = originX + 54.dp.toPx() * scale,
        bottom = originY + 55.dp.toPx() * scale,
        cornerRadius = CornerRadius(10.dp.toPx() * scale)
    )
    val baguettePath = Path().apply { addRoundRect(baguette) }
    drawPath(baguettePath, fillColor)
    drawPath(baguettePath, strokeColor, style = stroke)

    // Leafy greens
    drawCircle(fillColor, radius = 14.dp.toPx() * scale, center = Offset(originX + 74.dp.toPx() * scale, originY + 34.dp.toPx() * scale))
    drawCircle(strokeColor, radius = 14.dp.toPx() * scale, center = Offset(originX + 74.dp.toPx() * scale, originY + 34.dp.toPx() * scale), style = stroke)

    // Bag body
    val bagPath = Path().apply {
        moveTo(originX + 24.dp.toPx() * scale, originY + 46.dp.toPx() * scale)
        lineTo(originX + 98.dp.toPx() * scale, originY + 46.dp.toPx() * scale)
        lineTo(originX + 92.dp.toPx() * scale, originY + 118.dp.toPx() * scale)
        lineTo(originX + 30.dp.toPx() * scale, originY + 118.dp.toPx() * scale)
        close()
    }
    drawPath(bagPath, fillColor)
    drawPath(bagPath, strokeColor, style = stroke)

    // Bag handles
    val handlePath = Path().apply {
        moveTo(originX + 44.dp.toPx() * scale, originY + 46.dp.toPx() * scale)
        cubicTo(
            originX + 44.dp.toPx() * scale, originY + 24.dp.toPx() * scale,
            originX + 78.dp.toPx() * scale, originY + 24.dp.toPx() * scale,
            originX + 78.dp.toPx() * scale, originY + 46.dp.toPx() * scale
        )
    }
    drawPath(handlePath, strokeColor, style = stroke)

    // Apple / Citrus fruit beside bag
    val fruitCenter = Offset(originX + 112.dp.toPx() * scale, originY + 102.dp.toPx() * scale)
    drawCircle(fillColor, radius = 15.dp.toPx() * scale, center = fruitCenter)
    drawCircle(strokeColor, radius = 15.dp.toPx() * scale, center = fruitCenter, style = stroke)
}

// 5. Food & Cooking (Skillet pan, lid, steam curls, herbs)
private fun DrawScope.drawFoodIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.20f)
    val (originX, originY, scale) = calculateIllustrationBounds(145f, 130f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Bowl / Skillet
    val panPath = Path().apply {
        moveTo(originX + 18.dp.toPx() * scale, originY + 68.dp.toPx() * scale)
        lineTo(originX + 98.dp.toPx() * scale, originY + 68.dp.toPx() * scale)
        cubicTo(
            originX + 94.dp.toPx() * scale, originY + 110.dp.toPx() * scale,
            originX + 22.dp.toPx() * scale, originY + 110.dp.toPx() * scale,
            originX + 18.dp.toPx() * scale, originY + 68.dp.toPx() * scale
        )
        close()
    }
    drawPath(panPath, fillColor)
    drawPath(panPath, strokeColor, style = stroke)

    // Pan handle
    drawLine(
        color = strokeColor,
        start = Offset(originX + 98.dp.toPx() * scale, originY + 74.dp.toPx() * scale),
        end = Offset(originX + 134.dp.toPx() * scale, originY + 64.dp.toPx() * scale),
        strokeWidth = (4.dp.toPx() * scale).coerceAtLeast(2f),
        cap = StrokeCap.Round
    )

    // Steam aroma curls
    val steamPath = Path().apply {
        moveTo(originX + 38.dp.toPx() * scale, originY + 56.dp.toPx() * scale)
        cubicTo(
            originX + 30.dp.toPx() * scale, originY + 42.dp.toPx() * scale,
            originX + 46.dp.toPx() * scale, originY + 32.dp.toPx() * scale,
            originX + 38.dp.toPx() * scale, originY + 18.dp.toPx() * scale
        )
        moveTo(originX + 58.dp.toPx() * scale, originY + 54.dp.toPx() * scale)
        cubicTo(
            originX + 50.dp.toPx() * scale, originY + 38.dp.toPx() * scale,
            originX + 66.dp.toPx() * scale, originY + 28.dp.toPx() * scale,
            originX + 58.dp.toPx() * scale, originY + 14.dp.toPx() * scale
        )
        moveTo(originX + 78.dp.toPx() * scale, originY + 56.dp.toPx() * scale)
        cubicTo(
            originX + 70.dp.toPx() * scale, originY + 42.dp.toPx() * scale,
            originX + 86.dp.toPx() * scale, originY + 32.dp.toPx() * scale,
            originX + 78.dp.toPx() * scale, originY + 18.dp.toPx() * scale
        )
    }
    drawPath(steamPath, strokeColor, style = stroke)
}

// 6. Music (Headphones, sound waves & floating musical notes)
private fun DrawScope.drawMusicIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.20f)
    val (originX, originY, scale) = calculateIllustrationBounds(140f, 130f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Headband arc
    val headband = Path().apply {
        moveTo(originX + 28.dp.toPx() * scale, originY + 80.dp.toPx() * scale)
        cubicTo(
            originX + 28.dp.toPx() * scale, originY + 22.dp.toPx() * scale,
            originX + 96.dp.toPx() * scale, originY + 22.dp.toPx() * scale,
            originX + 96.dp.toPx() * scale, originY + 80.dp.toPx() * scale
        )
    }
    drawPath(headband, strokeColor, style = stroke)

    val earcupPath = Path().apply {
        addRoundRect(
            RoundRect(
                left = originX + 18.dp.toPx() * scale,
                top = originY + 68.dp.toPx() * scale,
                right = originX + 38.dp.toPx() * scale,
                bottom = originY + 108.dp.toPx() * scale,
                cornerRadius = CornerRadius(8.dp.toPx() * scale)
            )
        )
        addRoundRect(
            RoundRect(
                left = originX + 86.dp.toPx() * scale,
                top = originY + 68.dp.toPx() * scale,
                right = originX + 106.dp.toPx() * scale,
                bottom = originY + 108.dp.toPx() * scale,
                cornerRadius = CornerRadius(8.dp.toPx() * scale)
            )
        )
    }
    drawPath(earcupPath, fillColor)
    drawPath(earcupPath, strokeColor, style = stroke)

    // Floating Beamed Eighth Notes
    drawCircle(strokeColor, radius = 4.5.dp.toPx() * scale, center = Offset(originX + 112.dp.toPx() * scale, originY + 46.dp.toPx() * scale))
    drawCircle(strokeColor, radius = 4.5.dp.toPx() * scale, center = Offset(originX + 128.dp.toPx() * scale, originY + 40.dp.toPx() * scale))
    val noteStem = Path().apply {
        moveTo(originX + 116.dp.toPx() * scale, originY + 46.dp.toPx() * scale)
        lineTo(originX + 116.dp.toPx() * scale, originY + 20.dp.toPx() * scale)
        lineTo(originX + 132.dp.toPx() * scale, originY + 15.dp.toPx() * scale)
        lineTo(originX + 132.dp.toPx() * scale, originY + 40.dp.toPx() * scale)
    }
    drawPath(noteStem, strokeColor, style = stroke)
}

// 7. Recipes & Baking (Whisk, mixing bowl, rolling pin)
private fun DrawScope.drawRecipesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.20f)
    val (originX, originY, scale) = calculateIllustrationBounds(140f, 130f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Mixing Bowl
    val bowl = Path().apply {
        moveTo(originX + 20.dp.toPx() * scale, originY + 72.dp.toPx() * scale)
        lineTo(originX + 94.dp.toPx() * scale, originY + 72.dp.toPx() * scale)
        cubicTo(
            originX + 90.dp.toPx() * scale, originY + 115.dp.toPx() * scale,
            originX + 24.dp.toPx() * scale, originY + 115.dp.toPx() * scale,
            originX + 20.dp.toPx() * scale, originY + 72.dp.toPx() * scale
        )
        close()
    }
    drawPath(bowl, fillColor)
    drawPath(bowl, strokeColor, style = stroke)

    // Whisk sticking out of bowl
    val whisk = Path().apply {
        moveTo(originX + 64.dp.toPx() * scale, originY + 72.dp.toPx() * scale)
        lineTo(originX + 116.dp.toPx() * scale, originY + 20.dp.toPx() * scale)
        moveTo(originX + 52.dp.toPx() * scale, originY + 72.dp.toPx() * scale)
        cubicTo(
            originX + 58.dp.toPx() * scale, originY + 38.dp.toPx() * scale,
            originX + 86.dp.toPx() * scale, originY + 38.dp.toPx() * scale,
            originX + 76.dp.toPx() * scale, originY + 72.dp.toPx() * scale
        )
    }
    drawPath(whisk, strokeColor, style = stroke)
}

// 8. Notes & Pen (Spiral notebook & fountain pen)
private fun DrawScope.drawNotesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.20f)
    val (originX, originY, scale) = calculateIllustrationBounds(135f, 125f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val padRect = RoundRect(
        left = originX + 24.dp.toPx() * scale,
        top = originY + 24.dp.toPx() * scale,
        right = originX + 90.dp.toPx() * scale,
        bottom = originY + 110.dp.toPx() * scale,
        cornerRadius = CornerRadius(8.dp.toPx() * scale)
    )
    val pad = Path().apply { addRoundRect(padRect) }
    drawPath(pad, fillColor)
    drawPath(pad, strokeColor, style = stroke)

    for (i in 0..3) {
        val yLine = originY + (44 + i * 16).dp.toPx() * scale
        drawLine(
            color = strokeColor,
            start = Offset(originX + 36.dp.toPx() * scale, yLine),
            end = Offset(originX + 78.dp.toPx() * scale, yLine),
            strokeWidth = (1.8.dp.toPx() * scale).coerceAtLeast(1f),
            cap = StrokeCap.Round
        )
    }

    // Pen
    drawLine(
        color = strokeColor,
        start = Offset(originX + 118.dp.toPx() * scale, originY + 22.dp.toPx() * scale),
        end = Offset(originX + 96.dp.toPx() * scale, originY + 96.dp.toPx() * scale),
        strokeWidth = (5.dp.toPx() * scale).coerceAtLeast(2.2f),
        cap = StrokeCap.Round
    )
}

// 9. Travel & Places (Mountain peaks, sun, soaring plane)
private fun DrawScope.drawPlacesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.20f)
    val (originX, originY, scale) = calculateIllustrationBounds(145f, 125f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Sun above mountains
    val sunCenter = Offset(originX + 38.dp.toPx() * scale, originY + 34.dp.toPx() * scale)
    drawCircle(fillColor, radius = 14.dp.toPx() * scale, center = sunCenter)
    drawCircle(strokeColor, radius = 14.dp.toPx() * scale, center = sunCenter, style = stroke)

    // Mountain silhouettes
    val mountains = Path().apply {
        moveTo(originX + 8.dp.toPx() * scale, originY + 114.dp.toPx() * scale)
        lineTo(originX + 52.dp.toPx() * scale, originY + 52.dp.toPx() * scale)
        lineTo(originX + 82.dp.toPx() * scale, originY + 86.dp.toPx() * scale)
        lineTo(originX + 110.dp.toPx() * scale, originY + 38.dp.toPx() * scale)
        lineTo(originX + 140.dp.toPx() * scale, originY + 114.dp.toPx() * scale)
        close()
    }
    drawPath(mountains, fillColor)
    drawPath(mountains, strokeColor, style = stroke)
}

// 10. Cinema & Film (Clapperboard & stars)
private fun DrawScope.drawVideoIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.20f)
    val (originX, originY, scale) = calculateIllustrationBounds(140f, 125f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val board = RoundRect(
        left = originX + 26.dp.toPx() * scale,
        top = originY + 52.dp.toPx() * scale,
        right = originX + 108.dp.toPx() * scale,
        bottom = originY + 112.dp.toPx() * scale,
        cornerRadius = CornerRadius(8.dp.toPx() * scale)
    )
    val boardPath = Path().apply { addRoundRect(board) }
    drawPath(boardPath, fillColor)
    drawPath(boardPath, strokeColor, style = stroke)

    val clapper = Path().apply {
        moveTo(originX + 24.dp.toPx() * scale, originY + 48.dp.toPx() * scale)
        lineTo(originX + 104.dp.toPx() * scale, originY + 26.dp.toPx() * scale)
        lineTo(originX + 108.dp.toPx() * scale, originY + 38.dp.toPx() * scale)
        lineTo(originX + 28.dp.toPx() * scale, originY + 58.dp.toPx() * scale)
        close()
    }
    drawPath(clapper, fillColor)
    drawPath(clapper, strokeColor, style = stroke)

    drawSparkleStar(originX + 122.dp.toPx() * scale, originY + 58.dp.toPx() * scale, 9.dp.toPx() * scale, strokeColor)
}

// 11. Celebration (Balloons, sparkles & confetti)
private fun DrawScope.drawCelebrationIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.22f)
    val (originX, originY, scale) = calculateIllustrationBounds(140f, 130f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val b1Center = Offset(originX + 44.dp.toPx() * scale, originY + 46.dp.toPx() * scale)
    drawCircle(fillColor, radius = 20.dp.toPx() * scale, center = b1Center)
    drawCircle(strokeColor, radius = 20.dp.toPx() * scale, center = b1Center, style = stroke)

    val b2Center = Offset(originX + 82.dp.toPx() * scale, originY + 36.dp.toPx() * scale)
    drawCircle(fillColor, radius = 22.dp.toPx() * scale, center = b2Center)
    drawCircle(strokeColor, radius = 22.dp.toPx() * scale, center = b2Center, style = stroke)

    val stringsPath = Path().apply {
        moveTo(b1Center.x, b1Center.y + 20.dp.toPx() * scale)
        lineTo(originX + 64.dp.toPx() * scale, originY + 114.dp.toPx() * scale)
        moveTo(b2Center.x, b2Center.y + 22.dp.toPx() * scale)
        lineTo(originX + 64.dp.toPx() * scale, originY + 114.dp.toPx() * scale)
    }
    drawPath(stringsPath, strokeColor, style = stroke)

    drawSparkleStar(originX + 116.dp.toPx() * scale, originY + 34.dp.toPx() * scale, 9.dp.toPx() * scale, strokeColor)
    drawSparkleStar(originX + 20.dp.toPx() * scale, originY + 84.dp.toPx() * scale, 7.dp.toPx() * scale, strokeColor)
}

// 12. Storm & Weather (Cloud, lightning bolt, raindrops)
private fun DrawScope.drawStormIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.22f)
    val (originX, originY, scale) = calculateIllustrationBounds(145f, 130f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val cloud = Path().apply {
        moveTo(originX + 32.dp.toPx() * scale, originY + 66.dp.toPx() * scale)
        cubicTo(
            originX + 16.dp.toPx() * scale, originY + 66.dp.toPx() * scale,
            originX + 16.dp.toPx() * scale, originY + 44.dp.toPx() * scale,
            originX + 36.dp.toPx() * scale, originY + 44.dp.toPx() * scale
        )
        cubicTo(
            originX + 42.dp.toPx() * scale, originY + 20.dp.toPx() * scale,
            originX + 78.dp.toPx() * scale, originY + 20.dp.toPx() * scale,
            originX + 84.dp.toPx() * scale, originY + 40.dp.toPx() * scale
        )
        cubicTo(
            originX + 104.dp.toPx() * scale, originY + 38.dp.toPx() * scale,
            originX + 110.dp.toPx() * scale, originY + 66.dp.toPx() * scale,
            originX + 92.dp.toPx() * scale, originY + 66.dp.toPx() * scale
        )
        close()
    }
    drawPath(cloud, fillColor)
    drawPath(cloud, strokeColor, style = stroke)

    val bolt = Path().apply {
        moveTo(originX + 64.dp.toPx() * scale, originY + 66.dp.toPx() * scale)
        lineTo(originX + 50.dp.toPx() * scale, originY + 90.dp.toPx() * scale)
        lineTo(originX + 66.dp.toPx() * scale, originY + 90.dp.toPx() * scale)
        lineTo(originX + 54.dp.toPx() * scale, originY + 116.dp.toPx() * scale)
    }
    drawPath(bolt, strokeColor, style = stroke)
}

// 13. Night Sky & Dusk (Crescent moon & constellations)
private fun DrawScope.drawDuskIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.48f)
    val fillColor = tint.copy(alpha = 0.22f)
    val (originX, originY, scale) = calculateIllustrationBounds(140f, 130f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val moon = Path().apply {
        moveTo(originX + 68.dp.toPx() * scale, originY + 22.dp.toPx() * scale)
        cubicTo(
            originX + 30.dp.toPx() * scale, originY + 32.dp.toPx() * scale,
            originX + 30.dp.toPx() * scale, originY + 90.dp.toPx() * scale,
            originX + 74.dp.toPx() * scale, originY + 100.dp.toPx() * scale
        )
        cubicTo(
            originX + 48.dp.toPx() * scale, originY + 84.dp.toPx() * scale,
            originX + 48.dp.toPx() * scale, originY + 38.dp.toPx() * scale,
            originX + 68.dp.toPx() * scale, originY + 22.dp.toPx() * scale
        )
        close()
    }
    drawPath(moon, fillColor)
    drawPath(moon, strokeColor, style = stroke)

    drawSparkleStar(originX + 98.dp.toPx() * scale, originY + 42.dp.toPx() * scale, 12.dp.toPx() * scale, strokeColor)
    drawSparkleStar(originX + 118.dp.toPx() * scale, originY + 74.dp.toPx() * scale, 8.dp.toPx() * scale, strokeColor)
    drawSparkleStar(originX + 88.dp.toPx() * scale, originY + 106.dp.toPx() * scale, 10.dp.toPx() * scale, strokeColor)
}

// 14. Flora & Nature (Botanical leaf & foliage)
private fun DrawScope.drawNatureIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.22f)
    val (originX, originY, scale) = calculateIllustrationBounds(145f, 130f)
    val stroke = Stroke(width = (2.4.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val leaf = Path().apply {
        moveTo(originX + 38.dp.toPx() * scale, originY + 116.dp.toPx() * scale)
        cubicTo(
            originX + 20.dp.toPx() * scale, originY + 68.dp.toPx() * scale,
            originX + 44.dp.toPx() * scale, originY + 22.dp.toPx() * scale,
            originX + 96.dp.toPx() * scale, originY + 18.dp.toPx() * scale
        )
        cubicTo(
            originX + 128.dp.toPx() * scale, originY + 52.dp.toPx() * scale,
            originX + 116.dp.toPx() * scale, originY + 98.dp.toPx() * scale,
            originX + 76.dp.toPx() * scale, originY + 116.dp.toPx() * scale
        )
        close()
    }
    drawPath(leaf, fillColor)
    drawPath(leaf, strokeColor, style = stroke)

    drawLine(
        color = strokeColor,
        start = Offset(originX + 38.dp.toPx() * scale, originY + 116.dp.toPx() * scale),
        end = Offset(originX + 96.dp.toPx() * scale, originY + 18.dp.toPx() * scale),
        strokeWidth = (2.2.dp.toPx() * scale).coerceAtLeast(1.2f)
    )
}

// 15. Doodles & Scribbles (Expressive geometric curves & sparkles)
private fun DrawScope.drawDoodlesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.46f)
    val fillColor = tint.copy(alpha = 0.18f)
    val (originX, originY, scale) = calculateIllustrationBounds(140f, 125f)
    val stroke = Stroke(width = (2.6.dp.toPx() * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    drawCircle(fillColor, radius = 26.dp.toPx() * scale, center = Offset(originX + 68.dp.toPx() * scale, originY + 64.dp.toPx() * scale))

    val spiral = Path().apply {
        moveTo(originX + 58.dp.toPx() * scale, originY + 60.dp.toPx() * scale)
        cubicTo(
            originX + 42.dp.toPx() * scale, originY + 42.dp.toPx() * scale,
            originX + 38.dp.toPx() * scale, originY + 84.dp.toPx() * scale,
            originX + 72.dp.toPx() * scale, originY + 88.dp.toPx() * scale
        )
        cubicTo(
            originX + 102.dp.toPx() * scale, originY + 88.dp.toPx() * scale,
            originX + 102.dp.toPx() * scale, originY + 36.dp.toPx() * scale,
            originX + 54.dp.toPx() * scale, originY + 28.dp.toPx() * scale
        )
    }
    drawPath(spiral, strokeColor, style = stroke)

    drawSparkleStar(originX + 112.dp.toPx() * scale, originY + 38.dp.toPx() * scale, 10.dp.toPx() * scale, strokeColor)
    drawSparkleStar(originX + 32.dp.toPx() * scale, originY + 100.dp.toPx() * scale, 8.dp.toPx() * scale, strokeColor)
}

private fun DrawScope.drawSparkleStar(cx: Float, cy: Float, size: Float, color: Color) {
    val star = Path().apply {
        moveTo(cx, cy - size)
        quadraticTo(cx, cy, cx + size, cy)
        quadraticTo(cx, cy, cx, cy + size)
        quadraticTo(cx, cy, cx - size, cy)
        quadraticTo(cx, cy, cx, cy - size)
        close()
    }
    drawPath(star, color, style = Fill)
}
