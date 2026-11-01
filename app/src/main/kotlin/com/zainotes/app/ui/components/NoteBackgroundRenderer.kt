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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.NoteBackgroundTheme

/**
 * Renders native vector background themes and patterns for notes,
 * including Google Keep themes (Groceries, Food, Music, Recipes, Notes, Places, Cinema,
 * Celebration, Storm, Dusk) and extended patterns (Nature, Doodles, Ruled, Grid, Dots).
 */
@Composable
fun NoteBackgroundRenderer(
    theme: NoteBackgroundTheme,
    tint: Color,
    modifier: Modifier = Modifier
) {
    if (theme == NoteBackgroundTheme.NONE) return

    Canvas(modifier = modifier.fillMaxSize()) {
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

// 1. Ruled Lines Pattern
private fun DrawScope.drawRuledPattern(tint: Color) {
    val lineColor = tint.copy(alpha = 0.18f)
    val strokeWidth = 1.2.dp.toPx()
    val step = 28.dp.toPx()
    var y = 32.dp.toPx()

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
    val gridColor = tint.copy(alpha = 0.12f)
    val strokeWidth = 1.dp.toPx()
    val step = 24.dp.toPx()

    var x = step
    while (x < size.width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = strokeWidth
        )
        x += step
    }

    var y = step
    while (y < size.height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth
        )
        y += step
    }
}

// 3. Dot Matrix Pattern
private fun DrawScope.drawDotsPattern(tint: Color) {
    val dotColor = tint.copy(alpha = 0.22f)
    val dotRadius = 1.5.dp.toPx()
    val step = 22.dp.toPx()

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

// 4. Groceries (Shopping bag, stalks/baguette, apple)
private fun DrawScope.drawGroceriesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.24f)
    val fillColor = tint.copy(alpha = 0.10f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (140.dp.toPx() * scale)
    val originY = size.height - (130.dp.toPx() * scale)

    // Bag body
    val bagPath = Path().apply {
        moveTo(originX + 25.dp.toPx() * scale, originY + 45.dp.toPx() * scale)
        lineTo(originX + 95.dp.toPx() * scale, originY + 45.dp.toPx() * scale)
        lineTo(originX + 90.dp.toPx() * scale, originY + 115.dp.toPx() * scale)
        lineTo(originX + 30.dp.toPx() * scale, originY + 115.dp.toPx() * scale)
        close()
    }
    drawPath(bagPath, fillColor)
    drawPath(bagPath, strokeColor, style = stroke)

    // Bag handles
    val handlePath = Path().apply {
        moveTo(originX + 42.dp.toPx() * scale, originY + 45.dp.toPx() * scale)
        cubicTo(
            originX + 42.dp.toPx() * scale, originY + 22.dp.toPx() * scale,
            originX + 78.dp.toPx() * scale, originY + 22.dp.toPx() * scale,
            originX + 78.dp.toPx() * scale, originY + 45.dp.toPx() * scale
        )
    }
    drawPath(handlePath, strokeColor, style = stroke)

    // Celery / Baguette peeking out
    val celeryPath = Path().apply {
        moveTo(originX + 40.dp.toPx() * scale, originY + 45.dp.toPx() * scale)
        lineTo(originX + 32.dp.toPx() * scale, originY + 10.dp.toPx() * scale)
        moveTo(originX + 50.dp.toPx() * scale, originY + 45.dp.toPx() * scale)
        lineTo(originX + 58.dp.toPx() * scale, originY + 8.dp.toPx() * scale)
    }
    drawPath(celeryPath, strokeColor, style = stroke)

    // Apple / Orange fruit near bag
    drawCircle(
        color = fillColor,
        radius = 14.dp.toPx() * scale,
        center = Offset(originX + 105.dp.toPx() * scale, originY + 102.dp.toPx() * scale)
    )
    drawCircle(
        color = strokeColor,
        radius = 14.dp.toPx() * scale,
        center = Offset(originX + 105.dp.toPx() * scale, originY + 102.dp.toPx() * scale),
        style = stroke
    )
}

