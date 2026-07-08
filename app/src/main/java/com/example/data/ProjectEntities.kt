package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val width: Int,
    val height: Int,
    val fps: Int = 24,
    val backgroundColor: Int = 0xFFFFFFFF.toInt(),
    val isTransparent: Boolean = false,
    val isFavorite: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val folder: String = "All",
    val thumbnailPath: String = ""
)

@Entity(tableName = "frames")
data class Frame(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val sequenceIndex: Int,
    val strokesJson: String = "[]"
)

// Auxiliary models for drawing engine
data class DrawingStroke(
    val id: String,
    val points: List<DrawingPoint>,
    val color: Int,
    val size: Float,
    val opacity: Float = 1.0f,
    val toolType: String = "BRUSH" // BRUSH, ERASER, PENCIL, NEON, SHAPE_RECT, SHAPE_CIRCLE, SPARKLE
)

data class DrawingPoint(
    val x: Float,
    val y: Float
)

// Room Type Converters for JSON serialization
class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val strokeListType = Types.newParameterizedType(List::class.java, DrawingStroke::class.java)
    private val strokeListAdapter = moshi.adapter<List<DrawingStroke>>(strokeListType)

    @TypeConverter
    fun fromStrokesList(strokes: List<DrawingStroke>?): String {
        if (strokes == null) return "[]"
        return strokeListAdapter.toJson(strokes)
    }

    @TypeConverter
    fun toStrokesList(json: String?): List<DrawingStroke> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            strokeListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
