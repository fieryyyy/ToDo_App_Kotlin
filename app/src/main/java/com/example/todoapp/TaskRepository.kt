// TaskRepository.kt
// Repository class for managing task data operations.
package com.example.todoapp

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class TaskRepository(private val taskDao: TaskDao) {
    // LiveData for observing all tasks
    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()
    // LiveData for observing trashed tasks
    val trashedTasks: LiveData<List<Task>> = taskDao.getTrashedTasks()

    // Insert a new task
    @WorkerThread
    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    // Update an existing task
    @WorkerThread
    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    // Permanently delete all trashed tasks
    @WorkerThread
    suspend fun emptyTrash() {
        // Fetch trashed tasks and delete each one
        val trashedTasksList = taskDao.getTrashedTasksList()
        trashedTasksList.forEach { taskDao.deleteTask(it) }
    }
}
