package com.example.analyzer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatRequest(val message: String)
data class ChatResponse(val reply: String)

interface ChatApiService {
    @retrofit2.http.POST("chat")
    suspend fun sendMessage(@retrofit2.http.Body request: ChatRequest): Response<ChatResponse>
}

class ChatRepository(private val apiService: ChatApiService) {
    suspend fun sendMessage(message: String): Result<String> {
        return try {
            val response = apiService.sendMessage(ChatRequest(message))
            if (response.isSuccessful) {
                val chatResponse = response.body()
                if (chatResponse != null) {
                    Result.success(chatResponse.reply)
                } else {
                    Result.failure(IOException("Empty response body"))
                }
            } else {
                Result.failure(IOException("API call failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _chatMessages = MutableLiveData<List<ChatMessage>>(listOf())
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        val currentMessages = _chatMessages.value.orEmpty().toMutableList()
        currentMessages.add(ChatMessage(message, true))
        _chatMessages.value = currentMessages

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = repository.sendMessage(message)

                result.fold(
                    onSuccess = { reply ->
                        val updatedMessages = _chatMessages.value.orEmpty().toMutableList()
                        updatedMessages.add(ChatMessage(reply, false))
                        _chatMessages.value = updatedMessages
                        _errorMessage.value = null
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Error: ${exception.message ?: "Unknown error"}"
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf()
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

// Factory to create the ViewModel with dependencies
class ChatViewModelFactory(private val repository: ChatRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}