package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    // Sliders and toggle states
    var fps120Enabled by remember { mutableStateOf(true) }
    var gpuAccel by remember { mutableStateOf(true) }
    var ramLimit by remember { mutableStateOf(2048f) }
    
    var autoSaveInterval by remember { mutableStateOf(5f) }
    var cloudBackup by remember { mutableStateOf(false) }

    var stylusPressure by remember { mutableStateOf(true) }
    var palmRejection by remember { mutableStateOf(true) }
    var threeFingerGesture by remember { mutableStateOf(true) }

    var experimentalRigging by remember { mutableStateOf(false) }
    var experimentalScripting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Settings Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Studio Configuration Panel",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category 1: Rendering Performance
            SettingsSectionHeader("Engine & Performance")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color(0xFF2E314D))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 120 FPS toggle
                    SettingsToggleRow(
                        title = "120 FPS Fluid Playback",
                        desc = "Enable 120Hz display refresh synchronization where hardware supports it.",
                        checked = fps120Enabled,
                        onCheckedChange = { fps120Enabled = it }
                    )

                    Divider(color = Color(0xFF2E314D))

                    // GPU Render Acceleration
                    SettingsToggleRow(
                        title = "GPU OpenGL/Vulkan Pipeline",
                        desc = "Accelerate drawing operations and canvas strokes using on-board GPU drivers.",
                        checked = gpuAccel,
                        onCheckedChange = { gpuAccel = it }
                    )

                    Divider(color = Color(0xFF2E314D))

                    // RAM limit
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cache Allocation Limit", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("${ramLimit.toInt()} MB", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text("Limit JVM RAM boundaries allocated for history redo/undo state buffers.", fontSize = 11.sp, color = Color.LightGray)
                        Slider(
                            value = ramLimit,
                            onValueChange = { ramLimit = it },
                            valueRange = 512f..4096f,
                            steps = 7
                        )
                    }
                }
            }

            // Category 2: Core Saving Operations
            SettingsSectionHeader("Autosave & Backup Cycles")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color(0xFF2E314D))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Auto-save boundary slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Auto-Save Frequency", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("${autoSaveInterval.toInt()} Seconds", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text("Saves drawing buffer to SQLite Room database periodically.", fontSize = 11.sp, color = Color.LightGray)
                        Slider(
                            value = autoSaveInterval,
                            onValueChange = { autoSaveInterval = it },
                            valueRange = 5f..60f,
                            steps = 11
                        )
                    }

                    Divider(color = Color(0xFF2E314D))

                    // Cloud backups
                    SettingsToggleRow(
                        title = "Cloud Project Synchronizations",
                        desc = "Enable automated background backups of vector paths to your secure storage cloud.",
                        checked = cloudBackup,
                        onCheckedChange = { cloudBackup = it }
                    )
                }
            }

            // Category 3: Stylus Input
            SettingsSectionHeader("Stylus Touch & Gestures Mapping")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color(0xFF2E314D))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Pressure touch
                    SettingsToggleRow(
                        title = "Stylus Tip Pressure Sensitivity",
                        desc = "Support stroke thickness expansion based on stylus contact force.",
                        checked = stylusPressure,
                        onCheckedChange = { stylusPressure = it }
                    )

                    Divider(color = Color(0xFF2E314D))

                    // Palm rejection
                    SettingsToggleRow(
                        title = "Palm Rejection Buffers",
                        desc = "Ignore touch pointer event tracks when a stylus is detected on screen.",
                        checked = palmRejection,
                        onCheckedChange = { palmRejection = it }
                    )

                    Divider(color = Color(0xFF2E314D))

                    // Three finger undo
                    SettingsToggleRow(
                        title = "Three-Finger Gesture Trackers",
                        desc = "Three-finger swipe left triggers canvas stroke UNDO; swipe right triggers REDO.",
                        checked = threeFingerGesture,
                        onCheckedChange = { threeFingerGesture = it }
                    )
                }
            }

            // Category 4: Experimental features
            SettingsSectionHeader("Experimental Animation Add-ons")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color(0xFF2E314D))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Bone rigging
                    SettingsToggleRow(
                        title = "Inverse Kinematics (IK) Rigging",
                        desc = "Unlocks bone-skeletal mapping, character rigs, and joint constraints in the layers editor.",
                        checked = experimentalRigging,
                        onCheckedChange = { experimentalRigging = it }
                    )

                    Divider(color = Color(0xFF2E314D))

                    // Automation Scripting API
                    SettingsToggleRow(
                        title = "Automation Scripting SDK API",
                        desc = "Enables sandboxed python/JS script inputs to batch-interpolate complex matrix frame transformations.",
                        checked = experimentalScripting,
                        onCheckedChange = { experimentalScripting = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingsToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Text(desc, fontSize = 11.sp, color = Color.LightGray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
