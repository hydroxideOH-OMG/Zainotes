/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.data.model

enum class CardSize(val label: String, val spanColumns: Int) {
    STANDARD("Standard", 1),
    WIDE("Wide Banner", 2),
    COMPACT("Compact", 1),
    HERO("Expanded Hero", 2);

    companion object {
        fun fromName(name: String?): CardSize {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: STANDARD
        }
    }
}
