package com.example.analyzer.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.analyzer.remote.TongueAnalysisResponse
import com.patrykandpatryk.vico.core.entry.entryOf
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.line.lineSpec
import com.patrykandpatryk.vico.compose.component.shapeComponent
import com.patrykandpatryk.vico.core.chart.DefaultPointConnector
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.FloatEntry
import com.patrykandpatryk.vico.core.entry.entriesOf
import com.patrykandpatryk.vico.core.entry.entryModelOf
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    navController: NavController,
    userProfile: UserProfile
) {
    val sortedAnalysisHistory = remember {
        userProfile.tongueAnalysisHistory.entries
            .sortedByDescending { it.key }
            .toList()
    }

    // Extract data for charts
    val chartData = extractChartData(userProfile.tongueAnalysisHistory)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${userProfile.name}'s Analysis History",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color(0xFF121212)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ProfileSummary(userProfile)
                }

                item {
                    AnalysisTrends(chartData)
                }

                items(sortedAnalysisHistory) { (date, analysis) ->
                    AnalysisHistoryItem(date, analysis)
                }
            }
        }
    }
}

@Composable
fun ProfileSummary(userProfile: UserProfile) {
    val totalRecords = userProfile.tongueAnalysisHistory.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = userProfile.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${userProfile.age} years • ${userProfile.gender}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.LightGray
                    )
                }

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E2E2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.name.first().uppercase(),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFF2E2E2E)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem(
                    count = totalRecords,
                    label = "Total Records"
                )

                val lastAnalysis = userProfile.tongueAnalysisHistory.entries
                    .maxByOrNull { it.key }?.value

                ProfileStatItem(
                    count = lastAnalysis?.let {
                        it.NutritionScore?.toFloatOrNull()?.toInt() ?: 0
                    } ?: 0,
                    label = "Latest Nutrition Score"
                )

                ProfileStatItem(
                    count = lastAnalysis?.let {
                        it.MantleScore?.toFloatOrNull()?.toInt() ?: 0
                    } ?: 0,
                    label = "Latest Mantle Score"
                )
            }
        }
    }
}

