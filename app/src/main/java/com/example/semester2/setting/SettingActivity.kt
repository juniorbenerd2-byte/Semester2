package com.example.semester2.setting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.semester2.R
import com.example.semester2.auth.LoginActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SettingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    private lateinit var etEditNama: TextInputEditText
    private lateinit var etEditEmail: TextInputEditText
    private lateinit var etEditPassword: TextInputEditText
    private lateinit var ivProfilePicture: ShapeableImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        initView()
        loadUserData()
        setupListeners()
    }

    private fun initView() {
        etEditNama = findViewById(R.id.etEditNama)
        etEditEmail = findViewById(R.id.etEditEmail)
        etEditPassword = findViewById(R.id.etEditPassword)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            etEditEmail.setText(user.email)
            
            // Load name from Realtime Database
            database.getReference("users").child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val nama = snapshot.child("nama").getValue(String::class.java)
                        etEditNama.setText(nama)
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBackSetting).setOnClickListener {
            finish()
        }

        findViewById<CardView>(R.id.cardLogout).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnSimpanPerubahan).setOnClickListener {
            simpanPerubahan()
        }
        
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabEditPhoto).setOnClickListener {
            Toast.makeText(this, "Fitur ganti foto akan segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun simpanPerubahan() {
        val user = auth.currentUser ?: return
        val newNama = etEditNama.text.toString().trim()
        val newEmail = etEditEmail.text.toString().trim()
        val newPassword = etEditPassword.text.toString().trim()

        if (newNama.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Update Database (Nama)
        database.getReference("users").child(user.uid).child("nama").setValue(newNama)

        // 2. Update Email in Auth if changed
        if (newEmail != user.email) {
            user.updateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    database.getReference("users").child(user.uid).child("email").setValue(newEmail)
                    Toast.makeText(this, "Email berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal update email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 3. Update Password in Auth if filled
        if (newPassword.isNotEmpty()) {
            if (newPassword.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            } else {
                user.updatePassword(newPassword).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Gagal update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
    }
}
