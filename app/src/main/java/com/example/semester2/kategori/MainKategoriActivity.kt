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
import com.example.semester2.model.ModelCabang
import com.example.semester2.model.ModelPegawai
import com.example.semester2.model.ModelReport
import com.example.semester2.model.ModelTrolly
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

    // Variables to store dynamic data fetched from Firebase Database
    private var samplePegawai: ModelPegawai? = null
    private var sampleCabang: ModelCabang? = null
    private var sampleReport: ModelReport? = null
    private var sampleTrolly: ModelTrolly? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        tvEstimation = findViewById(R.id.tvEstimation)

        val mainView = findViewById<View>(R.id.header)
        val originalPaddingLeft = mainView.paddingLeft
        val originalPaddingTop = mainView.paddingTop
        val originalPaddingRight = mainView.paddingRight
        val originalPaddingBottom = mainView.paddingBottom
        
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                originalPaddingLeft + systemBars.left,
                originalPaddingTop + systemBars.top,
                originalPaddingRight + systemBars.right,
                originalPaddingBottom + systemBars.bottom
            )
            insets
        }

        setupHeader()
        setupClickListeners()
        fetchEstimationFromFirebase()
        initializeAndFetchFirebaseData()
    }

    private fun setupHeader() {
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvDate = findViewById<TextView>(R.id.tvDate)

        tvGreeting.text = "Selamat Belanja, Junior"

        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvDate.text = sdf.format(Date())
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cardPegawai).setOnClickListener {
            val intent = Intent(this, DataPegawaiActivity::class.java)
            startActivity(intent)
        }
        findViewById<CardView>(R.id.cardLayanan).setOnClickListener {
            showToast("Fitur Layanan segera hadir")
        }
        findViewById<CardView>(R.id.cardKategori).setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            startActivity(intent)
        }
        findViewById<CardView>(R.id.cardPrinter).setOnClickListener {
            showToast("Fitur Printer segera hadir")
        }
        findViewById<CardView>(R.id.cardCabang).setOnClickListener {
            val cabangText = sampleCabang?.let {
                "Cabang Terkoneksi: ${it.namaCabang} - ${it.alamatCabang}"
            } ?: "Fitur Cabang (Database Terkoneksi)"
            showToast(cabangText)
        }
        findViewById<CardView>(R.id.cardReport).setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }
        findViewById<CardView>(R.id.cardTrolly).setOnClickListener {
            val intent = Intent(this, TrollyActivity::class.java)
            startActivity(intent)
        }
        findViewById<CardView>(R.id.cardQuit).setOnClickListener {
            finish()
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

    private fun initializeAndFetchFirebaseData() {
        // 1. Kategori (check if node exists, initialize if empty)
        val kategoriRef = database.child("kategori")
        kategoriRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val sampleK = ModelKategori("K001", "Makanan & Minuman", "Aktif")
                    kategoriRef.child("K001").setValue(sampleK)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 2. Pegawai
        val pegawaiRef = database.child("pegawai")
        pegawaiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val p = ModelPegawai("P001", "Budi Santoso", "Kasir", "Aktif")
                    pegawaiRef.child("P001").setValue(p)
                } else {
                    for (child in snapshot.children) {
                        samplePegawai = child.getValue(ModelPegawai::class.java)
                        break
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 3. Cabang
        val cabangRef = database.child("cabang")
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val c = ModelCabang("C001", "Cabang Utama Malang", "Jl. Soekarno Hatta No. 9", "Aktif")
                    cabangRef.child("C001").setValue(c)
                } else {
                    for (child in snapshot.children) {
                        sampleCabang = child.getValue(ModelCabang::class.java)
                        break
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 4. Report
        val reportRef = database.child("report")
        reportRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val r = ModelReport("R001", 1750000L, 34, "23 Mei 2026")
                    reportRef.child("R001").setValue(r)
                } else {
                    for (child in snapshot.children) {
                        sampleReport = child.getValue(ModelReport::class.java)
                        break
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 5. Trolly
        val trollyRef = database.child("trolly")
        trollyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val t = ModelTrolly("T001", "Susu UHT 1L", 4, 18000L, 72000L)
                    trollyRef.child("T001").setValue(t)
                } else {
                    for (child in snapshot.children) {
                        sampleTrolly = child.getValue(ModelTrolly::class.java)
                        break
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
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