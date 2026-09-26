package com.example.ui.components

import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.media.TimedScriptLine
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.StudioDeepBg
import com.example.ui.theme.StudioSurfaceVariant
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SynchronizedScrollVideoPlayer(
    videoFile: File?,
    hasAudioTrack: Boolean,
    showScrollingTextOverlay: Boolean,
    timedLines: List<TimedScriptLine>,
    scrollSpeedMultiplier: Float = 1.0f,
    fontSizeSp: Float = 18f,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember(videoFile) { mutableStateOf(true) }
    var currentPosMs by remember(videoFile) { mutableLongStateOf(0L) }
    var durationMs by remember(videoFile) { mutableLongStateOf(6000L) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isMutedToggle by remember(videoFile, hasAudioTrack) { mutableStateOf(!hasAudioTrack) }

    // 60 FPS playback clock for smooth bottom-to-top voice-synchronized scrolling
    LaunchedEffect(videoFile, isPlaying) {
        while (isPlaying) {
            val vv = videoViewRef
            if (vv != null && vv.isPlaying) {
                currentPosMs = vv.currentPosition.toLong().coerceAtLeast(0L)
                val dur = vv.duration.toLong()
                if (dur > 0L) durationMs = dur
            } else if (videoFile != null) {
                currentPosMs = (currentPosMs + 32L) % max(1000L, durationMs)
            }
            delay(16L)
        }
    }

    DisposableEffect(videoFile) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(StudioDeepBg)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
    ) {
        // Video Monitor Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF070B14)),
            contentAlignment = Alignment.Center
        ) {
            if (videoFile != null && videoFile.exists()) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            videoViewRef = this
                            setVideoPath(videoFile.absolutePath)
                            setOnPreparedListener { mp ->
                                mediaPlayerRef = mp
                                mp.isLooping = true
                                val dur = mp.duration.toLong()
                                if (dur > 0L) durationMs = dur
                                val vol = if (!hasAudioTrack || isMutedToggle) 0f else 1f
                                mp.setVolume(vol, vol)
                                if (isPlaying) {
                                    start()
                                }
                            }
                            setOnCompletionListener {
                                currentPosMs = 0L
                                if (isPlaying) start()
                            }
                        }
                    },
                    update = { vv ->
                        videoViewRef = vv
                        val tagPath = vv.tag as? String
                        if (tagPath != videoFile.absolutePath) {
                            vv.tag = videoFile.absolutePath
                            vv.setVideoPath(videoFile.absolutePath)
                        }
                        try {
                            val vol = if (!hasAudioTrack || isMutedToggle) 0f else 1f
                            mediaPlayerRef?.setVolume(vol, vol)
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback animated studio canvas if no file yet
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF0B1325), Color(0xFF172544), Color(0xFF0A0F1D))
                        )
                    )
                }
            }

            // Top Studio Status Badges
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xCC0F172A),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (hasAudioTrack && !isMutedToggle) EmeraldReady else AmberGold
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasAudioTrack && !isMutedToggle) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Audio status",
                            tint = if (hasAudioTrack && !isMutedToggle) EmeraldReady else AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasAudioTrack && !isMutedToggle) {
                                "ಧ್ವನಿ ಸಕ್ರಿಯ (Voice ON)"
                            } else {
                                "ಧ್ವನಿ ತೆಗೆಯಲಾಗಿದೆ (Muted Video)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }

                if (showScrollingTextOverlay) {
                    Surface(
                        color = Color(0xCC0C344D),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Voice Sync Active",
                                tint = ElectricCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ • Voice Sync",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }

            // LIVE BOTTOM-TO-TOP VOICE-SYNCHRONIZED SCROLLING TEXT OVERLAY
            if (showScrollingTextOverlay && timedLines.isNotEmpty()) {
                val activeIdx = timedLines.indexOfFirst { currentPosMs in it.startMs..it.endMs }
                    .let { idx ->
                        if (idx >= 0) idx
                        else if (currentPosMs >= (timedLines.lastOrNull()?.endMs ?: 0L)) timedLines.lastIndex
                        else 0
                    }
                val activeCue = timedLines[activeIdx]
                val cueDur = max(1L, activeCue.endMs - activeCue.startMs).toFloat()
                val intraFraction = ((currentPosMs - activeCue.startMs).toFloat() / cueDur).coerceIn(0f, 1f)

                // Continuous upward scroll position driven directly by the voice timeline
                val continuousVoiceScroll = activeIdx.toFloat() + intraFraction
                val stepHeightDp = 58f * scrollSpeedMultiplier

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x880A0F1D),
                                    Color(0xE60A0F1D)
                                ),
                                startY = 80f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    timedLines.forEachIndexed { index, cue ->
                        // Positive offset -> below center (entering from bottom)
                        // Negative offset -> above center (scrolling upward to top)
                        val relOffset = index.toFloat() - continuousVoiceScroll
                        val yOffsetDp = (relOffset * stepHeightDp) + 22f

                        if (abs(relOffset) <= 2.7f) {
                            val isCurrent = index == activeIdx
                            val alphaVal = (1f - (abs(relOffset) / 2.8f)).coerceIn(0.15f, 1f)

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(0, (yOffsetDp * density).roundToInt()) }
                                    .fillMaxWidth(0.90f)
                                    .alpha(if (isCurrent) 1f else alphaVal)
                                    .then(
                                        if (isCurrent) {
                                            Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xD91E293B))
                                                .border(1.5.dp, AmberGold, RoundedCornerShape(14.dp))
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        } else {
                                            Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = cue.text,
                                        fontSize = if (isCurrent) fontSizeSp.sp else (fontSizeSp * 0.85f).sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isCurrent -> AmberGold
                                            index < activeIdx -> Color(0xFF94A3B8)
                                            else -> Color(0xFFF8FAFC)
                                        },
                                        textAlign = TextAlign.Center,
                                        lineHeight = (fontSizeSp * 1.3f).sp
                                    )
                                    if (isCurrent && isPlaying) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Mini live voice energy bar beneath the active spoken sentence
                                        Canvas(
                                            modifier = Modifier
                                                .width(96.dp)
                                                .height(6.dp)
                                        ) {
                                            val bars = 12
                                            val bw = size.width / (bars * 1.6f)
                                            val gap = (size.width - bw * bars) / max(1, bars - 1)
                                            for (b in 0 until bars) {
                                                val hFactor = (0.35f + 0.65f * abs(sin(wavePhase + b * 0.6f))) * cue.energyLevel
                                                val bh = size.height * hFactor.coerceIn(0.25f, 1f)
                                                drawRoundRect(
                                                    color = if (b % 2 == 0) AmberGold else ElectricCyan,
                                                    topLeft = Offset(b * (bw + gap), (size.height - bh) / 2f),
                                                    size = Size(bw, bh),
                                                    cornerRadius = CornerRadius(3f, 3f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Player Transport & Scrubber Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val vv = videoViewRef
                    if (isPlaying) {
                        try {
                            vv?.pause()
                        } catch (_: Exception) {
                        }
                        isPlaying = false
                    } else {
                        try {
                            vv?.start()
                        } catch (_: Exception) {
                        }
                        isPlaying = true
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(AmberGold.copy(alpha = 0.18f), CircleShape)
                    .testTag("player_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause Video" else "Play Video",
                    tint = AmberGold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = String.format("%.1fs", currentPosMs / 1000f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Slider(
                value = currentPosMs.toFloat().coerceIn(0f, max(1000L, durationMs).toFloat()),
                onValueChange = { newValue ->
                    currentPosMs = newValue.toLong()
                    try {
                        videoViewRef?.seekTo(newValue.toInt())
                    } catch (_: Exception) {
                    }
                },
                valueRange = 0f..max(1000L, durationMs).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = AmberGold,
                    activeTrackColor = ElectricCyan,
                    inactiveTrackColor = StudioDeepBg
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .testTag("player_timeline_slider")
            )

            Text(
                text = String.format("%.1fs", durationMs / 1000f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(
                onClick = {
                    currentPosMs = 0L
                    try {
                        videoViewRef?.seekTo(0)
                        videoViewRef?.start()
                        isPlaying = true
                    } catch (_: Exception) {
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("player_replay_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Replay Video",
                    tint = ElectricCyan
                )
            }
        }
    }
}
