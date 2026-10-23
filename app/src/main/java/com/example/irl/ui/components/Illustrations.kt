package com.example.irl.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.irl.ui.theme.ButterYellow
import com.example.irl.ui.theme.DeepTeal
import com.example.irl.ui.theme.Lavender
import com.example.irl.ui.theme.Peach
import com.example.irl.ui.theme.SageGreen
import com.example.irl.ui.theme.WarmCream
import kotlin.math.cos
import kotlin.math.sin

/**
 * Home screen hero illustration:
 * Layered mountain landscape with dreamy sky gradient,
 * rolling hills, and stars. Drawn purely with Canvas.
 */
@Composable
fun NatureLandscapeIllustration(
    modifier: Modifier = Modifier,
    height: Dp = 220.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height

        // Sky gradient — peach sunrise blending into soft lavender
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Peach.copy(alpha = 0.6f),
                    Lavender.copy(alpha = 0.45f),
                    WarmCream.copy(alpha = 0.9f)
                )
            ),
            size = Size(w, h)
        )

        // Far mountains (largest, most distant — muted sage)
        drawMountainLayer(
            w = w, h = h,
            peakHeightFraction = 0.48f,
            baseYFraction = 0.72f,
            color = SageGreen.copy(alpha = 0.35f),
            peaks = listOf(0.12f, 0.35f, 0.58f, 0.82f),
            peakVariance = listOf(1.0f, 0.75f, 0.9f, 0.7f)
        )

        // Mid mountains (deeper, richer — muted teal)
        drawMountainLayer(
            w = w, h = h,
            peakHeightFraction = 0.52f,
            baseYFraction = 0.80f,
            color = DeepTeal.copy(alpha = 0.35f),
            peaks = listOf(0.05f, 0.28f, 0.52f, 0.75f, 0.94f),
            peakVariance = listOf(0.65f, 1.0f, 0.8f, 0.9f, 0.6f)
        )

        // Foreground rolling hills (warm, full teal)
        drawHillLayer(
            w = w, h = h,
            topFraction = 0.74f,
            color = DeepTeal.copy(alpha = 0.55f)
        )

        // Foreground base hill (darkest, closest)
        drawHillLayer(
            w = w, h = h,
            topFraction = 0.84f,
            color = DeepTeal
        )

        // Small stars scattered in upper sky
        drawStars(w = w, h = h)

        // Soft sun/moon glow at upper left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ButterYellow.copy(alpha = 0.55f),
                    ButterYellow.copy(alpha = 0.0f)
                ),
                center = Offset(w * 0.2f, h * 0.18f),
                radius = h * 0.22f
            ),
            radius = h * 0.22f,
            center = Offset(w * 0.2f, h * 0.18f)
        )
    }
}

/**
 * Focus session illustration — botanical circle with
 * a soft radiating glow. Used on BlockingActivity and focus screens.
 */
@Composable
fun FocusCircleIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    primaryColor: Color = DeepTeal,
    accentColor: Color = SageGreen
) {
    Canvas(
        modifier = modifier
            .height(size)
            .fillMaxWidth()
    ) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w, h) * 0.38f

        // Outer glow rings
        for (i in 4 downTo 1) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.04f * i),
                radius = r + (i * r * 0.18f),
                center = Offset(cx, cy)
            )
        }

        // Main circle
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.3f),
                    primaryColor.copy(alpha = 0.65f)
                ),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r,
            center = Offset(cx, cy)
        )

        // Inner circle highlight
        drawCircle(
            color = WarmCream.copy(alpha = 0.15f),
            radius = r * 0.55f,
            center = Offset(cx, cy * 0.9f)
        )

        // Botanical leaf shapes
        drawLeafAccent(cx, cy, r, primaryColor)
    }
}

/**
 * Small botanical sprig illustration for section headers and cards.
 */
