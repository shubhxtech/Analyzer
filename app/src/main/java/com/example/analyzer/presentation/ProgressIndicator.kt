package com.example.analyzer.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A modern progress indicator that visually represents a value from 0-100
 *
 * @param value The progress value (0-100)
 * @param maxIsGood If true, higher values are considered good. If false, lower values are considered good.
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun ProgressIndicator(
    heading: String,
    value: Int,
    maxIsGood: Boolean,
    modifier: Modifier = Modifier
) {
    // Ensure value is within bounds
    val boundedValue = value.coerceIn(0, 100)

    // Animate the progress value
    val animatedProgress by animateFloatAsState(
        targetValue = boundedValue / 100f,
        animationSpec = tween(durationMillis = 500)
    )

    // Determine colors based on value and maxIsGood
    val progressColor = when {
        maxIsGood && boundedValue >= 75 -> Color(0xFF388E3C) // Green for high values when high is good
        maxIsGood && boundedValue >= 50 -> Color(0xFF81C784) // Light green for medium-high values
        !maxIsGood && boundedValue <= 25 -> Color(0xFF388E3C) // Green for low values when low is good
        !maxIsGood && boundedValue <= 50 -> Color(0xFF81C784) // Light green for medium-low values
        !maxIsGood && boundedValue >= 75 -> Color(0xFFD32F2F) // Red for high values when high is bad
        maxIsGood && boundedValue <= 25 -> Color(0xFFD32F2F) // Red for low values when low is bad
        else -> Color(0xFF9E9E9E) // Gray for neutral values
    }

    // Generate status text based on value and maxIsGood
    val statusText = when {
        maxIsGood && boundedValue >= 75 -> "Excellent"
        maxIsGood && boundedValue >= 50 -> "Good"
        maxIsGood && boundedValue >= 25 -> "Fair"
        maxIsGood && boundedValue < 25 -> "Poor"
        !maxIsGood && boundedValue <= 25 -> "Excellent"
        !maxIsGood && boundedValue <= 50 -> "Good"
        !maxIsGood && boundedValue <= 75 -> "Fair"
        else -> "Critical"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Header with value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$boundedValue%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(12.dp)
                        .background(progressColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show check icon for good status, warning for bad
                if ((maxIsGood && boundedValue >= 70) || (!maxIsGood && boundedValue <= 30)) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Good Status",
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(18.dp)
                    )
                } else if ((maxIsGood && boundedValue <= 30) || (!maxIsGood && boundedValue >= 70)) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Bad Status",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (statusText) {
                        "Excellent" -> Color(0xFF388E3C)
                        "Critical", "Poor" -> Color(0xFFD32F2F)
                        else -> Color.DarkGray
                    }
                )
            }
        }
    }
}

// Preview function to see the composable in Android Studio
@Preview(showBackground = true)
@Composable
fun ProgressIndicatorPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ProgressIndicator(value = 80, maxIsGood = true, heading = "MantleScore")
        Spacer(modifier = Modifier.height(16.dp))
        ProgressIndicator(value = 80, maxIsGood = true, heading = "MantleScore")
        Spacer(modifier = Modifier.height(16.dp))
        ProgressIndicator(value = 80, maxIsGood = true, heading = "MantleScore")
    }
}