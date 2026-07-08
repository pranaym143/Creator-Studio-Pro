package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ProjectViewModel
import com.example.ui.screens.AiLabScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.MyApplicationTheme

enum class ActiveScreen {
    DASHBOARD,
    AI_LAB,
    SETTINGS,
    WORKSPACE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ProjectViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(ActiveScreen.DASHBOARD) }
                var previousScreen by remember { mutableStateOf(ActiveScreen.DASHBOARD) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Display bottom bar ONLY on non-canvas screens for immersive drawing real estate
                        if (currentScreen != ActiveScreen.WORKSPACE) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == ActiveScreen.DASHBOARD,
                                    onClick = {
                                        previousScreen = currentScreen
                                        currentScreen = ActiveScreen.DASHBOARD
                                    },
                                    icon = { Icon(Icons.Default.Palette, contentDescription = "Projects") },
                                    label = { Text("Creations", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("nav_creations")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == ActiveScreen.AI_LAB,
                                    onClick = {
                                        previousScreen = currentScreen
                                        currentScreen = ActiveScreen.AI_LAB
                                    },
                                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Lab") },
                                    label = { Text("AI Laboratory", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("nav_ai_lab")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == ActiveScreen.SETTINGS,
                                    onClick = {
                                        previousScreen = currentScreen
                                        currentScreen = ActiveScreen.SETTINGS
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            ActiveScreen.DASHBOARD -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToWorkspace = {
                                        previousScreen = ActiveScreen.DASHBOARD
                                        currentScreen = ActiveScreen.WORKSPACE
                                    },
                                    onNavigateToAiLab = {
                                        previousScreen = ActiveScreen.DASHBOARD
                                        currentScreen = ActiveScreen.AI_LAB
                                    }
                                )
                            }
                            ActiveScreen.AI_LAB -> {
                                AiLabScreen(viewModel = viewModel)
                            }
                            ActiveScreen.SETTINGS -> {
                                SettingsScreen()
                            }
                            ActiveScreen.WORKSPACE -> {
                                WorkspaceScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        // Stop animation loop on exit
                                        viewModel.stopPlayback()
                                        currentScreen = previousScreen
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
