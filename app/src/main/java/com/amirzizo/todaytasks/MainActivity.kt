package com.amirzizo.todaytasks

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.amirzizo.todaytasks.ui.theme.TodayTasksTheme
import org.json.JSONArray
import org.json.JSONObject

data class Task(
    val id: Long,
    val title: String,
    val completed: Boolean
)

class TaskRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("today_tasks_prefs", Context.MODE_PRIVATE)

    fun loadTasks(): List<Task> {
        val raw = prefs.getString("tasks", null) ?: return emptyList()
        val array = JSONArray(raw)
        val tasks = mutableListOf<Task>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            tasks.add(
                Task(
                    id = item.getLong("id"),
                    title = item.getString("title"),
                    completed = item.getBoolean("completed")
                )
            )
        }
        return tasks
    }

    fun saveTasks(tasks: List<Task>) {
        val array = JSONArray()
        tasks.forEach { task ->
            val item = JSONObject()
            item.put("id", task.id)
            item.put("title", task.title)
            item.put("completed", task.completed)
            array.put(item)
        }
        prefs.edit().putString("tasks", array.toString()).apply()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodayTasksTheme {
                TodayTasksScreen()
            }
        }
    }
}

@Composable
fun TodayTasksScreen() {
    val context = LocalContext.current
    val repository = remember { TaskRepository(context.applicationContext) }
    var tasks by remember { mutableStateOf(repository.loadTasks()) }
    var newTaskText by remember { mutableStateOf("") }

    LaunchedEffect(tasks) {
        repository.saveTasks(tasks)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("لیست کارهای امروز")
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("کار جدید...") },
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val trimmed = newTaskText.trim()
                            if (trimmed.isNotEmpty()) {
                                tasks = tasks + Task(
                                    id = System.currentTimeMillis(),
                                    title = trimmed,
                                    completed = false
                                )
                                newTaskText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("افزودن")
                    }
                }

                if (tasks.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "هنوز کاری اضافه نشده است. کارهای امروز را اینجا بنویس!",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = task.completed,
                                        onCheckedChange = { checked ->
                                            tasks = tasks.map { current ->
                                                if (current.id == task.id) {
                                                    current.copy(completed = checked)
                                                } else {
                                                    current
                                                }
                                            }
                                        }
                                    )

                                    Text(
                                        text = task.title,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                                        color = if (task.completed) {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )

                                    IconButton(
                                        onClick = {
                                            tasks = tasks.filterNot { it.id == task.id }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
