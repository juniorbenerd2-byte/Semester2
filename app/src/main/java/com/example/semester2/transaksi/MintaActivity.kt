package com.example.semester2.transaksi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.semester2.R
import com.google.firebase.auth.FirebaseAuth

class MintaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minta)

        val tvEmail = findViewById<TextView>(R.id.tvUserEmailMinta)
        val btnSalin = findViewById<Button>(R.id.btnSalinEmail)
        val btnBack = findViewById<ImageButton>(R.id.btnBackMinta)

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        tvEmail.text = currentUserEmail ?: "Email tidak ditemukan"

        btnBack.setOnClickListener { finish() }

        btnSalin.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Email", tvEmail.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Email disalin ke clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}
