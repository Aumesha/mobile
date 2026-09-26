package com.example.media

import java.io.File

data class TimedScriptLine(
    val index: Int,
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val energyLevel: Float = 0.8f
)

data class ScrollStyleConfig(
    val fontSizePx: Float = 36f,
    val scrollSpeedMultiplier: Float = 1.0f,
    val highlightColorArgb: Int = 0xFFF59E0B.toInt(),
    val upcomingTextColorArgb: Int = 0xFFF8FAFC.toInt(),
    val passedTextColorArgb: Int = 0xFF94A3B8.toInt(),
    val backdropAlpha: Int = 175,
    val showVoiceSyncIndicator: Boolean = true
)

data class VideoProcessOutput(
    val outputFile: File,
    val durationMs: Long,
    val elapsedMs: Long,
    val hasAudioTrack: Boolean,
    val width: Int = 720,
    val height: Int = 1280,
    val summaryKn: String,
    val summaryEn: String
)

data class SavedDownloadResult(
    val success: Boolean,
    val displayPath: String,
    val uriString: String,
    val messageKn: String
)
