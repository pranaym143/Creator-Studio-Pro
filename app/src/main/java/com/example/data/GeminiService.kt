package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Moshi JSON models for Gemini REST API
    data class GeminiRequest(
        val contents: List<GeminiContent>,
        val generationConfig: GeminiGenerationConfig? = null,
        val systemInstruction: GeminiContent? = null
    )

    data class GeminiContent(val parts: List<GeminiPart>)
    data class GeminiPart(val text: String)
    data class GeminiGenerationConfig(
        val responseMimeType: String? = null,
        val temperature: Float? = null
    )

    data class GeminiResponse(val candidates: List<GeminiCandidate>?)
    data class GeminiCandidate(val content: GeminiResponseContent?)
    data class GeminiResponseContent(val parts: List<GeminiPart>?)

    // App models parsed from Gemini responses
    data class ColorToken(val name: String, val hex: String, val description: String)
    data class StoryboardStep(val stepIndex: Int, val title: String, val description: String, val drawingSuggestion: String)

    /**
     * Executes raw prompt on Gemini 3.5 Flash
     */
    suspend fun executePrompt(prompt: String, isJson: Boolean = false, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API key is default or empty. Please enter real key in Secrets panel.")
            return@withContext "Error: Gemini API Key is missing. Please set your key in the AI Studio Secrets panel."
        }

        try {
            val reqParts = listOf(GeminiPart(text = prompt))
            val reqContent = GeminiContent(parts = reqParts)
            val config = if (isJson) GeminiGenerationConfig(responseMimeType = "application/json", temperature = 0.2f) else null
            
            val systemContent = systemInstruction?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            }

            val requestObj = GeminiRequest(
                contents = listOf(reqContent),
                generationConfig = config,
                systemInstruction = systemContent
            )

            val requestAdapter = moshi.adapter(GeminiRequest::class.java)
            val requestBodyJson = requestAdapter.toJson(requestObj)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed with code ${response.code}: $errBody")
                    return@withContext "Error: API call failed with code ${response.code}."
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext "Error: Received empty response from AI model."
                }

                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val responseObj = responseAdapter.fromJson(responseBody)
                val responseText = responseObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                return@withContext responseText ?: "Error: Could not extract content from AI response."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during prompt execution", e)
            return@withContext "Error: ${e.message ?: "Unknown network error"}"
        }
    }

    /**
     * Generates a designer color palette
     */
    suspend fun generateColorPalette(theme: String): List<ColorToken> {
        val prompt = """
            Generate an exceptionally professional, themed color palette containing exactly 5 cohesive colors based on the design prompt: "$theme".
            Format your response strictly as a JSON list of objects. Each object MUST have:
            - "name": A creative descriptive name for the color token (e.g. "Starlight Neon", "Midnight Nebula")
            - "hex": A valid 6-character hexadecimal color string starting with '#' (e.g. "#FF5E62")
            - "description": A short explanation of how the digital artist or animator should apply this color in the canvas scene.
            
            Return ONLY the valid JSON list. Do not surround with markdown block tick marks or other conversational text.
        """.trimIndent()

        val response = executePrompt(prompt, isJson = true)
        if (response.startsWith("Error")) return emptyList()

        return try {
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, ColorToken::class.java)
            val adapter = moshi.adapter<List<ColorToken>>(listType)
            adapter.fromJson(response) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse color palette JSON: $response", e)
            emptyList()
        }
    }

    /**
     * Generates storyboard steps
     */
    suspend fun generateStoryboard(concept: String): List<StoryboardStep> {
        val prompt = """
            Create a highly detailed, professional 5-frame visual animation storyboard based on the concept: "$concept".
            Each frame must represent a sequential keyframe step in the animation.
            Format your response strictly as a JSON list of objects. Each object MUST have:
            - "stepIndex": Integer from 1 to 5
            - "title": Title of the keyframe pose
            - "description": Complete visual narration of what is moving, rotating, or scaling in the frame.
            - "drawingSuggestion": Extremely specific instructions for vector lines, shapes, and colors that the drawing engine or user can paint on the canvas.
            
            Return ONLY the valid JSON list. Do not surround with markdown or any other characters.
        """.trimIndent()

        val response = executePrompt(prompt, isJson = true)
        if (response.startsWith("Error")) return emptyList()

        return try {
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, StoryboardStep::class.java)
            val adapter = moshi.adapter<List<StoryboardStep>>(listType)
            adapter.fromJson(response) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse storyboard JSON: $response", e)
            emptyList()
        }
    }
}
