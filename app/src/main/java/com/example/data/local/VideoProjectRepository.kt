package com.example.data.local

import kotlinx.coroutines.flow.Flow

class VideoProjectRepository(private val dao: ProcessedVideoDao) {
    val allVideos: Flow<List<ProcessedVideoEntity>> = dao.getAllProcessedVideos()

    suspend fun insert(video: ProcessedVideoEntity): Long = dao.insertProcessedVideo(video)

    suspend fun deleteById(id: Int) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()
}
