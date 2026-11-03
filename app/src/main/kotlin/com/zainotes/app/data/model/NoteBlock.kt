/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.data.model

import java.util.UUID

enum class BlockType {
    TEXT,
    TASK,
    BULLET,
    NUMBERED,
    HEADING,
    QUOTE
}

data class NoteBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.TEXT,
    val content: String = "",
    val isChecked: Boolean = false,
    val order: Int = 0,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isHighlighted: Boolean = false,
    val isMonospace: Boolean = false,
    val textScale: Int = 0, // -1 = Small, 0 = Normal, 1 = Large
    val textAlign: Int = 0  // 0 = Start, 1 = Center, 2 = End
) {
    val hasCustomFormatting: Boolean
        get() = isBold || isItalic || isUnderline || isStrikethrough ||
            isHighlighted || isMonospace || textScale != 0 || textAlign != 0
}

/**
 * Calculates the sequential list number for a [BlockType.NUMBERED] block,
 * restarting at 1 for each consecutive group of numbered blocks.
 */
fun List<NoteBlock>.getNumberedIndex(index: Int): Int {
    if (index !in indices || this[index].type != BlockType.NUMBERED) return 1
    var count = 1
    var i = index - 1
    while (i >= 0 && this[i].type == BlockType.NUMBERED) {
        count++
        i--
    }
    return count
}
