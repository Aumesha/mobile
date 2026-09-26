package com.example.media

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

class VoiceRecorderHelper(private val context: Context) {
    @Volatile
    private var isRecordingState = false
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var currentOutputFile: File? = null

    val isRecording: Boolean
        get() = isRecordingState

    @SuppressLint("MissingPermission")
    fun startRecording(): File? {
        stopRecording()
        return try {
            val sampleRate = 22050
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                .coerceAtLeast(4096)

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufSize * 2
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                recorder.release()
                return null
            }

            val outputDir = File(context.filesDir, "recorded_voices").apply { mkdirs() }
            val outFile = File(outputDir, "voice_rec_${System.currentTimeMillis()}.wav")
            currentOutputFile = outFile
            audioRecord = recorder
            isRecordingState = true

            recorder.startRecording()

            recordingThread = thread(start = true, name = "DhvaniMicRecorder") {
                val pcmStream = ByteArrayOutputStream()
                val buffer = ByteArray(2048)
                while (isRecordingState) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        pcmStream.write(buffer, 0, read)
                    }
                }
                val pcmBytes = pcmStream.toByteArray()
                if (pcmBytes.isNotEmpty()) {
                    writeWavFile(outFile, pcmBytes, sampleRate, 1, 16)
                }
            }
            outFile
        } catch (_: Exception) {
            isRecordingState = false
            null
        }
    }

    fun stopRecording(): File? {
        if (!isRecordingState) return currentOutputFile
        isRecordingState = false
        return try {
            recordingThread?.join(600)
            recordingThread = null
            audioRecord?.apply {
                try {
                    stop()
                } catch (_: Exception) {
                }
                release()
            }
            audioRecord = null
            currentOutputFile?.takeIf { it.exists() && it.length() > 64L }
        } catch (_: Exception) {
            audioRecord = null
            null
        }
    }

    companion object {
        fun writeWavFile(
            outFile: File,
            pcmData: ByteArray,
            sampleRate: Int = 22050,
            channels: Int = 1,
            bitsPerSample: Int = 16
        ) {
            val byteRate = sampleRate * channels * bitsPerSample / 8
            val blockAlign = (channels * bitsPerSample / 8).toShort()
            val dataSize = pcmData.size
            val totalDataLen = dataSize + 36

            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray(Charsets.US_ASCII))
            header.putInt(totalDataLen)
            header.put("WAVE".toByteArray(Charsets.US_ASCII))
            header.put("fmt ".toByteArray(Charsets.US_ASCII))
            header.putInt(16) // Subchunk1Size for PCM
            header.putShort(1) // AudioFormat 1 = PCM
            header.putShort(channels.toShort())
            header.putInt(sampleRate)
            header.putInt(byteRate)
            header.putShort(blockAlign)
            header.putShort(bitsPerSample.toShort())
            header.put("data".toByteArray(Charsets.US_ASCII))
            header.putInt(dataSize)

            FileOutputStream(outFile).use { fos ->
                fos.write(header.array())
                fos.write(pcmData)
            }
        }
    }
}