@Composable
fun ProfileStatItem(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnalysisTrends(chartData: ChartData) {
    var expandedSection by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedSection = !expandedSection },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Health Trends",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = if (expandedSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandedSection) "Collapse" else "Expand",
                    tint = Color.White
                )
            }

            if (expandedSection) {
                Spacer(modifier = Modifier.height(16.dp))

                if (chartData.nutritionScores.size > 1) {
                    Text(
                        text = "Nutrition Score",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val nutritionEntries = chartData.dates.indices.map { index ->
                        entryOf(index.toFloat(), chartData.nutritionScores[index])
                    }

                            Chart( chart = lineChart(
                        lines = listOf(
                            lineSpec(
                                lineColor = Color(0xFF4CAF50),
                                lineThickness = 2.dp,
                                point = shapeComponent(
                                    shape = Shapes.pillShape,
                                    color = Color(0xFF4CAF50)
                                ),
                                pointSize = 8.dp,
                                pointConnector = DefaultPointConnector(cubicStrength = 0.2f),
                            )
                        )
                    ),
                    model = entryModelOf(nutritionEntries),
                    startAxis = startAxis(
                        valueFormatter = { value, _ ->
                            "${value.toInt()}"
                        }
                    ),
                    bottomAxis = bottomAxis(
                        valueFormatter = { index, _ ->
                            val formattedDate = if (index.toInt() < chartData.shortDates.size) {
                                chartData.shortDates[index.toInt()]
                            } else ""
                            formattedDate
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Mantle Score",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val mantleEntries = chartData.dates.indices.map { index ->
                        FloatEntry(index.toFloat(), chartData.mantleScores[index])
                    }

                    Chart(
                        chart = lineChart(
                            lines = listOf(
                                lineSpec(
                                    lineColor = Color(0xFF2196F3),
                                    lineThickness = 2.dp,
                                    point = shapeComponent(
                                        shape = Shapes.pillShape,
                                        color = Color(0xFF2196F3)
                                    ),
                                    pointSize = 8.dp,
                                    pointConnector = DefaultPointConnector(cubicStrength = 0.2f),
                                )
                            )
                        ),
                        model = entryModelOf(mantleEntries),
                        startAxis = startAxis(
                            valueFormatter = { value, _ ->
                                "${value.toInt()}"
                            }
                        ),
                        bottomAxis = bottomAxis(
                            valueFormatter = { index, _ ->
                                val formattedDate = if (index.toInt() < chartData.shortDates.size) {
                                    chartData.shortDates[index.toInt()]
                                } else ""
                                formattedDate
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Coating & Redness",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val rednessEntries = chartData.dates.indices.map { index ->
                        FloatEntry(index.toFloat(), chartData.rednessValues[index])
                    }

                    val coatingEntries = chartData.dates.indices.map { index ->
                        FloatEntry(index.toFloat(), chartData.coatingPercentages[index])
                    }

                    Chart(
                        chart = lineChart(
                            lines = listOf(
                                lineSpec(
                                    lineColor = Color(0xFFE91E63),
                                    lineThickness = 2.dp,
                                    point = shapeComponent(
                                        shape = Shapes.pillShape,
                                        color = Color(0xFFE91E63)
                                    ),
                                    pointSize = 8.dp,
                                    pointConnector = DefaultPointConnector(cubicStrength = 0.2f),
                                ),
                                lineSpec(
                                    lineColor = Color(0xFFFFEB3B),
                                    lineThickness = 2.dp,
                                    point = shapeComponent(
                                        shape = Shapes.pillShape,
                                        color = Color(0xFFFFEB3B)
                                    ),
                                    pointSize = 8.dp,
                                    pointConnector = DefaultPointConnector(cubicStrength = 0.2f),
                                )
                            )
                        ),
                        model = entryModelOf(rednessEntries, coatingEntries),
                        startAxis = startAxis(
                            valueFormatter = { value, _ ->
                                "${value.toInt()}%"
                            }
                        ),
                        bottomAxis = bottomAxis(
                            valueFormatter = { index, _ ->
                                val formattedDate = if (index.toInt() < chartData.shortDates.size) {
                                    chartData.shortDates[index.toInt()]
                                } else ""
                                formattedDate
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFFE91E63), CircleShape)
                            )
                            Text(
                                text = "Redness",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFFFFEB3B), CircleShape)
                            )
                            Text(
                                text = "White Coating",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Need more data points to show trends.\nAt least 2 analyses required.",
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnalysisHistoryItem(date: String, analysis: TongueAnalysisResponse) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val displayDate = formatDisplayDate(date)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Tongue Analysis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White
                )
            }

            if (expanded) {
                Divider(
                    color = Color(0xFF2E2E2E)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Segmented Image (if available)
                    analysis.segmented_image_path?.let { imagePath ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2E2E2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(imagePath))
                                    .build(),
                                contentDescription = "Tongue Analysis Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        }
                    }

                    // Health Scores
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        analysis.NutritionScore?.toFloatOrNull()?.let { score ->
                            ScoreIndicator(
                                score = score,
                                title = "Nutrition Score",
                                color = Color(0xFF4CAF50)
                            )
                        }

                        analysis.MantleScore?.toFloatOrNull()?.let { score ->
                            ScoreIndicator(
                                score = score,
                                title = "Mantle Score",
                                color = Color(0xFF2196F3)
                            )
                        }
                    }

                    // Analysis Details
                    AnalysisDetailItem(
                        title = "Jaggedness",
                        value = "${analysis.Jaggedness?.toFloatOrNull()?.toInt() ?: "-"}%",
                        subtitle = "Tongue edge irregularity"
                    )

                    AnalysisDetailItem(
                        title = "Cracks",
                        value = "Score: ${analysis.Cracks?.score ?: "-"}",
                        subtitle = "Surface crack detection"
                    )

                    AnalysisDetailItem(
                        title = "Redness",
                        value = "${analysis.redness?.toFloatOrNull()?.toInt() ?: "-"}/10",
                        subtitle = "Tongue color intensity"
                    )

                    AnalysisDetailItem(
                        title = "White Coating",
                        value = "${analysis.white_coating?.white_coating_percentage?.toFloat()?.toInt() ?: "-"}%",
                        subtitle = "Surface coating coverage"
                    )

                    // Papillae Analysis
                    analysis.papillae_analysis?.let { papillae ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2E2E2E)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Papillae Analysis",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    PapillaeDetailItem(
                                        title = "Total Count",
                                        value = "${papillae.total_papillae}"
                                    )

                                    PapillaeDetailItem(
                                        title = "Avg Size",
                                        value = "${papillae.avg_size?.toFloat()?.toInt() ?: "-"}px"
                                    )

                                    PapillaeDetailItem(
                                        title = "Avg Redness",
                                        value = "${(papillae.avg_redness?.toFloat() ?: 0f) * 10}/10"
                                    )
                                }
                            }
                        }
                    }

                    // Summary
                    analysis.Summary?.let { summary ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2E2E2E)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = summary.trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreIndicator(
    score: Float,
    title: String,
    color: Color
) {
    val animatedProgress = animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "ProgressAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(80.dp),
                color = Color(0xFF2E2E2E),
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round
            )

            CircularProgressIndicator(
                progress = { animatedProgress.value },
                modifier = Modifier.size(80.dp),
                color = color,
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round
            )

            Text(
                text = score.toInt().toString(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}

@Composable
fun AnalysisDetailItem(
    title: String,
    value: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(1.dp, Color(0xFF2E2E2E)),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PapillaeDetailItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )
    }
}

