package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Project
import com.example.ui.ProjectViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ProjectViewModel,
    onNavigateToWorkspace: () -> Unit,
    onNavigateToAiLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.allProjects.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Search and filters
    var searchQuery by remember { mutableStateOf("") }
    val filteredProjects = projects.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // Spacing for bottom navigation bar
        ) {
            // Immersive Glassmorphic Hero Banner using R.drawable.img_home_hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_home_hero),
                    contentDescription = "Studio Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Overlay Text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "120 FPS ENGINE",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "GPU ACCELERATED",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Creator Studio Pro",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 36.sp
                    )

                    Text(
                        text = "The World's Most Powerful Mobile 2D Animation Studio",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick actions / Search Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search your creations...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF2E314D),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                        .testTag("search_bar")
                )

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                        .testTag("add_project_button")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create New Project",
                        tint = Color.Black
                    )
                }
            }

            // Recent Projects Header
            Text(
                text = "Recent Masterpieces",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filteredProjects.isEmpty()) {
                // Empty state illustration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, Color(0xFF2E314D), RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Palette,
                            contentDescription = "Palette empty state",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching creations" else "Your Canvas is Clean",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try searching another title." else "Bring your sketches to life! Tap '+' to begin drawing vector keyframes in 120 FPS.",
                            fontSize = 13.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        if (searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("New Animation File", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // List of projects
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProjects) { project ->
                        ProjectCard(
                            project = project,
                            onClick = {
                                viewModel.selectProject(project)
                                onNavigateToWorkspace()
                            },
                            onDelete = {
                                viewModel.deleteProject(project)
                            }
                        )
                    }
                }
            }

            // Inspiration & Lab Row
            Text(
                text = "Next-Gen Laboratory",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // AI Assistant Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAiLab() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color(0xFF2E314D))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("AI Studio Lab", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Generate color palettes, storyboard frame scripts, and chat with AI.", fontSize = 12.sp, color = Color.LightGray)
                    }
                }

                // Templates / Guides Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showCreateDialog = true }, // Opens presets wizard
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color(0xFF2E314D))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            Icons.Default.VideoCall,
                            contentDescription = "Templates presets",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Canvas Presets", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Instant configurations for TikTok 9:16, YouTube 16:9, or 4K Cinemagraphs.", fontSize = 12.sp, color = Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Animated Creator Wizard / Create Project dialog
        if (showCreateDialog) {
            ProjectCreationDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, width, height, fps, color, transparent ->
                    viewModel.startNewProject(name, width, height, fps, color, transparent)
                    showCreateDialog = false
                    onNavigateToWorkspace()
                }
            )
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(project.dateModified))

    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, Color(0xFF2E314D))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual mockup drawing box inside card header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(project.backgroundColor))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (project.isTransparent) {
                    // Transparent pattern indicators
                    Text("Transparent Canvas", color = Color.DarkGray, fontSize = 10.sp)
                } else {
                    Icon(
                        Icons.Default.Movie,
                        contentDescription = "Project icon representation",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                // FPS & Ratio indicator pill
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "${project.fps} FPS",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Project Details
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = project.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                
                Text(
                    text = "${project.width} x ${project.height} px",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateString,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete project",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCreationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, Int, Int, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("My Masterpiece") }
    var width by remember { mutableStateOf("1080") }
    var height by remember { mutableStateOf("1920") }
    var fps by remember { mutableStateOf(24f) }
    var transparent by remember { mutableStateOf(false) }

    // Swatches selection
    val colorSwatches = listOf(
        0xFFFFFFFF.toInt(), // White
        0xFF0D0E15.toInt(), // Pitch Dark Space
        0xFFFFFCF4.toInt(), // Retro Cream
        0xFFBD93F9.toInt(), // Pastel Purple
        0xFF8BE9FD.toInt()  // Cyan Tint
    )
    var selectedColor by remember { mutableStateOf(colorSwatches[1]) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                "Animation Canvas Wizard",
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 20.sp
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Title") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // Presets Layout Choice
                Text("Popular Platform Presets", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        Triple("YouTube 16:9", "1920", "1080"),
                        Triple("TikTok 9:16", "1080", "1920"),
                        Triple("Instagram 1:1", "1080", "1080"),
                        Triple("Cinematic 4K", "3840", "2160"),
                        Triple("Retro Pixel", "128", "128")
                    )
                    presets.forEach { (label, w, h) ->
                        OutlinedButton(
                            onClick = {
                                width = w
                                height = h
                            },
                            border = BorderStroke(
                                1.dp,
                                if (width == w && height == h) MaterialTheme.colorScheme.primary else Color(0xFF2E314D)
                            )
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = width,
                        onValueChange = { width = it },
                        label = { Text("Width (px)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Height (px)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // FPS slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Framerate (FPS)", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${fps.toInt()} FPS", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
                }
                Slider(
                    value = fps,
                    onValueChange = { fps = it },
                    valueRange = 12f..120f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Background swatches selector
                Text(
                    "Canvas Background Color",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorSwatches.forEach { colorVal ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    3.dp,
                                    if (selectedColor == colorVal && !transparent) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    selectedColor = colorVal
                                    transparent = false
                                }
                        )
                    }

                    // Transparent toggle button
                    IconButton(
                        onClick = { transparent = !transparent },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (transparent) MaterialTheme.colorScheme.secondary else Color.DarkGray,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.GridOn,
                            contentDescription = "Transparency grid",
                            tint = if (transparent) Color.Black else Color.White
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel", color = Color.LightGray)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wInt = width.toIntOrNull() ?: 1080
                    val hInt = height.toIntOrNull() ?: 1920
                    onConfirm(name, wInt, hInt, fps.toInt(), selectedColor, transparent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Create Canvas", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}
