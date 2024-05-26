package com.example.todoapp

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE deletionDate IS NULL")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE deletionDate IS NOT NULL")
    fun getTrashedTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE deletionDate IS NOT NULL")
    suspend fun getTrashedTasksList(): List<Task>

    @Insert
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)
}
