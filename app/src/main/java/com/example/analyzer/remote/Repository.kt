package com.example.analyzer.remote

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

class TongueAnalysisRepository {

    private val apiService = TongueAnalysisClient.apiService

    suspend fun analyzeTongue(imageFile: File): Response<TongueAnalysisResponse> {
        val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
        return apiService.analyzeTongue(part)
    }

    suspend fun getImage(imagePath: String): Response<ResponseBody> {
        // Extract just the filename from the full path
        val filename = imagePath.substringAfterLast("/")
        return apiService.getImage(filename)
    }

    suspend fun getCsv(csvPath: String): Response<ResponseBody> {
        // Extract just the filename from the full path
        val filename = csvPath.substringAfterLast("/")
        return apiService.getCsv(filename)
    }

    suspend fun checkHealth(): Response<HealthCheckResponse> {
        return apiService.checkHealth()
    }
}