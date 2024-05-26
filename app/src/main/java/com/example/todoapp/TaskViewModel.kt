// TaskViewModel.kt
// ViewModel class for managing task data and business logic.

package com.example.todoapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Date

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    // Repository instance for accessing task data
    private val repository: TaskRepository
    // LiveData for observing all tasks
    val allTasks: LiveData<List<Task>>
    // LiveData for observing trashed tasks
    val trashedTasks: LiveData<List<Task>>

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        allTasks = repository.allTasks
        trashedTasks = repository.trashedTasks
    }

    // Insert a new task
    fun insert(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }

    // Update an existing task
    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    // Move a task to the trash
    fun moveToTrash(task: Task) = viewModelScope.launch {
        repository.update(task.copy(deletionDate = Date()))
    }

    // Restore a task from the trash
    fun restoreTask(task: Task) = viewModelScope.launch {
        repository.update(task.copy(deletionDate = null))
    }

    // Permanently delete all trashed tasks
    fun emptyTrash() = viewModelScope.launch {
        repository.emptyTrash()
    }
}
