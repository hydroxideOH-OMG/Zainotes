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
                        // Seed welcome note on first install
                        CoroutineScope(Dispatchers.IO).launch {
                            val welcomeNote = Note(
                                title = "Welcome to Zainotes",
                                colorId = "lavender",
                                backgroundTheme = "dusk",
                                cardSize = CardSize.WIDE,
                                isPinned = true,
                                blocks = listOf(
                                    NoteBlock(
                                        type = BlockType.HEADING,
                                        content = "Expressive Notes & Tasks"
                                    ),
                                    NoteBlock(
                                        type = BlockType.TEXT,
                                        content = "Zainotes is a native Android notes app built with Material 3 Expressive. It blends freeform notes, interactive task checklists, and bulletins into an adaptable canvas."
                                    ),
                                    NoteBlock(
                                        type = BlockType.TASK,
                                        content = "Tap this checkbox to complete a task",
                                        isChecked = true
                                    ),
                                    NoteBlock(
                                        type = BlockType.TASK,
                                        content = "Check out the resizable card sizes on the home screen",
                                        isChecked = false
                                    ),
                                    NoteBlock(
                                        type = BlockType.BULLET,
                                        content = "Quick bulletins for thought dumping"
                                    ),
                                    NoteBlock(
                                        type = BlockType.BULLET,
                                        content = "Pure AMOLED Black theme for battery saving"
                                    ),
                                    NoteBlock(
                                        type = BlockType.TEXT,
                                        content = "Enjoy complete offline privacy and open-source freedom under AGPLv3."
                                    )
                                )
                            )
                            INSTANCE?.noteDao()?.insertNote(welcomeNote)
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
