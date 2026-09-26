package com.example.data.remote

import android.content.Context
import android.util.Base64
import com.example.BuildConfig
import com.example.media.TimedScriptLine
import com.example.media.VideoProcessingEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object GeminiTranscriptionService {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Extracts audio from the video, sends it to gemini-3.5-flash if GEMINI_API_KEY is configured,
     * and returns voice-synchronized TimedScriptLine items for bottom-to-top scrolling.
     */
    suspend fun transcribeAndSyncVideoVoice(
        context: Context,
        videoFile: File,
        fallbackScriptLines: List<String>
    ): Pair<List<TimedScriptLine>, String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasRealKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        var transcribedLines: List<String>? = null
        var statusNote = "ಆಡಿಯೋ ತರಂಗಾಂತರ ವಿಶ್ಲೇಷಣೆಯೊಂದಿಗೆ ಪಠ್ಯ ಸಿಂಕ್ ಮಾಡಲಾಗಿದೆ (Voice Cadence Synced)"

        if (hasRealKey) {
            try {
                val audioM4a = VideoProcessingEngine.extractAudioTrackToM4a(context, videoFile)
                if (audioM4a != null && audioM4a.exists() && audioM4a.length() in 128L..(6 * 1024 * 1024L)) {
                    val base64Audio = Base64.encodeToString(audioM4a.readBytes(), Base64.NO_WRAP)
                    audioM4a.delete()

                    val prompt = """
                        Listen carefully to this audio track from a video.
                        Transcribe the spoken words accurately in the original language (Kannada or English).
                        Split the transcription into short, natural teleprompter lines (4 to 8 words per line),
                        separated by newlines (\n). Output ONLY the plain text lines without numbering or extra commentary.
                    """.trimIndent()

                    val requestJson = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply { put("text", prompt) })
                                    put(JSONObject().apply {
                                        put("inlineData", JSONObject().apply {
                                            put("mimeType", "audio/mp4")
                                            put("data", base64Audio)
                                        })
                                    })
                                })
                            })
                        })
                    }

                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                    val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder().url(url).post(body).build()

                    okHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val respStr = response.body?.string().orEmpty()
                            val root = JSONObject(respStr)
                            val text = root.optJSONArray("candidates")
                                ?.optJSONObject(0)
                                ?.optJSONObject("content")
                                ?.optJSONArray("parts")
                                ?.optJSONObject(0)
                                ?.optString("text")
                                .orEmpty()

                            val parsed = text.lines()
                                .map { it.trim().removePrefix("-").trim() }
                                .filter { it.isNotEmpty() }
                            if (parsed.isNotEmpty()) {
                                transcribedLines = parsed
                                statusNote = "Gemini AI ಮೂಲಕ ಧ್ವನಿಯನ್ನು ಪಠ್ಯಕ್ಕೆ ಪರಿವರ್ತಿಸಿ ಸಿಂಕ್ ಮಾಡಲಾಗಿದೆ!"
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Gracefully fall back to acoustic cadence alignment
            }
        }

        val finalLines = transcribedLines ?: fallbackScriptLines
        val timedCues = VideoProcessingEngine.analyzeAudioAndSyncLines(videoFile, finalLines)
        Pair(timedCues, statusNote)
    }
}