// 5. Food & Cooking (Chef hat, fork, spoon, pan)
private fun DrawScope.drawFoodIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.24f)
    val fillColor = tint.copy(alpha = 0.10f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (145.dp.toPx() * scale)
    val originY = size.height - (130.dp.toPx() * scale)

    // Pan / Bowl
    val panPath = Path().apply {
        moveTo(originX + 20.dp.toPx() * scale, originY + 70.dp.toPx() * scale)
        cubicTo(
            originX + 25.dp.toPx() * scale, originY + 105.dp.toPx() * scale,
            originX + 90.dp.toPx() * scale, originY + 105.dp.toPx() * scale,
            originX + 95.dp.toPx() * scale, originY + 70.dp.toPx() * scale
        )
        close()
    }
    drawPath(panPath, fillColor)
    drawPath(panPath, strokeColor, style = stroke)

    // Pan handle
    drawLine(
        color = strokeColor,
        start = Offset(originX + 95.dp.toPx() * scale, originY + 75.dp.toPx() * scale),
        end = Offset(originX + 130.dp.toPx() * scale, originY + 80.dp.toPx() * scale),
        strokeWidth = 3.dp.toPx() * scale,
        cap = StrokeCap.Round
    )

    // Steam aroma curls
    val steamPath = Path().apply {
        moveTo(originX + 45.dp.toPx() * scale, originY + 60.dp.toPx() * scale)
        cubicTo(
            originX + 40.dp.toPx() * scale, originY + 45.dp.toPx() * scale,
            originX + 50.dp.toPx() * scale, originY + 35.dp.toPx() * scale,
            originX + 46.dp.toPx() * scale, originY + 22.dp.toPx() * scale
        )
        moveTo(originX + 68.dp.toPx() * scale, originY + 60.dp.toPx() * scale)
        cubicTo(
            originX + 63.dp.toPx() * scale, originY + 45.dp.toPx() * scale,
            originX + 73.dp.toPx() * scale, originY + 35.dp.toPx() * scale,
            originX + 70.dp.toPx() * scale, originY + 20.dp.toPx() * scale
        )
    }
    drawPath(steamPath, strokeColor, style = stroke)
}

// 6. Music (Headphones & floating musical notes)
private fun DrawScope.drawMusicIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.26f)
    val fillColor = tint.copy(alpha = 0.12f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (140.dp.toPx() * scale)
    val originY = size.height - (130.dp.toPx() * scale)

    // Headband arc
    val headband = Path().apply {
        moveTo(originX + 30.dp.toPx() * scale, originY + 80.dp.toPx() * scale)
        cubicTo(
            originX + 30.dp.toPx() * scale, originY + 25.dp.toPx() * scale,
            originX + 95.dp.toPx() * scale, originY + 25.dp.toPx() * scale,
            originX + 95.dp.toPx() * scale, originY + 80.dp.toPx() * scale
        )
    }
    drawPath(headband, strokeColor, style = stroke)

    // Left and Right earcups
    val leftEarcup = RoundRect(
        left = originX + 22.dp.toPx() * scale,
        top = originY + 70.dp.toPx() * scale,
        right = originX + 38.dp.toPx() * scale,
        bottom = originY + 105.dp.toPx() * scale,
        cornerRadius = CornerRadius(6.dp.toPx() * scale)
    )
    val rightEarcup = RoundRect(
        left = originX + 88.dp.toPx() * scale,
        top = originY + 70.dp.toPx() * scale,
        right = originX + 104.dp.toPx() * scale,
        bottom = originY + 105.dp.toPx() * scale,
        cornerRadius = CornerRadius(6.dp.toPx() * scale)
    )
    val earcupsPath = Path().apply {
        addRoundRect(leftEarcup)
        addRoundRect(rightEarcup)
    }
    drawPath(earcupsPath, fillColor)
    drawPath(earcupsPath, strokeColor, style = stroke)

    // Floating Eighth Notes
    drawCircle(strokeColor, radius = 4.dp.toPx() * scale, center = Offset(originX + 115.dp.toPx() * scale, originY + 45.dp.toPx() * scale))
    drawCircle(strokeColor, radius = 4.dp.toPx() * scale, center = Offset(originX + 128.dp.toPx() * scale, originY + 40.dp.toPx() * scale))
    val noteStem = Path().apply {
        moveTo(originX + 119.dp.toPx() * scale, originY + 45.dp.toPx() * scale)
        lineTo(originX + 119.dp.toPx() * scale, originY + 22.dp.toPx() * scale)
        lineTo(originX + 132.dp.toPx() * scale, originY + 17.dp.toPx() * scale)
        lineTo(originX + 132.dp.toPx() * scale, originY + 40.dp.toPx() * scale)
    }
    drawPath(noteStem, strokeColor, style = stroke)
}

