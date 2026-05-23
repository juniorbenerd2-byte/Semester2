package com.example.semester2.kategori

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.semester2.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class KategoriActivity : AppCompatActivity() {

    private lateinit var tvTitleKategori: TextView
    private lateinit var etNamaKategori: TextInputEditText
    private lateinit var radioGroupStatus: RadioGroup
    private lateinit var rbAktif: RadioButton
    private lateinit var rbTidakAktif: RadioButton
    private lateinit var btnSimpan: Button

    private lateinit var database: DatabaseReference
    private var editKategori: ModelKategori? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kategori)

        initView()
        setupFirebase()
        setupListeners()
        checkEditMode()
    }

    private fun initView() {
        tvTitleKategori = findViewById(R.id.tvTitleKategori)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        radioGroupStatus = findViewById(R.id.radioGroupStatus)
        rbAktif = findViewById(R.id.rbAktif)
        rbTidakAktif = findViewById(R.id.rbTidakAktif)
        btnSimpan = findViewById(R.id.btnSimpan)

        val mainView = findViewById<View>(R.id.main_kategori)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkEditMode() {
        if (intent.hasExtra("EXTRA_KATEGORI")) {
            editKategori = IntentCompat.getParcelableExtra(intent, "EXTRA_KATEGORI", ModelKategori::class.java)
            populateFields()
        }
    }

    private fun populateFields() {
        editKategori?.let { kategori ->
            tvTitleKategori.text = "Edit Kategori"
            btnSimpan.text = "Perbarui"
            etNamaKategori.setText(kategori.namaKategori)
            
            if (kategori.statusKategori?.equals("Aktif", ignoreCase = true) == true) {
                rbAktif.isChecked = true
            } else {
                rbTidakAktif.isChecked = true
            }
        }
    }

    private fun setupFirebase() {
        // Menggunakan referensi "kategori" sesuai dengan struktur data yang diinginkan
        database = FirebaseDatabase.getInstance().getReference("kategori")
    }

    private fun setupListeners() {
        btnSimpan.setOnClickListener {
            simpanKategori()
        }
    }

    private fun simpanKategori() {
        val namaKategori = etNamaKategori.text.toString().trim()

        // Validasi input
        if (namaKategori.isEmpty()) {
            etNamaKategori.error = "Nama kategori tidak boleh kosong"
            return
        }

        val selectedId = radioGroupStatus.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Pilih status kategori", Toast.LENGTH_SHORT).show()
            return
        }

        val status = if (selectedId == R.id.rbAktif) "Aktif" else "Tidak Aktif"

        btnSimpan.isEnabled = false

        val isEdit = editKategori != null
        val myRef = if (isEdit) {
            database.child(editKategori!!.idKategori!!)
        } else {
            database.push()
        }

        val kategoriId = if (isEdit) {
            editKategori!!.idKategori!!
        } else {
            myRef.key ?: run {
                Log.e("DEBUG", "key null!")
                btnSimpan.isEnabled = true
                return
            }
        }

        val kategoriData = hashMapOf<String, Any>(
            "idKategori" to kategoriId,
            "namaKategori" to namaKategori,
            "statusKategori" to status
        )

        myRef.setValue(kategoriData)
            .addOnSuccessListener {
                val msg = if (isEdit) "Kategori berhasil diperbarui" else "Kategori berhasil disimpan"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish() // Menutup activity setelah berhasil simpan
            }
            .addOnFailureListener { error ->
                Log.e("DEBUG", "Simpan gagal: ${error.message}")
                btnSimpan.isEnabled = true
                Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}