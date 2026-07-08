package com.example.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ProjectViewModel"
    private val repository: ProjectRepository

    // Database Flows
    val allProjects: StateFlow<List<Project>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProjectRepository(database.projectDao())
        allProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Active Project & Frame State
    var activeProject by mutableStateOf<Project?>(null)
        private set

    var framesList by mutableStateOf<List<Frame>>(emptyList())
        private set

    var activeFrameIndex by mutableStateOf(0)
        private set

    // Vector drawing state for the currently visible frame
    var currentStrokes by mutableStateOf<List<DrawingStroke>>(emptyList())
        private set

    // Layer Management
    data class Layer(
        val id: Int,
        val name: String,
        val isVisible: Boolean = true,
        val isLocked: Boolean = false,
        val opacity: Float = 1.0f
    )

    var layersList by mutableStateOf<List<Layer>>(
        listOf(
            Layer(id = 1, name = "Background Layer"),
            Layer(id = 2, name = "Line Art Layer")
        )
    )
        private set

    var activeLayerId by mutableStateOf(2)
        private set

    // Undo/Redo dual-stack engine
    private val undoStack = mutableListOf<List<DrawingStroke>>()
    private val redoStack = mutableListOf<List<DrawingStroke>>()

    // Toolbar settings
    var currentTool by mutableStateOf("BRUSH") // BRUSH, ERASER, PENCIL, NEON, SHAPE_RECT, SHAPE_CIRCLE, SPARKLE
    var brushColor by mutableStateOf(0xFFBD93F9.toInt()) // Neon Purple
    var brushSize by mutableStateOf(12f)
    var brushOpacity by mutableStateOf(1f)

    // Animation Playback Engine
    var isPlaying by mutableStateOf(false)
        private set
    private var playbackJob: Job? = null
    var onionSkinEnabled by mutableStateOf(true)

    // AI Assistant state
    var isAiGenerating by mutableStateOf(false)
        private set

    var aiPalette by mutableStateOf<List<GeminiService.ColorToken>>(emptyList())
        private set

    var aiStoryboardSteps by mutableStateOf<List<GeminiService.StoryboardStep>>(emptyList())
        private set

    var aiChatHistory by mutableStateOf<List<Pair<String, Boolean>>>(
        listOf("Hello! I am your AI Animation Assistant. Ask me to generate a palette, design a storyboard, or explain rigging curves!" to true)
    )
        private set

    // Auto-save control
    private var autoSaveJob: Job? = null

    init {
        startAutoSaveTimer()
    }

    fun startNewProject(name: String, width: Int, height: Int, fps: Int, bgColor: Int, isTransparent: Boolean) {
        viewModelScope.launch {
            val project = Project(
                name = name,
                width = width,
                height = height,
                fps = fps,
                backgroundColor = bgColor,
                isTransparent = isTransparent
            )
            val newId = repository.createProjectWithFrames(project, frameCount = 4)
            val savedProject = repository.getProjectById(newId)
            if (savedProject != null) {
                selectProject(savedProject)
            }
        }
    }

    fun selectProject(project: Project) {
        // Save old frame strokes if any before switching
        saveCurrentFrameStrokesSync()

        activeProject = project
        isPlaying = false
        playbackJob?.cancel()

        viewModelScope.launch {
            repository.getFramesForProject(project.id).collect { frames ->
                framesList = frames
                if (frames.isNotEmpty()) {
                    if (activeFrameIndex >= frames.size) {
                        activeFrameIndex = frames.size - 1
                    }
                    loadFrameStrokes(activeFrameIndex)
                }
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            if (activeProject?.id == project.id) {
                activeProject = null
                framesList = emptyList()
                currentStrokes = emptyList()
                undoStack.clear()
                redoStack.clear()
            }
            repository.deleteProjectWithFrames(project.id)
        }
    }

    fun selectFrame(index: Int) {
        if (index < 0 || index >= framesList.size) return
        saveCurrentFrameStrokesSync()
        activeFrameIndex = index
        loadFrameStrokes(index)
    }

    private fun loadFrameStrokes(index: Int) {
        if (index < 0 || index >= framesList.size) return
        val frame = framesList[index]
        val converter = Converters()
        currentStrokes = converter.toStrokesList(frame.strokesJson)
        undoStack.clear()
        redoStack.clear()
    }

    // Drawing Operations
    fun addStroke(points: List<DrawingPoint>) {
        val newStroke = DrawingStroke(
            id = UUID.randomUUID().toString(),
            points = points,
            color = if (currentTool == "ERASER") 0x00000000 else brushColor,
            size = brushSize,
            opacity = brushOpacity,
            toolType = currentTool
        )

        // Save current list state to undo stack
        undoStack.add(currentStrokes.toList())
        redoStack.clear()

        // Append new stroke routed to active layer
        // We inject activeLayerId into the stroke metadata by associating its ID inside standard parameters or tags
        // For simplicity, color high byte or metadata can represent layer. Or we append layer ID in toolType
        val layeredStroke = newStroke.copy(toolType = "${currentTool}_L${activeLayerId}")
        currentStrokes = currentStrokes + layeredStroke
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(currentStrokes.toList())
            currentStrokes = undoStack.removeAt(undoStack.size - 1)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(currentStrokes.toList())
            currentStrokes = redoStack.removeAt(redoStack.size - 1)
        }
    }

    fun clearCanvas() {
        undoStack.add(currentStrokes.toList())
        redoStack.clear()
        currentStrokes = emptyList()
    }

    // Frame Operations
    fun addNewFrame() {
        val project = activeProject ?: return
        viewModelScope.launch {
            val newFrame = Frame(
                projectId = project.id,
                sequenceIndex = framesList.size,
                strokesJson = "[]"
            )
            repository.insertFrame(newFrame)
        }
    }

    fun duplicateCurrentFrame() {
        val project = activeProject ?: return
        if (framesList.isEmpty()) return
        saveCurrentFrameStrokesSync()

        viewModelScope.launch {
            val oldFrame = framesList[activeFrameIndex]
            val newFrame = Frame(
                projectId = project.id,
                sequenceIndex = activeFrameIndex + 1,
                strokesJson = oldFrame.strokesJson
            )

            // Shift indices of subsequent frames
            val updatedFrames = framesList.map { f ->
                if (f.sequenceIndex > activeFrameIndex) {
                    f.copy(sequenceIndex = f.sequenceIndex + 1)
                } else f
            }

            repository.saveAllFrames(updatedFrames)
            repository.insertFrame(newFrame)
        }
    }

    fun deleteCurrentFrame() {
        val project = activeProject ?: return
        if (framesList.size <= 1) {
            clearCanvas()
            return
        }
        viewModelScope.launch {
            val targetFrame = framesList[activeFrameIndex]
            
            // Shift subsequent frames left
            val remainingFrames = framesList.filter { it.id != targetFrame.id }.mapIndexed { idx, f ->
                f.copy(sequenceIndex = idx)
            }

            repository.deleteFrame(targetFrame)
            repository.saveAllFrames(remainingFrames)

            if (activeFrameIndex >= remainingFrames.size) {
                activeFrameIndex = remainingFrames.size - 1
            }
            loadFrameStrokes(activeFrameIndex)
        }
    }

    fun toggleOnionSkin() {
        onionSkinEnabled = !onionSkinEnabled
    }

    // Playback Engine
    fun togglePlayback() {
        if (isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        val project = activeProject ?: return
        if (framesList.isEmpty()) return
        saveCurrentFrameStrokesSync()

        isPlaying = true
        val frameDelay = (1000 / project.fps).toLong()

        playbackJob = viewModelScope.launch(Dispatchers.Main) {
            var playIdx = activeFrameIndex
            while (isPlaying) {
                playIdx = (playIdx + 1) % framesList.size
                activeFrameIndex = playIdx
                loadFrameStrokes(playIdx)
                delay(frameDelay)
            }
        }
    }

    fun stopPlayback() {
        isPlaying = false
        playbackJob?.cancel()
    }

    // Layers Operations
    fun toggleLayerVisibility(layerId: Int) {
        layersList = layersList.map {
            if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
        }
    }

    fun toggleLayerLock(layerId: Int) {
        layersList = layersList.map {
            if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
        }
    }

    fun selectLayer(layerId: Int) {
        val layer = layersList.find { it.id == layerId }
        if (layer != null && !layer.isLocked) {
            activeLayerId = layerId
        }
    }

    fun addLayer() {
        val nextId = (layersList.maxOfOrNull { it.id } ?: 0) + 1
        val newLayer = Layer(id = nextId, name = "Layer $nextId")
        layersList = layersList + newLayer
        activeLayerId = nextId
    }

    fun deleteLayer(layerId: Int) {
        if (layersList.size <= 1) return
        layersList = layersList.filter { it.id != layerId }
        if (activeLayerId == layerId) {
            activeLayerId = layersList.first().id
        }
        // Remove strokes that belonged to that layer
        currentStrokes = currentStrokes.filter { !it.toolType.endsWith("_L$layerId") }
    }

    // Auto-save logic
    private fun startAutoSaveTimer() {
        autoSaveJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(5000) // Auto-save every 5 seconds as requested in Goals
                if (activeProject != null && !isPlaying) {
                    saveCurrentFrameStrokesSync()
                }
            }
        }
    }

    private fun saveCurrentFrameStrokesSync() {
        val project = activeProject ?: return
        if (framesList.isEmpty() || activeFrameIndex >= framesList.size) return
        val currentFrame = framesList[activeFrameIndex]
        val converter = Converters()
        val strokesJson = converter.fromStrokesList(currentStrokes)

        // Only save if it has changed to minimize DB wear
        if (currentFrame.strokesJson != strokesJson) {
            viewModelScope.launch(Dispatchers.IO) {
                val updatedFrame = currentFrame.copy(strokesJson = strokesJson)
                repository.updateFrame(updatedFrame)

                // Update project modified timestamp
                val updatedProj = project.copy(dateModified = System.currentTimeMillis())
                repository.updateProject(updatedProj)
            }
        }
    }

    // AI Assistant integrations using GeminiService
    fun generateColorPaletteFromAi(theme: String) {
        viewModelScope.launch {
            isAiGenerating = true
            aiPalette = emptyList()
            val colors = GeminiService.generateColorPalette(theme)
            if (colors.isNotEmpty()) {
                aiPalette = colors
                aiChatHistory = aiChatHistory + ("Generated color palette for visual theme '$theme' successfully! Check the active colors below." to false)
            } else {
                aiChatHistory = aiChatHistory + ("I'm sorry, I encountered an issue generating a palette. Double check your Gemini API key in the Secrets panel." to false)
            }
            isAiGenerating = false
        }
    }

    fun generateStoryboardFromAi(concept: String) {
        viewModelScope.launch {
            isAiGenerating = true
            aiStoryboardSteps = emptyList()
            val steps = GeminiService.generateStoryboard(concept)
            if (steps.isNotEmpty()) {
                aiStoryboardSteps = steps
                aiChatHistory = aiChatHistory + ("Storyboard for '$concept' is ready! I generated 5 sequential frames with creative directions for you." to false)
            } else {
                aiChatHistory = aiChatHistory + ("I had trouble storyboarding your concept. Make sure your API Key is inserted correctly!" to false)
            }
            isAiGenerating = false
        }
    }

    fun sendChatMessageToAi(message: String) {
        if (message.isBlank()) return
        aiChatHistory = aiChatHistory + (message to true)

        viewModelScope.launch {
            isAiGenerating = true
            val systemInstruction = "You are a professional 2D animator, storyboard designer, and digital artist. You assist users in writing scripts, defining rigs, designing spacing guides, and choosing brushes. Keep answers visual, concise, and full of pro animation tips."
            val reply = GeminiService.executePrompt(message, isJson = false, systemInstruction = systemInstruction)
            aiChatHistory = aiChatHistory + (reply to false)
            isAiGenerating = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
        playbackJob?.cancel()
    }
}
