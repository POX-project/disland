/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinchIslandShapeTest {

    private val density = Density(2.75f)
    private val layoutDirection = LayoutDirection.Ltr

    @Test
    fun testZeroPinchProgressReturnsRoundedOutline() {
        val shape = PinchIslandShape(pinchProgress = 0f, cornerRadius = 16.dp)
        val outline = shape.createOutline(
            size = Size(200f, 60f),
            layoutDirection = layoutDirection,
            density = density
        )
        assertTrue("When pinchProgress is 0, outline should be Rounded", outline is Outline.Rounded)
    }

    @Test
    fun testFullPinchProgressReturnsGenericPathOutline() {
        val shape = PinchIslandShape(pinchProgress = 1f, cornerRadius = 16.dp)
        val outline = shape.createOutline(
            size = Size(200f, 60f),
            layoutDirection = layoutDirection,
            density = density
        )
        assertTrue("When pinchProgress is 1, outline should be Generic path", outline is Outline.Generic)
    }

    @Test
    fun testPartialPinchProgressReturnsGenericPathOutline() {
        val shape = PinchIslandShape(pinchProgress = 0.5f, cornerRadius = 16.dp)
        val outline = shape.createOutline(
            size = Size(200f, 60f),
            layoutDirection = layoutDirection,
            density = density
        )
        assertTrue("When pinchProgress is 0.5, outline should be Generic path", outline is Outline.Generic)
    }

    @Test
    fun testZeroDimensionsReturnsEmptyGenericOutline() {
        val shape = PinchIslandShape(pinchProgress = 1f, cornerRadius = 16.dp)
        val outline = shape.createOutline(
            size = Size(0f, 0f),
            layoutDirection = layoutDirection,
            density = density
        )
        assertTrue(outline is Outline.Generic)
    }

    @Test
    fun testEqualityAndHashCode() {
        val shape1 = PinchIslandShape(pinchProgress = 1f, cornerRadius = 16.dp)
        val shape2 = PinchIslandShape(pinchProgress = 1f, cornerRadius = 16.dp)
        val shape3 = PinchIslandShape(pinchProgress = 0.5f, cornerRadius = 16.dp)

        assertEquals(shape1, shape2)
        assertEquals(shape1.hashCode(), shape2.hashCode())
        assertNotEquals(shape1, shape3)
    }
}
