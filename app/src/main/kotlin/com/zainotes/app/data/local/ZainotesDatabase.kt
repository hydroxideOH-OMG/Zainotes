/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zainotes.app.data.model.CardSize
import com.zainotes.app.data.model.Note
import com.zainotes.app.data.model.NoteBlock
import com.zainotes.app.data.model.BlockType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ZainotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: ZainotesDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN backgroundTheme TEXT NOT NULL DEFAULT 'none'")
            }
        }

        fun getDatabase(context: Context): ZainotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZainotesDatabase::class.java,
                    "zainotes_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed expressive sample notes on first install to match preview 1:1
                        CoroutineScope(Dispatchers.IO).launch {
                            val now = System.currentTimeMillis()
                            val sampleNotes = listOf(
                                Note(
                                    title = "Welcome to Zainotes",
                                    colorId = "lavender",
                                    backgroundTheme = "dusk",
                                    cardSize = CardSize.WIDE,
                                    isPinned = true,
                                    createdAt = now,
                                    updatedAt = now,
                                    blocks = listOf(
                                        NoteBlock(
                                            type = BlockType.HEADING,
                                            content = "Expressive Notes & Tasks",
                                            isBold = true
                                        ),
                                        NoteBlock(
                                            type = BlockType.TEXT,
                                            content = "Zainotes is a native Android notes app built with Material 3 Expressive and Google Sans Flex typography."
                                        ),
                                        NoteBlock(
                                            type = BlockType.TASK,
                                            content = "Tap this checkbox to complete a task",
                                            isChecked = true
                                        ),
                                        NoteBlock(
                                            type = BlockType.TASK,
                                            content = "Try the dual-tier floating formatting bar in the note editor",
                                            isChecked = false,
                                            isBold = true,
                                            isHighlighted = true
                                        )
                                    )
                                ),
                                Note(
                                    title = "Weekend Farmers Market",
                                    colorId = "mint",
                                    backgroundTheme = "groceries",
                                    cardSize = CardSize.FREEFORM,
                                    isPinned = true,
                                    createdAt = now - 720_000L,
                                    updatedAt = now - 720_000L,
                                    blocks = listOf(
                                        NoteBlock(
                                            type = BlockType.TASK,
                                            content = "Organic sourdough boule",
                                            isChecked = true
                                        ),
                                        NoteBlock(
                                            type = BlockType.TASK,
                                            content = "Honeycrisp apples & figs",
                                            isChecked = false,
                                            isBold = true
                                        ),
                                        NoteBlock(
                                            type = BlockType.TASK,
                                            content = "Cold brew oat concentrate",
                                            isChecked = false
                                        )
                                    )
                                ),
                                Note(
                                    title = "Late Night Synthwave Session",
                                    colorId = "cobalt",
                                    backgroundTheme = "music",
                                    cardSize = CardSize.FREEFORM,
                                    isPinned = false,
                                    createdAt = now - 3_600_000L,
                                    updatedAt = now - 3_600_000L,
                                    blocks = listOf(
                                        NoteBlock(
                                            type = BlockType.QUOTE,
                                            content = "Warm analog arpeggios at 108 BPM with tape chorus.",
                                            isItalic = true
                                        ),
                                        NoteBlock(
                                            type = BlockType.TEXT,
                                            content = "Patch 04: Juno-60 brass pad + gated reverb snare",
                                            isMonospace = true
                                        )
                                    )
                                ),
                                Note(
                                    title = "Kyoto Autumn Itinerary",
                                    colorId = "peach",
                                    backgroundTheme = "places",
                                    cardSize = CardSize.FREEFORM,
                                    isPinned = false,
                                    createdAt = now - 10_800_000L,
                                    updatedAt = now - 10_800_000L,
                                    blocks = listOf(
                                        NoteBlock(
                                            type = BlockType.TASK,
                                            content = "Arashiyama bamboo grove at sunrise",
                                            isChecked = true
                                        ),
                                        NoteBlock(
                                            type = BlockType.TASK,
                                            content = "Matcha parfaits in Uji",
                                            isChecked = false,
                                            isUnderline = true
                                        )
                                    )
                                ),
                                Note(
                                    title = "Botanical Greenhouse Notes",
                                    colorId = "sage",
                                    backgroundTheme = "nature",
                                    cardSize = CardSize.FREEFORM,
                                    isPinned = false,
                                    createdAt = now - 86_400_000L,
                                    updatedAt = now - 86_400_000L,
                                    blocks = listOf(
                                        NoteBlock(
                                            type = BlockType.BULLET,
                                            content = "Monstera Thai Constellation misting schedule"
                                        ),
                                        NoteBlock(
                                            type = BlockType.BULLET,
                                            content = "Repot variegated philodendron in bark mix"
                                        )
                                    )
                                )
                            )
                            INSTANCE?.noteDao()?.insertNotes(sampleNotes)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
