package com.example.analyzer.presentation

import com.example.analyzer.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.analyzer.remote.UserFlowViewModel
import com.example.analyzer.remote.UserProfileViewModel
import com.example.analyzer.remote.roomdatabase.UserProfilesRepository


@Composable
fun HomeScreen(
    navController: NavHostController,
    userProfilesRepository: UserProfilesRepository,
    viewModel: UserProfileViewModel,
    shareViewModel: UserFlowViewModel
) {

    var showUserDetailsDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section with logo and title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.doctor),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(128.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Health Lingua",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "AI-powered tongue analysis for health insights",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            // Middle section with feature highlights
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
            ) {
                FeatureCard(
                    icon = R.drawable.capture,
                    title = "Capture & Analyze",
                    description = "Take a clear photo of your tongue"
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureCard(
                    icon = R.drawable.insights,
                    title = "Health Insights",
                    description = "Get detailed health insights based"
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureCard(
                    icon = R.drawable.track,
                    title = "Track Changes",
                    description = "Monitor your tongue health over time"
                )
            }

            // Bottom section with action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                PrimaryButton(
                    text = "Take Photo",
                    onClick = { showUserDetailsDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SecondaryButton(
                    text = "View History",
                    onClick = { navController.navigate("history") }
                )
            }

            if (showUserDetailsDialog) {
                UserDetailsDialog(
                    onDismiss = { showUserDetailsDialog = false },
                    onConfirm = { name, age, gender ->
                        // Save user details
                        val newProfile = UserProfile(name, age, gender)
                        viewModel.saveUserProfile(newProfile)
                        showUserDetailsDialog = false
                        // Navigate to camera screen
                        shareViewModel.updateCurrentUser(newProfile)
                        navController.navigate("camera")
                    },
                    userProfilesRepository = userProfilesRepository,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun FeatureCard(icon: Int, title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)) // Change 8.dp to adjust roundness
                )

            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}