// 7. Recipes & Baking (Whisk, bowl, rolling pin)
private fun DrawScope.drawRecipesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.24f)
    val fillColor = tint.copy(alpha = 0.10f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (140.dp.toPx() * scale)
    val originY = size.height - (130.dp.toPx() * scale)

    // Rolling pin
    val pin = Path().apply {
        moveTo(originX + 15.dp.toPx() * scale, originY + 30.dp.toPx() * scale)
        lineTo(originX + 105.dp.toPx() * scale, originY + 105.dp.toPx() * scale)
    }
    drawPath(pin, strokeColor, style = Stroke(width = 8.dp.toPx() * scale, cap = StrokeCap.Round))

    // Whisk wires
    val whiskPath = Path().apply {
        moveTo(originX + 75.dp.toPx() * scale, originY + 45.dp.toPx() * scale)
        cubicTo(
            originX + 55.dp.toPx() * scale, originY + 25.dp.toPx() * scale,
            originX + 95.dp.toPx() * scale, originY + 15.dp.toPx() * scale,
            originX + 105.dp.toPx() * scale, originY + 35.dp.toPx() * scale
        )
        lineTo(originX + 125.dp.toPx() * scale, originY + 15.dp.toPx() * scale)
    }
    drawPath(whiskPath, strokeColor, style = stroke)

    // Cupcake / Bowl silhouette
    drawCircle(fillColor, radius = 18.dp.toPx() * scale, center = Offset(originX + 50.dp.toPx() * scale, originY + 95.dp.toPx() * scale))
    drawCircle(strokeColor, radius = 18.dp.toPx() * scale, center = Offset(originX + 50.dp.toPx() * scale, originY + 95.dp.toPx() * scale), style = stroke)
}

// 8. Notes & Doodles (Notepad, fountain pen, clips)
private fun DrawScope.drawNotesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.25f)
    val fillColor = tint.copy(alpha = 0.10f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (135.dp.toPx() * scale)
    val originY = size.height - (125.dp.toPx() * scale)

    // Tilted notebook page
    val pad = Path().apply {
        moveTo(originX + 25.dp.toPx() * scale, originY + 30.dp.toPx() * scale)
        lineTo(originX + 85.dp.toPx() * scale, originY + 30.dp.toPx() * scale)
        lineTo(originX + 85.dp.toPx() * scale, originY + 105.dp.toPx() * scale)
        lineTo(originX + 25.dp.toPx() * scale, originY + 105.dp.toPx() * scale)
        close()
    }
    drawPath(pad, fillColor)
    drawPath(pad, strokeColor, style = stroke)

    // Notepad spiral rings
    for (i in 0..4) {
        val yRing = originY + (42.dp.toPx() + (i * 12).dp.toPx()) * scale
        drawLine(
            color = strokeColor,
            start = Offset(originX + 20.dp.toPx() * scale, yRing),
            end = Offset(originX + 28.dp.toPx() * scale, yRing),
            strokeWidth = 2.dp.toPx() * scale,
            cap = StrokeCap.Round
        )
    }

    // Pen leaning on page
    val pen = Path().apply {
        moveTo(originX + 90.dp.toPx() * scale, originY + 20.dp.toPx() * scale)
        lineTo(originX + 115.dp.toPx() * scale, originY + 85.dp.toPx() * scale)
    }
    drawPath(pen, strokeColor, style = Stroke(width = 4.dp.toPx() * scale, cap = StrokeCap.Round))
}

