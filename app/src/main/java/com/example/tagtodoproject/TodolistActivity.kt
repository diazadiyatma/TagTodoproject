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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodolistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Ambil data kategori dari Intent
        categoryName = intent.getStringExtra("CATEGORY_NAME")
        binding.tvCategoryTitle.text = "To-Do List: $categoryName"

        // Tampilkan gambar kategori
        setCategoryImage(categoryName)

        // Setup RecyclerView
        adapter = TaskAdapter(
            tasks,
            onEditClick = { task, position -> showEditDialog(task, position) },
            onDeleteClick = { position ->
                val taskId = tasks[position].id
                deleteTaskFromFirestore(taskId)
                tasks.removeAt(position)
                adapter.notifyItemRemoved(position)
            },
            onCheckboxClick = { task, position ->
                tasks[position] = task.copy(isCompleted = !task.isCompleted)
                updateTaskInFirestore(tasks[position])
                adapter.notifyItemChanged(position)
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter

        // Load tugas dari Firestore berdasarkan kategori
        loadTasksFromFirestore()

        // Pilih Tanggal
        binding.btnPickDate.setOnClickListener {
            showDatePicker()
        }

        // Tambah Task Baru
        binding.btnAddTask.setOnClickListener {
            val taskName = binding.etTaskName.text.toString()
            val taskTags = binding.etTaskTags.text.toString()
            val taskDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)

            if (taskName.isNotEmpty() && taskTags.isNotEmpty()) {
                val newTask = Task(
                    id = tasks.size + 1,
                    name = taskName,
                    date = taskDate,
                    tags = taskTags,
                    category = categoryName ?: "Unknown",
                    isCompleted = false
                )
                Log.d("TodolistActivity", "Menambahkan task baru: $newTask")
                saveTaskToFirestore(newTask)
                tasks.add(newTask)
                adapter.notifyItemInserted(tasks.size - 1)
                binding.etTaskName.text.clear()
                binding.etTaskTags.text.clear()
            } else {
                Log.e("TodolistActivity", "Input tidak valid: taskName=$taskName, taskTags=$taskTags")
                Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menampilkan gambar kategori
    private fun setCategoryImage(categoryName: String?) {
        when (categoryName?.lowercase()) {
            "school" -> binding.ivCategoryImage.setImageResource(R.drawable.sekulah)
            "work" -> binding.ivCategoryImage.setImageResource(R.drawable.rodi)
            "exercise" -> binding.ivCategoryImage.setImageResource(R.drawable.gym)
            "home" -> binding.ivCategoryImage.setImageResource(R.drawable.rumah)
            else -> binding.ivCategoryImage.setImageResource(R.drawable.ic_default_image) // Gambar default
        }
    }

    // Fungsi untuk menyimpan tugas ke Firestore
    private fun saveTaskToFirestore(task: Task) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e("TodolistActivity", "User belum login! Tidak bisa menyimpan task.")
            return
        }

        Log.d("TodolistActivity", "Menyimpan task untuk userId: $userId")

        firestore.collection("users")
            .document(userId)
            .collection("tasks")
            .document(task.id.toString())
            .set(task)
            .addOnSuccessListener {
                Log.d("TodolistActivity", "Task berhasil disimpan: ${task.name}")
                Toast.makeText(this, "Tugas disimpan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("TodolistActivity", "Gagal menyimpan task: ${e.message}")
                Toast.makeText(this, "Gagal menyimpan tugas!", Toast.LENGTH_SHORT).show()
            }
    }


    // Fungsi untuk memuat tugas dari Firestore
    private fun loadTasksFromFirestore() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e("TodolistActivity", "User belum login! Tidak bisa memuat task.")
            return
        }

        Log.d("TodolistActivity", "Memuat task untuk userId: $userId")

        firestore.collection("users")
            .document(userId)
            .collection("tasks")
            .whereEqualTo("category", categoryName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TodolistActivity", "Gagal memuat task: ${error.message}")
                    Toast.makeText(this, "Gagal memuat tugas!", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                tasks.clear()
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val task = document.toObject(Task::class.java)
                        if (task != null) {
                            tasks.add(task)
                            Log.d("TodolistActivity", "Task dimuat: ${task.name}")
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }


    // Fungsi untuk memperbarui tugas di Firestore
    private fun updateTaskInFirestore(task: Task) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e("TodolistActivity", "User belum login! Tidak bisa memperbarui task.")
            return
        }

        Log.d("TodolistActivity", "Memperbarui task untuk userId: $userId, Task ID: ${task.id}")

        firestore.collection("users")
            .document(userId)
            .collection("tasks")
            .document(task.id.toString())
            .set(task)
            .addOnSuccessListener {
                Log.d("TodolistActivity", "Task berhasil diupdate: ${task.name}")
            }
            .addOnFailureListener { e ->
                Log.e("TodolistActivity", "Gagal mengupdate task: ${e.message}")
            }
    }


    // Fungsi untuk menghapus tugas dari Firestore
    private fun deleteTaskFromFirestore(taskId: Int) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.e("TodolistActivity", "User belum login! Tidak bisa menghapus task.")
            return
        }

        Log.d("TodolistActivity", "Menghapus task untuk userId: $userId, Task ID: $taskId")

        firestore.collection("users")
            .document(userId)
            .collection("tasks")
            .document(taskId.toString())
            .delete()
            .addOnSuccessListener {
                Log.d("TodolistActivity", "Task berhasil dihapus: $taskId")
            }
            .addOnFailureListener { e ->
                Log.e("TodolistActivity", "Gagal menghapus task: ${e.message}")
            }
    }


    // Fungsi untuk menampilkan DatePicker
    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                binding.btnPickDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // Fungsi untuk menampilkan dialog edit tugas
    private fun showEditDialog(task: Task, position: Int) {
        val dialogBinding = DialogEditTaskBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Edit Tugas")
            .setPositiveButton("Simpan") { _, _ ->
                val newName = dialogBinding.etEditTaskName.text.toString()
                val newTags = dialogBinding.etEditTaskTags.text.toString()
                if (newName.isNotEmpty() && newTags.isNotEmpty()) {
                    tasks[position] = task.copy(name = newName, tags = newTags)
                    Log.d("TodolistActivity", "Task diupdate: ${tasks[position]}")
                    updateTaskInFirestore(tasks[position])
                    adapter.notifyItemChanged(position)
                } else {
                    Log.e("TodolistActivity", "Input tidak valid: newName=$newName, newTags=$newTags")
                    Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .create()

        // Isi data lama ke dialog
        dialogBinding.etEditTaskName.setText(task.name)
        dialogBinding.etEditTaskTags.setText(task.tags)

        dialog.show()
    }
}