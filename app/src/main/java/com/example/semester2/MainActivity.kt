package com.example.semester2

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)

        // Penanganan Window Insets untuk Edge-to-Edge
        val mainView = findViewById<android.view.View>(R.id.tvGreeting).parent as? android.view.View
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Setup Data Otomatis
        setupHeader()
    }

    private fun setupHeader() {
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvDate = findViewById<TextView>(R.id.tvDate)

        // Set Nama User
        tvGreeting.text = "Selamat Belanja, Junior"

        // Set Tanggal Otomatis (Hari ini)
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvDate.text = sdf.format(Date())
    }
}