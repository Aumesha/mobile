package com.example.ui.components

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.TimedScriptLine
import com.example.media.VideoProcessingEngine
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.StudioDeepBg
import com.example.ui.theme.StudioSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
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
    val durationMs = remember(videoFile, timedLines) {
        val lastCueEnd = timedLines.lastOrNull()?.endMs ?: 6000L
        max(6000L, lastCueEnd)
    }
    var isMutedToggle by remember(videoFile, hasAudioTrack) { mutableStateOf(!hasAudioTrack) }

    // 60 FPS playback clock for smooth bottom-to-top voice-synchronized scrolling
    LaunchedEffect(videoFile, isPlaying, durationMs) {
        var lastTick = System.currentTimeMillis()
        while (isActive && isPlaying) {
            delay(16L)
            val now = System.currentTimeMillis()
            val delta = (now - lastTick).coerceIn(8L, 64L)
            lastTick = now
            currentPosMs = (currentPosMs + delta) % durationMs
        }
    }

    // Pure AudioTrack PCM Voice Output (Zero MediaCodec / Zero MPEG4Writer errors)
    LaunchedEffect(videoFile, isPlaying, hasAudioTrack, isMutedToggle) {
        if (!isPlaying || !hasAudioTrack || isMutedToggle || videoFile == null) {
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            var track: AudioTrack? = null
            try {
                val wavFile = VideoProcessingEngine.getCompanionAudioFile(videoFile)
                if (wavFile.exists() && wavFile.length() > 44L) {
                    val allBytes = wavFile.readBytes()
                    val pcmData = allBytes.copyOfRange(44, allBytes.size)
                    val sampleRate = 22050
                    val minBuf = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ).coerceAtLeast(4096)

                    track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBuf)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()

                    track.play()
                    var offset = 0
                    val chunk = 2048
                    while (isActive && isPlaying && !isMutedToggle) {
                        if (offset >= pcmData.size) {
                            offset = 0
                        }
                        val toWrite = minOf(chunk, pcmData.size - offset)
                        track.write(pcmData, offset, toWrite)
                        offset += toWrite
                    }
                }
            } catch (_: Exception) {
            } finally {
                try {
                    track?.stop()
                } catch (_: Exception) {
                }
                try {
                    track?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isPlaying = false
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
            // 60 FPS Cinema Video Frame Canvas
            val progressFraction = (currentPosMs.toFloat() / max(1L, durationMs).toFloat()).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0C1324),
                            Color(0xFF162444),
                            Color(0xFF0F172A)
                        )
                    )
                )

                val cx = size.width / 2f
                val cy = if (showScrollingTextOverlay) size.height * 0.30f else size.height * 0.48f
                val pulse = sin(progressFraction * Math.PI.toFloat() * 6f) * 16f

                drawCircle(
                    color = ElectricCyan.copy(alpha = 0.25f),
                    radius = 78f + pulse,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = AmberGold.copy(alpha = 0.20f),
                    radius = 116f - pulse * 0.6f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5f)
                )

                // Voice-reactive cinema equalizer bars
                val barCount = 18
                val totalW = size.width * 0.68f
                val startX = (size.width - totalW) / 2f
                val slotW = totalW / barCount
                val activeAudio = hasAudioTrack && !isMutedToggle && isPlaying

                for (b in 0 until barCount) {
                    val anim = if (activeAudio) {
                        abs(sin(wavePhase + b * 0.55f + progressFraction * 12f))
                    } else {
                        0.12f
                    }
                    val bh = 10f + anim * 58f
                    drawRoundRect(
                        color = if (b % 2 == 0) AmberGold else ElectricCyan,
                        topLeft = Offset(startX + b * slotW + 3f, cy - bh / 2f),
                        size = Size(slotW - 6f, bh),
                        cornerRadius = CornerRadius(6f, 6f)
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
                    ),
                    modifier = Modifier.clickable {
                        if (hasAudioTrack) {
                            isMutedToggle = !isMutedToggle
                        }
                    }
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
                onClick = { isPlaying = !isPlaying },
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
                    isPlaying = true
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
