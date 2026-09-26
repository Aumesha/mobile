package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessedVideoDao {
    @Query("SELECT * FROM processed_videos ORDER BY createdAt DESC")
    fun getAllProcessedVideos(): Flow<List<ProcessedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcessedVideo(video: ProcessedVideoEntity): Long

    @Query("DELETE FROM processed_videos WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM processed_videos")
    suspend fun clearAll()
}