// 9. Travel & Places (Mountains, airplane trail)
private fun DrawScope.drawPlacesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.25f)
    val fillColor = tint.copy(alpha = 0.10f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (145.dp.toPx() * scale)
    val originY = size.height - (125.dp.toPx() * scale)

    // Mountain silhouettes
    val mountains = Path().apply {
        moveTo(originX + 10.dp.toPx() * scale, originY + 115.dp.toPx() * scale)
        lineTo(originX + 55.dp.toPx() * scale, originY + 50.dp.toPx() * scale)
        lineTo(originX + 85.dp.toPx() * scale, originY + 85.dp.toPx() * scale)
        lineTo(originX + 110.dp.toPx() * scale, originY + 40.dp.toPx() * scale)
        lineTo(originX + 140.dp.toPx() * scale, originY + 115.dp.toPx() * scale)
        close()
    }
    drawPath(mountains, fillColor)
    drawPath(mountains, strokeColor, style = stroke)

    // Airplane soaring arc
    val airplaneTrail = Path().apply {
        moveTo(originX + 30.dp.toPx() * scale, originY + 90.dp.toPx() * scale)
        cubicTo(
            originX + 60.dp.toPx() * scale, originY + 20.dp.toPx() * scale,
            originX + 100.dp.toPx() * scale, originY + 15.dp.toPx() * scale,
            originX + 130.dp.toPx() * scale, originY + 18.dp.toPx() * scale
        )
    }
    drawPath(airplaneTrail, strokeColor, style = Stroke(width = 1.5.dp.toPx() * scale, cap = StrokeCap.Round))

    // Tiny plane at end of trail
    val plane = Path().apply {
        moveTo(originX + 130.dp.toPx() * scale, originY + 18.dp.toPx() * scale)
        lineTo(originX + 120.dp.toPx() * scale, originY + 14.dp.toPx() * scale)
        lineTo(originX + 123.dp.toPx() * scale, originY + 18.dp.toPx() * scale)
        lineTo(originX + 120.dp.toPx() * scale, originY + 22.dp.toPx() * scale)
        close()
    }
    drawPath(plane, strokeColor, style = Fill)
}

// 10. Cinema & Video (Clapperboard, film roll)
private fun DrawScope.drawVideoIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.25f)
    val fillColor = tint.copy(alpha = 0.10f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (140.dp.toPx() * scale)
    val originY = size.height - (125.dp.toPx() * scale)

    // Clapperboard body
    val board = RoundRect(
        left = originX + 30.dp.toPx() * scale,
        top = originY + 50.dp.toPx() * scale,
        right = originX + 105.dp.toPx() * scale,
        bottom = originY + 110.dp.toPx() * scale,
        cornerRadius = CornerRadius(6.dp.toPx() * scale)
    )
    val boardPath = Path().apply { addRoundRect(board) }
    drawPath(boardPath, fillColor)
    drawPath(boardPath, strokeColor, style = stroke)

    // Clapper top stick (angled open)
    val clapper = Path().apply {
        moveTo(originX + 28.dp.toPx() * scale, originY + 50.dp.toPx() * scale)
        lineTo(originX + 102.dp.toPx() * scale, originY + 30.dp.toPx() * scale)
        lineTo(originX + 106.dp.toPx() * scale, originY + 40.dp.toPx() * scale)
        lineTo(originX + 32.dp.toPx() * scale, originY + 58.dp.toPx() * scale)
        close()
    }
    drawPath(clapper, strokeColor, style = stroke)

    // Popcorn kernel / star bursts
    drawCircle(strokeColor, radius = 5.dp.toPx() * scale, center = Offset(originX + 118.dp.toPx() * scale, originY + 70.dp.toPx() * scale))
    drawCircle(strokeColor, radius = 3.dp.toPx() * scale, center = Offset(originX + 125.dp.toPx() * scale, originY + 90.dp.toPx() * scale))
}

