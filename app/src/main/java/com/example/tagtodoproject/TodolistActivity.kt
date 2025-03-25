package com.example.tagtodoproject

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tagtodoproject.databinding.ActivityTodolistBinding
import com.example.tagtodoproject.databinding.DialogEditTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TodolistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTodolistBinding
    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter
    private val calendar = Calendar.getInstance()
    private var categoryName: String? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodolistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        categoryName = intent.getStringExtra("CATEGORY_NAME")
        binding.tvCategoryTitle.text = "To-Do List: $categoryName"
        setCategoryImage(categoryName)

        adapter = TaskAdapter(
            tasks,
            onEditClick = { task, position -> showEditDialog(task, position) },
            onDeleteClick = { position ->
                val task = tasks[position]
                if (task.documentId.isNotBlank()) {
                    deleteTaskFromFirestore(task.documentId)
                }
            },
            onCheckboxClick = { task, position ->
                // Immediately update UI
                val newState = !task.isCompleted
                tasks[position] = task.copy(isCompleted = newState).apply {
                    documentId = task.documentId
                }
                adapter.notifyItemChanged(position)

                // Update Firestore
                updateTaskCompletion(task.documentId, newState)
            }
        )

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter

        binding.btnPickDate.setOnClickListener { showDatePicker() }

        binding.btnAddTask.setOnClickListener {
            val taskName = binding.etTaskName.text.toString()
            val taskTags = binding.etTaskTags.text.toString()
            val taskDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)

            if (taskName.isNotEmpty() && taskTags.isNotEmpty()) {
                val newTask = Task(
                    name = taskName,
                    date = taskDate,
                    tags = taskTags,
                    category = categoryName ?: "Unknown",
                    isCompleted = false
                )
                addTaskToFirestore(newTask)
            } else {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        loadTasksFromFirestore()
    }

    override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
    }

    private fun setCategoryImage(categoryName: String?) {
        when (categoryName?.lowercase()) {
            "school" -> binding.ivCategoryImage.setImageResource(R.drawable.sekulah)
            "work" -> binding.ivCategoryImage.setImageResource(R.drawable.rodi)
            "exercise" -> binding.ivCategoryImage.setImageResource(R.drawable.gym)
            "home" -> binding.ivCategoryImage.setImageResource(R.drawable.rumah)
            else -> binding.ivCategoryImage.setImageResource(R.drawable.ic_default_image)
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                binding.btnPickDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showEditDialog(task: Task, position: Int) {
        val dialogBinding = DialogEditTaskBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val updatedName = dialogBinding.etEditTaskName.text.toString()
                val updatedTags = dialogBinding.etEditTaskTags.text.toString()
                val updatedTask = task.copy(
                    name = updatedName,
                    tags = updatedTags
                ).apply {
                    documentId = task.documentId
                    isCompleted = task.isCompleted
                }
                tasks[position] = updatedTask
                updateTaskInFirestore(updatedTask)
                adapter.notifyItemChanged(position)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .apply {
                dialogBinding.etEditTaskName.setText(task.name)
                dialogBinding.etEditTaskTags.setText(task.tags)
                show()
            }
    }

    private fun loadTasksFromFirestore() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        snapshotListener = firestore.collection("users").document(userId)
            .collection("tasks")
            .whereEqualTo("category", categoryName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TodolistActivity", "Error loading tasks", error)
                    return@addSnapshotListener
                }

                tasks.clear()
                snapshot?.documents?.forEach { document ->
                    document.toObject(Task::class.java)?.apply {
                        documentId = document.id
                    }?.let { tasks.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun addTaskToFirestore(task: Task) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("tasks")
            .add(task.toMap())
            .addOnSuccessListener { documentReference ->
                Log.d("TodolistActivity", "Task added with ID: ${documentReference.id}")
                binding.etTaskName.text.clear()
                binding.etTaskTags.text.clear()
            }
            .addOnFailureListener { e ->
                Log.e("TodolistActivity", "Error adding task", e)
                Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTaskCompletion(documentId: String, isCompleted: Boolean) {
        if (documentId.isBlank()) return

        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("tasks").document(documentId)
            .update("isCompleted", isCompleted)
            .addOnSuccessListener {
                Log.d("TodolistActivity", "Completion updated for $documentId")
            }
            .addOnFailureListener { e ->
                Log.e("TodolistActivity", "Error updating completion", e)
                // Revert UI change if update fails
                tasks.find { it.documentId == documentId }?.let { task ->
                    task.isCompleted = !isCompleted
                    adapter.notifyItemChanged(tasks.indexOf(task))
                }
            }
    }

    private fun updateTaskInFirestore(task: Task) {
        if (task.documentId.isBlank()) return

        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("tasks").document(task.documentId)
            .set(task.toMap())
            .addOnSuccessListener {
                Log.d("TodolistActivity", "Task updated: ${task.documentId}")
            }
            .addOnFailureListener { e ->
                Log.e("TodolistActivity", "Error updating task", e)
            }
    }

    private fun deleteTaskFromFirestore(documentId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("tasks").document(documentId)
            .delete()
            .addOnSuccessListener {
                Log.d("TodolistActivity", "Task deleted: $documentId")
            }
            .addOnFailureListener { e ->
                Log.e("TodolistActivity", "Error deleting task", e)
            }
    }
}
