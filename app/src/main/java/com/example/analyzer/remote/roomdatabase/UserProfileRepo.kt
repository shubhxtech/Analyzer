package com.example.analyzer.remote.roomdatabase

import com.example.analyzer.presentation.UserProfile
import androidx.lifecycle.asLiveData
import com.example.analyzer.remote.TongueAnalysisResponse
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserProfilesRepository(private val userProfileDao: UserProfileDao) {

    // Get all profiles as LiveData
    val allUserProfiles = userProfileDao.getAllUserProfiles()
        .map { entityList -> entityList.map { it.toUserProfile() } }
        .asLiveData()

    // Convert between domain model and entity
    private fun UserProfileEntity.toUserProfile() = UserProfile(
        name = this.name,
        age = this.age,
        gender = this.gender,
        tongueAnalysisHistory = this.tongueAnalysisHistory.toMutableMap()
    )

    private fun UserProfile.toEntity() = UserProfileEntity(
        name = this.name,
        age = this.age,
        gender = this.gender,
        tongueAnalysisHistory = this.tongueAnalysisHistory
    )

    // Save a user profile
    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        // Check if profile already exists
        val existingProfile = userProfileDao.findUserProfile(profile.name, profile.age, profile.gender)

        if (existingProfile != null) {
            // Update existing profile - PRESERVE existing tongue analysis history
            val mergedHistory = existingProfile.tongueAnalysisHistory.toMutableMap().apply {
                // Add all entries from the new profile's history
                putAll(profile.tongueAnalysisHistory)
            }

            val updatedEntity = existingProfile.copy(
                tongueAnalysisHistory = mergedHistory
            )
            userProfileDao.insertUserProfile(updatedEntity)
        } else {
            // Insert new profile
            userProfileDao.insertUserProfile(profile.toEntity())
        }
    }

    // Add a tongue analysis to a user profile
    suspend fun addTongueAnalysis(profile: UserProfile, date: String, analysis: TongueAnalysisResponse) =
        withContext(Dispatchers.IO) {
            // Fetch the latest profile data from the database
            val existingProfile = userProfileDao.findUserProfile(profile.name, profile.age, profile.gender)

            if (existingProfile != null) {
                // Use the existing history and add the new analysis
                val updatedHistory = existingProfile.tongueAnalysisHistory.toMutableMap().apply {
                    put(date, analysis)
                }

                val updatedEntity = existingProfile.copy(
                    tongueAnalysisHistory = updatedHistory
                )
                userProfileDao.insertUserProfile(updatedEntity)
            } else {
                // If profile doesn't exist yet (unlikely but possible race condition)
                val updatedProfile = profile.copy(
                    tongueAnalysisHistory = profile.tongueAnalysisHistory.toMutableMap().apply {
                        put(date, analysis)
                    }
                )
                userProfileDao.insertUserProfile(updatedProfile.toEntity())
            }
        }

    suspend fun deleteUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        val existingProfile = userProfileDao.findUserProfile(profile.name, profile.age, profile.gender)
        existingProfile?.let {
            userProfileDao.deleteUserProfile(it)
        }
    }
}