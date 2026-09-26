package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_videos")
data class ProcessedVideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val stage: String, // "MUTED", "DUBBED", "SCROLLED_TEXT"
    val internalFilePath: String,
    val downloadsFilePath: String = "",
    val durationMs: Long = 0L,
    val elapsedProcessMs: Long = 0L,
    val scriptLinesRaw: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
