package com.example.semester2.setting

import android.app.AlertDialog
import android.app.ProgressDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    private lateinit var etEditNama: TextInputEditText
    private lateinit var etEditEmail: TextInputEditText
    private lateinit var etEditPassword: TextInputEditText
    private lateinit var ivProfilePicture: ShapeableImageView
    private lateinit var loadingDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        loadingDialog = ProgressDialog(this).apply {
            setMessage("Memproses...")
            setCancelable(false)
        }

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
            
            database.getReference("users").child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val nama = snapshot.child("nama").getValue(String::class.java)
                            etEditNama.setText(nama)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@AccountActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBackAccount).setOnClickListener {
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

        findViewById<CardView>(R.id.cardDeleteAccount).setOnClickListener {
            tampilkanDialogKonfirmasiHapus()
        }
        
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabEditPhoto).setOnClickListener {
            Toast.makeText(this, "Fitur ganti foto akan segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun tampilkanDialogKonfirmasiHapus() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Akun")
            .setMessage("Apakah Anda yakin ingin menghapus akun ini secara permanen? Semua data saldo dan profil akan hilang.")
            .setPositiveButton("Hapus") { _, _ ->
                hapusAkunPermanen()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusAkunPermanen() {
        val user = auth.currentUser ?: return
        loadingDialog.setMessage("Menghapus akun...")
        loadingDialog.show()

        // 1. Hapus data dari Realtime Database
        database.getReference("users").child(user.uid).removeValue()
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    // 2. Hapus akun dari Firebase Auth
                    user.delete().addOnCompleteListener { authTask ->
                        loadingDialog.dismiss()
                        if (authTask.isSuccessful) {
                            Toast.makeText(this, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            // Seringkali gagal jika user sudah lama login (membutuhkan re-authentication)
                            Toast.makeText(this, "Gagal: ${authTask.exception?.message}. Silakan logout dan login kembali untuk menghapus akun.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Gagal menghapus data database", Toast.LENGTH_SHORT).show()
                }
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

        loadingDialog.setMessage("Menyimpan perubahan...")
        loadingDialog.show()

        database.getReference("users").child(user.uid).child("nama").setValue(newNama)
            .addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    if (newEmail != user.email) {
                        user.updateEmail(newEmail).addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful) {
                                database.getReference("users").child(user.uid).child("email").setValue(newEmail)
                                checkPasswordUpdate(newPassword)
                            } else {
                                loadingDialog.dismiss()
                                Toast.makeText(this, "Gagal update email: ${emailTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        checkPasswordUpdate(newPassword)
                    }
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Gagal update database", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkPasswordUpdate(newPassword: String) {
        val user = auth.currentUser
        if (newPassword.isNotEmpty() && newPassword.length >= 6) {
            user?.updatePassword(newPassword)?.addOnCompleteListener { passTask ->
                loadingDialog.dismiss()
                if (passTask.isSuccessful) {
                    Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal update password: ${passTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            loadingDialog.dismiss()
            Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
