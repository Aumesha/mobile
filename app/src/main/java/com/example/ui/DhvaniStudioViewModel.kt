package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GitHubDesktopTemplates
import com.example.data.local.AppDatabase
import com.example.data.local.ProcessedVideoEntity
import com.example.data.local.VideoProjectRepository
import com.example.data.remote.GeminiTranscriptionService
import com.example.media.DownloadsFolderHelper
import com.example.media.SavedDownloadResult
import com.example.media.ScrollStyleConfig
import com.example.media.TimedScriptLine
import com.example.media.VideoProcessOutput
import com.example.media.VideoProcessingEngine
import com.example.media.VoiceRecorderHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class StudioUiState(
    val selectedTab: Int = 0,
    val forceDesktopPreviewMode: Boolean = false,
    val statusBannerKn: String = "ಸ್ವಾಗತ! ವಿಡಿಯೋ ಅಪ್ಲೋಡ್ ಮಾಡಿ ಅಥವಾ ತಕ್ಷಣದ ಡೆಮೊ ಬಳಸಿ.",
    val statusBannerEn: String = "Ready • Upload a video or test with instant studio sample",
    val isBusy: Boolean = false,

    // Menu 1: Strip Audio
    val menu1InputFile: File? = null,
    val menu1MutedOutput: VideoProcessOutput? = null,

    // Menu 2: Remove Old Audio & Add New Voice
    val menu2VideoFile: File? = null,
    val menu2VoiceFile: File? = null,
    val menu2VoiceSourceLabel: String = "ಕನ್ನಡ/English ಧ್ವನಿ ಸ್ಕ್ರಿಪ್ಟ್ ಸಿದ್ಧವಾಗಿದೆ",
    val menu2CustomScript: String = "ನಮಸ್ಕಾರ ಗೆಳೆಯರೇ! ಇದು ಹೊಸ ಧ್ವನಿಯ ವಿಡಿಯೋ.\nಹಳೆಯ ಧ್ವನಿಯನ್ನು ತೆಗೆದು ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಲಾಗಿದೆ.\nಈಗ ಮಾತನಾಡುವ ಧ್ವನಿಗೆ ತಕ್ಕಂತೆ ಪಠ್ಯ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಆಗುತ್ತದೆ!\nಕೆಲವೇ ಸೆಕೆಂಡುಗಳಲ್ಲಿ ನಿಮ್ಮ ಮೊಬೈಲ್ Downloads ನಲ್ಲಿ ಸೇವ್ ಆಗುತ್ತದೆ.",
    val isRecordingMic: Boolean = false,
    val menu2DubbedOutput: VideoProcessOutput? = null,

    // Menu 3: Voice-to-Text Bottom-to-Top Synchronized Scroll & Download
    val menu3VideoFile: File? = null,
    val menu3ScriptText: String = "ನಮಸ್ಕಾರ ಗೆಳೆಯರೇ! ಇದು ಹೊಸ ಧ್ವನಿಯ ವಿಡಿಯೋ.\nಹಳೆಯ ಧ್ವನಿಯನ್ನು ತೆಗೆದು ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಲಾಗಿದೆ.\nಈಗ ಮಾತನಾಡುವ ಧ್ವನಿಗೆ ತಕ್ಕಂತೆ ಪಠ್ಯ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಆಗುತ್ತದೆ!\nಕೆಲವೇ ಸೆಕೆಂಡುಗಳಲ್ಲಿ ನಿಮ್ಮ ಮೊಬೈಲ್ Downloads ನಲ್ಲಿ ಸೇವ್ ಆಗುತ್ತದೆ.",
    val menu3TimedLines: List<TimedScriptLine> = emptyList(),
    val menu3SyncNote: String = "ಧ್ವನಿಯ ವೇಗಕ್ಕೆ ತಕ್ಕಂತೆ ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಸಿಂಕ್ ಆಗಿದೆ",
    val menu3ScrollSpeed: Float = 1.0f,
    val menu3FontSizeSp: Float = 18f,
    val menu3RenderProgress: Float = 0f,
    val menu3ScrolledOutput: VideoProcessOutput? = null,

    // Downloads notification
    val lastDownloadResult: SavedDownloadResult? = null
)

class DhvaniStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository = VideoProjectRepository(
        AppDatabase.getInstance(context).processedVideoDao()
    )
    private val voiceRecorder = VoiceRecorderHelper(context)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val historyItems: StateFlow<List<ProcessedVideoEntity>> = repository.allVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Prepare an instant sample video on launch so the player and all 3 menus work out-of-the-box!
        prepareInitialStudioSample()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index.coerceIn(0, 3)) }
    }

    fun toggleDesktopPreviewMode() {
        _uiState.update { it.copy(forceDesktopPreviewMode = !it.forceDesktopPreviewMode) }
    }

    fun dismissDownloadBanner() {
        _uiState.update { it.copy(lastDownloadResult = null) }
    }

    fun prepareInitialStudioSample() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusBannerKn = "ಸ್ಟುಡಿಯೋ ಸಿದ್ಧವಾಗುತ್ತಿದೆ...",
                    statusBannerEn = "Initializing instant studio video engine..."
                )
            }
            val lines = _uiState.value.menu2CustomScript.lines().filter { it.isNotBlank() }
            val sampleOut = VideoProcessingEngine.generateInstantSampleVideoWithVoice(context, lines)
            val mutedOut = VideoProcessingEngine.stripAudioInSeconds(context, sampleOut.outputFile)
            val timedLines = VideoProcessingEngine.analyzeAudioAndSyncLines(sampleOut.outputFile, lines)

            _uiState.update { state ->
                state.copy(
                    isBusy = false,
                    menu1InputFile = sampleOut.outputFile,
                    menu1MutedOutput = mutedOut,
                    menu2VideoFile = mutedOut.outputFile,
                    menu2DubbedOutput = sampleOut,
                    menu3VideoFile = sampleOut.outputFile,
                    menu3TimedLines = timedLines,
                    statusBannerKn = mutedOut.summaryKn,
                    statusBannerEn = mutedOut.summaryEn
                )
            }
        }
    }

    // ---------------- MENU 1 ACTIONS (STRIP AUDIO IN SECONDS) ----------------

    fun onMenu1VideoUploaded(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusBannerKn = "ವಿಡಿಯೋದಿಂದ ಧ್ವನಿಯನ್ನು ತೆಗೆಯಲಾಗುತ್ತಿದೆ...",
                    statusBannerEn = "Removing audio from uploaded video..."
                )
            }
            val localFile = VideoProcessingEngine.copyUriToLocalFile(context, uri, "menu1_upload", ".mp4")
            val mutedOut = VideoProcessingEngine.stripAudioInSeconds(context, localFile)

            repository.insert(
                ProcessedVideoEntity(
                    title = "ಧ್ವನಿ ತೆಗೆದ ವಿಡಿಯೋ (Muted Video)",
                    stage = "MUTED",
                    internalFilePath = mutedOut.outputFile.absolutePath,
                    durationMs = mutedOut.durationMs,
                    elapsedProcessMs = mutedOut.elapsedMs
                )
            )

            _uiState.update {
                it.copy(
                    isBusy = false,
                    menu1InputFile = localFile,
                    menu1MutedOutput = mutedOut,
                    menu2VideoFile = mutedOut.outputFile,
                    statusBannerKn = mutedOut.summaryKn,
                    statusBannerEn = mutedOut.summaryEn
                )
            }
        }
    }

    fun onMenu1StripCurrentVideoNow() {
        val currentInput = _uiState.value.menu1InputFile ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusBannerKn = "ವಿಡಿಯೋದಿಂದ ಧ್ವನಿಯನ್ನು ತೆಗೆಯಲಾಗುತ್ತಿದೆ...",
                    statusBannerEn = "Stripping audio track in seconds..."
                )
            }
            val mutedOut = VideoProcessingEngine.stripAudioInSeconds(context, currentInput)
            repository.insert(
                ProcessedVideoEntity(
                    title = "ಧ್ವನಿ ತೆಗೆದ ವಿಡಿಯೋ (Muted Video)",
                    stage = "MUTED",
                    internalFilePath = mutedOut.outputFile.absolutePath,
                    durationMs = mutedOut.durationMs,
                    elapsedProcessMs = mutedOut.elapsedMs
                )
            )
            _uiState.update {
                it.copy(
                    isBusy = false,
                    menu1MutedOutput = mutedOut,
                    menu2VideoFile = mutedOut.outputFile,
                    statusBannerKn = mutedOut.summaryKn,
                    statusBannerEn = mutedOut.summaryEn
                )
            }
        }
    }

    fun onSaveMenu1MutedVideoToDownloads() {
        val mutedFile = _uiState.value.menu1MutedOutput?.outputFile ?: return
        viewModelScope.launch {
            val fileName = "DhvaniFlow_Muted_${System.currentTimeMillis()}.mp4"
            val res = DownloadsFolderHelper.saveFileToDownloads(context, mutedFile, fileName, "video/mp4")
            _uiState.update {
                it.copy(
                    lastDownloadResult = res,
                    statusBannerKn = res.messageKn
                )
            }
        }
    }

    fun onProceedFromMenu1ToMenu2() {
        val mutedFile = _uiState.value.menu1MutedOutput?.outputFile ?: _uiState.value.menu1InputFile
        _uiState.update {
            it.copy(
                menu2VideoFile = mutedFile,
                selectedTab = 1,
                statusBannerKn = "ಹಂತ 2: ಈಗ ಧ್ವನಿ ತೆಗೆದ ವಿಡಿಯೋಗೆ ಹೊಸ ಧ್ವನಿಯನ್ನು ಸೇರಿಸಿ",
                statusBannerEn = "Step 2: Add or record a new voice for the muted video"
            )
        }
    }

    // ---------------- MENU 2 ACTIONS (REMOVE OLD AUDIO & ADD NEW VOICE) ----------------

    fun onMenu2VideoUploaded(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusBannerKn = "ಮೊದಲು ಹಳೆಯ ಧ್ವನಿ ತೆಗೆಯಲಾಗುತ್ತಿದೆ...",
                    statusBannerEn = "Removing original audio from video first..."
                )
            }
            val rawVideo = VideoProcessingEngine.copyUriToLocalFile(context, uri, "menu2_vid", ".mp4")
            val mutedOut = VideoProcessingEngine.stripAudioInSeconds(context, rawVideo)
            _uiState.update {
                it.copy(
                    isBusy = false,
                    menu2VideoFile = mutedOut.outputFile,
                    statusBannerKn = "ಹಳೆಯ ಧ್ವನಿ ತೆಗೆಯಲಾಗಿದೆ! ಈಗ ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಿ.",
                    statusBannerEn = "Original audio stripped! Now select or record new voice."
                )
            }
        }
    }

    fun onMenu2CustomScriptChanged(newScript: String) {
        _uiState.update { it.copy(menu2CustomScript = newScript) }
    }

    fun onMenu2AudioFileUploaded(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusBannerKn = "ಹೊಸ ಆಡಿಯೋ ಫೈಲ್ ಅನ್ನು ವಿಡಿಯೋಗೆ ಜೋಡಿಸಲಾಗುತ್ತಿದೆ...",
                    statusBannerEn = "Merging uploaded voice file into muted video..."
                )
            }
            val voiceFile = VideoProcessingEngine.copyUriToLocalFile(context, uri, "menu2_voice", ".m4a")
            val targetVideo = _uiState.value.menu2VideoFile ?: _uiState.value.menu1MutedOutput?.outputFile
            if (targetVideo != null) {
                val dubbedOut = VideoProcessingEngine.mergeVoiceWithVideoInSeconds(context, targetVideo, voiceFile)
                val syncedLines = VideoProcessingEngine.analyzeAudioAndSyncLines(
                    dubbedOut.outputFile,
                    _uiState.value.menu2CustomScript.lines()
                )
                repository.insert(
                    ProcessedVideoEntity(
                        title = "ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಿದ ವಿಡಿಯೋ (Dubbed Video)",
                        stage = "DUBBED",
                        internalFilePath = dubbedOut.outputFile.absolutePath,
                        durationMs = dubbedOut.durationMs,
                        elapsedProcessMs = dubbedOut.elapsedMs,
                        scriptLinesRaw = _uiState.value.menu2CustomScript
                    )
                )
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        menu2VoiceFile = voiceFile,
                        menu2VoiceSourceLabel = "ಆಡಿಯೋ ಫೈಲ್ ಜೋಡಿಸಲಾಗಿದೆ (${voiceFile.name})",
                        menu2DubbedOutput = dubbedOut,
                        menu3VideoFile = dubbedOut.outputFile,
                        menu3ScriptText = it.menu2CustomScript,
                        menu3TimedLines = syncedLines,
                        statusBannerKn = dubbedOut.summaryKn,
                        statusBannerEn = dubbedOut.summaryEn
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        menu2VoiceFile = voiceFile,
                        menu2VoiceSourceLabel = "ಆಡಿಯೋ ಫೈಲ್ ಸಿದ್ಧವಾಗಿದೆ"
                    )
                }
            }
        }
    }

    fun toggleMicRecordingForMenu2() {
        if (_uiState.value.isRecordingMic) {
            val recordedFile = voiceRecorder.stopRecording()
            _uiState.update { it.copy(isRecordingMic = false) }
            if (recordedFile != null) {
                mergeRecordedOrSynthVoice(recordedFile, "ಮೈಕ್ ಧ್ವನಿ ರೆಕಾರ್ಡ್ ಆಗಿದೆ (Mic Voice Recorded)")
            } else {
                // If emulator lacks hardware mic input, synthesize voice seamlessly from script
                onSynthesizeAndMergeVoiceForMenu2()
            }
        } else {
            val started = voiceRecorder.startRecording()
            if (started != null) {
                _uiState.update {
                    it.copy(
                        isRecordingMic = true,
                        statusBannerKn = "ಮೈಕ್ ರೆಕಾರ್ಡಿಂಗ್ ನಡೆಯುತ್ತಿದೆ... ಮಾತನಾಡಿ ಮತ್ತು ನಿಲ್ಲಿಸಲು ಮತ್ತೆ ಒತ್ತಿ.",
                        statusBannerEn = "Recording microphone voice... Speak now & tap again to finish."
                    )
                }
            } else {
                // Fallback if emulator has no physical mic
                onSynthesizeAndMergeVoiceForMenu2()
            }
        }
    }

    fun onSynthesizeAndMergeVoiceForMenu2() {
        val targetVideo = _uiState.value.menu2VideoFile ?: _uiState.value.menu1MutedOutput?.outputFile ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusBannerKn = "ಹೊಸ ಧ್ವನಿ ರಚಿಸಿ ವಿಡಿಯೋಗೆ ಸೇರಿಸಲಾಗುತ್ತಿದೆ...",
                    statusBannerEn = "Synthesizing voice & merging with muted video..."
                )
            }
            val lines = _uiState.value.menu2CustomScript.lines().filter { it.isNotBlank() }
            val voiceFile = VideoProcessingEngine.createSynthesizedVoiceFile(
                context = context,
                scriptLines = lines,
                durationMs = _uiState.value.menu1MutedOutput?.durationMs ?: 6000L
            )
            mergeRecordedOrSynthVoice(voiceFile, "ಹೊಸ ಧ್ವನಿ ಸಂಯೋಜಿಸಲಾಗಿದೆ (Voice Merged)")
        }
    }

    private fun mergeRecordedOrSynthVoice(voiceFile: File, label: String) {
        val targetVideo = _uiState.value.menu2VideoFile ?: _uiState.value.menu1MutedOutput?.outputFile ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val dubbedOut = VideoProcessingEngine.mergeVoiceWithVideoInSeconds(context, targetVideo, voiceFile)
            val syncedLines = VideoProcessingEngine.analyzeAudioAndSyncLines(
                dubbedOut.outputFile,
                _uiState.value.menu2CustomScript.lines()
            )
            repository.insert(
                ProcessedVideoEntity(
                    title = "ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಿದ ವಿಡಿಯೋ (Dubbed Video)",
                    stage = "DUBBED",
                    internalFilePath = dubbedOut.outputFile.absolutePath,
                    durationMs = dubbedOut.durationMs,
                    elapsedProcessMs = dubbedOut.elapsedMs,
                    scriptLinesRaw = _uiState.value.menu2CustomScript
                )
            )
            _uiState.update {
                it.copy(
                    isBusy = false,
                    menu2VoiceFile = voiceFile,
                    menu2VoiceSourceLabel = label,
                    menu2DubbedOutput = dubbedOut,
                    menu3VideoFile = dubbedOut.outputFile,
                    menu3ScriptText = it.menu2CustomScript,
                    menu3TimedLines = syncedLines,
                    statusBannerKn = dubbedOut.summaryKn,
                    statusBannerEn = dubbedOut.summaryEn
                )
            }
        }
    }

    fun onSaveMenu2DubbedVideoToDownloads() {
        val dubbedFile = _uiState.value.menu2DubbedOutput?.outputFile ?: return
        viewModelScope.launch {
            val fileName = "DhvaniFlow_DubbedVoice_${System.currentTimeMillis()}.mp4"
            val res = DownloadsFolderHelper.saveFileToDownloads(context, dubbedFile, fileName, "video/mp4")
            _uiState.update {
                it.copy(
                    lastDownloadResult = res,
                    statusBannerKn = res.messageKn
                )
            }
        }
    }

    fun onProceedFromMenu2ToMenu3() {
        val dubbedFile = _uiState.value.menu2DubbedOutput?.outputFile ?: _uiState.value.menu2VideoFile
        if (dubbedFile != null) {
            viewModelScope.launch {
                val synced = VideoProcessingEngine.analyzeAudioAndSyncLines(
                    dubbedFile,
                    _uiState.value.menu2CustomScript.lines()
                )
                _uiState.update {
                    it.copy(
                        selectedTab = 2,
                        menu3VideoFile = dubbedFile,
                        menu3ScriptText = it.menu2CustomScript,
                        menu3TimedLines = synced,
                        statusBannerKn = "ಹಂತ 3: ಧ್ವನಿಗೆ ತಕ್ಕಂತೆ ಪಠ್ಯ ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಆಗುತ್ತಿದೆ!",
                        statusBannerEn = "Step 3: Voice converted to text & scrolling bottom-to-top in sync!"
                    )
                }
            }
        } else {
            selectTab(2)
        }
    }

    // ---------------- MENU 3 ACTIONS (VOICE-TO-TEXT BOTTOM-TO-TOP SCROLL & DOWNLOAD) ----------------

    fun onMenu3VideoWithVoiceUploaded(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusBannerKn = "ವಿಡಿಯೋದಲ್ಲಿನ ಧ್ವನಿಯನ್ನು ಪಠ್ಯ ರೂಪಕ್ಕೆ ಪರಿವರ್ತಿಸಿ ಸಿಂಕ್ ಮಾಡಲಾಗುತ್ತಿದೆ...",
                    statusBannerEn = "Transcribing video voice to text & syncing bottom-to-top scroll..."
                )
            }
            val uploadedFile = VideoProcessingEngine.copyUriToLocalFile(context, uri, "menu3_voice_vid", ".mp4")
            val fallbackLines = _uiState.value.menu3ScriptText.lines().filter { it.isNotBlank() }
            val (timedLines, note) = GeminiTranscriptionService.transcribeAndSyncVideoVoice(
                context = context,
                videoFile = uploadedFile,
                fallbackScriptLines = fallbackLines
            )
            val updatedScript = timedLines.joinToString("\n") { it.text }

            _uiState.update {
                it.copy(
                    isBusy = false,
                    menu3VideoFile = uploadedFile,
                    menu3ScriptText = updatedScript,
                    menu3TimedLines = timedLines,
                    menu3SyncNote = note,
                    statusBannerKn = "ಧ್ವನಿಯನ್ನು ಪಠ್ಯಕ್ಕೆ ಪರಿವರ್ತಿಸಲಾಗಿದೆ! ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ವೀಕ್ಷಿಸಿ.",
                    statusBannerEn = "Voice transcribed & synchronized to scroll bottom-to-top!"
                )
            }
        }
    }

    fun onAutoTranscribeCurrentMenu3Video() {
        val currentVideo = _uiState.value.menu3VideoFile ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusBannerKn = "ಧ್ವನಿಯನ್ನು ಪಠ್ಯ ರೂಪಕ್ಕೆ ಪರಿವರ್ತಿಸಿ ಸಿಂಕ್ ಮಾಡಲಾಗುತ್ತಿದೆ...",
                    statusBannerEn = "Analyzing voice track & syncing bottom-to-top text scroll..."
                )
            }
            val fallbackLines = _uiState.value.menu3ScriptText.lines().filter { it.isNotBlank() }
            val (timedLines, note) = GeminiTranscriptionService.transcribeAndSyncVideoVoice(
                context = context,
                videoFile = currentVideo,
                fallbackScriptLines = fallbackLines
            )
            val updatedScript = timedLines.joinToString("\n") { it.text }
            _uiState.update {
                it.copy(
                    isBusy = false,
                    menu3ScriptText = updatedScript,
                    menu3TimedLines = timedLines,
                    menu3SyncNote = note,
                    statusBannerKn = note
                )
            }
        }
    }

    fun onMenu3ScriptEdited(newText: String) {
        _uiState.update { it.copy(menu3ScriptText = newText) }
        val videoFile = _uiState.value.menu3VideoFile ?: return
        viewModelScope.launch {
            val reSynced = VideoProcessingEngine.analyzeAudioAndSyncLines(
                videoFile = videoFile,
                rawLines = newText.lines()
            )
            _uiState.update { it.copy(menu3TimedLines = reSynced) }
        }
    }

    fun onMenu3ScrollSpeedChanged(newSpeed: Float) {
        _uiState.update { it.copy(menu3ScrollSpeed = newSpeed.coerceIn(0.6f, 1.8f)) }
    }

    fun onMenu3FontSizeChanged(newSizeSp: Float) {
        _uiState.update { it.copy(menu3FontSizeSp = newSizeSp.coerceIn(14f, 26f)) }
    }

    /**
     * Renders the final MP4 video with the bottom-to-top scrolling text burned into the video frames
     * AND immediately saves it into the mobile's Downloads folder!
     */
    fun onRenderAndSaveMenu3ScrolledVideoToDownloads() {
        val sourceVideo = _uiState.value.menu3VideoFile ?: _uiState.value.menu2DubbedOutput?.outputFile ?: return
        val lines = _uiState.value.menu3TimedLines
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    menu3RenderProgress = 0.05f,
                    statusBannerKn = "ಸ್ವೈಪ್ ಪಠ್ಯದ ವಿಡಿಯೋ ಸಿದ್ಧಪಡಿಸಿ Downloads ಫೋಲ್ಡರ್‌ಗೆ ಸೇವ್ ಮಾಡಲಾಗುತ್ತಿದೆ...",
                    statusBannerEn = "Rendering bottom-to-top scrolling text video & saving to Downloads..."
                )
            }

            val styleConfig = ScrollStyleConfig(
                fontSizePx = _uiState.value.menu3FontSizeSp * 2.0f,
                scrollSpeedMultiplier = _uiState.value.menu3ScrollSpeed
            )

            val renderedOutput = VideoProcessingEngine.renderBottomToTopScrollingTextVideo(
                context = context,
                inputVideoFile = sourceVideo,
                timedLines = lines,
                styleConfig = styleConfig,
                onProgress = { prog ->
                    _uiState.update { s -> s.copy(menu3RenderProgress = prog) }
                }
            )

            val desiredFileName = "DhvaniFlow_ScrollText_${System.currentTimeMillis()}.mp4"
            val downloadResult = DownloadsFolderHelper.saveFileToDownloads(
                context = context,
                sourceFile = renderedOutput.outputFile,
                desiredFileName = desiredFileName,
                mimeType = "video/mp4"
            )

            repository.insert(
                ProcessedVideoEntity(
                    title = "ಪಠ್ಯ ಸ್ವೈಪ್ ವಿಡಿಯೋ (Voice-Synced Scroll Video)",
                    stage = "SCROLLED_TEXT",
                    internalFilePath = renderedOutput.outputFile.absolutePath,
                    downloadsFilePath = downloadResult.displayPath,
                    durationMs = renderedOutput.durationMs,
                    elapsedProcessMs = renderedOutput.elapsedMs,
                    scriptLinesRaw = _uiState.value.menu3ScriptText
                )
            )

            _uiState.update {
                it.copy(
                    isBusy = false,
                    menu3RenderProgress = 1.0f,
                    menu3ScrolledOutput = renderedOutput,
                    lastDownloadResult = downloadResult,
                    statusBannerKn = "${renderedOutput.summaryKn} • ${downloadResult.messageKn}",
                    statusBannerEn = "Saved to Downloads: ${downloadResult.displayPath}"
                )
            }
        }
    }

    // ---------------- MENU 4 ACTIONS (GITHUB WORKFLOW & DESKTOP EXPORT) ----------------

    fun onSaveGitHubWorkflowToDownloads() {
        viewModelScope.launch {
            val res = DownloadsFolderHelper.saveTextFileToDownloads(
                context = context,
                content = GitHubDesktopTemplates.GITHUB_WORKFLOW_YAML,
                fileName = "build-mobile-desktop.yml",
                mimeType = "text/yaml"
            )
            _uiState.update {
                it.copy(
                    lastDownloadResult = res,
                    statusBannerKn = "GitHub Workflow ಫೈಲ್ (build-mobile-desktop.yml) ನಿಮ್ಮ Downloads ಫೋಲ್ಡರ್‌ನಲ್ಲಿ ಸೇವ್ ಆಗಿದೆ!"
                )
            }
        }
    }

    fun onSaveDesktopPythonAppToDownloads() {
        viewModelScope.launch {
            val res = DownloadsFolderHelper.saveTextFileToDownloads(
                context = context,
                content = GitHubDesktopTemplates.DESKTOP_PYTHON_APP,
                fileName = "dhvaniflow_desktop.py",
                mimeType = "text/x-python"
            )
            _uiState.update {
                it.copy(
                    lastDownloadResult = res,
                    statusBannerKn = "Desktop App ಕೋಡ್ (dhvaniflow_desktop.py) ನಿಮ್ಮ Downloads ಫೋಲ್ಡರ್‌ನಲ್ಲಿ ಸೇವ್ ಆಗಿದೆ!"
                )
            }
        }
    }

    fun onDeleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}
