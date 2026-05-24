package com.example.semester2.kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.semester2.R
import com.example.semester2.model.ModelCabang
import com.example.semester2.model.ModelPegawai
import com.example.semester2.model.ModelReport
import com.example.semester2.model.ModelTrolly
import com.example.semester2.transaksi.KirimActivity
import com.example.semester2.transaksi.MintaActivity
import com.example.semester2.transaksi.TopupActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.semester2.setting.SettingActivity

class MainKategoriActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var tvEstimation: TextView
    private lateinit var tvGreeting: TextView

    // Variables to store dynamic data fetched from Firebase Database
    private var samplePegawai: ModelPegawai? = null
    private var sampleCabang: ModelCabang? = null
    private var sampleReport: ModelReport? = null
    private var sampleTrolly: ModelTrolly? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        tvEstimation = findViewById(R.id.tvEstimation)
        tvGreeting = findViewById(R.id.tvGreeting)

        val headerView = findViewById<View>(R.id.header)
        ViewCompat.setOnApplyWindowInsetsListener(headerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        setupHeader()
        setupClickListeners()
        fetchUserData()
        initializeAndFetchFirebaseData()
    }

    private fun setupHeader() {
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvDate.text = sdf.format(Date())
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nama = snapshot.child("nama").getValue(String::class.java) ?: "User"
                    val saldo = snapshot.child("saldo").getValue(Long::class.java) ?: 0L
                    
                    tvGreeting.text = "Selamat Belanja, $nama"
                    tvEstimation.text = formatRupiah(saldo)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupClickListeners() {
        // Transaksi Buttons
        findViewById<LinearLayout>(R.id.btnTransaksi).setOnClickListener {
            startActivity(Intent(this, KirimActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnTopup).setOnClickListener {
            startActivity(Intent(this, TopupActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnMinta).setOnClickListener {
            startActivity(Intent(this, MintaActivity::class.java))
        }

        // Card Menu Buttons
        findViewById<CardView>(R.id.cardPegawai).setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }
        findViewById<CardView>(R.id.cardLayanan).setOnClickListener {
            showToast("Fitur Layanan segera hadir")
        }
        findViewById<CardView>(R.id.cardKategori).setOnClickListener {
            startActivity(Intent(this, DataKategoriActivity::class.java))
        }
        findViewById<CardView>(R.id.cardPrinter).setOnClickListener {
            showToast("Fitur Printer segera hadir")
        }
        findViewById<CardView>(R.id.cardCabang).setOnClickListener {
            startActivity(Intent(this, DataCabangActivity::class.java))
        }
        findViewById<CardView>(R.id.cardReport).setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
        findViewById<CardView>(R.id.cardTrolly).setOnClickListener {
            startActivity(Intent(this, TrollyActivity::class.java))
        }
        
        findViewById<CardView>(R.id.cardProfileTop).setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }

    private fun initializeAndFetchFirebaseData() {
        // ... (data initialization logic remains similar but updated for UI consistency)
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
