// MainActivity.kt
// This file contains the main entry point of the application and sets up the UI using Jetpack Compose.
package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoapp.ui.theme.TodoAppTheme
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    // ViewModel instance for managing task data
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Display the main screen with task and trash bin tabs
                    MainScreen(taskViewModel = taskViewModel)
                }
            }
        }
    }
}

@Composable
// MainScreen displays the tab layout to switch between the Tasks screen and Trash Bin screen.
fun MainScreen(taskViewModel: TaskViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tasks", "Trash Bin")

    Column(modifier = Modifier.fillMaxSize()) {
        // TabRow for switching between Tasks and Trash Bin
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Display the appropriate screen based on the selected tab
        when (selectedTabIndex) {
            0 -> TaskScreen(taskViewModel = taskViewModel)
            1 -> TrashBinScreen(taskViewModel = taskViewModel)
        }
    }
}

@Composable
// TaskScreen displays the list of active tasks and the input field to add new tasks.
fun TaskScreen(taskViewModel: TaskViewModel) {
    val tasks by taskViewModel.allTasks.observeAsState(emptyList())
    var taskDescription by remember { mutableStateOf(TextFieldValue("")) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Input field and button to add or update a task
        TaskInput(
            taskDescription = taskDescription,
            onDescriptionChange = { taskDescription = it },
            onAddTask = {
                if (taskToEdit == null) {
                    // Insert new task
                    taskViewModel.insert(Task(description = taskDescription.text, creationDate = Date()))
                } else {
                    // Update existing task
                    taskViewModel.update(taskToEdit!!.copy(description = taskDescription.text))
                    taskToEdit = null
                }
                taskDescription = TextFieldValue("")
            },
            isEditing = taskToEdit != null
        )
        Spacer(modifier = Modifier.height(16.dp))
        // List of tasks
        TaskList(tasks = tasks, onDeleteTask = { taskViewModel.moveToTrash(it) }, onEditTask = { task ->
            taskDescription = TextFieldValue(task.description)
            taskToEdit = task
        })
    }
}

@Composable
// TaskInput provides an input field and a button to add or update a task.
fun TaskInput(
    taskDescription: TextFieldValue,
    onDescriptionChange: (TextFieldValue) -> Unit,
    onAddTask: () -> Unit,
    isEditing: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        // TextField for task description
        TextField(
            value = taskDescription,
            onValueChange = onDescriptionChange,
            modifier = Modifier.weight(1f),
            label = { Text(if (isEditing) "Edit Task" else "Task Description") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Button to add or update the task
        Button(onClick = onAddTask) {
            Text(if (isEditing) "Update Task" else "Add Task")
        }
    }
}

@Composable
// TaskList displays a list of tasks.
fun TaskList(tasks: List<Task>, onDeleteTask: (Task) -> Unit, onEditTask: (Task) -> Unit) {
    LazyColumn {
        items(tasks) { task ->
            // Display each task
            TaskItem(task = task, onDeleteTask = onDeleteTask, onEditTask = onEditTask)
        }
    }
}

@Composable
// TaskItem displays an individual task with options to edit or delete it.
fun TaskItem(task: Task, onDeleteTask: (Task) -> Unit, onEditTask: (Task) -> Unit) {
    val dateFormatter = remember {
        SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .shadow(2.dp, shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display task creation date
                Text(
                    text = "Created on: ${dateFormatter.format(task.creationDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Row {
                    // Button to edit the task
                    IconButton(onClick = { onEditTask(task) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Task")
                    }
                    // Button to delete the task
                    IconButton(onClick = { onDeleteTask(task) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Task")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Display task description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            task.deletionDate?.let {
                Spacer(modifier = Modifier.height(8.dp))
                // Display task deletion date if available
                Text(
                    text = "Deleted on: ${dateFormatter.format(it)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
// TrashBinScreen displays the list of deleted tasks and provides an option to empty the trash bin.
fun TrashBinScreen(taskViewModel: TaskViewModel) {
    val trashedTasks by taskViewModel.trashedTasks.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Trash Bin", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        // Button to empty the trash bin
        Button(onClick = { taskViewModel.emptyTrash() }) {
            Text("Empty Trash Bin")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // List of trashed tasks
        LazyColumn {
            items(trashedTasks) { task ->
                // Display each trashed task
                TrashItem(task = task, onRestoreTask = { taskViewModel.restoreTask(it) })
            }
        }
    }
}

@Composable
// TrashItem displays an individual trashed task with an option to restore it.
fun TrashItem(task: Task, onRestoreTask: (Task) -> Unit) {
    val dateFormatter = remember {
        SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .shadow(2.dp, shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display task deletion date
                Text(
                    text = "Deleted on: ${dateFormatter.format(task.deletionDate!!)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                // Button to restore the task
                IconButton(onClick = { onRestoreTask(task) }) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = "Restore Task")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Display task description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


