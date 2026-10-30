/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.data.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zainotes.app.data.model.Note
import com.zainotes.app.data.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupData(
    val app: String = "Zainotes",
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val notes: List<Note> = emptyList()
)

class BackupManager(
    private val context: Context,
    private val repository: NotesRepository
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportNotesToJson(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val notes = repository.getAllNotesDirect()
            val backup = BackupData(notes = notes)
            val jsonString = gson.toJson(backup)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(Exception("Could not open destination file"))

            Result.success(notes.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importNotesFromJson(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val content = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        content.append(line)
                        line = reader.readLine()
                    }
                }
            } ?: return@withContext Result.failure(Exception("Could not read backup file"))

            val backup = gson.fromJson(content.toString(), BackupData::class.java)
            if (backup?.notes.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Backup contains no notes"))
            }

            // Re-assign IDs with 0 so Room generates fresh unique IDs without collisions
            val freshNotes = backup.notes.map { note ->
                note.copy(id = 0)
            }
            repository.insertNotes(freshNotes)
            Result.success(freshNotes.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
