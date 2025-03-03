package com.ryen.bondhub.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ryen.bondhub.data.entity.UserProfileEntity

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfileEntity: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE uid = :userId")
    suspend fun getUserProfileById(userId: String): UserProfileEntity?

    @Query("DELETE FROM user_profiles WHERE uid = :userId")
    suspend fun deleteUserProfile(userId: String)

    @Query("SELECT * FROM user_profiles WHERE uid = :userId AND lastUpdated > :timestamp")
    suspend fun getUserProfileIfFresh(userId: String, timestamp: Long): UserProfileEntity?

}