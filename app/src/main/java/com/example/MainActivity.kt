package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SwipeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ProcessedVideoEntity
import com.example.ui.DhvaniStudioViewModel
import com.example.ui.StudioUiState
import com.example.ui.screens.Menu1MuteVideoScreen
import com.example.ui.screens.Menu2AddVoiceScreen
import com.example.ui.screens.Menu3ScrollTextVideoScreen
import com.example.ui.screens.Menu4GitHubDesktopScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioDeepBg
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val studioViewModel: DhvaniStudioViewModel = viewModel()
                val uiState by studioViewModel.uiState.collectAsStateWithLifecycle()
                val historyItems by studioViewModel.historyItems.collectAsStateWithLifecycle()

                DhvaniFlowStudioApp(
                    uiState = uiState,
                    historyItems = historyItems,
                    viewModel = studioViewModel
                )
            }
        }
    }
}

private data class NavMenuItem(
    val index: Int,
    val titleKn: String,
    val subtitleEn: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun DhvaniFlowStudioApp(
    uiState: StudioUiState,
    historyItems: List<ProcessedVideoEntity>,
    viewModel: DhvaniStudioViewModel
) {
    if (uiState.selectedTab != 0) {
        BackHandler {
            viewModel.selectTab(0)
        }
    }

    val navItems = listOf(
        NavMenuItem(0, "1. ಧ್ವನಿ ತೆಗೆಯಿರಿ", "Mute Video", Icons.Default.VolumeOff, "nav_tab_mute"),
        NavMenuItem(1, "2. ಧ್ವನಿ ಸೇರಿಸಿ", "Add Voice", Icons.Default.RecordVoiceOver, "nav_tab_dub"),
        NavMenuItem(2, "3. ಪಠ್ಯ ಸ್ವೈಪ್", "Voice Scroll", Icons.Default.SwipeUp, "nav_tab_scroll"),
        NavMenuItem(3, "4. GitHub/PC", "Workflow", Icons.Default.Code, "nav_tab_github")
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDeepBg)
    ) {
        val isDesktopOrWide = maxWidth >= 600.dp || uiState.forceDesktopPreviewMode

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = StudioDeepBg,
            topBar = {
                StudioTopBar(
                    uiState = uiState,
                    isDesktopMode = isDesktopOrWide,
                    onToggleDesktopMode = viewModel::toggleDesktopPreviewMode
                )
            },
            bottomBar = {
                if (!isDesktopOrWide) {
                    NavigationBar(
                        containerColor = StudioSurface,
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        navItems.forEach { item ->
                            val selected = uiState.selectedTab == item.index
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(item.index) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.titleKn
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.titleKn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0A0F1D),
                                    selectedTextColor = AmberGold,
                                    indicatorColor = AmberGold,
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isDesktopOrWide) {
                    NavigationRail(
                        containerColor = StudioSurface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .border(1.dp, StudioSurfaceVariant)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        navItems.forEach { item ->
                            val selected = uiState.selectedTab == item.index
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(item.index) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.titleKn
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.titleKn,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color(0xFF0A0F1D),
                                    selectedTextColor = AmberGold,
                                    indicatorColor = AmberGold,
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag("${item.tag}_rail")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = if (isDesktopOrWide) 840.dp else 600.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Live Status Banner
                        StudioLiveStatusPill(uiState = uiState)

                        when (uiState.selectedTab) {
                            0 -> Menu1MuteVideoScreen(
                                uiState = uiState,
                                onUploadVideo = viewModel::onMenu1VideoUploaded,
                                onStripCurrentVideo = viewModel::onMenu1StripCurrentVideoNow,
                                onRegenerateSample = viewModel::prepareInitialStudioSample,
                                onSaveMutedToDownloads = viewModel::onSaveMenu1MutedVideoToDownloads,
                                onProceedToMenu2 = viewModel::onProceedFromMenu1ToMenu2
                            )
                            1 -> Menu2AddVoiceScreen(
                                uiState = uiState,
                                onUploadVideoForMenu2 = viewModel::onMenu2VideoUploaded,
                                onUploadVoiceAudio = viewModel::onMenu2AudioFileUploaded,
                                onToggleMicRecording = viewModel::toggleMicRecordingForMenu2,
                                onCustomScriptChange = viewModel::onMenu2CustomScriptChanged,
                                onSynthesizeAndMergeVoice = viewModel::onSynthesizeAndMergeVoiceForMenu2,
                                onSaveDubbedToDownloads = viewModel::onSaveMenu2DubbedVideoToDownloads,
                                onProceedToMenu3 = viewModel::onProceedFromMenu2ToMenu3
                            )
                            2 -> Menu3ScrollTextVideoScreen(
                                uiState = uiState,
                                onUploadVideoWithVoice = viewModel::onMenu3VideoWithVoiceUploaded,
                                onAutoTranscribeAndSync = viewModel::onAutoTranscribeCurrentMenu3Video,
                                onScriptTextEdited = viewModel::onMenu3ScriptEdited,
                                onScrollSpeedChanged = viewModel::onMenu3ScrollSpeedChanged,
                                onFontSizeChanged = viewModel::onMenu3FontSizeChanged,
                                onRenderAndSaveToDownloads = viewModel::onRenderAndSaveMenu3ScrolledVideoToDownloads
                            )
                            else -> Menu4GitHubDesktopScreen(
                                uiState = uiState,
                                historyItems = historyItems,
                                onToggleDesktopPreviewMode = viewModel::toggleDesktopPreviewMode,
                                onSaveWorkflowToDownloads = viewModel::onSaveGitHubWorkflowToDownloads,
                                onSaveDesktopAppToDownloads = viewModel::onSaveDesktopPythonAppToDownloads,
                                onDeleteHistoryItem = viewModel::onDeleteHistoryItem
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioTopBar(
    uiState: StudioUiState,
    isDesktopMode: Boolean,
    onToggleDesktopMode: () -> Unit
) {
    Surface(
        color = StudioSurface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AmberGold.copy(alpha = 0.2f))
                        .border(1.5.dp, AmberGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "DhvaniFlow Logo",
                        tint = AmberGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ಧ್ವನಿಫ್ಲೋ ಸ್ಟುಡಿಯೋ • DhvaniFlow",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isDesktopMode) {
                            "Desktop & Tablet Studio Mode"
                        } else {
                            "ಮೊಬೈಲ್ & ಡೆಸ್ಕ್‌ಟಾಪ್ ವಿಡಿಯೋ ಧ್ವನಿ ಸ್ಟುಡಿಯೋ"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan
                    )
                }
            }

            IconButton(
                onClick = onToggleDesktopMode,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioSurfaceVariant)
                    .testTag("topbar_desktop_mode_toggle")
            ) {
                Icon(
                    imageVector = if (isDesktopMode) Icons.Default.PhoneAndroid else Icons.Default.DesktopWindows,
                    contentDescription = "Switch Mobile or Desktop View",
                    tint = AmberGold
                )
            }
        }
    }
}

@Composable
private fun StudioLiveStatusPill(uiState: StudioUiState) {
    Surface(
        color = StudioSurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (uiState.isBusy) AmberGold else EmeraldReady.copy(alpha = 0.55f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = uiState.isBusy) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = AmberGold,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
            if (!uiState.isBusy) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Status Ready",
                    tint = EmeraldReady,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column {
                Text(
                    text = uiState.statusBannerKn,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
                Text(
                    text = uiState.statusBannerEn,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
