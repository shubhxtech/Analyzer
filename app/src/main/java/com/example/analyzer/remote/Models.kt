package com.example.analyzer.remote

import kotlinx.serialization.Serializable

data class HealthCheckResponse(
    val status: String,
    val sam_model: String,
    val roboflow_client: String
)
@Serializable
data class TongueAnalysisResponse(
    val Jaggedness: String?,
    val Cracks: CrackCom?,
    val redness: String?,
    val Summary: String?,
    val MantleScore: String?,
    val NutritionScore: String?,
    val segmented_image_path: String?,
    val white_coating: WhiteCoating?,
    val papillae_analysis: PapillaeAnalysis?
)
@Serializable
data class CrackCom(
    val morph : String?,
    val score : String?
)

@Serializable

data class WhiteCoating(
    val white_coating_percentage: Double?,
    val visualization_path: String?,
    val severity: String? = null  // Add this if it's in your API response
)
@Serializable

data class PapillaeAnalysis(
    val total_papillae: Int?,
    val avg_size: Double?,
    val avg_redness: Double?
)

// Severity enum used in the UI
