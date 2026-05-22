package com.example.semester2.kategori

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.semester2.R

// Sebaiknya dipindah ke file KategoriCard.kt tersendiri
data class KategoriCard(
    val id: String? = null,
    val namaKategori: String = "",
    val isAktif: Boolean = true
)

class KategoriCardActivity : AppCompatActivity() {

    private lateinit var tvKategori: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Aktifkan tampilan layar penuh (Edge-to-Edge)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kategoricard)

        // 2. Inisialisasi View
        tvKategori = findViewById(R.id.tv_kategori)
        val mainView = findViewById<View>(R.id.main_kategori)

        // 3. Atur Padding otomatis agar tidak tertutup Status Bar/Navigasi
        mainView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // 4. Contoh mengambil data yang dikirim dari activity sebelumnya
        // Jika Anda mengirim data menggunakan intent.putExtra("KATEGORI_NAME", "Elektronik")
        val namaDariIntent = intent.getStringExtra("KATEGORI_NAME") ?: "Nama Kategori"
        tvKategori.text = namaDariIntent
    }
}