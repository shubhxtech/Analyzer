package com.example.analyzer.remote


import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface TongueAnalysisApiService {

    @Multipart
    @POST("analyze_tongue")
    suspend fun analyzeTongue(
        @Part image: MultipartBody.Part
    ): Response<TongueAnalysisResponse>

    @GET("image/{imagePath}")
    suspend fun getImage(
        @Path("imagePath") imagePath: String
    ): Response<ResponseBody>

    @GET("csv/{csvPath}")
    suspend fun getCsv(
        @Path("csvPath") csvPath: String
    ): Response<ResponseBody>

    @GET("health")
    suspend fun checkHealth(): Response<HealthCheckResponse>
}