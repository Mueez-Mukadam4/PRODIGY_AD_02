package com.example.todoapp
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var etTask: EditText
    private lateinit var btnAdd: Button
    private lateinit var rvTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private var tasks = mutableListOf<Task>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        etTask = findViewById(R.id.etTask)
        btnAdd = findViewById(R.id.btnAdd)
        rvTasks = findViewById(R.id.rvTasks)


        sharedPreferences = getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)

        loadTasks()
        rvTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(tasks, { task, isChecked ->
            updateTaskStatus(task, isChecked)
        }, { task ->
            deleteTask(task)
        })
        rvTasks.adapter = taskAdapter

        btnAdd.setOnClickListener {
            addTask()
        }
    }

    private fun addTask() {
        val taskName = etTask.text.toString().trim()
        if (taskName.isEmpty()) {
            Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
            return
        }

        tasks.add(Task(nextId, taskName, false))
        nextId++
        saveTasks()
        taskAdapter.updateTasks(tasks)
        etTask.text.clear()
        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
    }

    private fun updateTaskStatus(task: Task, isChecked: Boolean) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task.copy(isCompleted = isChecked)
            saveTasks()
            taskAdapter.updateTasks(tasks)
        }
    }

    private fun deleteTask(task: Task) {
        tasks.removeAll { it.id == task.id }
        saveTasks()
        taskAdapter.updateTasks(tasks)
        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
    }

    private fun saveTasks() {
        val gson = Gson()
        val json = gson.toJson(tasks)
        sharedPreferences.edit().putString("tasks", json).apply()
        sharedPreferences.edit().putInt("nextId", nextId).apply()
    }

    private fun loadTasks() {
        val gson = Gson()
        val json = sharedPreferences.getString("tasks", "[]")
        val type = object : TypeToken<MutableList<Task>>() {}.type
        tasks = gson.fromJson(json, type)

        nextId = sharedPreferences.getInt("nextId", 0)

        if (tasks.isEmpty() && nextId == 0) {

            tasks.add(Task(nextId++, "Welcome to To-Do App!", false))
            tasks.add(Task(nextId++, "Click Add to create new tasks", false))
            tasks.add(Task(nextId++, "Check the box to complete", false))
            tasks.add(Task(nextId++, "Click red delete button to remove", false))
            saveTasks()
        }
    }
}