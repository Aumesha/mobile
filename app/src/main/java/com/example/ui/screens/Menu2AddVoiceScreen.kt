package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.StudioUiState
import com.example.ui.components.SynchronizedScrollVideoPlayer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRecord
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioSurfaceVariant

@Composable
fun Menu2AddVoiceScreen(
    uiState: StudioUiState,
    onUploadVideoForMenu2: (Uri) -> Unit,
    onUploadVoiceAudio: (Uri) -> Unit,
    onToggleMicRecording: () -> Unit,
    onCustomScriptChange: (String) -> Unit,
    onSynthesizeAndMergeVoice: () -> Unit,
    onSaveDubbedToDownloads: () -> Unit,
    onProceedToMenu3: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onUploadVideoForMenu2(uri)
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onUploadVoiceAudio(uri)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            onToggleMicRecording()
        } else {
            // If mic permission denied on emulator, synthesize from script seamlessly
            onSynthesizeAndMergeVoice()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = ElectricCyan.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                ) {
                    Text(
                        text = "ಮೆನು 2 • ಹಳೆಯ ಧ್ವನಿ ತೆಗೆದು ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಿ (Add New Voice)",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Text(
                    text = "ವಿಡಿಯೋಗೆ ನಿಮ್ಮ ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಿ (Voice Dubbing)",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "ಮೊದಲನೇ ಮೆನುವಿನ ಧ್ವನಿ ತೆಗೆದ ವಿಡಿಯೋ ಸಿದ್ಧವಾಗಿದೆ (ಅಥವಾ ಬೇರೆ ವಿಡಿಯೋ ಅಪ್ಲೋಡ್ ಮಾಡಿ). ಇದಕ್ಕೆ ನಿಮ್ಮ ಧ್ವನಿ ಫೈಲ್, ಮೈಕ್ ರೆಕಾರ್ಡಿಂಗ್ ಅಥವಾ ಕನ್ನಡ/English ಧ್ವನಿಯನ್ನು ಕೆಲವೇ ಸೆಕೆಂಡುಗಳಲ್ಲಿ ಸೇರಿಸಿ.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1)
                )

                OutlinedButton(
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("menu2_upload_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = "Change Video",
                        tint = AmberGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ಬೇರೆ ವಿಡಿಯೋ ಅಪ್ಲೋಡ್ ಮಾಡಿ (ಮೊದಲು ಧ್ವನಿ ತೆಗೆಯುತ್ತದೆ)",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberGold
                    )
                }
            }
        }

        // Add New Voice Options Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ಹೊಸ ಧ್ವನಿಯನ್ನು ಆಯ್ಕೆಮಾಡಿ ಅಥವಾ ರೆಕಾರ್ಡ್ ಮಾಡಿ:",
                    style = MaterialTheme.typography.titleMedium,
                    color = AmberGold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0A0F1D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("menu2_upload_audio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = "Upload Audio",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ಆಡಿಯೋ ಅಪ್ಲೋಡ್",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                onToggleMicRecording()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isRecordingMic) CoralRecord else AmberGold,
                            contentColor = if (uiState.isRecordingMic) Color.White else Color(0xFF0A0F1D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("menu2_record_mic_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isRecordingMic) Icons.Default.StopCircle else Icons.Default.Mic,
                            contentDescription = "Record Voice",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isRecordingMic) "ರೆಕಾರ್ಡ್ ನಿಲ್ಲಿಸಿ" else "ಮೈಕ್ ಧ್ವನಿ ರೆಕಾರ್ಡ್",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Script-to-Voice input so the user can also type what the voice speaks
                OutlinedTextField(
                    value = uiState.menu2CustomScript,
                    onValueChange = onCustomScriptChange,
                    label = {
                        Text("ಮಾತನಾಡುವ ಧ್ವನಿಯ ಪಠ್ಯ (ಕನ್ನಡ / English - ಮೆನು 3ರಲ್ಲಿ ಸ್ವೈಪ್ ಆಗುತ್ತದೆ)")
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("menu2_voice_script_input")
                )

                Button(
                    onClick = onSynthesizeAndMergeVoice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = Color(0xFF0A0F1D)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("menu2_merge_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Merge Voice"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ಈ ಹೊಸ ಧ್ವನಿಯನ್ನು ವಿಡಿಯೋಗೆ ಸೇರಿಸಿ (Merge Voice Now)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Video Player Preview for Dubbed Video
        val previewFile = uiState.menu2DubbedOutput?.outputFile ?: uiState.menu2VideoFile
        SynchronizedScrollVideoPlayer(
            videoFile = previewFile,
            hasAudioTrack = uiState.menu2DubbedOutput != null,
            showScrollingTextOverlay = false,
            timedLines = emptyList()
        )

        // Result Card & Proceed to Menu 3
        if (uiState.menu2DubbedOutput != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldReady.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Dubbed Ready",
                            tint = EmeraldReady
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = uiState.menu2DubbedOutput.summaryKn,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.menu2VoiceSourceLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldReady
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSaveDubbedToDownloads,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("menu2_save_downloads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Save Dubbed Video",
                                tint = EmeraldReady
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Downloads ಗೆ ಸೇವ್ ಮಾಡಿ",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldReady
                            )
                        }

                        Button(
                            onClick = onProceedToMenu3,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGold,
                                contentColor = Color(0xFF0A0F1D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("menu2_next_menu3_button")
                        ) {
                            Text(
                                text = "ನೆಕ್ಸ್ಟ್ ಮೆನು (ಪಠ್ಯ ಸ್ವೈಪ್)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Proceed to Menu 3"
                            )
                        }
                    }
                }
            }
        }
    }
}
