package com.example.tagtodoproject

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tagtodoproject.databinding.ActivityCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set onClickListener untuk tombol "User"
        binding.btnUser.setOnClickListener {
            showUserPopupMenu(it)
        }

        // Set onClickListener untuk setiap ImageButton
        binding.ibCategory1.setOnClickListener {
            saveCategoryToFirebase("School")
            navigateToTodolistActivity("School")
        }

        binding.ibCategory2.setOnClickListener {
            saveCategoryToFirebase("Work")
            navigateToTodolistActivity("Work")
        }

        binding.ibCategory3.setOnClickListener {
            saveCategoryToFirebase("Exercise")
            navigateToTodolistActivity("Exercise")
        }

        binding.ibCategory4.setOnClickListener {
            saveCategoryToFirebase("Home")
            navigateToTodolistActivity("Home")
        }
    }

    // Fungsi untuk menyimpan kategori ke Firestore
    private fun saveCategoryToFirebase(categoryName: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        // Simpan kategori ke Firestore
        val categoryData = hashMapOf(
            "name" to categoryName,
            "userId" to userId
        )

        firestore.collection("categories")
            .document(categoryName)
            .set(categoryData)
            .addOnSuccessListener {
                Toast.makeText(this, "Kategori $categoryName disimpan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan kategori: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk menampilkan pop-up menu "User"
    private fun showUserPopupMenu(view: android.view.View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_user_info -> {
                    showUserInfo()
                    true
                }
                R.id.menu_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    // Fungsi untuk menampilkan informasi pengguna
    private fun showUserInfo() {
        val user = firebaseAuth.currentUser
        val userName = user?.displayName ?: "Unknown"
        val userEmail = user?.email ?: "Unknown"

        AlertDialog.Builder(this)
            .setTitle("User Information")
            .setMessage("Nama: $userName\nEmail: $userEmail")
            .setPositiveButton("OK", null)
            .show()
    }

    // Fungsi untuk logout
    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                firebaseAuth.signOut() // Logout dari Firebase
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Tutup CategoryActivity
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    // Fungsi untuk berpindah ke TodolistActivity
    private fun navigateToTodolistActivity(categoryName: String) {
        val intent = Intent(this, TodolistActivity::class.java)
        intent.putExtra("CATEGORY_NAME", categoryName)
        startActivity(intent)
    }
}