// 11. Celebration (Balloons, party popper, confetti)
private fun DrawScope.drawCelebrationIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.26f)
    val fillColor = tint.copy(alpha = 0.12f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (140.dp.toPx() * scale)
    val originY = size.height - (130.dp.toPx() * scale)

    // Balloon 1
    val b1Center = Offset(originX + 45.dp.toPx() * scale, originY + 45.dp.toPx() * scale)
    drawCircle(fillColor, radius = 20.dp.toPx() * scale, center = b1Center)
    drawCircle(strokeColor, radius = 20.dp.toPx() * scale, center = b1Center, style = stroke)
    val s1 = Path().apply {
        moveTo(b1Center.x, b1Center.y + 20.dp.toPx() * scale)
        cubicTo(b1Center.x - 5.dp.toPx() * scale, originY + 85.dp.toPx() * scale, b1Center.x + 8.dp.toPx() * scale, originY + 95.dp.toPx() * scale, originX + 65.dp.toPx() * scale, originY + 115.dp.toPx() * scale)
    }
    drawPath(s1, strokeColor, style = stroke)

    // Balloon 2
    val b2Center = Offset(originX + 80.dp.toPx() * scale, originY + 38.dp.toPx() * scale)
    drawCircle(fillColor, radius = 22.dp.toPx() * scale, center = b2Center)
    drawCircle(strokeColor, radius = 22.dp.toPx() * scale, center = b2Center, style = stroke)
    val s2 = Path().apply {
        moveTo(b2Center.x, b2Center.y + 22.dp.toPx() * scale)
        cubicTo(b2Center.x + 6.dp.toPx() * scale, originY + 80.dp.toPx() * scale, b2Center.x - 4.dp.toPx() * scale, originY + 95.dp.toPx() * scale, originX + 65.dp.toPx() * scale, originY + 115.dp.toPx() * scale)
    }
    drawPath(s2, strokeColor, style = stroke)

    // Confetti stars
    drawCircle(strokeColor, radius = 2.5.dp.toPx() * scale, center = Offset(originX + 110.dp.toPx() * scale, originY + 25.dp.toPx() * scale))
    drawCircle(strokeColor, radius = 3.dp.toPx() * scale, center = Offset(originX + 122.dp.toPx() * scale, originY + 55.dp.toPx() * scale))
    drawCircle(strokeColor, radius = 2.dp.toPx() * scale, center = Offset(originX + 115.dp.toPx() * scale, originY + 85.dp.toPx() * scale))
}

