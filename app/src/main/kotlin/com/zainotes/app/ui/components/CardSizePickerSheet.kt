/*
 * Copyright (C) 2026 Zainotes Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.zainotes.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zainotes.app.data.model.CardSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSizePickerSheet(
    selectedSize: CardSize,
    onSizeSelected: (CardSize) -> Unit,
    sheetState: SheetState,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp)
        ) {
            Text(
                text = "Homepage Card Size",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Text(
                text = "Adjust how this note appears in your home screen grid",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            CardSize.entries.forEach { size ->
                val icon = when (size) {
                    CardSize.STANDARD -> Icons.Default.CropSquare
                    CardSize.WIDE -> Icons.Default.CropLandscape
                    CardSize.COMPACT -> Icons.Default.CropPortrait
                    CardSize.HERO -> Icons.Default.ViewCarousel
                    CardSize.FREEFORM -> Icons.Default.DashboardCustomize
                }

                val description = when (size) {
                    CardSize.STANDARD -> "Standard single column grid tile"
                    CardSize.WIDE -> "Spans across full 2-columns as a banner"
                    CardSize.COMPACT -> "Space-saving compact tile"
                    CardSize.HERO -> "Expanded spotlight card with full preview"
                    CardSize.FREEFORM -> "Dynamic organic height showing all checklist items and blocks"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSizeSelected(size) }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selectedSize == size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = size.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RadioButton(
                        selected = (selectedSize == size),
                        onClick = { onSizeSelected(size) }
                    )
                }
            }
        }
    }
}
