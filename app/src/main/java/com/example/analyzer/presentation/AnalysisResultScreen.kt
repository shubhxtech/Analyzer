package com.example.analyzer.presentation

import ChatBotFloatingButton
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.analyzer.remote.TongueAnalysisViewModel
import com.example.analyzer.remote.UserFlowViewModel
import com.example.analyzer.remote.UserProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
fun String?.toFloatSafely(): Float {
    if (this == null) return 0f
    return try {
        // Handle string with percentage sign
        if (this.endsWith("%")) {
            this.removeSuffix("%").trim().toFloatOrNull() ?: 0f
        } else {
            // Handle plain string
            this.trim().toFloatOrNull() ?: 0f
        }
    } catch (e: Exception) {
        0f
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    navController: NavHostController,
    imageUri: String?,
    viewModel: TongueAnalysisViewModel = viewModel(),
    userProfile : UserProfileViewModel,
    userShared : UserFlowViewModel
) {
    val context = LocalContext.current
    val uri = imageUri?.let {
        try {
            Uri.parse(Uri.decode(it))
        } catch (e: Exception) {
            Log.e("AnalysisScreen", "Invalid URI: $it", e)
            null
        }
    }

    // Safe parsing extensions


// Analysis states
    var isAnalyzing by remember { mutableStateOf(true) }
    var analysisProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(targetValue = analysisProgress)

// Observe ViewModel states
    val loading by viewModel.isLoading.observeAsState(initial = true)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val analysisResult by viewModel.analysisResult.observeAsState()
    val segmentedImage by viewModel.segmentedImageBitmap.observeAsState()
    val coatingVisualization by viewModel.coatingVisualizationBitmap.observeAsState()
    val crackDetection by viewModel.segmentVisualizationBitmap.observeAsState()

// Process the analysis results from API
    val conditions = remember(analysisResult) {
        if (analysisResult == null) {
            emptyList()
        } else {
            // Safely get values with fallbacks
            val coatingPercentage = analysisResult!!.white_coating?.white_coating_percentage?.toString() ?: "0"
            val jaggedness = analysisResult!!.Jaggedness ?: "0"
            val cracks = analysisResult!!.Cracks?.score ?: "0"
            val redness = analysisResult!!.redness ?: "0"

            // Parse values safely
            val coatingPercent = coatingPercentage.toFloatSafely()
            val jaggednessPercent = jaggedness.toFloatSafely()
            val cracksPercent = cracks.toFloatSafely()
            val rednessPercent = redness.toFloatSafely()

            val score = analysisResult!!.white_coating?.white_coating_percentage?.toFloat()
            // Default severity to handle null
            val severity = when {
                score!! > 0.8f -> "Severe"
                score > 0.6f -> "Moderate"
                score > 0.4f -> "Mild"
                else -> "Normal"
            }


            listOf(
                ConditionResult(
                    name = "White Coating",
                    description = "White coating present: ${coatingPercent.toInt()}%",
                    status = severity,
                    confidence = coatingPercent / 100f,
                    severity = ConditionSeverity.SEVERE
                ),
                ConditionResult(
                    name = "Jaggedness",
                    description = "Edge irregularity: ${jaggednessPercent.toInt()}%",
                    status = if (jaggednessPercent > 30f) "Concern" else "Normal",
                    confidence = jaggednessPercent / 100f,
                    severity = when {
                        jaggednessPercent < 20f -> ConditionSeverity.NORMAL
                        jaggednessPercent < 40f -> ConditionSeverity.MILD
                        jaggednessPercent < 60f -> ConditionSeverity.MODERATE
                        else -> ConditionSeverity.SEVERE
                    }
                ),
                ConditionResult(
                    name = "Cracks",
                    description = "Surface cracks detected: ${cracksPercent.toInt()}%",
                    status = if (cracksPercent > 30f) "Concern" else "Normal",
                    confidence = cracksPercent / 100f,
                    severity = when {
                        cracksPercent < 20f -> ConditionSeverity.NORMAL
                        cracksPercent < 40f -> ConditionSeverity.MILD
                        cracksPercent < 60f -> ConditionSeverity.MODERATE
                        else -> ConditionSeverity.SEVERE
                    }
                ),
                ConditionResult(
                    name = "Redness",
                    description = "Tongue color: ${rednessPercent.toInt()}%",
                    status = if (rednessPercent > 80f) "Concern" else "Normal",
                    confidence = rednessPercent / 100f,
                    severity = when {
                        rednessPercent < 60f -> ConditionSeverity.MILD
                        rednessPercent < 70f -> ConditionSeverity.MODERATE
                        rednessPercent < 85f -> ConditionSeverity.NORMAL
                        else -> ConditionSeverity.SEVERE
                    }
                )
            )
        }
    }


// Generate recommendations based on analysis
    val recommendations = remember(analysisResult) {
        val recommendations = mutableListOf<String>()

        analysisResult?.let { result ->
            // Safely parse percentage values
            val coatingPercentage = result.white_coating?.white_coating_percentage?.toString() ?: "0"
            val jaggedness = result.Jaggedness ?: "0"
            val cracks = result.Cracks?.score ?: "0"
            val redness = result.redness ?: "0"

            val coatingLevel = coatingPercentage.toFloatSafely()
            val jaggednessLevel = jaggedness.toFloatSafely()
            val cracksLevel = cracks.toFloatSafely()
            val rednessLevel = redness.toFloatSafely()

            // Coating recommendations
            if (coatingLevel > 40f) {
                recommendations.add("Consider reducing intake of dairy products and processed foods")
                recommendations.add("Drink more water to help cleanse the digestive system")
            }

            // Jaggedness recommendations
            if (jaggednessLevel > 30f) {
                recommendations.add("Practice stress reduction techniques like meditation or deep breathing")
                recommendations.add("Ensure adequate intake of B vitamins")
            }

            // Cracks recommendations
            if (cracksLevel > 20f) {
                recommendations.add("Stay hydrated with at least 8 glasses of water daily")
                recommendations.add("Consider adding more moisture-rich foods to your diet")
            }

            // Redness recommendations
            if (rednessLevel > 80f || rednessLevel < 60f) {
                recommendations.add("Monitor your diet for foods that may cause irritation")
                recommendations.add("Follow up with a healthcare provider if abnormal color persists")
            }

            // General recommendations
            recommendations.add("Track your tongue health over time using this app")
        }

        if (recommendations.isEmpty()) {
            listOf(
                "Maintain your current healthy habits",
                "Continue with regular oral hygiene practices",
                "Stay hydrated throughout the day",
                "Track your tongue health monthly for any changes"
            )
        } else {
            recommendations
        }
    }


    // Start analysis when the screen is loaded
    LaunchedEffect(uri) {
        uri?.let {
            try {
                val file = viewModel.uriToFile(it, context.contentResolver, context.cacheDir)
                viewModel.analyzeTongueImage(file)
            } catch (e: Exception) {
                Log.e("AnalysisScreen", "Error processing image", e)
            }
        }

        // Simulate progress for better UX
        while (analysisProgress < 1f) {
            delay(100)
            analysisProgress += 0.01f
        }
       while(isAnalyzing) delay(100)
    }

    // Update analyzing state based on ViewModel
    LaunchedEffect(loading) {
        isAnalyzing = loading
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top app bar

            TopAppBar(
                title = { Text("Analysis Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!isAnalyzing) {
                        IconButton(onClick = { /* Share functionality */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share"
                            )
                        }
                        IconButton(onClick = { /* Save functionality */ }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (isAnalyzing) {
                // Analysis in progress UI
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(120.dp),
                            strokeWidth = 8.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Analyzing Your Tongue...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Our AI is examining the image for potential health indicators",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .padding(horizontal = 32.dp, vertical = 16.dp)
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (errorMessage != null) {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Analysis Failed",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = errorMessage ?: "Unknown error occurred",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                uri?.let {
                                    try {
                                        viewModel.viewModelScope.launch {
                                            val file = viewModel.uriToFile(it, context.contentResolver, context.cacheDir)
                                            viewModel.analyzeTongueImage(file)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AnalysisScreen", "Error processing image", e)
                                    }
                                }
                            }
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {



                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Image carousel with segmented image and visualization
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        // Original image
                        item {
                            ImageCard(
                                painter = rememberAsyncImagePainter(uri),
                                title = "Original Image",
                                modifier = Modifier
                                    .fillParentMaxHeight()
                                    .width(300.dp)
                                    .padding(horizontal = 8.dp)
                            )
                        }

                        // Segmented image
                        item {
                            segmentedImage?.let {
                                ImageCard(
                                    painter = rememberAsyncImagePainter(it),
                                    title = "Segmented Analysis",
                                    modifier = Modifier
                                        .fillParentMaxHeight()
                                        .width(300.dp)
                                        .padding(horizontal = 8.dp)
                                )
                            }
                        }

                        // Coating visualization
                        item {
                            coatingVisualization?.let {
                                ImageCard(
                                    painter = rememberAsyncImagePainter(it),
                                    title = "Coating Analysis",
                                    modifier = Modifier
                                        .fillParentMaxHeight()
                                        .width(300.dp)
                                        .padding(horizontal = 8.dp)
                                )
                            }
                        }
                        Log.d("crck",crackDetection.toString())
                        item {
                            crackDetection?.let { morphPath ->
                                ImageCard(
                                    painter = rememberAsyncImagePainter(morphPath),
                                    title = "Crack Analysis",
                                    modifier = Modifier
                                        .fillParentMaxHeight()
                                        .width(300.dp)
                                        .padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }

                    // Summary card
                    Column {
                        analysisResult?.NutritionScore?.let {
                            ProgressIndicator(
                                heading = "Nutrition Score",
                                value = it.toFloatSafely().toInt(),
                                maxIsGood =true,
                            )
                        }
                        analysisResult?.MantleScore?.let {
                            ProgressIndicator(
                                heading = "Mantle Score",
                                value = it.toFloatSafely().toInt(),
                                maxIsGood =true,
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Summary",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val summaryText = analysisResult?.Summary.toString()

                            if (summaryText != null) {
                                Text(
                                     text = summaryText,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "This analysis is not a medical diagnosis",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Detailed condition analysis
                    Text(
                        text = "Detailed Analysis",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    conditions.forEach { condition ->
                        ConditionCard(condition)
                    }

                    // Recommendations section
                    Text(
                        text = "Recommendations",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            recommendations.forEachIndexed { index, recommendation ->
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = recommendation,
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (index < recommendations.size - 1) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }

                    // Call to action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                if (analysisResult == null) {
                                    Log.d("saving", "analysisResult is null")
                                    return@Button
                                }

                                val currentUser = userShared.currentUser
                                if (currentUser == null) {
                                    Log.d("saving", "currentUser is null")
                                    return@Button
                                }

                                Log.d("saving", "analysisResult: $analysisResult")
                                Log.d("saving", "currentUser: $currentUser")

                                try {
                                    userProfile.addTongueAnalysis(currentUser, analysisResult!!)
                                    Log.d("saving", "addTongueAnalysis executed")
                                } catch (e: Exception) {
                                    Log.e("saving", "Exception in addTongueAnalysis", e)
                                }
                            }
                            ,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save to History")
                        }

                        Button(
                            onClick = {
                                navController.navigate("track/${userShared.currentUser?.name}")

                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Track Changes")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
                    ChatBotFloatingButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
            }
        }}
    }
}


@Composable
fun ConditionCard(condition: ConditionResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = condition.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                StatusIndicator(condition.severity)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = condition.description,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { condition.confidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when (condition.severity) {
                    ConditionSeverity.NORMAL -> MaterialTheme.colorScheme.primary
                    ConditionSeverity.MILD -> Color(0xFFFFD600) // Amber
                    ConditionSeverity.MODERATE -> Color(0xFFFF9800) // Orange
                    ConditionSeverity.SEVERE -> Color(0xFFF44336) // Red
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Confidence",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Text(
                    text = "${(condition.confidence * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusIndicator(severity: ConditionSeverity) {
    val color = when (severity) {
        ConditionSeverity.NORMAL -> Color(0xFF4CAF50) // Green
        ConditionSeverity.MILD -> Color(0xFFFFD600) // Amber
        ConditionSeverity.MODERATE -> Color(0xFFFF9800) // Orange
        ConditionSeverity.SEVERE -> Color(0xFFF44336) // Red
    }

    val text = when (severity) {
        ConditionSeverity.NORMAL -> "Normal"
        ConditionSeverity.MILD -> "Mild"
        ConditionSeverity.MODERATE -> "Moderate"
        ConditionSeverity.SEVERE -> "Severe"
    }

    val icon = when (severity) {
        ConditionSeverity.NORMAL -> Icons.Default.CheckCircle
        else -> Icons.Outlined.Warning
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
@Composable
fun ImageCard(
    painter: Painter,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Image(
                painter = painter,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = 600f
                        )
                    )
            )

            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}
enum class ConditionSeverity {
    NORMAL,
    MILD,
    MODERATE,
    SEVERE
}

data class ConditionResult(
    val name: String,
    val description: String,
    val status: String,
    val confidence: Float,
    val severity: ConditionSeverity
)