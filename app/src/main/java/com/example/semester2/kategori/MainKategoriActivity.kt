package com.example.semester2.kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.semester2.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainKategoriActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var tvEstimation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)

        // Inisialisasi Firebase Database
        database = FirebaseDatabase.getInstance().reference

        tvEstimation = findViewById(R.id.tvEstimation)

        val mainView = findViewById<View>(R.id.header)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupHeader()
        setupClickListeners()
        fetchEstimationFromFirebase()
    }

    private fun setupHeader() {
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvDate = findViewById<TextView>(R.id.tvDate)

        tvGreeting.text = "Selamat Belanja, Junior"

        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvDate.text = sdf.format(Date())
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cardAkun).setOnClickListener {
            showToast("Fitur Akun segera hadir")
        }
        findViewById<CardView>(R.id.cardLayanan).setOnClickListener {
            showToast("Fitur Layanan segera hadir")
        }
        findViewById<CardView>(R.id.cardTambahan).setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            startActivity(intent)
        }
        findViewById<CardView>(R.id.cardPegawai).setOnClickListener {
            showToast("Fitur Pegawai segera hadir")
        }
        findViewById<CardView>(R.id.cardCabang).setOnClickListener {
            showToast("Fitur Cabang segera hadir")
        }
        findViewById<CardView>(R.id.cardPrinter).setOnClickListener {
            showToast("Fitur Printer segera hadir")
        }
    }

    private fun fetchEstimationFromFirebase() {

        database.child("summary").child("daily_estimation")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val amount = snapshot.getValue(Long::class.java) ?: 0L
                    tvEstimation.text = formatRupiah(amount)
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Gagal mengambil data: ${error.message}")
                }
            })
    }

    private fun formatRupiah(number: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace("Rp", "Rp ")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}