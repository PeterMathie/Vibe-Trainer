package com.petermathie.vibecheck

import com.petermathie.vibecheck.domain.style.PaletteContrast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PaletteContrastTest {
    @Test
    fun wcagRatioUsesExpectedBlackWhiteContrast() {
        assertEquals(21.0, PaletteContrast.ratio(0xFF000000.toInt(), 0xFFFFFFFF.toInt()), 0.0001)
    }

    @Test
    fun customPaletteRejectsUnreadableTextPairs() {
        assertNull(PaletteContrast.customPaletteError(0xFFC2F85A.toInt(), 0xFF081017.toInt(), 0xFF101B23.toInt()))
        assertNotNull(PaletteContrast.customPaletteError(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt()))
    }
}
