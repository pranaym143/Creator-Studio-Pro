package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiLabScreen(
    viewModel: ProjectViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("AI Studio Chat", "AI Palette Generator", "AI Storyboarder")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "AI icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Next-Gen AI Laboratory",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Tab bar switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Tab content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> AiChatTab(viewModel)
                    1 -> AiPaletteTab(viewModel)
                    2 -> AiStoryboardTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun AiChatTab(viewModel: ProjectViewModel) {
    var chatInput by remember { mutableStateOf("") }
    val chatHistory = viewModel.aiChatHistory

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat History list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color(0xFF2E314D), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatHistory) { (text, isUser) ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFF2E314D),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isUser) 12.dp else 0.dp,
                                bottomEnd = if (isUser) 0.dp else 12.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = text,
                                color = if (isUser) Color.Black else Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                if (viewModel.isAiGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini is thinking...", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Ask about rigs, spacing, lip sync...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF2E314D)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    viewModel.sendChatMessageToAi(chatInput)
                    chatInput = ""
                },
                enabled = chatInput.isNotBlank() && !viewModel.isAiGenerating,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (chatInput.isNotBlank()) MaterialTheme.colorScheme.primary else Color.DarkGray,
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = if (chatInput.isNotBlank()) Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
fun AiPaletteTab(viewModel: ProjectViewModel) {
    var themeInput by remember { mutableStateOf("Cyberpunk Tokyo Neon") }
    val palette = viewModel.aiPalette

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "AI Palette Architect",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "Enter a theme keyword, and the Gemini API will generate a matching set of 5 professional color tokens. Tap any generated color to set your brush active color immediately!",
            fontSize = 12.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = themeInput,
                onValueChange = { themeInput = it },
                label = { Text("Theme keyword (e.g. Sunset Lagoon)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { viewModel.generateColorPaletteFromAi(themeInput) },
                enabled = themeInput.isNotBlank() && !viewModel.isAiGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(52.dp)
            ) {
                if (viewModel.isAiGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                } else {
                    Text("Architect", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (palette.isEmpty() && !viewModel.isAiGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No palette generated yet. Enter a concept above and press 'Architect'.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        } else {
            // Render beautiful color cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                palette.forEach { token ->
                    val colorVal = try {
                        Color(android.graphics.Color.parseColor(token.hex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    viewModel.brushColor = android.graphics.Color.parseColor(token.hex)
                                } catch (e: Exception) {}
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, Color(0xFF2E314D))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorVal)
                                    .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(token.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(token.hex, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(token.description, fontSize = 11.sp, color = Color.LightGray)
                            }

                            Icon(
                                Icons.Default.CheckCircleOutline,
                                contentDescription = "Active selection indicator",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiStoryboardTab(viewModel: ProjectViewModel) {
    var conceptInput by remember { mutableStateOf("A rocket ship launching into space and flying past the moon") }
    val steps = viewModel.aiStoryboardSteps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "AI Storyboard Architect",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "Describe your animation concept. Gemini will break it down into a 5-step keyframe storyboard with detailed sketching guidelines.",
            fontSize = 12.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = conceptInput,
                onValueChange = { conceptInput = it },
                label = { Text("Animation concept") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { viewModel.generateStoryboardFromAi(conceptInput) },
                enabled = conceptInput.isNotBlank() && !viewModel.isAiGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(52.dp)
            ) {
                if (viewModel.isAiGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                } else {
                    Text("Storyboard", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (steps.isEmpty() && !viewModel.isAiGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No storyboard generated yet. Enter a concept above and press 'Storyboard'.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        } else {
            // Render beautiful storyboard list
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                steps.forEach { step ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, Color(0xFF2E314D))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Frame #${step.stepIndex}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                Text(step.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }

                            Divider(color = Color(0xFF2E314D), modifier = Modifier.padding(vertical = 6.dp))

                            Text("Visual Narrative:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp)
                            Text(step.description, fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 6.dp))

                            Text("Vector Drawing Instructions:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 11.sp)
                            Text(step.drawingSuggestion, fontSize = 12.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}
