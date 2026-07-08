package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    // Project operations
    @Query("SELECT * FROM projects ORDER BY dateModified DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    // Frame operations
    @Query("SELECT * FROM frames WHERE projectId = :projectId ORDER BY sequenceIndex ASC")
    fun getFramesForProject(projectId: Long): Flow<List<Frame>>

    @Query("SELECT * FROM frames WHERE projectId = :projectId ORDER BY sequenceIndex ASC")
    suspend fun getFramesForProjectSync(projectId: Long): List<Frame>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrame(frame: Frame): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrames(frames: List<Frame>)

    @Update
    suspend fun updateFrame(frame: Frame)

    @Delete
    suspend fun deleteFrame(frame: Frame)

    @Query("DELETE FROM frames WHERE projectId = :projectId")
    suspend fun deleteFramesForProject(projectId: Long)

    @Query("DELETE FROM frames WHERE projectId = :projectId AND sequenceIndex = :sequenceIndex")
    suspend fun deleteFrameBySequence(projectId: Long, sequenceIndex: Int)

    @Transaction
    suspend fun deleteProjectWithFrames(projectId: Long) {
        deleteFramesForProject(projectId)
        deleteProjectById(projectId)
    }

    @Transaction
    suspend fun createProjectWithFrames(project: Project, frameCount: Int): Long {
        val projectId = insertProject(project)
        val initialFrames = (0 until frameCount).map { i ->
            Frame(projectId = projectId, sequenceIndex = i, strokesJson = "[]")
        }
        insertFrames(initialFrames)
        return projectId
    }
}
