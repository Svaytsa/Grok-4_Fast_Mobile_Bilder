package com.example.palette

import com.example.palette.util.HexUtils
import com.example.palette.util.SwatchInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HexUtilsTest {

    @Test
    fun colorIntToHex_formatsProperly() {
        assertEquals("#000000", HexUtils.colorIntToHex(0x000000))
        assertEquals("#FFFFFF", HexUtils.colorIntToHex(0xFFFFFF))
        assertEquals("#0A0B0C", HexUtils.colorIntToHex(0x0A0B0C))
    }

    @Test
    fun generateHexPalette_sortsByPopulation() {
        val swatches = listOf(
            SwatchInfo(rgb = 0x00FF00, population = 50),
            SwatchInfo(rgb = 0xFF0000, population = 200),
            SwatchInfo(rgb = 0x0000FF, population = 100)
        )

        val result = HexUtils.generateHexPalette(swatches, averageColor = 0x123456)

        assertEquals(5, result.size)
        assertEquals("#FF0000", result[0])
        assertEquals("#0000FF", result[1])
        assertEquals("#00FF00", result[2])
    }

    @Test
    fun generateHexPalette_handlesDuplicatesAndPads() {
        val swatches = listOf(
            SwatchInfo(rgb = 0x112233, population = 10),
            SwatchInfo(rgb = 0x112233, population = 5)
        )

        val result = HexUtils.generateHexPalette(swatches, averageColor = 0x445566)

        assertEquals(5, result.size)
        assertTrue(result.contains("#112233"))
        assertEquals(result.toSet().size, result.size)
    }

    @Test
    fun generateHexPalette_usesAverageWhenNoSwatches() {
        val result = HexUtils.generateHexPalette(emptyList(), averageColor = 0xABCDEF)

        assertEquals(5, result.size)
        assertEquals("#ABCDEF", result.first())
    }
}