// 12. Storm & Weather (Storm cloud, lightning bolt, raindrops)
private fun DrawScope.drawStormIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.26f)
    val fillColor = tint.copy(alpha = 0.12f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (145.dp.toPx() * scale)
    val originY = size.height - (130.dp.toPx() * scale)

    // Cloud bubbles
    val cloud = Path().apply {
        moveTo(originX + 35.dp.toPx() * scale, originY + 65.dp.toPx() * scale)
        cubicTo(
            originX + 20.dp.toPx() * scale, originY + 65.dp.toPx() * scale,
            originX + 20.dp.toPx() * scale, originY + 45.dp.toPx() * scale,
            originX + 38.dp.toPx() * scale, originY + 45.dp.toPx() * scale
        )
        cubicTo(
            originX + 42.dp.toPx() * scale, originY + 25.dp.toPx() * scale,
            originX + 75.dp.toPx() * scale, originY + 25.dp.toPx() * scale,
            originX + 80.dp.toPx() * scale, originY + 42.dp.toPx() * scale
        )
        cubicTo(
            originX + 98.dp.toPx() * scale, originY + 40.dp.toPx() * scale,
            originX + 105.dp.toPx() * scale, originY + 65.dp.toPx() * scale,
            originX + 90.dp.toPx() * scale, originY + 65.dp.toPx() * scale
        )
        close()
    }
    drawPath(cloud, fillColor)
    drawPath(cloud, strokeColor, style = stroke)

    // Lightning bolt
    val bolt = Path().apply {
        moveTo(originX + 62.dp.toPx() * scale, originY + 66.dp.toPx() * scale)
        lineTo(originX + 50.dp.toPx() * scale, originY + 88.dp.toPx() * scale)
        lineTo(originX + 64.dp.toPx() * scale, originY + 88.dp.toPx() * scale)
        lineTo(originX + 55.dp.toPx() * scale, originY + 115.dp.toPx() * scale)
    }
    drawPath(bolt, strokeColor, style = stroke)

    // Raindrops
    drawLine(strokeColor, Offset(originX + 35.dp.toPx() * scale, originY + 78.dp.toPx() * scale), Offset(originX + 30.dp.toPx() * scale, originY + 92.dp.toPx() * scale), strokeWidth = 2.dp.toPx() * scale, cap = StrokeCap.Round)
    drawLine(strokeColor, Offset(originX + 80.dp.toPx() * scale, originY + 75.dp.toPx() * scale), Offset(originX + 75.dp.toPx() * scale, originY + 89.dp.toPx() * scale), strokeWidth = 2.dp.toPx() * scale, cap = StrokeCap.Round)
    drawLine(strokeColor, Offset(originX + 95.dp.toPx() * scale, originY + 82.dp.toPx() * scale), Offset(originX + 90.dp.toPx() * scale, originY + 96.dp.toPx() * scale), strokeWidth = 2.dp.toPx() * scale, cap = StrokeCap.Round)
}

// 13. Night Sky & Dusk (Crescent moon, shining stars, constellation)
private fun DrawScope.drawDuskIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.28f)
    val fillColor = tint.copy(alpha = 0.12f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (140.dp.toPx() * scale)
    val originY = size.height - (130.dp.toPx() * scale)

    // Crescent moon
    val moon = Path().apply {
        moveTo(originX + 65.dp.toPx() * scale, originY + 25.dp.toPx() * scale)
        cubicTo(
            originX + 35.dp.toPx() * scale, originY + 35.dp.toPx() * scale,
            originX + 35.dp.toPx() * scale, originY + 85.dp.toPx() * scale,
            originX + 70.dp.toPx() * scale, originY + 95.dp.toPx() * scale
        )
        cubicTo(
            originX + 48.dp.toPx() * scale, originY + 80.dp.toPx() * scale,
            originX + 48.dp.toPx() * scale, originY + 40.dp.toPx() * scale,
            originX + 65.dp.toPx() * scale, originY + 25.dp.toPx() * scale
        )
        close()
    }
    drawPath(moon, fillColor)
    drawPath(moon, strokeColor, style = stroke)

    // Twinkling stars (4-pointed sparkles)
    drawSparkleStar(originX + 95.dp.toPx() * scale, originY + 45.dp.toPx() * scale, 12.dp.toPx() * scale, strokeColor)
    drawSparkleStar(originX + 115.dp.toPx() * scale, originY + 75.dp.toPx() * scale, 8.dp.toPx() * scale, strokeColor)
    drawSparkleStar(originX + 85.dp.toPx() * scale, originY + 105.dp.toPx() * scale, 10.dp.toPx() * scale, strokeColor)
}

