package com.example.analyzer.remote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.analyzer.presentation.UserProfile

class UserFlowViewModel : ViewModel() {
    var currentUser: UserProfile? by mutableStateOf(null)

    fun updateCurrentUser(profile: UserProfile) {
        currentUser = profile
    }
}

