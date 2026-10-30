/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val colorId: String = "default",
    val cardSize: CardSize = CardSize.STANDARD,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val blocks: List<NoteBlock> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Total tasks count */
    val totalTasksCount: Int
        get() = blocks.count { it.type == BlockType.TASK }

    /** Completed tasks count */
    val completedTasksCount: Int
        get() = blocks.count { it.type == BlockType.TASK && it.isChecked }

    /** Plain preview text for search and concise views */
    val previewText: String
        get() = blocks.joinToString(" ") { it.content }.trim()
}