// Helper function to format display date
@RequiresApi(Build.VERSION_CODES.O)
fun formatDisplayDate(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(dateString, formatter)
        val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")
        dateTime.format(displayFormatter)
    } catch (e: DateTimeParseException) {
        dateString
    }
}

// Data class for chart data
data class ChartData(
    val dates: List<String>,
    val shortDates: List<String>,
    val nutritionScores: List<Float>,
    val mantleScores: List<Float>,
    val rednessValues: List<Float>,
    val coatingPercentages: List<Float>
)

// Helper function to extract chart data
@RequiresApi(Build.VERSION_CODES.O)
fun extractChartData(analysisHistory: Map<String, TongueAnalysisResponse>): ChartData {
    val sortedEntries = analysisHistory.entries.sortedBy { it.key }

    val dates = sortedEntries.map { it.key }
    val shortDates = dates.map {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dateTime = LocalDateTime.parse(it, formatter)
            val displayFormatter = DateTimeFormatter.ofPattern("MM/dd")
            dateTime.format(displayFormatter)
        } catch (e: DateTimeParseException) {
            it.take(5)
        }
    }

    val nutritionScores = sortedEntries.map { entry ->
        entry.value.NutritionScore?.toFloatOrNull() ?: 0f
    }

    val mantleScores = sortedEntries.map { entry ->
        entry.value.MantleScore?.toFloatOrNull() ?: 0f
    }

    val rednessValues = sortedEntries.map { entry ->
        (entry.value.redness?.toFloatOrNull() ?: 0f) * 10f  // Scale to 0-100 for visualization
    }

    val coatingPercentages = sortedEntries.map { entry ->
        entry.value.white_coating?.white_coating_percentage?.toFloat() ?: 0f
    }

    return ChartData(
        dates = dates,
        shortDates = shortDates,
        nutritionScores = nutritionScores,
        mantleScores = mantleScores,
        rednessValues = rednessValues,
        coatingPercentages = coatingPercentages
    )
}