package com.example.analyzer.remote.roomdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.analyzer.remote.TongueAnalysisResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "user_profiles")
@TypeConverters(MapTypeConverter::class)
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val age: String,
    val gender: String,
    val tongueAnalysisHistory: Map<String, TongueAnalysisResponse> = mapOf()
)

class MapTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromMapToString(map: Map<String, TongueAnalysisResponse>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromStringToMap(value: String): Map<String, TongueAnalysisResponse> {
        val mapType = object : TypeToken<Map<String, TongueAnalysisResponse>>() {}.type
        return gson.fromJson(value, mapType)
    }
}