package com.example.palette.util

import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import androidx.palette.graphics.Palette
import kotlin.math.roundToInt

data class SwatchInfo(val rgb: Int, val population: Int)

object HexUtils {
    fun colorIntToHex(colorInt: Int): String {
        return String.format("#%06X", 0xFFFFFF and colorInt).uppercase()
    }

    fun extractDominantHexColors(bitmap: Bitmap): List<String> {
        val palette = Palette.from(bitmap).clearFilters().generate()
        val swatches = palette.swatches
            .filter { it.population > 0 }
            .map { SwatchInfo(it.rgb, it.population) }
        val average = calculateAverageColor(bitmap)
        return generateHexPalette(swatches, average)
    }

    @VisibleForTesting
    internal fun generateHexPalette(swatchInfos: List<SwatchInfo>, averageColor: Int?): List<String> {
        val sorted = swatchInfos.sortedByDescending { it.population }
        val uniqueHexes = mutableListOf<String>()
        sorted.forEach { swatch ->
            val hex = colorIntToHex(swatch.rgb)
            if (!uniqueHexes.contains(hex)) {
                uniqueHexes.add(hex)
            }
            if (uniqueHexes.size == 5) return uniqueHexes
        }

        val baseColor = averageColor ?: swatchInfos.firstOrNull()?.rgb ?: 0x888888
        val fallbacks = generateFallbackColors(baseColor, 5 - uniqueHexes.size, uniqueHexes)
        uniqueHexes.addAll(fallbacks)
        return uniqueHexes.take(5)
    }

    private fun generateFallbackColors(baseColor: Int, needed: Int, existing: List<String>): List<String> {
        val factors = listOf(1.0f, 0.85f, 1.15f, 0.7f, 1.3f, 0.55f, 1.45f)
        val colors = mutableListOf<String>()
        for (factor in factors) {
            if (colors.size == needed) break
            val adjusted = adjustColor(baseColor, factor)
            val hex = colorIntToHex(adjusted)
            if (!existing.contains(hex) && !colors.contains(hex)) {
                colors.add(hex)
            }
        }
        while (colors.size < needed) {
            val extra = adjustColor(baseColor, 0.5f + 0.1f * colors.size)
            val hex = colorIntToHex(extra)
            if (!existing.contains(hex) && !colors.contains(hex)) {
                colors.add(hex)
            }
        }
        return colors
    }

    private fun adjustColor(color: Int, factor: Float): Int {
        val r = ((color shr 16) and 0xFF)
        val g = ((color shr 8) and 0xFF)
        val b = (color and 0xFF)
        val newR = (r * factor).roundToInt().coerceIn(0, 255)
        val newG = (g * factor).roundToInt().coerceIn(0, 255)
        val newB = (b * factor).roundToInt().coerceIn(0, 255)
        return (newR shl 16) or (newG shl 8) or newB
    }

    private fun calculateAverageColor(bitmap: Bitmap): Int? {
        if (bitmap.width == 0 || bitmap.height == 0) return null
        val resized = if (bitmap.width * bitmap.height > 200_000) {
            Bitmap.createScaledBitmap(bitmap, bitmap.width / 4, bitmap.height / 4, true)
        } else bitmap

        val pixels = IntArray(resized.width * resized.height)
        resized.getPixels(pixels, 0, resized.width, 0, 0, resized.width, resized.height)
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        pixels.forEach { color ->
            rSum += (color shr 16) and 0xFF
            gSum += (color shr 8) and 0xFF
            bSum += color and 0xFF
        }
        val count = pixels.size.takeIf { it > 0 } ?: return null
        val rAvg = (rSum / count).toInt()
        val gAvg = (gSum / count).toInt()
        val bAvg = (bSum / count).toInt()
        return (rAvg shl 16) or (gAvg shl 8) or bAvg
    }
}
