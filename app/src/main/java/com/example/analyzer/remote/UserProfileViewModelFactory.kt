package com.example.analyzer.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.analyzer.remote.roomdatabase.UserProfilesRepository

class UserProfileViewModelFactory(private val repository: UserProfilesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}