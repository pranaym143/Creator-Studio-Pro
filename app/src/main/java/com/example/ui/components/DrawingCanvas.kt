package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.example.data.Converters
import com.example.data.DrawingPoint
import com.example.data.DrawingStroke
import com.example.data.Frame

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    strokes: List<DrawingStroke>,
    onStrokeDraw: (List<DrawingPoint>) -> Unit,
    activeLayerId: Int,
    layersList: List<com.example.ui.ProjectViewModel.Layer>,
    onionSkinEnabled: Boolean,
    previousFrame: Frame?,
    nextFrame: Frame?,
    canvasColor: Color = Color.White,
    isTransparent: Boolean = false
) {
    // Local stroke state for responsive drawing under 120 FPS
    var currentPoints = remember { mutableStateListOf<DrawingPoint>() }

    // Parse layer indices of strokes to group drawings
    fun getStrokeLayerId(toolType: String): Int {
        val parts = toolType.split("_L")
        return if (parts.size > 1) {
            parts[1].toIntOrNull() ?: 2
        } else {
            2
        }
    }

    // Helper to draw a single stroke
    fun DrawScope.drawSingleStroke(
        stroke: DrawingStroke,
        overrideColor: Color? = null,
        overrideAlpha: Float? = null
    ) {
        val layerId = getStrokeLayerId(stroke.toolType)
        val layer = layersList.find { it.id == layerId }
        
        // Hide if layer is invisible
        if (layer != null && !layer.isVisible && overrideColor == null) return

        val alpha = overrideAlpha ?: (stroke.opacity * (layer?.opacity ?: 1f))
        val strokeColor = overrideColor ?: Color(stroke.color).copy(alpha = alpha)
        
        // Handle tool subtypes
        val cleanTool = stroke.toolType.split("_L")[0]

        if (stroke.points.size < 2) {
            // Draw a single dot
            stroke.points.firstOrNull()?.let { pt ->
                drawCircle(
                    color = strokeColor,
                    radius = stroke.size / 2f,
                    center = Offset(pt.x, pt.y)
                )
            }
            return
        }

        // Build path
        val path = Path().apply {
            val first = stroke.points.first()
            moveTo(first.x, first.y)
            for (i in 1 until stroke.points.size) {
                val pt = stroke.points[i]
                lineTo(pt.x, pt.y)
            }
        }

        when {
            cleanTool == "ERASER" -> {
                // Erase cuts through standard canvas coloring
                drawPath(
                    path = path,
                    color = canvasColor,
                    style = Stroke(
                        width = stroke.size,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            cleanTool == "NEON" -> {
                // Neon glow effect: draw thick translucent path then thin bright core
                drawPath(
                    path = path,
                    color = strokeColor.copy(alpha = alpha * 0.2f),
                    style = Stroke(
                        width = stroke.size * 2.2f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                drawPath(
                    path = path,
                    color = strokeColor.copy(alpha = alpha * 0.4f),
                    style = Stroke(
                        width = stroke.size * 1.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = stroke.size * 0.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            cleanTool == "SHAPE_RECT" -> {
                val first = stroke.points.first()
                val last = stroke.points.last()
                val left = minOf(first.x, last.x)
                val top = minOf(first.y, last.y)
                val width = kotlin.math.abs(first.x - last.x)
                val height = kotlin.math.abs(first.y - last.y)
                
                drawRect(
                    color = strokeColor,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = stroke.size)
                )
            }
            cleanTool == "SHAPE_CIRCLE" -> {
                val first = stroke.points.first()
                val last = stroke.points.last()
                val left = minOf(first.x, last.x)
                val top = minOf(first.y, last.y)
                val width = kotlin.math.abs(first.x - last.x)
                val height = kotlin.math.abs(first.y - last.y)

                drawOval(
                    color = strokeColor,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = stroke.size)
                )
            }
            cleanTool == "SPARKLE" -> {
                // Sparkle line draws points with little starry sparkles
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(
                        width = stroke.size * 0.4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                // Draw star crosses at intervals
                stroke.points.forEachIndexed { index, pt ->
                    if (index % 5 == 0) {
                        val sparkleColor = strokeColor.copy(alpha = alpha * 0.8f)
                        val d = stroke.size * 1.2f
                        drawLine(
                            color = sparkleColor,
                            start = Offset(pt.x - d, pt.y),
                            end = Offset(pt.x + d, pt.y),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = sparkleColor,
                            start = Offset(pt.x, pt.y - d),
                            end = Offset(pt.x, pt.y + d),
                            strokeWidth = 2f
                        )
                    }
                }
            }
            else -> {
                // Standard BRUSH, PENCIL
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(
                        width = stroke.size,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isTransparent) {
                    // Gray checked background for transparent canvas transparency grids
                    Color(0xFFE2E8F0)
                } else {
                    canvasColor
                }
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { event ->
                    val activeLayer = layersList.find { it.id == activeLayerId }
                    // Prevent drawing if layer is locked or invisible
                    if (activeLayer?.isLocked == true || activeLayer?.isVisible == false) {
                        return@pointerInteropFilter false
                    }

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            currentPoints.clear()
                            currentPoints.add(DrawingPoint(event.x, event.y))
                        }
                        MotionEvent.ACTION_MOVE -> {
                            currentPoints.add(DrawingPoint(event.x, event.y))
                        }
                        MotionEvent.ACTION_UP -> {
                            if (currentPoints.isNotEmpty()) {
                                onStrokeDraw(currentPoints.toList())
                            }
                            currentPoints.clear()
                        }
                    }
                    true
                }
        ) {
            val converter = Converters()

            // 1. Draw previous Onion Skin in transparent Cyan/Blue
            if (onionSkinEnabled && previousFrame != null) {
                val prevStrokes = converter.toStrokesList(previousFrame.strokesJson)
                prevStrokes.forEach { stroke ->
                    drawSingleStroke(
                        stroke = stroke,
                        overrideColor = Color(0xFF06B6D4).copy(alpha = 0.2f),
                        overrideAlpha = 0.2f
                    )
                }
            }

            // 2. Draw next Onion Skin in transparent Magenta/Red
            if (onionSkinEnabled && nextFrame != null) {
                val nextStrokes = converter.toStrokesList(nextFrame.strokesJson)
                nextStrokes.forEach { stroke ->
                    drawSingleStroke(
                        stroke = stroke,
                        overrideColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                        overrideAlpha = 0.2f
                    )
                }
            }

            // 3. Draw historic strokes of active frame
            strokes.forEach { stroke ->
                drawSingleStroke(stroke)
            }

            // 4. Draw current path actively drawn by user
            if (currentPoints.size > 0) {
                val activeStroke = DrawingStroke(
                    id = "current",
                    points = currentPoints.toList(),
                    color = if (onStrokeDraw == {}) 0 else Color(strokes.firstOrNull()?.color ?: 0xFFFFFFFF.toInt()).toArgb(), // handled by current color
                    size = 10f, // active placeholder visual
                    toolType = "BRUSH"
                )
                // Draw current stroke draft locally
                val path = Path().apply {
                    val first = currentPoints.first()
                    moveTo(first.x, first.y)
                    for (i in 1 until currentPoints.size) {
                        val pt = currentPoints[i]
                        lineTo(pt.x, pt.y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color(strokes.firstOrNull()?.color ?: 0xFFBD93F9.toInt()),
                    style = Stroke(
                        width = strokes.firstOrNull()?.size ?: 12f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
