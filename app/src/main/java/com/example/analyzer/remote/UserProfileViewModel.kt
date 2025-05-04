package com.example.analyzer.remote

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.analyzer.presentation.UserProfile
import com.example.analyzer.remote.roomdatabase.UserProfilesRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserProfileViewModel(private val repository: UserProfilesRepository) : ViewModel() {
//    val allProfiles = repository.allUserProfiles
    private val _profileList = mutableStateListOf<UserProfile>()
    val profileList: List<UserProfile> get() = _profileList

    val allProfiles = repository.allUserProfiles

    init {
        allProfiles.observeForever { profiles ->
            _profileList.clear()
            _profileList.addAll(profiles)
        }
    }
    fun saveUserProfile(profile: UserProfile) = viewModelScope.launch {
        repository.saveUserProfile(profile)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addTongueAnalysis(profile: UserProfile, analysis: TongueAnalysisResponse) =
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedDateTime = now.format(formatter)
            repository.addTongueAnalysis(profile, formattedDateTime, analysis)
        }

}