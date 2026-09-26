package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.StudioUiState
import com.example.ui.components.SynchronizedScrollVideoPlayer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioSurfaceVariant

@Composable
fun Menu1MuteVideoScreen(
    uiState: StudioUiState,
    onUploadVideo: (Uri) -> Unit,
    onStripCurrentVideo: () -> Unit,
    onRegenerateSample: () -> Unit,
    onSaveMutedToDownloads: () -> Unit,
    onProceedToMenu2: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onUploadVideo(uri)
    }

    val documentVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onUploadVideo(uri)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, AmberGold.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_studio_hero),
                contentDescription = "DhvaniFlow Studio Hero Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xEB0A0F1D),
                                Color(0xBF0A0F1D),
                                Color(0x660A0F1D)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween) {
                    Surface(
                        color = AmberGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Fast",
                                tint = AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ಮೆನು 1 • ಧ್ವನಿ ತೆಗೆಯಿರಿ (Mute Video in Seconds)",
                                style = MaterialTheme.typography.labelMedium,
                                color = AmberGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ವಿಡಿಯೋದಿಂದ ಧ್ವನಿಯನ್ನು ತೆಗೆದು ಕೇವಲ ವಿಡಿಯೋ ಮಾಡಿ",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "ವಿಡಿಯೋ ಅಪ್ಲೋಡ್ ಮಾಡಿದ ತಕ್ಷಣ ಕೆಲವೇ ಸೆಕೆಂಡುಗಳಲ್ಲಿ ಧ್ವನಿ ರಹಿತ ವಿಡಿಯೋ ಸಿದ್ಧವಾಗುತ್ತದೆ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // Upload & Action Controls Card
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
                    text = "1. ವಿಡಿಯೋ ಆಯ್ಕೆಮಾಡಿ (Upload Video)",
                    style = MaterialTheme.typography.titleMedium,
                    color = AmberGold
                )

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = Color(0xFF0A0F1D)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("menu1_upload_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Upload Video"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ವಿಡಿಯೋ ಅಪ್ಲೋಡ್ ಮಾಡಿ (ಮತ್ತು ತಕ್ಷಣ ಧ್ವನಿ ತೆಗೆಯಿರಿ)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { documentVideoLauncher.launch("video/*") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("menu1_browse_files_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Browse Files",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ಫೈಲ್ ಬ್ರೌಸ್ ಮಾಡಿ",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan
                        )
                    }

                    OutlinedButton(
                        onClick = onRegenerateSample,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("menu1_sample_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "Sample Video",
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ತಕ್ಷಣದ ಡೆಮೊ ವಿಡಿಯೋ",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberGold
                        )
                    }
                }
            }
        }

        // Live Muted Video Preview & Instant Results
        val activeMutedFile = uiState.menu1MutedOutput?.outputFile ?: uiState.menu1InputFile
        SynchronizedScrollVideoPlayer(
            videoFile = activeMutedFile,
            hasAudioTrack = false,
            showScrollingTextOverlay = false,
            timedLines = emptyList()
        )

        // Speed & Action Footer Card
        if (uiState.menu1MutedOutput != null) {
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
                            contentDescription = "Ready",
                            tint = EmeraldReady
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = uiState.menu1MutedOutput.summaryKn,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.menu1MutedOutput.summaryEn,
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
                            onClick = onSaveMutedToDownloads,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("menu1_save_downloads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Save to Downloads",
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
                            onClick = onProceedToMenu2,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = Color(0xFF0A0F1D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("menu1_next_menu_button")
                        ) {
                            Text(
                                text = "ನೆಕ್ಸ್ಟ್ ಮೆನು (ಧ್ವನಿ ಸೇರಿಸಿ)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Menu"
                            )
                        }
                    }
                }
            }
        }
    }
}
