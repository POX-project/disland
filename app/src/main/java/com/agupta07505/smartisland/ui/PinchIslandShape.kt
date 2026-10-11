/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * A custom Shape that models the teardrop / waterdrop notch profile of devices like the
 * Redmi Note 8 Pro (Dot Drop notch).
 *
 * When [pinchProgress] is 1f:
 * - The top edge connects flush to the top bezel, flaring outward with smooth concave curves.
 * - The wings extend symmetrically to house content (music art, visualizers, call timers)
 *   completely clear of the physical camera cutout.
 * - The bottom corners remain softly rounded.
 *
 * When [pinchProgress] is 0f (or when the island expands into a card):
 * - It renders a standard smooth rounded rectangle with radius [cornerRadius].
 *
 * Transitioning [pinchProgress] between 0f and 1f smoothly morphs the shape between the
 * bezel-flush pinch notch and the expanded floating island.
 */
class PinchIslandShape(
    val pinchProgress: Float,
    val cornerRadius: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val p = pinchProgress.coerceIn(0f, 1f)
        val w = size.width
        val h = size.height

        if (w <= 0f || h <= 0f) {
            return Outline.Generic(Path())
        }

        val rPx = with(density) { cornerRadius.toPx() }
            .coerceAtMost(h / 2f)
            .coerceAtMost(w / 2f)
            .coerceAtLeast(0f)

        if (p <= 0.001f) {
            return Outline.Rounded(
                RoundRect(
                    rect = Rect(0f, 0f, w, h),
                    cornerRadius = CornerRadius(rPx, rPx)
                )
            )
        }

        val topR = rPx * (1f - p)
        val flare = (h * 0.45f) * p

        val path = Path().apply {
            moveTo(topR, 0f)
            lineTo(w - topR, 0f)

            if (p < 0.05f) {
                arcTo(
                    rect = Rect(w - 2f * rPx, 0f, w, 2f * rPx),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(w, h - rPx)
                arcTo(
                    rect = Rect(w - 2f * rPx, h - 2f * rPx, w, h),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(rPx, h)
                arcTo(
                    rect = Rect(0f, h - 2f * rPx, 2f * rPx, h),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(0f, rPx)
                arcTo(
                    rect = Rect(0f, 0f, 2f * rPx, 2f * rPx),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                close()
            } else {
                // Right shoulder concave pinch flare into top bezel
                cubicTo(
                    x1 = w - flare * 0.4f,
                    y1 = topR * 0.5f,
                    x2 = w,
                    y2 = flare * 0.6f,
                    x3 = w,
                    y3 = flare
                )

                // Right vertical side down to bottom-right rounded corner
                val bottomY = (h - rPx).coerceAtLeast(flare)
                lineTo(w, bottomY)

                // Bottom-right rounded corner
                arcTo(
                    rect = Rect(
                        left = w - 2f * rPx,
                        top = h - 2f * rPx,
                        right = w,
                        bottom = h
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Bottom edge across
                lineTo(rPx, h)

                // Bottom-left rounded corner
                arcTo(
                    rect = Rect(
                        left = 0f,
                        top = h - 2f * rPx,
                        right = 2f * rPx,
                        bottom = h
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                // Left vertical side up to left shoulder flare
                lineTo(0f, flare)

                // Left shoulder concave pinch flare into top bezel
                cubicTo(
                    x1 = 0f,
                    y1 = flare * 0.6f,
                    x2 = flare * 0.4f,
                    y2 = topR * 0.5f,
                    x3 = topR,
                    y3 = 0f
                )
                close()
            }
        }

        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PinchIslandShape) return false
        return pinchProgress == other.pinchProgress && cornerRadius == other.cornerRadius
    }

    override fun hashCode(): Int {
        var result = pinchProgress.hashCode()
        result = 31 * result + cornerRadius.hashCode()
        return result
    }
}
