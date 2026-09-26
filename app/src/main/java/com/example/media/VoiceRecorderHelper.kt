package com.example.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var isRecordingState = false

    val isRecording: Boolean
        get() = isRecordingState

    fun startRecording(): File? {
        stopRecording()
        return try {
            val outputDir = File(context.filesDir, "recorded_voices").apply { mkdirs() }
            val outFile = File(outputDir, "voice_rec_${System.currentTimeMillis()}.m4a")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioSamplingRate(44100)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setOutputFile(outFile.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
            currentOutputFile = outFile
            isRecordingState = true
            outFile
        } catch (e: Exception) {
            isRecordingState = false
            null
        }
    }

    fun stopRecording(): File? {
        if (!isRecordingState) return currentOutputFile
        return try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (_: Exception) {
                }
                release()
            }
            mediaRecorder = null
            isRecordingState = false
            currentOutputFile?.takeIf { it.exists() && it.length() > 256L }
        } catch (e: Exception) {
            mediaRecorder = null
            isRecordingState = false
            null
        }
    }
}
