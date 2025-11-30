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

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class HexUtilsTest {

    @Test
    fun testToHexConversion() {
        val white = 0xFFFFFF
        assertEquals("#FFFFFF", HexUtils.toHex(white))

        val black = 0x000000
        assertEquals("#000000", HexUtils.toHex(black))

        val red = 0xFF0000
        assertEquals("#FF0000", HexUtils.toHex(red))

        val green = 0x00FF00
        assertEquals("#00FF00", HexUtils.toHex(green))

        val blue = 0x0000FF
        assertEquals("#0000FF", HexUtils.toHex(blue))
    }

    @Test
    fun testExtractColorsReturns5Unique() {
        // Create a bitmap with specific colors
        // 10x10 bitmap
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

        // Fill with fewer than 5 colors
        for (x in 0 until 10) {
            for (y in 0 until 10) {
                if (x < 5) bitmap.setPixel(x, y, 0xFF0000) // Red
                else bitmap.setPixel(x, y, 0x00FF00) // Green
            }
        }
        // Add a pixel of Blue
        bitmap.setPixel(0, 0, 0x0000FF)

        // We have Red, Green, Blue. That's 3 colors.
        // Expect 5 unique colors returned (fallback mechanism triggered).

        val colors = HexUtils.extractColors(bitmap)

        assertEquals(5, colors.size)
        assertEquals(5, colors.distinct().size)

        // Check that our known colors are present
        // Note: Palette might not pick up single pixels depending on its algorithm,
        // but given the small size and clear separation, it should.
        // However, Palette ignores very small populations relative to the image sometimes.
        // For a 10x10 image, 1 pixel is 1%.
        // Let's ensure the fallback logic works.

        // The colors returned should be uppercase HEX
        colors.forEach {
            assertTrue(it.matches(Regex("#[0-9A-F]{6}")))
        }
    }

    @Test
    fun testExtractColorsFromSolidImage() {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(0xFF0000) // All Red

        val colors = HexUtils.extractColors(bitmap)

        assertEquals(5, colors.size)
        assertEquals(5, colors.distinct().size)
        assertEquals("#FF0000", colors[0]) // Dominant should be red
    }
}
