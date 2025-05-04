package com.example.analyzer.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class TongueAnalysisViewModel : ViewModel() {

    private val repository = TongueAnalysisRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _analysisResult = MutableLiveData<TongueAnalysisResponse>()
    val analysisResult: LiveData<TongueAnalysisResponse> = _analysisResult

    private val _segmentedImageBitmap = MutableLiveData<Bitmap>()
    val segmentedImageBitmap: LiveData<Bitmap> = _segmentedImageBitmap

    private val _coatingVisualizationBitmap = MutableLiveData<Bitmap>()
    val coatingVisualizationBitmap: LiveData<Bitmap> = _coatingVisualizationBitmap

    private val _segmentVisualizationBitmap = MutableLiveData<Bitmap>()
    val segmentVisualizationBitmap: LiveData<Bitmap> = _segmentVisualizationBitmap

    fun analyzeTongueImage(imageFile: File) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.analyzeTongue(imageFile)

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        _analysisResult.value = result

                        // Fetch the segmented image
                        result.segmented_image_path?.let { loadSegmentedImage(it) }

                        // Fetch the coating visualization
                        result.white_coating?.visualization_path?.let { loadCoatingVisualization(it) }

                        result.Cracks?.morph?.let { loadCreackedImage(it) }
                    }
                } else {
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Exception: ${e.message}"
                Log.e("TongueAnalysisVM", "Error analyzing tongue", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadSegmentedImage(imagePath: String) {
        try {
            val response = repository.getImage(imagePath)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val bitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeStream(body.byteStream())
                    }
                    _segmentedImageBitmap.value = bitmap
                }
            }
        } catch (e: Exception) {
            Log.e("TongueAnalysisVM", "Error loading segmented image", e)
        }
    }
    private suspend fun loadCreackedImage(imagePath: String) {
        try {
            val response = repository.getImage(imagePath)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val bitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeStream(body.byteStream())
                    }
                    _segmentVisualizationBitmap.value = bitmap
                }
            }
        } catch (e: Exception) {
            Log.e("TongueAnalysisVM", "Error loading Segmented image", e)
        }
    }

    private suspend fun loadCoatingVisualization(imagePath: String) {
        try {
            val response = repository.getImage(imagePath)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val bitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeStream(body.byteStream())
                    }
                    _coatingVisualizationBitmap.value = bitmap
                }
            }
        } catch (e: Exception) {
            Log.e("TongueAnalysisVM", "Error loading coating visualization", e)
        }
    }

    fun checkServerHealth() {
        viewModelScope.launch {
            try {
                val response = repository.checkHealth()
                if (response.isSuccessful) {
                    val health = response.body()
                    Log.d("TongueAnalysisVM", "Server health: ${health?.status}")
                } else {
                    Log.e("TongueAnalysisVM", "Health check failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("TongueAnalysisVM", "Health check exception", e)
            }
        }
    }

    // Helper function to convert content URI to File
    suspend fun uriToFile(uri: Uri, contentResolver: android.content.ContentResolver, cacheDir: File): File {
        return withContext(Dispatchers.IO) {
            val tempFile = File.createTempFile("tongue_image", ".jpg", cacheDir)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        }
    }
}