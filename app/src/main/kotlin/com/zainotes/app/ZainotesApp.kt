/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app

import android.app.Application
import com.zainotes.app.data.backup.BackupManager
import com.zainotes.app.data.local.ZainotesDatabase
import com.zainotes.app.data.preferences.SettingsRepository
import com.zainotes.app.data.repository.NotesRepository

class ZainotesApp : Application() {

    lateinit var database: ZainotesDatabase
        private set

    lateinit var notesRepository: NotesRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var backupManager: BackupManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = ZainotesDatabase.getDatabase(this)
        notesRepository = NotesRepository(database.noteDao())
        settingsRepository = SettingsRepository(this)
        backupManager = BackupManager(this, notesRepository)
    }
}
