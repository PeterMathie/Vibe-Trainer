package com.petermathie.vibetrainer.domain.style

object PaletteContrast {
    private const val ON_ACCENT = 0xFF081007.toInt()
    private const val TEXT_PRIMARY = 0xFFF1F5F3.toInt()
    private const val TEXT_SECONDARY = 0xFFA8B5BC.toInt()

    private fun channel(value: Int): Double {
        val normalized = value / 255.0
        return if (normalized <= 0.04045) normalized / 12.92 else Math.pow((normalized + 0.055) / 1.055, 2.4)
    }

    fun ratio(foreground: Int, background: Int): Double {
        fun luminance(argb: Int): Double =
            0.2126 * channel(argb shr 16 and 0xFF) +
                0.7152 * channel(argb shr 8 and 0xFF) +
                0.0722 * channel(argb and 0xFF)
        val first = luminance(foreground)
        val second = luminance(background)
        return (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
    }

    fun customPaletteError(accent: Int, background: Int, surface: Int): String? =
        when {
            ratio(ON_ACCENT, accent) < 4.5 -> "Accent needs more contrast with button text."
            ratio(TEXT_PRIMARY, background) < 4.5 -> "Background needs more contrast with primary text."
            ratio(TEXT_PRIMARY, surface) < 4.5 -> "Surface needs more contrast with primary text."
            ratio(TEXT_SECONDARY, surface) < 4.5 -> "Surface needs more contrast with secondary text."
            else -> null
        }
}
