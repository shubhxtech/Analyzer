package com.example.analyzer
import kotlinx.serialization.json.Json

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.analyzer.presentation.AnalysisHistory
import com.example.analyzer.presentation.AnalysisResultScreen
import com.example.analyzer.presentation.CameraScreen
import com.example.analyzer.presentation.HistoryScreen
import com.example.analyzer.presentation.HomeScreen
import com.example.analyzer.presentation.TrackScreen
import com.example.analyzer.presentation.UserProfile
import com.example.analyzer.remote.TongueAnalysisResponse
import com.example.analyzer.remote.UserFlowViewModel
import com.example.analyzer.remote.UserProfileViewModel
import com.example.analyzer.remote.UserProfileViewModelFactory
import com.example.analyzer.remote.roomdatabase.AppDatabase
import com.example.analyzer.remote.roomdatabase.UserProfilesRepository
import com.example.analyzer.ui.theme.AnalyzerTheme
import java.nio.file.Files.find


class MainActivity : ComponentActivity() {
    private lateinit var viewModel: UserProfileViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val application = application as TongueAnalyzerApplication
        val repository = application.repository


        // Create the ViewModel using the factory
        viewModel = ViewModelProvider(
            this,
            UserProfileViewModelFactory(repository)
        )[UserProfileViewModel::class.java]

        // Observe profiles from the ViewModel
        viewModel.allProfiles.observe(this) { profiles ->
            // Update UI with profiles
          //  updateProfilesList(profiles)
        }

        setContent {
            AnalyzerTheme {
                TongueAnalysisApp(
                    repository,
                    ViewModel = viewModel
                )
            }
        }
    }

    // Function to save a new profile
     fun saveProfile(name: String, age: String, gender: String) {
        val newProfile = UserProfile(name, age, gender)
        viewModel.saveUserProfile(newProfile)
    }

    // Function to add tongue analysis to a profile
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addAnalysis(profile: UserProfile, analysis: TongueAnalysisResponse) {
        viewModel.addTongueAnalysis(profile, analysis)
    }

}
class TongueAnalyzerApplication : Application() {
    // Lazy initialization of the database
    val database by lazy { AppDatabase.getDatabase(this) }

    // Lazy initialization of the repository
    val repository by lazy { UserProfilesRepository(database.userProfileDao()) }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TongueAnalysisApp(repository: UserProfilesRepository,ViewModel: UserProfileViewModel) {
    val navController = rememberNavController()
    val userViewModel: UserFlowViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                navController = navController, userProfilesRepository = repository, ViewModel,
                shareViewModel = userViewModel
            )
        }

        composable(
            route = "track/{profileName}",
            arguments = listOf(
                navArgument("profileName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val profileName = backStackEntry.arguments?.getString("profileName") ?: ""
            val selectedProfile = ViewModel.profileList.find { it.name == profileName }

            selectedProfile?.let {
                TrackScreen(
                    navController = navController,
                    userProfile = it
                )
            }
        }
        composable("camera") {
            CameraScreen(navController = navController)
        }
        composable(
            route = "analysis/{imagePath}",
            arguments = listOf(navArgument("imagePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString("imagePath")
            AnalysisResultScreen(navController,imagePath, userProfile = ViewModel, userShared = userViewModel)
        }
        composable(
            route = "analysishistory/{username}/{tongueAnalysisJson}",
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                },
                navArgument("tongueAnalysisJson") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")?.let { Uri.decode(it) } ?: ""
            val tongueAnalysisJson = backStackEntry.arguments?.getString("tongueAnalysisJson") ?: ""
            val decodedJson = Uri.decode(tongueAnalysisJson)
            val tongueAnalysis = Json.decodeFromString<TongueAnalysisResponse>(decodedJson)

            val userProfile = username

            userProfile?.let {
                AnalysisHistory(
                    navController = navController,
                    user = username,
                    tongueAnalysis = tongueAnalysis
                )
            }
        }




        composable("history") {
            HistoryScreen(navController = navController, userProfile = ViewModel)
        }
    }
}