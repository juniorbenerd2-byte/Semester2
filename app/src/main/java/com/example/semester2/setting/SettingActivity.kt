package com.example.semester2.setting

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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

class SettingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    private lateinit var etEditNama: TextInputEditText
    private lateinit var etEditEmail: TextInputEditText
    private lateinit var etEditPassword: TextInputEditText
    private lateinit var ivProfilePicture: ShapeableImageView
    private lateinit var rgTheme: RadioGroup
    private lateinit var loadingDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        loadingDialog = ProgressDialog(this).apply {
            setMessage("Memproses...")
            setCancelable(false)
        }

        initView()
        loadUserData()
        setupThemeSelection()
        setupListeners()
    }

    private fun initView() {
        etEditNama = findViewById(R.id.etEditNama)
        etEditEmail = findViewById(R.id.etEditEmail)
        etEditPassword = findViewById(R.id.etEditPassword)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        rgTheme = findViewById(R.id.rgTheme)
    }

    private fun setupThemeSelection() {
        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val savedTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        // Set RadioButton sesuai tema yang tersimpan
        when(savedTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> rgTheme.check(R.id.rbLight)
            AppCompatDelegate.MODE_NIGHT_YES -> rgTheme.check(R.id.rbDark)
            else -> rgTheme.check(R.id.rbSystem)
        }

        rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when(checkedId) {
                R.id.rbLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            sharedPreferences.edit().putInt("theme_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        etEditEmail.setText(user.email)
        
        database.getReference("users").child(user.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val nama = snapshot.child("nama").getValue(String::class.java)
                        etEditNama.setText(nama)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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
            .setMessage("Apakah Anda yakin ingin menghapus akun ini secara permanen?")
            .setPositiveButton("Hapus") { _, _ -> hapusAkun() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusAkun() {
        val user = auth.currentUser ?: return
        loadingDialog.show()
        database.getReference("users").child(user.uid).removeValue().addOnCompleteListener { 
            user.delete().addOnCompleteListener { 
                loadingDialog.dismiss()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun simpanPerubahan() {
        val user = auth.currentUser ?: return
        val newNama = etEditNama.text.toString().trim()
        if (newNama.isEmpty()) return

        loadingDialog.show()
        database.getReference("users").child(user.uid).child("nama").setValue(newNama)
            .addOnCompleteListener { 
                loadingDialog.dismiss()
                Toast.makeText(this, "Berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
