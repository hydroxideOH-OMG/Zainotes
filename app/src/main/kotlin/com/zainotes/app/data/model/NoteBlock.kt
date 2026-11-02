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
    HEADING
}

data class NoteBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.TEXT,
    val content: String = "",
    val isChecked: Boolean = false,
    val order: Int = 0
)

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
