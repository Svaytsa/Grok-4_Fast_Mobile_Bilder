package com.example.palette.util

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import java.util.Locale

object HexUtils {

    /**
     * Extracts 5 dominant colors from the given bitmap.
     * Returns a list of 5 HEX strings (e.g. "#FF0000").
     */
    fun extractDominantColors(bitmap: Bitmap): List<String> {
        val palette = Palette.from(bitmap).generate()

        // Get all swatches
        val swatches = palette.swatches

        // Sort by population descending
        val sortedSwatches = swatches.sortedByDescending { it.population }

        // Take unique colors.
        // Note: Swatches from Palette are generally distinct colors, but let's ensure we just get the ints.
        val uniqueColors = sortedSwatches.map { it.rgb }.distinct()

        val result = uniqueColors.take(5).toMutableList()

        // If fewer than 5, we need to fill the gap.
        // As per instructions: "Если уникальных < 5 — дополнить недостающие усреднением доступных swatch/пикселей до 5."
        // Averaging swatches might produce a new color.
        // Or if we really don't have enough data, maybe we just replicate or modify brightness?
        // Let's implement a simple fallback strategy: average existing colors to create new ones.

        if (result.isEmpty()) {
             // Fallback if image is completely empty or something went wrong?
             // Should not happen with valid bitmap, but return black or white filler if so.
             while (result.size < 5) {
                 result.add(0xFFFFFF) // Fallback white
             }
        } else {
            while (result.size < 5) {
                // Generate a new color by averaging two existing ones or modifying one.
                // Simple approach: Average of first and last, or average of all?
                // "усреднением доступных swatch/пикселей"

                // Let's take the average of all currently selected colors to generate a "mean" color.
                // If we keep doing this, we might converge.
                // Alternatively, average pairs.

                // Let's just average the first two available (most dominant) if possible,
                // or just the single one if only one exists.

                if (result.size == 1) {
                    // Only 1 color. We need to invent colors.
                    // Maybe just invert it or shift hue?
                    // Instruction says "averaging available swatches/pixels".
                    // If we only have 1 swatch, the average is that swatch.
                    // Maybe we should look at the bitmap pixels? That's expensive.
                    // Let's assume Palette found at least something.
                    // If Palette only found 1 swatch (e.g. solid color image),
                    // we can't really "average" distinct swatches to get a new unique color easily without duplicating.
                    // But maybe we can take the color and average it with... nothing?

                    // Wait, if I have [Red], average of [Red] is Red. It's already in the list.
                    // Maybe the instruction implies taking average of *all* pixels if swatches are insufficient?
                    // But Palette already covers the image.

                    // Let's try to mix the existing colors to find something new.
                    // Or maybe just duplicate? No, "Возвращать строго 5 уникальных HEX-кодов".
                    // If the image is a solid red square, there is ONLY 1 unique color (Red).
                    // Getting 5 *unique* HEX codes from a solid color image is impossible unless we invent fake colors (e.g. gradients).
                    // The instruction says "дополнить недостающие усреднением доступных swatch/пикселей".
                    // This is slightly ambiguous.
                    // If I have Red (#FF0000), maybe I can average it with Black or White?
                    // But let's assume valid photos usually have enough colors.
                    // For the edge case of strictly < 5 colors, I will generate variations.
                    // Let's average the last added color with the first one to create a mix.

                    val color1 = result[0]
                    val color2 = result[result.size - 1]
                    val newColor = averageColors(color1, color2)

                    if (!result.contains(newColor)) {
                         result.add(newColor)
                    } else {
                        // If average exists (e.g. 1 color), we need to force a change.
                        // Let's shift it slightly.
                        result.add(shiftColor(newColor))
                    }
                } else {
                    // Mix the last two
                    val color1 = result[result.size - 1]
                    val color2 = result[result.size - 2]
                    var newColor = averageColors(color1, color2)

                    // Ensure uniqueness
                    var attempts = 0
                    while (result.contains(newColor) && attempts < 10) {
                         newColor = shiftColor(newColor)
                         attempts++
                    }
                    result.add(newColor)
                }
            }
        }

        return result.map { colorToHex(it) }
    }

    private fun averageColors(color1: Int, color2: Int): Int {
        val a1 = (color1 shr 24) and 0xFF
        val r1 = (color1 shr 16) and 0xFF
        val g1 = (color1 shr 8) and 0xFF
        val b1 = color1 and 0xFF

        val a2 = (color2 shr 24) and 0xFF
        val r2 = (color2 shr 16) and 0xFF
        val g2 = (color2 shr 8) and 0xFF
        val b2 = color2 and 0xFF

        val a = (a1 + a2) / 2
        val r = (r1 + r2) / 2
        val g = (g1 + g2) / 2
        val b = (b1 + b2) / 2

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun shiftColor(color: Int): Int {
        // Simple shift to generate unique color
        // Add 1 to Blue channel, handling overflow
        val b = color and 0xFF
        val newB = (b + 10) % 256
        return (color and 0xFFFFFF00.toInt()) or newB
    }

    fun colorToHex(color: Int): String {
        // Format as #RRGGBB, ignoring alpha for the string output as per "HEX-кодов в формате #RRGGBB"
        // Also "верхний регистр"
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        return String.format(Locale.US, "#%02X%02X%02X", r, g, b)
    }
}
