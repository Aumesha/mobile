package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SwipeUp
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.media.DownloadsFolderHelper
import com.example.ui.StudioUiState
import com.example.ui.components.SynchronizedScrollVideoPlayer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDeepBg
import com.example.ui.theme.StudioSurfaceVariant

@Composable
fun Menu3ScrollTextVideoScreen(
    uiState: StudioUiState,
    onUploadVideoWithVoice: (Uri) -> Unit,
    onAutoTranscribeAndSync: () -> Unit,
    onScriptTextEdited: (String) -> Unit,
    onScrollSpeedChanged: (Float) -> Unit,
    onFontSizeChanged: (Float) -> Unit,
    onRenderAndSaveToDownloads: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onUploadVideoWithVoice(uri)
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onUploadVideoWithVoice(uri)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmberGold.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = AmberGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwipeUp,
                            contentDescription = "Swipe Up",
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ಮೆನು 3 • ಧ್ವನಿ ಪಠ್ಯ ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ (Voice-Synced Scroll)",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberGold
                        )
                    }
                }

                Text(
                    text = "ಧ್ವನಿಗೆ ತಕ್ಕಂತೆ ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಆಗುವ ಪಠ್ಯದ ವಿಡಿಯೋ",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "ಹೊಸ ಧ್ವನಿ ಹಾಕಿದ ವಿಡಿಯೋವನ್ನು ಅಪ್ಲೋಡ್ ಮಾಡಿ (ಅಥವಾ ಮೆನು 2ರಿಂದ ನೇರವಾಗಿ ಬಳಸಿ). ಅದರಲ್ಲಿರುವ ಧ್ವನಿಯನ್ನು ಪಠ್ಯ ರೂಪಕ್ಕೆ ಪರಿವರ್ತಿಸಿ ಮಾತನಾಡುವ ವೇಗಕ್ಕೆ ತಕ್ಕಂತೆ ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಮಾಡುತ್ತದೆ.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0A0F1D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("menu3_upload_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Upload Video with Voice"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ಧ್ವನಿ ವಿಡಿಯೋ ಅಪ್ಲೋಡ್",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onAutoTranscribeAndSync,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("menu3_auto_transcribe_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Voice to Text Sync",
                            tint = AmberGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ಧ್ವನಿ → ಪಠ್ಯ ಸಿಂಕ್",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberGold
                        )
                    }
                }
            }
        }

        // LIVE VIDEO PLAYER WITH BOTTOM-TO-TOP SYNCHRONIZED SCROLLING TEXT
        val previewVideoFile = uiState.menu3ScrolledOutput?.outputFile
            ?: uiState.menu3VideoFile
            ?: uiState.menu2DubbedOutput?.outputFile

        SynchronizedScrollVideoPlayer(
            videoFile = previewVideoFile,
            hasAudioTrack = true,
            showScrollingTextOverlay = true,
            timedLines = uiState.menu3TimedLines,
            scrollSpeedMultiplier = uiState.menu3ScrollSpeed,
            fontSizeSp = uiState.menu3FontSizeSp
        )

        // PRIMARY DOWNLOAD & RENDER CTA CARD (Saves directly to mobile Downloads folder!)
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, EmeraldReady, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ವಿಡಿಯೋ ಸಿದ್ಧಪಡಿಸಿ ಮತ್ತು ನೇರವಾಗಿ Downloads ಫೋಲ್ಡರ್‌ನಲ್ಲಿ ಸೇವ್ ಮಾಡಿ",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmeraldReady,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.isBusy && uiState.menu3RenderProgress > 0f && uiState.menu3RenderProgress < 1f) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { uiState.menu3RenderProgress },
                            color = EmeraldReady,
                            trackColor = StudioDeepBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                        Text(
                            text = "ಸ್ವೈಪ್ ಪಠ್ಯದ ವಿಡಿಯೋ ಸಿದ್ಧವಾಗುತ್ತಿದೆ... ${(uiState.menu3RenderProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberGold
                        )
                    }
                }

                Button(
                    onClick = onRenderAndSaveToDownloads,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldReady,
                        contentColor = Color(0xFF0A0F1D)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("menu3_render_and_download_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Render and Save to Downloads"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ವಿಡಿಯೋ ಡೌನ್‌ಲೋಡ್ ಮಾಡಿ (Save to Downloads Folder)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.lastDownloadResult != null && uiState.lastDownloadResult.success) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF063E2E))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Saved",
                                    tint = EmeraldReady,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.lastDownloadResult.messageKn,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Path: ${uiState.lastDownloadResult.displayPath}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldReady
                            )
                            OutlinedButton(
                                onClick = {
                                    DownloadsFolderHelper.shareOrOpenSavedUri(
                                        context,
                                        uiState.lastDownloadResult.uriString,
                                        "video/mp4"
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("menu3_open_saved_video_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Open Saved Video",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ಡೌನ್‌ಲೋಡ್ ಆದ ವಿಡಿಯೋ ತೆರೆಯಿರಿ (Open Saved Video)",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scroll Speed, Font Size & Timed Voice Script Customization Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = "Script Settings",
                        tint = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಸೆಟ್ಟಿಂಗ್ಸ್ & ಪಠ್ಯ (Voice Sync Cues)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                // Scroll Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Scroll Speed",
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ಸ್ವೈಪ್ ವೇಗ: ${String.format("%.1fx", uiState.menu3ScrollSpeed)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.width(100.dp)
                    )
                    Slider(
                        value = uiState.menu3ScrollSpeed,
                        onValueChange = onScrollSpeedChanged,
                        valueRange = 0.6f..1.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberGold,
                            activeTrackColor = AmberGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("menu3_scroll_speed_slider")
                    )
                }

                // Font Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "Font Size",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ಪಠ್ಯ ಗಾತ್ರ: ${uiState.menu3FontSizeSp.toInt()}sp",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.width(100.dp)
                    )
                    Slider(
                        value = uiState.menu3FontSizeSp,
                        onValueChange = onFontSizeChanged,
                        valueRange = 14f..26f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("menu3_font_size_slider")
                    )
                }

                OutlinedTextField(
                    value = uiState.menu3ScriptText,
                    onValueChange = onScriptTextEdited,
                    label = {
                        Text("ವಿಡಿಯೋದಲ್ಲಿ ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಆಗುವ ಪಠ್ಯ ಸಾಲುಗಳು (ಪ್ರತಿ ಸಾಲಿಗೊಂದು ವಾಕ್ಯ)")
                    },
                    minLines = 4,
                    maxLines = 7,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("menu3_script_text_input")
                )

                // Voice-Synced Timestamp Pills Preview
                if (uiState.menu3TimedLines.isNotEmpty()) {
                    Text(
                        text = "ಧ್ವನಿ ಸಮಯದ ಸಿಂಕ್ (Voice Timing Sync Map):",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberGold
                    )
                    uiState.menu3TimedLines.forEach { cue ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioDeepBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${String.format("%.1fs", cue.startMs / 1000f)} - ${String.format("%.1fs", cue.endMs / 1000f)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan,
                                modifier = Modifier.width(88.dp)
                            )
                            Text(
                                text = cue.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
