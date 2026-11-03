/*
 * Copyright (C) 2026 Zainotes Contributors
 * Licensed under the GNU Affero General Public License v3.0 or later.
 */

package com.zainotes.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.BlockType
import com.zainotes.app.data.model.NoteBlock

/**
 * Dual-Tier Material 3 Expressive Editor Floating Dock.
 *
 * 1. Upper Floating Bar (Text Formatting Pill):
 *    Positioned directly above the bottom bar, styled as a matching M3 Expressive floating pill,
 *    providing Bold, Italic, Underline, Strikethrough, Highlight, Code/Monospace, Font Size,
 *    Text Alignment, and Clear Formatting for the active block.
 * 2. Lower Floating Bar (Block & Media Dock):
 *    Leftmost circular '+' button to add files (Images, Audio, Video) + middle floating pill
 *    containing Text, Checklist, Bullets, 1 2 3, Heading, Quote, Table, Code, and Size +
 *    right circular Check (Done) button.
 */
@Composable
fun EditorFloatingDock(
    activeBlock: NoteBlock?,
    onDone: () -> Unit,
    onAddBlock: (BlockType) -> Unit,
    onOpenSizePicker: () -> Unit,
    onOpenMediaPicker: () -> Unit = {},
    onToggleBold: () -> Unit = {},
    onToggleItalic: () -> Unit = {},
    onToggleUnderline: () -> Unit = {},
    onToggleStrikethrough: () -> Unit = {},
    onToggleHighlight: () -> Unit = {},
    onToggleMonospace: () -> Unit = {},
    onCycleTextSize: () -> Unit = {},
    onCycleTextAlign: () -> Unit = {},
    onClearFormatting: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Upper Floating Bar: Rich Text Formatting Pill
        Surface(
            modifier = Modifier
                .height(48.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.16f),
                    spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f)
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FormattingToggleChip(
                    icon = Icons.Default.FormatBold,
                    label = "Bold",
                    selected = activeBlock?.isBold == true,
                    onClick = onToggleBold,
                    contentDescription = "Toggle bold text"
                )

                FormattingToggleChip(
                    icon = Icons.Default.FormatItalic,
                    label = "Italic",
                    selected = activeBlock?.isItalic == true,
                    onClick = onToggleItalic,
                    contentDescription = "Toggle italic text"
                )

                FormattingToggleChip(
                    icon = Icons.Default.FormatUnderlined,
                    label = "Underline",
                    selected = activeBlock?.isUnderline == true,
                    onClick = onToggleUnderline,
                    contentDescription = "Toggle underline text"
                )

                FormattingToggleChip(
                    icon = Icons.Default.FormatStrikethrough,
                    label = "Strike",
                    selected = activeBlock?.isStrikethrough == true,
                    onClick = onToggleStrikethrough,
                    contentDescription = "Toggle strikethrough text"
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(22.dp)
                        .padding(horizontal = 2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )

                FormattingToggleChip(
                    icon = Icons.Default.BorderColor,
                    label = "Highlight",
                    selected = activeBlock?.isHighlighted == true,
                    onClick = onToggleHighlight,
                    contentDescription = "Toggle text marker highlight"
                )

                FormattingToggleChip(
                    icon = Icons.Default.Code,
                    label = "Code",
                    selected = activeBlock?.isMonospace == true,
                    onClick = onToggleMonospace,
                    contentDescription = "Toggle monospace code font"
                )

                val sizeLabel = when (activeBlock?.textScale ?: 0) {
                    1 -> "Large"
                    -1 -> "Small"
                    else -> "Size"
                }
                FormattingToggleChip(
                    icon = Icons.Default.FormatSize,
                    label = sizeLabel,
                    selected = (activeBlock?.textScale ?: 0) != 0,
                    onClick = onCycleTextSize,
                    contentDescription = "Cycle text size"
                )

                val alignIcon = when (activeBlock?.textAlign ?: 0) {
                    1 -> Icons.Default.FormatAlignCenter
                    2 -> Icons.Default.FormatAlignRight
                    else -> Icons.Default.FormatAlignLeft
                }
                val alignLabel = when (activeBlock?.textAlign ?: 0) {
                    1 -> "Center"
                    2 -> "Right"
                    else -> "Align"
                }
                FormattingToggleChip(
                    icon = alignIcon,
                    label = alignLabel,
                    selected = (activeBlock?.textAlign ?: 0) != 0,
                    onClick = onCycleTextAlign,
                    contentDescription = "Cycle text alignment"
                )

                if (activeBlock?.hasCustomFormatting == true) {
                    VerticalDivider(
                        modifier = Modifier
                            .height(22.dp)
                            .padding(horizontal = 2.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                    FormattingToggleChip(
                        icon = Icons.Default.FormatClear,
                        label = "Clear",
                        selected = false,
                        onClick = onClearFormatting,
                        contentDescription = "Clear block formatting"
                    )
                }
            }
        }

        // 2. Lower Floating Bar: Leftmost '+' FAB (Add Image/Audio/Video) + Middle Block Pill + Right Check (Done) FAB
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val addMediaInteraction = remember { MutableInteractionSource() }
            val isAddMediaPressed by addMediaInteraction.collectIsPressedAsState()
            val addMediaScale by animateFloatAsState(
                targetValue = if (isAddMediaPressed) 0.90f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "addMediaButtonScale"
            )

            // Most left button: '+' button to add Images, Audio, or Video files
            FloatingActionButton(
                onClick = onOpenMediaPicker,
                interactionSource = addMediaInteraction,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .scale(addMediaScale)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Add image, audio, or video file"
                    },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add image, audio, or video file",
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Middle Long Floating Pill with Block Insertion & Theme Controls
            Surface(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(54.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.16f),
                        spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f)
                    ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EditorDockItem(
                        icon = Icons.Default.TextFields,
                        label = "Text",
                        onClick = { onAddBlock(BlockType.TEXT) },
                        contentDescription = "Add text paragraph"
                    )

                    EditorDockItem(
                        icon = Icons.Default.CheckBox,
                        label = "Checklist",
                        onClick = { onAddBlock(BlockType.TASK) },
                        contentDescription = "Add checklist task item"
                    )

                    EditorDockItem(
                        icon = Icons.Default.FormatListBulleted,
                        label = "Bullets",
                        onClick = { onAddBlock(BlockType.BULLET) },
                        contentDescription = "Add bullet list item"
                    )

                    EditorDockItem(
                        icon = Icons.Default.FormatListNumbered,
                        label = "1 2 3",
                        onClick = { onAddBlock(BlockType.NUMBERED) },
                        contentDescription = "Add numbered 1 2 3 list item"
                    )

                    EditorDockItem(
                        icon = Icons.Default.Title,
                        label = "Heading",
                        onClick = { onAddBlock(BlockType.HEADING) },
                        contentDescription = "Add heading block"
                    )

                    EditorDockItem(
                        icon = Icons.Default.FormatQuote,
                        label = "Quote",
                        onClick = { onAddBlock(BlockType.QUOTE) },
                        contentDescription = "Add quote callout block"
                    )

                    EditorDockItem(
                        icon = Icons.Default.TableChart,
                        label = "Table",
                        onClick = { onAddBlock(BlockType.TABLE) },
                        contentDescription = "Add interactive table block"
                    )

                    EditorDockItem(
                        icon = Icons.Default.Terminal,
                        label = "Code",
                        onClick = { onAddBlock(BlockType.CODE) },
                        contentDescription = "Add code block"
                    )

                    EditorDockItem(
                        icon = Icons.Default.AspectRatio,
                        label = "Size",
                        onClick = onOpenSizePicker,
                        contentDescription = "Change card size layout"
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val doneInteraction = remember { MutableInteractionSource() }
            val isDonePressed by doneInteraction.collectIsPressedAsState()
            val doneScale by animateFloatAsState(
                targetValue = if (isDonePressed) 0.90f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "doneButtonScale"
            )

            FloatingActionButton(
                onClick = onDone,
                interactionSource = doneInteraction,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .scale(doneScale)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Done editing note"
                    },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done editing note",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FormattingToggleChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "formatChipBg"
    )

    val fgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "formatChipFg"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 650f
        ),
        label = "formatChipScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = bgColor,
        contentColor = fgColor,
        modifier = modifier
            .height(38.dp)
            .scale(scale)
            .semantics {
                role = Role.Checkbox
                this.selected = selected
                this.contentDescription = contentDescription
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun EditorDockItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    contentDescription: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = 620f
        ),
        label = "editorDockItemScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = if (highlight) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        contentColor = if (highlight) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .height(44.dp)
            .scale(scale)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
