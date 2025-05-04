package com.example.analyzer.remote.roomdatabase

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles ORDER BY id DESC")
    fun getAllUserProfiles(): Flow<List<UserProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity): Long

    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteUserProfileById(id: Long)

    @Query("SELECT * FROM user_profiles WHERE name = :name AND age = :age AND gender = :gender LIMIT 1")
    suspend fun findUserProfile(name: String, age: String, gender: String): UserProfileEntity?
}