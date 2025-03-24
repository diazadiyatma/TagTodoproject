package com.example.tagtodoproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Mengatur layout dari activity_main.xml

        // Menginisialisasi tombol "Get Started"
        val getStartedButton = findViewById<Button>(R.id.getStartedButton)
        // Menambahkan listener untuk tombol "Get Started"
        getStartedButton.setOnClickListener { // Intent untuk pindah ke aktivitas berikutnya
            val intent = Intent(
                this@MainActivity,
                RegisterActivity::class.java
            ) // Ganti dengan aktivitas berikutnya
            startActivity(intent)
        }
    }
}