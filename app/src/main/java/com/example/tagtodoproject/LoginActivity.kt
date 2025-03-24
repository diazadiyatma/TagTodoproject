package com.example.tagtodoproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tagtodoproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Tombol Login diklik
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
            } else {
                // Lakukan proses login dengan Firebase Auth
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Login berhasil
                            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                            // Redirect ke MainActivity atau Activity lainnya
                            val intent = Intent(this, CategoryActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Jika login gagal, tampilkan pesan error
                            Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Teks Register diklik
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}