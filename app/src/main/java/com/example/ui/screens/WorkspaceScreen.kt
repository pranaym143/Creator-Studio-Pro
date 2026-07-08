package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Converters
import com.example.ui.ProjectViewModel
import com.example.ui.components.DrawingCanvas
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.JavascriptInterface
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: ProjectViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project = viewModel.activeProject
    val frames = viewModel.framesList
    val activeFrameIdx = viewModel.activeFrameIndex
    val currentStrokes = viewModel.currentStrokes
    val layers = viewModel.layersList
    val activeLayerId = viewModel.activeLayerId

    var showExportDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }

    // Layers Drawer Toggle State
    var layersExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isWebMode by remember { mutableStateOf(true) } // Default to Web App Mode!
    var exportedHtmlContent by remember { mutableStateOf<String?>(null) }
    var showWebExportSuccessDialog by remember { mutableStateOf(false) }

    // Swatches definitions
    val swatches = listOf(
        Color(0xFFBD93F9), // Purple Neon
        Color(0xFF8BE9FD), // Cyan Neon
        Color(0xFFFF79C6), // Pink Neon
        Color(0xFF50FA7B), // Lime Green
        Color(0xFFF1FA8C), // Pastel Yellow
        Color(0xFFFFB86C), // Pastel Orange
        Color(0xFFFF5555), // Pastel Red
        Color(0xFFFFFFFF), // White
        Color(0xFF000000)  // Black
    )

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active project loaded. Return to main screen.", color = Color.White)
        }
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            // Immersive studio top bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isWebMode) "${project.name} (Web Suite)" else project.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWebMode) MaterialTheme.colorScheme.primary else Color.White
                        )
                        Text(
                            text = if (isWebMode) "HTML5 Engine & Live IDE Editor" else "${project.width}x${project.height} | ${project.fps} FPS | Frame ${activeFrameIdx + 1}/${frames.size}",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Switch between Native App & Web App Mode
                    Button(
                        onClick = { isWebMode = !isWebMode },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWebMode) MaterialTheme.colorScheme.primary else Color(0xFF2E314D)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isWebMode) Icons.Default.Language else Icons.Default.PhoneAndroid,
                            contentDescription = "Toggle workspace mode",
                            tint = if (isWebMode) Color.Black else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isWebMode) "Web Studio" else "Native Studio",
                            color = if (isWebMode) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isWebMode) {
                        // Undo with validation
                        IconButton(
                            onClick = { viewModel.undo() },
                            modifier = Modifier.testTag("undo_button")
                        ) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = "Undo stroke",
                                tint = Color.White
                            )
                        }

                        // Redo with validation
                        IconButton(
                            onClick = { viewModel.redo() },
                            modifier = Modifier.testTag("redo_button")
                        ) {
                            Icon(
                                Icons.Default.Redo,
                                contentDescription = "Redo stroke",
                                tint = Color.White
                            )
                        }

                        // Onion Skin toggle
                        IconButton(
                            onClick = { viewModel.toggleOnionSkin() },
                            modifier = Modifier.background(
                                if (viewModel.onionSkinEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                CircleShape
                            )
                        ) {
                            Icon(
                                Icons.Default.Layers,
                                contentDescription = "Toggle Onion Skin",
                                tint = if (viewModel.onionSkinEnabled) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }

                        // Export project trigger
                        Button(
                            onClick = { showExportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("export_project_button")
                        ) {
                            Icon(Icons.Default.IosShare, contentDescription = "Export", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isWebMode) {
                WebStudioWorkspace(
                    onExportHtml = { html ->
                        exportedHtmlContent = html
                        showWebExportSuccessDialog = true
                    },
                    showToast = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Background grid for canvas centering
            val previousFrame = if (activeFrameIdx > 0 && frames.size > activeFrameIdx - 1) frames[activeFrameIdx - 1] else null
            val nextFrame = if (activeFrameIdx < frames.size - 1 && frames.size > activeFrameIdx + 1) frames[activeFrameIdx + 1] else null

            // Immersive Canvas Component
            DrawingCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("drawing_canvas"),
                strokes = currentStrokes,
                onStrokeDraw = { points ->
                    viewModel.addStroke(points)
                },
                activeLayerId = activeLayerId,
                layersList = layers,
                onionSkinEnabled = viewModel.onionSkinEnabled,
                previousFrame = previousFrame,
                nextFrame = nextFrame,
                canvasColor = Color(project.backgroundColor),
                isTransparent = project.isTransparent
            )

            // LEFT FLOATING TOOLBAR
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF2E314D), RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val toolsList = listOf(
                        "BRUSH" to Icons.Default.Brush,
                        "PENCIL" to Icons.Default.Edit,
                        "NEON" to Icons.Default.Flare,
                        "SPARKLE" to Icons.Default.AutoAwesome,
                        "ERASER" to Icons.Default.CleaningServices,
                        "SHAPE_RECT" to Icons.Default.CropSquare,
                        "SHAPE_CIRCLE" to Icons.Default.RadioButtonUnchecked
                    )

                    toolsList.forEach { (toolName, icon) ->
                        val isSelected = viewModel.currentTool == toolName
                        IconButton(
                            onClick = { viewModel.currentTool = toolName },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .testTag("tool_${toolName.lowercase()}")
                        ) {
                            Icon(
                                icon,
                                contentDescription = toolName,
                                tint = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Divider(color = Color(0xFF2E314D), modifier = Modifier.width(28.dp))

                    // Color picker launcher icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(viewModel.brushColor))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { showColorPickerDialog = true }
                    )
                }
            }

            // FLOATING TOOL CONFIGURATION PANEL (Top overlay)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF2E314D), RoundedCornerShape(12.dp))
                    .width(320.dp)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Size: ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                        Slider(
                            value = viewModel.brushSize,
                            onValueChange = { viewModel.brushSize = it },
                            valueRange = 2f..100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                        )
                        Text("${viewModel.brushSize.toInt()}px", color = Color.White, fontSize = 11.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                    }

                    // Opacity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Alpha: ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                        Slider(
                            value = viewModel.brushOpacity,
                            onValueChange = { viewModel.brushOpacity = it },
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                        )
                        Text("${(viewModel.brushOpacity * 100).toInt()}%", color = Color.White, fontSize = 11.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                    }

                    // Swatches scrolling row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        swatches.forEach { swatch ->
                            val isSelected = viewModel.brushColor == swatch.toArgb()
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(swatch)
                                    .border(
                                        2.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { viewModel.brushColor = swatch.toArgb() }
                            )
                        }
                    }
                }
            }

            // RIGHT FLOATING LAYERS BUTTON / DRAWER
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    // Floating triggers
                    IconButton(
                        onClick = { layersExpanded = !layersExpanded },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF2E314D), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            Icons.Default.FlipToFront,
                            contentDescription = "Expand Layers Panel",
                            tint = if (layersExpanded) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = layersExpanded,
                        enter = slideInHorizontally { it } + fadeIn(),
                        exit = slideOutHorizontally { it } + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .height(240.dp)
                                .testTag("layers_panel"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                            border = BorderStroke(1.dp, Color(0xFF2E314D)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Layers", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    IconButton(
                                        onClick = { viewModel.addLayer() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Layer", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Divider(color = Color(0xFF2E314D), modifier = Modifier.padding(vertical = 4.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    layers.reversed().forEach { layer ->
                                        val isActive = layer.id == activeLayerId
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                                .border(
                                                    1.dp,
                                                    if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable { viewModel.selectLayer(layer.id) }
                                                .padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = layer.name,
                                                fontSize = 11.sp,
                                                color = if (isActive) Color.White else Color.LightGray,
                                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1
                                            )

                                            // Visibility Switch
                                            IconButton(
                                                onClick = { viewModel.toggleLayerVisibility(layer.id) },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(
                                                    if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle Visibility",
                                                    tint = if (layer.isVisible) MaterialTheme.colorScheme.secondary else Color.Gray,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }

                                            // Lock Switch
                                            IconButton(
                                                onClick = { viewModel.toggleLayerLock(layer.id) },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(
                                                    if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                                    contentDescription = "Toggle Lock",
                                                    tint = if (layer.isLocked) MaterialTheme.colorScheme.error else Color.Gray,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }

                                            // Delete secondary layer
                                            if (layers.size > 1) {
                                                IconButton(
                                                    onClick = { viewModel.deleteLayer(layer.id) },
                                                    modifier = Modifier.size(18.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete layer",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(12.dp)
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

            // BOTTOM TIMELINE PANEL
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .border(BorderStroke(1.dp, Color(0xFF2E314D)))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Timeline Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause loop preview
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.togglePlayback() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (viewModel.isPlaying) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .testTag("play_pause_button")
                            ) {
                                Icon(
                                    if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play loop animation",
                                    tint = Color.Black
                                )
                            }

                            // Quick Index Nav
                            IconButton(
                                onClick = { viewModel.selectFrame(activeFrameIdx - 1) },
                                enabled = activeFrameIdx > 0,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev frame", tint = if (activeFrameIdx > 0) Color.White else Color.Gray)
                            }

                            IconButton(
                                onClick = { viewModel.selectFrame(activeFrameIdx + 1) },
                                enabled = activeFrameIdx < frames.size - 1,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next frame", tint = if (activeFrameIdx < frames.size - 1) Color.White else Color.Gray)
                            }
                        }

                        // Framing operations
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.addNewFrame() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E314D)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Frame", tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Frame", color = Color.White, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { viewModel.duplicateCurrentFrame() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E314D)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Frame", tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Clone", color = Color.White, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { viewModel.deleteCurrentFrame() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E314D)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Frame", tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Delete", color = Color.White, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { viewModel.clearCanvas() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Clear", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scrolling list of frame thumbnails (The timeline bar)
                    val timelineListState = rememberLazyListState()
                    LazyRow(
                        state = timelineListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(frames) { idx, frame ->
                            val isActive = idx == activeFrameIdx
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.DarkGray.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        2.dp,
                                        if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF2E314D),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectFrame(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                    // Visual line indicator to represent strokes count
                                    val count = remember(frame.strokesJson) {
                                        Converters().toStrokesList(frame.strokesJson).size
                                    }
                                    if (count > 0) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            repeat(minOf(count, 4)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.secondary)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } // end of bottom timeline Box
            } // end of isWebMode else
        } // end of outer Box
    } // end of Scaffold

    // Web Standalone HTML Export Success Dialog
    if (showWebExportSuccessDialog && exportedHtmlContent != null) {
        AlertDialog(
            onDismissRequest = { showWebExportSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = "Web", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Web Standalone App Ready!", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "We have compiled your full timeline of animation frames, layers, CSS styles, and drawing engines into a single standalone HTML5 page.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFF0D0E15), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2E314D), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = (exportedHtmlContent ?: "").take(200) + "...\n\n/* Standalone Animation Player */",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFF50FA7B)
                        )
                    }

                    Text(
                        text = "Click 'Copy Web Code' below to copy the code. You can paste it into an index.html file or upload it directly to make your animation active on any device!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(exportedHtmlContent ?: ""))
                            Toast.makeText(context, "Standalone Web App Code copied to clipboard!", Toast.LENGTH_LONG).show()
                            showWebExportSuccessDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Web Code", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = { showWebExportSuccessDialog = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dismiss", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        )
    }

    // Custom Color picker alert dialog
    if (showColorPickerDialog) {
        AlertDialog(
            onDismissRequest = { showColorPickerDialog = false },
            title = { Text("Studio Color Canvas", color = Color.White) },
            containerColor = MaterialTheme.colorScheme.surface,
            text = {
                Column {
                    Text("Select a color palette tint or pick custom values.", fontSize = 12.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    // RGB Color Mix Sliders
                    var r by remember { mutableStateOf(Color(viewModel.brushColor).red) }
                    var g by remember { mutableStateOf(Color(viewModel.brushColor).green) }
                    var b by remember { mutableStateOf(Color(viewModel.brushColor).blue) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("R: ", color = Color.White, modifier = Modifier.width(20.dp))
                        Slider(value = r, onValueChange = { r = it }, modifier = Modifier.weight(1f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("G: ", color = Color.White, modifier = Modifier.width(20.dp))
                        Slider(value = g, onValueChange = { g = it }, modifier = Modifier.weight(1f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("B: ", color = Color.White, modifier = Modifier.width(20.dp))
                        Slider(value = b, onValueChange = { b = it }, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live review swatch
                    val finalCol = Color(red = r, green = g, blue = b)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(finalCol),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ACTIVE PREVIEW", fontWeight = FontWeight.Bold, color = if (r+g+b > 1.5f) Color.Black else Color.White, fontSize = 12.sp)
                    }

                    // Save choice helper
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.brushColor = finalCol.toArgb()
                            showColorPickerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Lock Color Selection", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Export Animation Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Render Animation Engine", fontWeight = FontWeight.Bold, color = Color.White) },
            containerColor = MaterialTheme.colorScheme.surface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select output formats to compile and render your vector canvas timeline.", fontSize = 12.sp, color = Color.LightGray)

                    listOf(
                        "MP4 High Definition Video" to "Render frames in 120 FPS high-contrast h.264 profile.",
                        "Animated GIF Sequence" to "Create looping останавливающий block-animations.",
                        "ZIP Image Sequence (PNG)" to "Lossless rendering of independent vector strokes.",
                        "PSD Layer Preservation Bundle" to "Preserve individual background / vector drawings."
                    ).forEach { (format, desc) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Render mock flow
                                    showExportDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E314D).copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, Color(0xFF2E314D))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(format, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text(desc, fontSize = 11.sp, color = Color.LightGray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close Panel")
                }
            }
        )
    }
}

@Composable
fun WebStudioWorkspace(
    modifier: Modifier = Modifier,
    onExportHtml: (String) -> Unit,
    showToast: (String) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    allowFileAccess = true
                    allowContentAccess = true
                }
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun showToast(message: String) {
                        showToast(message)
                    }

                    @JavascriptInterface
                    fun onExportComplete(htmlContent: String) {
                        onExportHtml(htmlContent)
                    }
                }, "Android")
                
                loadUrl("file:///android_asset/web_studio.html")
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
