package io.github.kormcrete.lagoon.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LagoonTokensTest {
    @Test
    fun coloursComeFromTheTokenFile() {
        assertEquals(Color(0xFF1868C7), LagoonColors.Light.primary)
        assertEquals(Color(0xFFF4F7FA), LagoonColors.Light.ground)
    }

    @Test
    fun alphaColoursKeepTheirAlpha() {
        assertTrue(LagoonColors.Light.scrim.alpha < 1f)
    }

    @Test
    fun desktopIsDenserThanMobile() {
        assertEquals(44.dp, LagoonMetrics.Mobile.controlHeight)
        assertEquals(36.dp, LagoonMetrics.Desktop.controlHeight)
        assertEquals(14.sp, LagoonMetrics.Desktop.bodySize)
    }

    @Test
    fun aKeyAlwaysGetsTheSameTile() {
        val colors = LagoonColors.Light
        assertEquals(colors.tile("Birch & Co"), colors.tile("Birch & Co"))
        assertEquals(6, colors.tiles.size)
    }
}
