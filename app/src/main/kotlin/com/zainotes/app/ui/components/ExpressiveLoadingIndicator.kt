/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Shape-Morphing Loading Indicator.
 * Replaces classic CircularProgressIndicator with an expressive morphing scalloped/starburst polygon.
 */
@Composable
fun ExpressiveMorphingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = Color.Transparent
) {
    val transition = rememberInfiniteTransition(label = "M3ExpressiveMorph")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "MorphRotation"
    )

    val morphPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MorphPhase"
    )

    val pulseScale by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MorphScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .then(
                if (containerColor != Color.Transparent) {
                    Modifier
                        .clip(RoundedCornerShape(percent = 36))
                        .background(containerColor)
                } else {
                    Modifier
                }
            )
    ) {
        Canvas(
            modifier = Modifier
                .size(if (containerColor != Color.Transparent) size * 0.68f else size)
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val baseRadius = (this.size.minDimension / 2f) * 0.78f

            // Morph smoothly between 4-lobed squircle star, 6-lobed flower, and 8-lobed cookie
            val morphMix = (sin(morphPhase.toDouble()) + 1.0) / 2.0
            val amp4 = 0.22 * (1.0 - morphMix)
            val amp7 = 0.19 * morphMix

            val path = Path()
            val steps = 96
            for (i in 0..steps) {
                val theta = (i.toDouble() / steps.toDouble()) * 2.0 * PI
                val rMod = 1.0 +
                    amp4 * cos(4.0 * theta + morphPhase) +
                    amp7 * cos(6.0 * theta - morphPhase * 0.7)
                val r = (baseRadius * rMod).toFloat()
                val x = cx + r * cos(theta).toFloat()
                val y = cy + r * sin(theta).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(
                path = path,
                color = color
            )

            // Inner contrasting core dot that counter-pulses
            drawCircle(
                color = if (containerColor != Color.Transparent) containerColor else color.copy(alpha = 0.25f),
                radius = baseRadius * 0.32f,
                center = Offset(cx, cy)
            )
        }
    }
}

/**
 * Material 3 Expressive Wave Track Indicator for inline loading bars.
 */
@Composable
fun ExpressiveWaveProgressBar(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    val transition = rememberInfiniteTransition(label = "M3ExpressiveWave")
    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        val w = size.width
        val h = size.height
        val cy = h / 2f
        val amp = h * 0.32f

        // Background track line
        drawLine(
            color = trackColor,
            start = Offset(0f, cy),
            end = Offset(w, cy),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Expressive sinusoidal wave path
        val wavePath = Path()
        val steps = 64
        for (i in 0..steps) {
            val fraction = i.toFloat() / steps.toFloat()
            val x = fraction * w
            val y = cy + amp * sin((fraction * 4.0 * PI) - wavePhase.toDouble()).toFloat()
            if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
        }

        drawPath(
            path = wavePath,
            color = color,
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Material 3 Expressive AI Thinking Card shown in AI Chat while waiting for model completion.
 */
@Composable
fun ExpressiveAiThinkingCard(
    modelDisplayName: String,
    statusOverride: String? = null,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    val transition = rememberInfiniteTransition(label = "ThinkingShimmer")
    val barPulse1 by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BarPulse1"
    )
    val barPulse2 by transition.animateFloat(
        initialValue = 0.78f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 980, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BarPulse2"
    )

    val statusHint = when {
        !statusOverride.isNullOrBlank() -> statusOverride
        elapsedSeconds < 4 -> "Connecting to stream..."
        elapsedSeconds < 12 -> "Reasoning & checking workspace context..."
        else -> "Deep thinking (${elapsedSeconds}s) — local or reasoning models can take a moment..."
    }

    Surface(
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 26.dp,
            bottomStart = 8.dp,
            bottomEnd = 26.dp
        ),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        ),
        modifier = modifier.fillMaxWidth(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    ExpressiveMorphingIndicator(
                        size = 40.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = modelDisplayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = statusHint,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (elapsedSeconds > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            text = "${elapsedSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Expressive Sinusoidal Wave Bar
            ExpressiveWaveProgressBar(
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            // Expressive Morphing Shimmer Skeleton Pills
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barPulse1)
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barPulse2)
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                                )
                            )
                        )
                )
            }
        }
    }
}
