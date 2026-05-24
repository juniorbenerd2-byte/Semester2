package com.example.semester2.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.semester2.R
import com.example.semester2.kategori.MainKategoriActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Cek jika user sudah login sebelumnya
        if (auth.currentUser != null) {
            val intent = Intent(this, MainKategoriActivity::class.java)
            startActivity(intent)
            finish()
        }

        val etEmail = findViewById<TextInputEditText>(R.id.etEmailLogin)
        val etPassword = findViewById<TextInputEditText>(R.id.etPasswordLogin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvToRegister = findViewById<TextView>(R.id.tvToRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proses Login ke Firebase Auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                        
                        // Pindah ke MainKategoriActivity dan hapus tumpukan activity sebelumnya
                        val intent = Intent(this, MainKategoriActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Menampilkan pesan error spesifik dari Firebase
                        Toast.makeText(this, "Login Gagal", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
