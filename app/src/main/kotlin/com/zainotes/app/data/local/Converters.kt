/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.NoteBlock

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromBlockList(blocks: List<NoteBlock>?): String {
        return gson.toJson(blocks ?: emptyList<NoteBlock>())
    }

    @TypeConverter
    fun toBlockList(json: String?): List<NoteBlock> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<NoteBlock>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromCardSize(size: CardSize?): String {
        return (size ?: CardSize.STANDARD).name
    }

    @TypeConverter
    fun toCardSize(name: String?): CardSize {
        return CardSize.fromName(name)
    }
}
