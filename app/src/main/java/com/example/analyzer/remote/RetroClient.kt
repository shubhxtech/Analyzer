package com.example.analyzer.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TongueAnalysisClient {
    // Replace with your actual server URL
    private const val BASE_URL = "http:/172.16.9.117:8000/"  // For local testing in emulator

    // Create Logger
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Create OkHttp Client with longer timeouts for image processing
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logger)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    // Create Gson converter
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Create Retrofit Instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttp)
        .build()

    // Create Service
    val apiService: TongueAnalysisApiService = retrofit.create(TongueAnalysisApiService::class.java)
}