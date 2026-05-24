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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.semester2.R
import com.example.semester2.model.ModelTrolly
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class KategoriActivity : AppCompatActivity() {

    private lateinit var tvTitleKategori: TextView
    private lateinit var etNamaKategori: TextInputEditText
    private lateinit var etJenisKategori: TextInputEditText
    private lateinit var etHargaKategori: TextInputEditText
    private lateinit var radioGroupStatus: RadioGroup
    private lateinit var rbAktif: RadioButton
    private lateinit var rbTidakAktif: RadioButton
    private lateinit var btnSimpan: Button
    private lateinit var btnHapusKategori: Button

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private var editKategori: ModelKategori? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kategori)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Sesi berakhir, silakan login kembali", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        setupFirebase()
        setupListeners()
        checkEditMode()
    }

    private fun initView() {
        tvTitleKategori = findViewById(R.id.tvTitleKategori)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        etJenisKategori = findViewById(R.id.etJenisKategori)
        etHargaKategori = findViewById(R.id.etHargaKategori)
        radioGroupStatus = findViewById(R.id.radioGroupStatus)
        rbAktif = findViewById(R.id.rbAktif)
        rbTidakAktif = findViewById(R.id.rbTidakAktif)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapusKategori = findViewById(R.id.btnHapusKategori)

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
            btnHapusKategori.visibility = View.VISIBLE
            populateFields()
        }
    }

    private fun populateFields() {
        editKategori?.let { kategori ->
            tvTitleKategori.text = "Edit Kategori"
            btnSimpan.text = "Perbarui"
            etNamaKategori.setText(kategori.namaKategori)
            etJenisKategori.setText(kategori.jenisKategori)
            etHargaKategori.setText(kategori.hargaKategori.toString())
            
            if (kategori.statusKategori?.equals("Aktif", ignoreCase = true) == true) {
                rbAktif.isChecked = true
            } else {
                rbTidakAktif.isChecked = true
            }
        }
    }

    private fun setupFirebase() {
        database = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("kategori")
    }

    private fun setupListeners() {
        btnSimpan.setOnClickListener {
            simpanKategori()
        }
        btnHapusKategori.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Apakah Anda yakin ingin menghapus kategori ini?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusKategori()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusKategori() {
        editKategori?.idKategori?.let { id ->
            database.child(id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Kategori berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun simpanKategori() {
        val namaKategori = etNamaKategori.text.toString().trim()
        val jenisKategori = etJenisKategori.text.toString().trim()
        val hargaString = etHargaKategori.text.toString().trim()

        if (namaKategori.isEmpty() || jenisKategori.isEmpty() || hargaString.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val hargaKategori = hargaString.toLongOrNull() ?: 0L
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
            myRef.key ?: ""
        }

        val kategoriData = ModelKategori(
            idKategori = kategoriId,
            namaKategori = namaKategori,
            jenisKategori = jenisKategori,
            hargaKategori = hargaKategori,
            statusKategori = status
        )

        myRef.setValue(kategoriData)
            .addOnSuccessListener {
                if (!isEdit) {
                    val trollyRef = FirebaseDatabase.getInstance().getReference("users_data")
                        .child(userId).child("trolly").push()
                    val trollyId = trollyRef.key ?: ""
                    val trollyData = ModelTrolly(
                        idTrolly = trollyId,
                        namaProduk = namaKategori,
                        jumlah = 1,
                        harga = hargaKategori,
                        totalHarga = hargaKategori
                    )
                    trollyRef.setValue(trollyData)
                }
                val msg = if (isEdit) "Kategori berhasil diperbarui" else "Kategori berhasil disimpan"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                btnSimpan.isEnabled = true
                Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
