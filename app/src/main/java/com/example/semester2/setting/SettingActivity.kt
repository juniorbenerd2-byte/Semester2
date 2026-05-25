package com.example.semester2.setting

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.semester2.R
import com.example.semester2.auth.LoginActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SettingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    private var etEditNama: TextInputEditText? = null
    private var etEditEmail: TextInputEditText? = null
    private var etEditPassword: TextInputEditText? = null
    private var ivProfilePicture: ShapeableImageView? = null
    private var rgTheme: RadioGroup? = null
    private lateinit var loadingDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        
        loadingDialog = ProgressDialog(this).apply {
            setMessage("Memproses...")
            setCancelable(false)
        }

        initView()
        handleInsets()
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

    private fun handleInsets() {
        val root = findViewById<View>(R.id.main_setting)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupThemeSelection() {
        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val savedTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        rgTheme?.let { group ->
            when(savedTheme) {
                AppCompatDelegate.MODE_NIGHT_NO -> group.check(R.id.rbLight)
                AppCompatDelegate.MODE_NIGHT_YES -> group.check(R.id.rbDark)
                else -> group.check(R.id.rbSystem)
            }

            group.setOnCheckedChangeListener { _, checkedId ->
                val mode = when(checkedId) {
                    R.id.rbLight -> AppCompatDelegate.MODE_NIGHT_NO
                    R.id.rbDark -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                
                if (mode != savedTheme) {
                    sharedPreferences.edit().putInt("theme_mode", mode).apply()
                    AppCompatDelegate.setDefaultNightMode(mode)
                    recreate()
                }
            }
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        etEditEmail?.setText(user.email)
        
        database.getReference("users").child(user.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val nama = snapshot.child("nama").getValue(String::class.java)
                        etEditNama?.setText(nama)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBackSetting)?.setOnClickListener { finish() }

        findViewById<CardView>(R.id.cardLogout)?.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnSimpanPerubahan)?.setOnClickListener { simpanPerubahan() }

        findViewById<CardView>(R.id.cardDeleteAccount)?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Akun Permanen")
                .setMessage("Tindakan ini tidak bisa dibatalkan. Semua data Anda akan dihapus selamanya. Anda tidak akan bisa login lagi dengan akun ini.")
                .setPositiveButton("Hapus Sekarang") { _, _ -> hapusAkunPermanen() }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun hapusAkunPermanen() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        loadingDialog.setMessage("Menghapus akun dan data...")
        loadingDialog.show()

        // LANGKAH 1: Hapus data di database selagi masih ter-autentikasi
        val dbRef = FirebaseDatabase.getInstance().reference
        val updates = HashMap<String, Any?>()
        updates["users/$userId"] = null
        updates["users_data/$userId"] = null

        dbRef.updateChildren(updates).addOnCompleteListener { dbTask ->
            if (dbTask.isSuccessful) {
                // LANGKAH 2: Hapus user dari Firebase Auth
                user.delete().addOnCompleteListener { authTask ->
                    loadingDialog.dismiss()
                    if (authTask.isSuccessful) {
                        Toast.makeText(this, "Akun telah dihapus permanen.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        if (authTask.exception is FirebaseAuthRecentLoginRequiredException) {
                            Toast.makeText(this, "Sesi lama terdeteksi. Silakan Logout dan Login kembali sebelum menghapus akun.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Gagal menghapus akun: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                loadingDialog.dismiss()
                Toast.makeText(this, "Gagal menghapus data database.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun simpanPerubahan() {
        val user = auth.currentUser ?: return
        val newNama = etEditNama?.text.toString().trim()
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