@Suppress("Unused")
@Composable
fun BotanicalSprigIllustration(
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    color: Color = SageGreen
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // Simple leaf/stem drawing
        val stemPath = Path().apply {
            moveTo(cx, h * 0.9f)
            cubicTo(cx - w * 0.02f, h * 0.6f, cx + w * 0.02f, h * 0.4f, cx, h * 0.1f)
        }

        drawPath(
            path = stemPath,
            color = color.copy(alpha = 0.7f),
            style = Stroke(width = 2.5f)
        )

        // Left leaf
        drawLeafShape(cx - w * 0.04f, h * 0.45f, w * 0.12f, color.copy(alpha = 0.55f), mirrorX = false)
        // Right leaf
        drawLeafShape(cx + w * 0.04f, h * 0.35f, w * 0.12f, color.copy(alpha = 0.55f), mirrorX = true)
    }
}

// ─── Private drawing helpers ────────────────────────────────────────────────

private fun DrawScope.drawMountainLayer(
    w: Float, h: Float,
    peakHeightFraction: Float,
    baseYFraction: Float,
    color: Color,
    peaks: List<Float>,
    peakVariance: List<Float>
) {
    val path = Path()
    val baseY = h * baseYFraction

    path.moveTo(0f, baseY)

    peaks.forEachIndexed { i, xFrac ->
        val peakX = w * xFrac
        val peakY = h * peakHeightFraction * (peakVariance.getOrElse(i) { 1f })
        val prevX = if (i == 0) 0f else w * peaks[i - 1]
        val cp1X = (prevX + peakX) / 2f
        path.cubicTo(cp1X, baseY, cp1X, peakY, peakX, peakY)
        if (i < peaks.size - 1) {
            val nextX = w * peaks[i + 1]
            val cp2X = (peakX + nextX) / 2f
            path.cubicTo(peakX, peakY, cp2X, baseY * 0.85f, cp2X, baseY * 0.92f)
        }
    }

    path.lineTo(w, baseY)
    path.lineTo(w, h)
    path.lineTo(0f, h)
    path.close()

    drawPath(path, color)
}

private fun DrawScope.drawHillLayer(
    w: Float, h: Float,
    topFraction: Float,
    color: Color
) {
    val topY = h * topFraction
    val path = Path().apply {
        moveTo(0f, topY)
        cubicTo(w * 0.25f, topY - h * 0.08f, w * 0.55f, topY + h * 0.04f, w * 0.75f, topY - h * 0.05f)
        cubicTo(w * 0.88f, topY - h * 0.09f, w * 0.95f, topY, w, topY)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawStars(w: Float, h: Float) {
    val starPositions = listOf(
        Offset(w * 0.10f, h * 0.08f),
        Offset(w * 0.42f, h * 0.05f),
        Offset(w * 0.67f, h * 0.10f),
        Offset(w * 0.85f, h * 0.06f),
        Offset(w * 0.30f, h * 0.14f),
        Offset(w * 0.76f, h * 0.17f)
    )
    val sizes = listOf(2.5f, 2f, 3f, 1.8f, 2.2f, 1.5f)
    starPositions.forEachIndexed { i, pos ->
        drawCircle(
            color = ButterYellow.copy(alpha = 0.7f),
            radius = sizes.getOrElse(i) { 2f },
            center = pos
        )
    }
}

private fun DrawScope.drawLeafAccent(
    cx: Float, cy: Float, r: Float,
    color: Color
) {
    val leafCount = 6
    for (i in 0 until leafCount) {
        val angle = Math.toRadians((i * 360.0 / leafCount) + 30.0)
        val leafX = cx + r * 0.85f * cos(angle).toFloat()
        val leafY = cy + r * 0.85f * sin(angle).toFloat()
        drawCircle(
            color = color.copy(alpha = 0.25f),
            radius = r * 0.18f,
            center = Offset(leafX, leafY)
        )
    }
}

private fun DrawScope.drawLeafShape(
    x: Float, y: Float, size: Float,
    color: Color, mirrorX: Boolean
) {
    val path = Path().apply {
        val dir = if (mirrorX) 1f else -1f
        moveTo(x, y)
        cubicTo(
            x + dir * size * 0.5f, y - size * 0.3f,
            x + dir * size * 0.8f, y - size * 0.1f,
            x + dir * size * 0.6f, y + size * 0.2f
        )
        cubicTo(
            x + dir * size * 0.2f, y + size * 0.35f,
            x - dir * size * 0.1f, y + size * 0.1f,
            x, y
        )
        close()
    }
    drawPath(path, color)
}