package com.example.analyzer.presentation

import android.util.Log
import com.example.analyzer.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.analyzer.remote.TongueAnalysisResponse
import com.example.analyzer.remote.UserFlowViewModel
import com.example.analyzer.remote.UserProfileViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController, userProfile: UserProfileViewModel) {
    // Get all profiles from the view model
    val allProfiles by userProfile.allProfiles.observeAsState(emptyList())
    Log.d("mine",allProfiles.toString())



    val historyItems  = remember(allProfiles) {
        allProfiles.flatMap { profile ->
            profile.tongueAnalysisHistory.map { (date, analysis) ->
                val dateTime = date.split(" ")
                val dateStr = dateTime.getOrNull(0) ?: "Unknown date"

                HistoryItem(
                    profile = profile.name,
                    id = "${profile.name}-$date", // Unique ID combining profile and date
                    date = dateStr,
                    analyis = analysis,
                    imageRes = R.drawable.tongue
                    // Add more fields if needed
                )
            }
        }.sortedByDescending { it.date }
    }

Log.d("minee",historyItems.toString())
    // Determine whether to show the empty state
    val showEmptyState = historyItems.isEmpty()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top app bar
            TopAppBar(
                title = { Text("Analysis History") },
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

            if (showEmptyState) {
                // Empty state
                EmptyHistoryState(navController)
            } else {
                // History items list
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Progress overview card with data from actual analyses
                            ProgressOverviewCard()
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Analyses",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                TextButton(onClick = {
                                    // Implement clear functionality
                                    // userProfile.clearAnalysisHistory(currentProfile?.name ?: "")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Clear All",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear All")
                                }
                            }
                        }

                        items(historyItems) { item ->
                            HistoryItemCard(
                                item = item,
                                onClick = {
                                    val userName = URLEncoder.encode(item.profile, "UTF-8")
                                    val analysisJson = URLEncoder.encode(Json.encodeToString(item.analyis), "UTF-8")
                                    navController.navigate("analysishistory/$userName/$analysisJson")
                                    }
                            )
                        }

                        // Additional space at the bottom
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ProgressStat(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = "Tongue image",
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))
            val score = item.analyis.MantleScore.toFloatSafely() // Replace with logic from analysis

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row {
                    Text(
                        text = item.profile,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${item.date} ",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val severity = when {
                    score > 0.8f -> ConditionSeverity.NORMAL
                    score > 0.6f -> ConditionSeverity.MILD
                    score > 0.4f -> ConditionSeverity.MODERATE
                    else -> ConditionSeverity.SEVERE
                }

                StatusIndicator(severity = severity)
            }

            // Health score bubble
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when {
                            score > 0.8f -> Color(0xFF4CAF50) // Green
                            score > 0.6f -> Color(0xFFFFC107) // Yellow
                            else -> Color(0xFFF44336) // Red
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(score).toInt()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryState(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.tongue),
            contentDescription = "No history",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No Analysis History",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Take your first tongue photo to start tracking your health indicators",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("camera") },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Take First Photo")
        }
    }
}


@Composable
fun ProgressOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Tongue Health Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressStat(
                    value = "4",
                    label = "Total Scans",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                ProgressStat(
                    value = "4",
                    label = "Normal",
                    color = Color(0xFF4CAF50)
                )

                ProgressStat(
                    value = "0",
                    label = "Mild",
                    color = Color(0xFFFFD600)
                )

                ProgressStat(
                    value = "0",
                    label = "Moderate",
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Track your progress over time for better insights",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}




data class HistoryItem(
    val profile : String,
    val id: String,
    val date: String,
    val analyis : TongueAnalysisResponse,
    val imageRes: Int,
  //  val mainCondition: String,
//val severity: ConditionSeverity
)