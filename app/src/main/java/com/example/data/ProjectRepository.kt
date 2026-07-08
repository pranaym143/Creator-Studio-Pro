package com.example.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: Long): Project? {
        return projectDao.getProjectById(id)
    }

    suspend fun insertProject(project: Project): Long {
        return projectDao.insertProject(project)
    }

    suspend fun updateProject(project: Project) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: Project) {
        projectDao.deleteProject(project)
    }

    suspend fun deleteProjectWithFrames(projectId: Long) {
        projectDao.deleteProjectWithFrames(projectId)
    }

    suspend fun createProjectWithFrames(project: Project, frameCount: Int): Long {
        return projectDao.createProjectWithFrames(project, frameCount)
    }

    fun getFramesForProject(projectId: Long): Flow<List<Frame>> {
        return projectDao.getFramesForProject(projectId)
    }

    suspend fun getFramesForProjectSync(projectId: Long): List<Frame> {
        return projectDao.getFramesForProjectSync(projectId)
    }

    suspend fun insertFrame(frame: Frame): Long {
        return projectDao.insertFrame(frame)
    }

    suspend fun updateFrame(frame: Frame) {
        projectDao.updateFrame(frame)
    }

    suspend fun deleteFrame(frame: Frame) {
        projectDao.deleteFrame(frame)
    }

    suspend fun saveAllFrames(frames: List<Frame>) {
        projectDao.insertFrames(frames)
    }
}
