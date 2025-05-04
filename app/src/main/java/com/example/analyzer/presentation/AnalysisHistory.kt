package com.example.analyzer.presentation

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.analyzer.remote.TongueAnalysisResponse
import com.example.analyzer.remote.TongueAnalysisViewModel
import com.example.analyzer.remote.UserFlowViewModel
import com.example.analyzer.remote.UserProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisHistory(
    user : String,
    navController: NavController,
    tongueAnalysis: TongueAnalysisResponse
) {
    val coatingPercentage = tongueAnalysis.white_coating?.white_coating_percentage?.toString() ?: "0"
    val jaggedness = tongueAnalysis.Jaggedness ?: "0"
    val cracks = tongueAnalysis.Cracks?.score ?: "0"
    val redness = tongueAnalysis.redness ?: "0"

    // Parse values safely
    val coatingPercent = coatingPercentage.toFloatOrNull() ?: 0f
    val jaggednessPercent = jaggedness.toFloatOrNull() ?: 0f
    val cracksPercent = cracks.toFloatOrNull() ?: 0f
    val rednessPercent = redness.toFloatOrNull() ?: 0f

    // Process the analysis results from API
    val conditions = remember(tongueAnalysis) {
        // Safely get severity value with fallback
        val severity = tongueAnalysis.white_coating?.severity ?: "Unknown"

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

    // Generate recommendations based on analysis
    val recommendations = remember(tongueAnalysis) {
        val recommendations = mutableListOf<String>()

        // Coating recommendations
        if (coatingPercent > 40f) {
            recommendations.add("Consider reducing intake of dairy products and processed foods")
            recommendations.add("Drink more water to help cleanse the digestive system")
        }

        // Jaggedness recommendations
        if (jaggednessPercent > 30f) {
            recommendations.add("Practice stress reduction techniques like meditation or deep breathing")
            recommendations.add("Ensure adequate intake of B vitamins")
        }

        // Cracks recommendations
        if (cracksPercent > 20f) {
            recommendations.add("Stay hydrated with at least 8 glasses of water daily")
            recommendations.add("Consider adding more moisture-rich foods to your diet")
        }

        // Redness recommendations
        if (rednessPercent > 80f || rednessPercent < 60f) {
            recommendations.add("Monitor your diet for foods that may cause irritation")
            recommendations.add("Follow up with a healthcare provider if abnormal color persists")
        }

        // General recommendations
        recommendations.add("Track your tongue health over time using this app")

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp)
            ) {
                // Image carousel with segmented image and visualization


                Column {
                    tongueAnalysis?.NutritionScore?.let {
                        ProgressIndicator(
                            heading = "Nutrition Score",
                            value = it.toFloatSafely().toInt(),
                            maxIsGood =true,
                        )
                    }
                    tongueAnalysis?.MantleScore?.let {
                        ProgressIndicator(
                            heading = "Mantle Score",
                            value = it.toFloatSafely().toInt(),
                            maxIsGood =true,
                        )
                    }
                }
                // Summary card
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

                        val summaryText = tongueAnalysis.Summary?.replace(oldChar = '+', newChar = ' ')

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


                Spacer(modifier = Modifier.height(12.dp))
                Column (modifier = Modifier.padding(12.dp)){
                    PrimaryButton(
                        text = "Track Changes",
                        onClick = {
                            navController.navigate("track/$user")

                        }
                    )
                }
            }
        }
    }
}