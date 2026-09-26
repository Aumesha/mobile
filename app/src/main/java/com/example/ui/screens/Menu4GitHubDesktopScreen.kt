package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.GitHubDesktopTemplates
import com.example.data.local.ProcessedVideoEntity
import com.example.ui.StudioUiState
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDeepBg
import com.example.ui.theme.StudioSurfaceVariant

@Composable
fun Menu4GitHubDesktopScreen(
    uiState: StudioUiState,
    historyItems: List<ProcessedVideoEntity>,
    onToggleDesktopPreviewMode: () -> Unit,
    onSaveWorkflowToDownloads: () -> Unit,
    onSaveDesktopAppToDownloads: () -> Unit,
    onDeleteHistoryItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label ಕಾಪಿ ಮಾಡಲಾಗಿದೆ (Copied!)", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mobile vs Desktop Studio Layout Switcher Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DesktopWindows,
                        contentDescription = "Desktop & Mobile",
                        tint = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ಮೊಬೈಲ್ ಆಪ್ + ಡೆಸ್ಕ್‌ಟಾಪ್ ಆಪ್ & GitHub ವರ್ಕ್‌ಫ್ಲೋ",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "ಈ ಪ್ರಾಜೆಕ್ಟ್‌ನಲ್ಲಿ `.github/workflows/build-mobile-desktop.yml` ಮತ್ತು `desktop/dhvaniflow_desktop.py` ಈಗಾಗಲೇ ಸಿದ್ಧವಾಗಿದೆ!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Button(
                    onClick = onToggleDesktopPreviewMode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = Color(0xFF0A0F1D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("toggle_desktop_mode_button")
                ) {
                    Icon(
                        imageVector = if (uiState.forceDesktopPreviewMode) {
                            Icons.Default.PhoneAndroid
                        } else {
                            Icons.Default.DesktopWindows
                        },
                        contentDescription = "Switch Layout"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.forceDesktopPreviewMode) {
                            "ಮೊಬೈಲ್ ವ್ಯೂಗೆ ಬದಲಾಯಿಸಿ (Switch to Mobile View)"
                        } else {
                            "ಡೆಸ್ಕ್‌ಟಾಪ್ ಸ್ಟುಡಿಯೋ ವ್ಯೂ ನೋಡಿ (Preview Desktop Studio Layout)"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // GitHub Actions Workflow YAML Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "GitHub Workflow",
                            tint = EmeraldReady
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ".github/workflows/build-mobile-desktop.yml",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldReady,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "ನೀವು ಈ ಪ್ರಾಜೆಕ್ಟ್ ಅನ್ನು GitHub ಗೆ Push ಮಾಡಿದ ತಕ್ಷಣ Android APK ಮತ್ತು Windows/macOS/Linux Desktop ಆಪ್ ಎರಡನ್ನೂ ಸ್ವಯಂಚಾಲಿತವಾಗಿ ಬಿಲ್ಡ್ ಮಾಡುತ್ತದೆ.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            copyToClipboard("GitHub Workflow YAML", GitHubDesktopTemplates.GITHUB_WORKFLOW_YAML)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("copy_workflow_yaml_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy YAML",
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ವರ್ಕ್‌ಫ್ಲೋ ಕಾಪಿ ಮಾಡಿ", color = AmberGold)
                    }

                    Button(
                        onClick = onSaveWorkflowToDownloads,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldReady,
                            contentColor = Color(0xFF0A0F1D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_workflow_yaml_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save YAML",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Downloads ಗೆ ಸೇವ್", fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioDeepBg)
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = GitHubDesktopTemplates.GITHUB_WORKFLOW_YAML,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // Desktop App Python Code Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DesktopWindows,
                        contentDescription = "Desktop Code",
                        tint = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "desktop/dhvaniflow_desktop.py (Desktop App)",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            copyToClipboard("Desktop App Code", GitHubDesktopTemplates.DESKTOP_PYTHON_APP)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("copy_desktop_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Desktop Code",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ಕೋಡ್ ಕಾಪಿ ಮಾಡಿ", color = ElectricCyan)
                    }

                    Button(
                        onClick = onSaveDesktopAppToDownloads,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0A0F1D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_desktop_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save Desktop Code",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Downloads ಗೆ ಸೇವ್", fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioDeepBg)
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = GitHubDesktopTemplates.DESKTOP_PYTHON_APP,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // Room Database Processed Videos History
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = AmberGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ಇತ್ತೀಚಿನ ಸಿದ್ಧಪಡಿಸಿದ ವಿಡಿಯೋಗಳು (Processed History)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                if (historyItems.isEmpty()) {
                    Text(
                        text = "ನೀವು ವಿಡಿಯೋ ಮ್ಯೂಟ್, ಡಬ್ಬಿಂಗ್ ಅಥವಾ ಪಠ್ಯ ಸ್ವೈಪ್ ಮಾಡಿದ ತಕ್ಷಣ ಇಲ್ಲಿ ಕಾಣಿಸುತ್ತದೆ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    historyItems.take(6).forEach { item ->
                        Surface(
                            color = StudioCardElevated,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "ವೇಗ: ${item.elapsedProcessMs}ms • ${item.downloadsFilePath.ifBlank { item.internalFilePath }}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = EmeraldReady
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteHistoryItem(item.id) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete history item",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Security Caution Notice for Prototype API Key Usage
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioDeepBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Notice",
                    tint = AmberGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.security_warning_prototype),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
