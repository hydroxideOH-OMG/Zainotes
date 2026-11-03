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
    QUOTE,
    CODE,
    TABLE
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
    val textAlign: Int = 0, // 0 = Start, 1 = Center, 2 = End
    val tableData: List<List<String>> = emptyList()
) {
    val hasCustomFormatting: Boolean
        get() = isBold || isItalic || isUnderline || isStrikethrough ||
            isHighlighted || isMonospace || textScale != 0 || textAlign != 0

    fun resolvedTable(): List<List<String>> {
        if (tableData.isNotEmpty() && tableData.first().isNotEmpty()) {
            val cols = tableData.maxOf { it.size }.coerceAtLeast(1)
            return tableData.map { row ->
                if (row.size < cols) row + List(cols - row.size) { "" } else row
            }
        }
        if (content.isNotBlank()) {
            val rows = content.lines()
                .map { line -> line.trim().removePrefix("|").removeSuffix("|").split("|").map { it.trim() } }
                .filter { row -> row.any { cell -> cell.isNotEmpty() && !cell.all { ch -> ch == '-' || ch == ':' } } }
            if (rows.isNotEmpty()) {
                val cols = rows.maxOf { it.size }.coerceAtLeast(2)
                return rows.map { row ->
                    if (row.size < cols) row + List(cols - row.size) { "" } else row
                }
            }
        }
        return listOf(
            listOf("Header 1", "Header 2", "Header 3"),
            listOf("", "", ""),
            listOf("", "", "")
        )
    }
}

fun List<List<String>>.toTableMarkdown(): String {
    return joinToString("\n") { row -> row.joinToString(" | ") }
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
