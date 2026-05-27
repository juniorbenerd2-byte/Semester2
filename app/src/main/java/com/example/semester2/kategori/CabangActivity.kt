package com.example.semester2.kategori

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import com.example.semester2.model.ModelCabang
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CabangActivity : AppCompatActivity() {

    private lateinit var tvTitleCabang: TextView
    private lateinit var etNamaCabang: TextInputEditText
    private lateinit var spAlamatCabang: AutoCompleteTextView
    private lateinit var etTelpCabang: TextInputEditText
    private lateinit var radioGroupStatusCabang: RadioGroup
    private lateinit var rbAktifCabang: RadioButton
    private lateinit var rbTidakAktifCabang: RadioButton
    private lateinit var btnSimpanCabang: Button
    private lateinit var btnHapusCabang: Button

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private var editCabang: ModelCabang? = null

    private val pilihanCabang = arrayOf("Surakarta", "Sukoharjo", "Mojosongo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cabang)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Sesi berakhir", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        setupDropdown()
        setupFirebase()
        setupListeners()
        checkEditMode()
    }

    private fun initView() {
        tvTitleCabang = findViewById(R.id.tvTitleCabang)
        etNamaCabang = findViewById(R.id.etNamaCabang)
        spAlamatCabang = findViewById(R.id.spAlamatCabang)
        etTelpCabang = findViewById(R.id.etTelpCabang)
        radioGroupStatusCabang = findViewById(R.id.radioGroupStatusCabang)
        rbAktifCabang = findViewById(R.id.rbAktifCabang)
        rbTidakAktifCabang = findViewById(R.id.rbTidakAktifCabang)
        btnSimpanCabang = findViewById(R.id.btnSimpanCabang)
        btnHapusCabang = findViewById(R.id.btnHapusCabang)

        val mainView = findViewById<View>(R.id.main_cabang)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, pilihanCabang)
        spAlamatCabang.setAdapter(adapter)
    }

    private fun checkEditMode() {
        if (intent.hasExtra("EXTRA_CABANG")) {
            editCabang = IntentCompat.getParcelableExtra(intent, "EXTRA_CABANG", ModelCabang::class.java)
            btnHapusCabang.visibility = View.VISIBLE
            populateFields()
        }
    }

    private fun populateFields() {
        editCabang?.let { cabang ->
            tvTitleCabang.text = "Edit Cabang"
            btnSimpanCabang.text = "Perbarui Cabang"
            etNamaCabang.setText(cabang.namaCabang)
            spAlamatCabang.setText(cabang.alamatCabang, false)
            etTelpCabang.setText(cabang.teleponCabang)
            
            if (cabang.statusCabang?.equals("Aktif", ignoreCase = true) == true) {
                rbAktifCabang.isChecked = true
            } else {
                rbTidakAktifCabang.isChecked = true
            }
        }
    }

    private fun setupFirebase() {
        database = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("cabang")
    }

    private fun setupListeners() {
        btnSimpanCabang.setOnClickListener {
            simpanCabang()
        }
        btnHapusCabang.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Cabang")
            .setMessage("Apakah Anda yakin ingin menghapus cabang ini?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusCabang()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusCabang() {
        editCabang?.idCabang?.let { id ->
            database.child(id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Cabang berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun simpanCabang() {
        val nama = etNamaCabang.text.toString().trim()
        val alamat = spAlamatCabang.text.toString().trim()
        val telp = etTelpCabang.text.toString().trim()

        if (nama.isEmpty() || alamat.isEmpty() || telp.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedId = radioGroupStatusCabang.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Pilih status cabang", Toast.LENGTH_SHORT).show()
            return
        }

        val status = if (selectedId == R.id.rbAktifCabang) "Aktif" else "Tidak Aktif"

        btnSimpanCabang.isEnabled = false

        val isEdit = editCabang != null
        val myRef = if (isEdit) {
            database.child(editCabang!!.idCabang!!)
        } else {
            database.push()
        }

        val cabangId = if (isEdit) {
            editCabang!!.idCabang!!
        } else {
            myRef.key ?: ""
        }

        val cabangData = ModelCabang(
            idCabang = cabangId,
            namaCabang = nama,
            alamatCabang = alamat,
            teleponCabang = telp,
            statusCabang = status
        )

        myRef.setValue(cabangData)
            .addOnSuccessListener {
                val msg = if (isEdit) "Cabang diperbarui" else "Cabang berhasil ditambah"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                btnSimpanCabang.isEnabled = true
                Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