// 14. Flora & Nature (Monstera tropical leaf & botanical foliage)
private fun DrawScope.drawNatureIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.25f)
    val fillColor = tint.copy(alpha = 0.10f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (145.dp.toPx() * scale)
    val originY = size.height - (130.dp.toPx() * scale)

    // Monstera leaf outline
    val leaf = Path().apply {
        moveTo(originX + 40.dp.toPx() * scale, originY + 115.dp.toPx() * scale)
        cubicTo(
            originX + 25.dp.toPx() * scale, originY + 70.dp.toPx() * scale,
            originX + 45.dp.toPx() * scale, originY + 25.dp.toPx() * scale,
            originX + 90.dp.toPx() * scale, originY + 20.dp.toPx() * scale
        )
        cubicTo(
            originX + 125.dp.toPx() * scale, originY + 50.dp.toPx() * scale,
            originX + 115.dp.toPx() * scale, originY + 95.dp.toPx() * scale,
            originX + 80.dp.toPx() * scale, originY + 115.dp.toPx() * scale
        )
        close()
    }
    drawPath(leaf, fillColor)
    drawPath(leaf, strokeColor, style = stroke)

    // Main leaf vein
    drawLine(
        color = strokeColor,
        start = Offset(originX + 40.dp.toPx() * scale, originY + 115.dp.toPx() * scale),
        end = Offset(originX + 90.dp.toPx() * scale, originY + 20.dp.toPx() * scale),
        strokeWidth = 2.dp.toPx() * scale
    )

    // Side veins
    drawLine(strokeColor, Offset(originX + 60.dp.toPx() * scale, originY + 80.dp.toPx() * scale), Offset(originX + 35.dp.toPx() * scale, originY + 65.dp.toPx() * scale), strokeWidth = 1.5.dp.toPx() * scale)
    drawLine(strokeColor, Offset(originX + 75.dp.toPx() * scale, originY + 50.dp.toPx() * scale), Offset(originX + 110.dp.toPx() * scale, originY + 60.dp.toPx() * scale), strokeWidth = 1.5.dp.toPx() * scale)
}

// 15. Doodles & Scribbles (Creative spirals & geometric squiggles)
private fun DrawScope.drawDoodlesIllustration(tint: Color) {
    val strokeColor = tint.copy(alpha = 0.26f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scale = (size.width.coerceAtMost(size.height) / 300f).coerceIn(0.65f, 1.4f)
    val originX = size.width - (140.dp.toPx() * scale)
    val originY = size.height - (125.dp.toPx() * scale)

    // Spiral swirl
    val spiral = Path().apply {
        moveTo(originX + 60.dp.toPx() * scale, originY + 60.dp.toPx() * scale)
        cubicTo(
            originX + 45.dp.toPx() * scale, originY + 45.dp.toPx() * scale,
            originX + 40.dp.toPx() * scale, originY + 80.dp.toPx() * scale,
            originX + 70.dp.toPx() * scale, originY + 85.dp.toPx() * scale
        )
        cubicTo(
            originX + 95.dp.toPx() * scale, originY + 85.dp.toPx() * scale,
            originX + 95.dp.toPx() * scale, originY + 40.dp.toPx() * scale,
            originX + 55.dp.toPx() * scale, originY + 30.dp.toPx() * scale
        )
    }
    drawPath(spiral, strokeColor, style = stroke)

    // Decorative sparks & crosses
    drawSparkleStar(originX + 110.dp.toPx() * scale, originY + 40.dp.toPx() * scale, 10.dp.toPx() * scale, strokeColor)
    drawSparkleStar(originX + 35.dp.toPx() * scale, originY + 100.dp.toPx() * scale, 8.dp.toPx() * scale, strokeColor)
    drawSparkleStar(originX + 115.dp.toPx() * scale, originY + 95.dp.toPx() * scale, 7.dp.toPx() * scale, strokeColor)
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
