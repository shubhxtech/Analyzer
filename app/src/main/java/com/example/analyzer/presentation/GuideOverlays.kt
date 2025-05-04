package com.example.analyzer.presentation


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun GuideOverlay() {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Calculate guide dimensions
    val guideWidth = screenWidth * 0.8f
    val guideHeight = guideWidth * 0.75f  // 4:3 aspect ratio
    val guideX = (screenWidth - guideWidth) / 2
    val guideY = screenHeight * 0.4f - guideHeight / 2

    // Dashed stroke pattern
    val dashPattern = floatArrayOf(20f, 20f)
    val strokePathEffect = PathEffect.dashPathEffect(dashPattern, 0f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Semi-transparent overlay
        drawRect(
            color = Color.Black.copy(alpha = 0.4f),
            size = Size(screenWidth, screenHeight)
        )

        // Cut out the guide area (make it transparent)
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(guideX, guideY),
            size = Size(guideWidth, guideHeight),
            cornerRadius = CornerRadius(20f, 20f),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // Draw dashed border around the guide area
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(guideX, guideY),
            size = Size(guideWidth, guideHeight),
            cornerRadius = CornerRadius(20f, 20f),
            style = Stroke(width = 4f, pathEffect = strokePathEffect)
        )

        // Draw crosshair in the center (optional)
        val centerX = guideX + guideWidth / 2
        val centerY = guideY + guideHeight / 2
        val crosshairSize = 20f

        // Horizontal line
        drawLine(
            color = Color.White,
            start = Offset(centerX - crosshairSize, centerY),
            end = Offset(centerX + crosshairSize, centerY),
            strokeWidth = 2f
        )

        // Vertical line
        drawLine(
            color = Color.White,
            start = Offset(centerX, centerY - crosshairSize),
            end = Offset(centerX, centerY + crosshairSize),
            strokeWidth = 2f
        )
    }
}