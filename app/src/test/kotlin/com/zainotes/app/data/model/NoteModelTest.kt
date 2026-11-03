package com.zainotes.app.data.model

import com.zainotes.app.ui.theme.NoteColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteModelTest {

    @Test
    fun `NoteBlock formatting flags accurately compute hasCustomFormatting`() {
        val plain = NoteBlock(type = BlockType.TEXT, content = "Hello")
        assertFalse(plain.hasCustomFormatting)

        val bold = plain.copy(isBold = true)
        assertTrue(bold.hasCustomFormatting)

        val italic = plain.copy(isItalic = true)
        assertTrue(italic.hasCustomFormatting)

        val underline = plain.copy(isUnderline = true)
        assertTrue(underline.hasCustomFormatting)

        val strike = plain.copy(isStrikethrough = true)
        assertTrue(strike.hasCustomFormatting)

        val highlighted = plain.copy(isHighlighted = true)
        assertTrue(highlighted.hasCustomFormatting)

        val code = plain.copy(isMonospace = true)
        assertTrue(code.hasCustomFormatting)

        val scaled = plain.copy(textScale = 1)
        assertTrue(scaled.hasCustomFormatting)

        val aligned = plain.copy(textAlign = 1)
        assertTrue(aligned.hasCustomFormatting)
    }

    @Test
    fun `NoteBackgroundTheme resolves all custom themes and fallback`() {
        assertEquals(NoteBackgroundTheme.GROCERIES, NoteBackgroundTheme.fromId("groceries"))
        assertEquals(NoteBackgroundTheme.MUSIC, NoteBackgroundTheme.fromId("MUSIC"))
        assertEquals(NoteBackgroundTheme.NATURE, NoteBackgroundTheme.fromId("nature"))
        assertEquals(NoteBackgroundTheme.NONE, NoteBackgroundTheme.fromId("unknown_theme"))
        assertEquals(NoteBackgroundTheme.NONE, NoteBackgroundTheme.fromId(null))
    }

    @Test
    fun `NoteColor resolves all expressive color palettes and fallback`() {
        assertEquals(NoteColor.LAVENDER, NoteColor.fromId("lavender"))
        assertEquals(NoteColor.MINT, NoteColor.fromId("MINT"))
        assertEquals(NoteColor.DEFAULT, NoteColor.fromId("nonexistent"))
        assertEquals(NoteColor.DEFAULT, NoteColor.fromId(null))
    }

    @Test
    fun `Note computes task completion counts from checklist blocks`() {
        val note = Note(
            title = "Sprint Checklist",
            blocks = listOf(
                NoteBlock(type = BlockType.TASK, content = "Task 1", isChecked = true),
                NoteBlock(type = BlockType.TASK, content = "Task 2", isChecked = false),
                NoteBlock(type = BlockType.TEXT, content = "Notes")
            )
        )
        assertEquals(2, note.totalTasksCount)
        assertEquals(1, note.completedTasksCount)
    }
}
