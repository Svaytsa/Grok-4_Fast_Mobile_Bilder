package com.example.palette

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import com.example.palette.util.HexUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// We need Robolectric to mock Bitmap and Palette behavior as they depend on Android SDK
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HexUtilsTest {

    @Test
    fun testColorToHex() {
        val red = 0xFFFF0000.toInt()
        assertEquals("#FF0000", HexUtils.colorToHex(red))

        val green = 0xFF00FF00.toInt()
        assertEquals("#00FF00", HexUtils.colorToHex(green))

        val blue = 0xFF0000FF.toInt()
        assertEquals("#0000FF", HexUtils.colorToHex(blue))

        val white = 0xFFFFFFFF.toInt()
        assertEquals("#FFFFFF", HexUtils.colorToHex(white))

        val black = 0xFF000000.toInt()
        assertEquals("#000000", HexUtils.colorToHex(black))
    }

    @Test
    fun testExtractDominantColors_Returns5UniqueColors() {
        // Create a bitmap with distinct colors
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        // Fill parts of bitmap with different colors
        for (x in 0 until 100) {
            for (y in 0 until 100) {
                when {
                    x < 20 -> bitmap.setPixel(x, y, 0xFFFF0000.toInt()) // Red
                    x < 40 -> bitmap.setPixel(x, y, 0xFF00FF00.toInt()) // Green
                    x < 60 -> bitmap.setPixel(x, y, 0xFF0000FF.toInt()) // Blue
                    x < 80 -> bitmap.setPixel(x, y, 0xFFFFFF00.toInt()) // Yellow
                    else -> bitmap.setPixel(x, y, 0xFF00FFFF.toInt())   // Cyan
                }
            }
        }

        val colors = HexUtils.extractDominantColors(bitmap)

        assertEquals(5, colors.size)
        // Verify uniqueness
        assertEquals(5, colors.distinct().size)

        // Verify format
        colors.forEach {
            assertTrue(it.matches(Regex("#[0-9A-F]{6}")))
        }
    }

    @Test
    fun testExtractDominantColors_WithSingleColor_FillsWithGenerated() {
        // Create a solid red bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(0xFFFF0000.toInt())

        val colors = HexUtils.extractDominantColors(bitmap)

        assertEquals(5, colors.size)
        assertEquals(5, colors.distinct().size)
        // First one should be Red
        assertEquals("#FF0000", colors[0])
    }
}
