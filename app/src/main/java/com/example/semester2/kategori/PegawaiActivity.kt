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
import com.example.semester2.model.ModelPegawai
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PegawaiActivity : AppCompatActivity() {

    private lateinit var tvTitlePegawaiForm: TextView
    private lateinit var etNamaPegawai: TextInputEditText
    private lateinit var etAlamatPegawai: TextInputEditText
    private lateinit var etUmurPegawai: TextInputEditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var rbLakiLaki: RadioButton
    private lateinit var rbPerempuan: RadioButton
    private lateinit var radioGroupRole: RadioGroup
    private lateinit var rbKasir: RadioButton
    private lateinit var rbAdmin: RadioButton
    private lateinit var rbManager: RadioButton
    private lateinit var radioGroupStatusPegawai: RadioGroup
    private lateinit var rbPegawaiAktif: RadioButton
    private lateinit var rbPegawaiTidakAktif: RadioButton
    private lateinit var btnSimpanPegawai: Button
    private lateinit var btnHapusPegawai: Button

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private var editPegawai: ModelPegawai? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pegawai)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Sesi berakhir", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        setupFirebase()
        setupListeners()
        checkEditMode()
    }

    private fun initView() {
        tvTitlePegawaiForm = findViewById(R.id.tvTitlePegawaiForm)
        etNamaPegawai = findViewById(R.id.etNamaPegawai)
        etAlamatPegawai = findViewById(R.id.etAlamatPegawai)
        etUmurPegawai = findViewById(R.id.etUmurPegawai)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        rbLakiLaki = findViewById(R.id.rbLakiLaki)
        rbPerempuan = findViewById(R.id.rbPerempuan)
        radioGroupRole = findViewById(R.id.radioGroupRole)
        rbKasir = findViewById(R.id.rbKasir)
        rbAdmin = findViewById(R.id.rbAdmin)
        rbManager = findViewById(R.id.rbManager)
        radioGroupStatusPegawai = findViewById(R.id.radioGroupStatusPegawai)
        rbPegawaiAktif = findViewById(R.id.rbPegawaiAktif)
        rbPegawaiTidakAktif = findViewById(R.id.rbPegawaiTidakAktif)
        btnSimpanPegawai = findViewById(R.id.btnSimpanPegawai)
        btnHapusPegawai = findViewById(R.id.btnHapusPegawai)

        val mainView = findViewById<View>(R.id.main_pegawai)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkEditMode() {
        if (intent.hasExtra("EXTRA_PEGAWAI")) {
            editPegawai = IntentCompat.getParcelableExtra(intent, "EXTRA_PEGAWAI", ModelPegawai::class.java)
            btnHapusPegawai.visibility = View.VISIBLE
            populateFields()
        }
    }

    private fun populateFields() {
        editPegawai?.let { pegawai ->
            tvTitlePegawaiForm.text = "Edit Pegawai"
            btnSimpanPegawai.text = "Perbarui"
            etNamaPegawai.setText(pegawai.namaPegawai)
            etAlamatPegawai.setText(pegawai.alamatPegawai)
            etUmurPegawai.setText(pegawai.umurPegawai?.toString())

            if (pegawai.genderPegawai?.equals("Laki-laki", ignoreCase = true) == true) {
                rbLakiLaki.isChecked = true
            } else if (pegawai.genderPegawai?.equals("Perempuan", ignoreCase = true) == true) {
                rbPerempuan.isChecked = true
            }
            
            when (pegawai.rolePegawai?.lowercase()) {
                "kasir" -> rbKasir.isChecked = true
                "admin" -> rbAdmin.isChecked = true
                "manager" -> rbManager.isChecked = true
            }
            
            if (pegawai.statusPegawai?.equals("Aktif", ignoreCase = true) == true) {
                rbPegawaiAktif.isChecked = true
            } else {
                rbPegawaiTidakAktif.isChecked = true
            }
        }
    }

    private fun setupFirebase() {
        database = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("pegawai")
    }

    private fun setupListeners() {
        btnSimpanPegawai.setOnClickListener {
            simpanPegawai()
        }
        btnHapusPegawai.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pegawai")
            .setMessage("Apakah Anda yakin ingin menghapus pegawai ini?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusPegawai()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusPegawai() {
        editPegawai?.idPegawai?.let { id ->
            database.child(id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Pegawai berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun simpanPegawai() {
        val nama = etNamaPegawai.text.toString().trim()
        val alamat = etAlamatPegawai.text.toString().trim()
        val umurStr = etUmurPegawai.text.toString().trim()

        if (nama.isEmpty() || alamat.isEmpty() || umurStr.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        val umur = umurStr.toIntOrNull()

        val checkedGenderId = radioGroupGender.checkedRadioButtonId
        if (checkedGenderId == -1) {
            Toast.makeText(this, "Pilih gender pegawai", Toast.LENGTH_SHORT).show()
            return
        }
        val gender = if (checkedGenderId == R.id.rbLakiLaki) "Laki-laki" else "Perempuan"

        val checkedRoleId = radioGroupRole.checkedRadioButtonId
        if (checkedRoleId == -1) {
            Toast.makeText(this, "Pilih role pegawai", Toast.LENGTH_SHORT).show()
            return
        }
        val role = when (checkedRoleId) {
            R.id.rbKasir -> "Kasir"
            R.id.rbAdmin -> "Admin"
            else -> "Manager"
        }

        val checkedStatusId = radioGroupStatusPegawai.checkedRadioButtonId
        if (checkedStatusId == -1) {
            Toast.makeText(this, "Pilih status pegawai", Toast.LENGTH_SHORT).show()
            return
        }
        val status = if (checkedStatusId == R.id.rbPegawaiAktif) "Aktif" else "Tidak Aktif"

        btnSimpanPegawai.isEnabled = false

        val isEdit = editPegawai != null
        val myRef = if (isEdit) {
            database.child(editPegawai!!.idPegawai!!)
        } else {
            database.push()
        }

        val id = if (isEdit) {
            editPegawai!!.idPegawai!!
        } else {
            myRef.key ?: ""
        }

        val data = ModelPegawai(
            idPegawai = id,
            namaPegawai = nama,
            rolePegawai = role,
            statusPegawai = status,
            alamatPegawai = alamat,
            umurPegawai = umur,
            genderPegawai = gender
        )

        myRef.setValue(data)
            .addOnSuccessListener {
                val msg = if (isEdit) "Pegawai berhasil diperbarui" else "Pegawai berhasil disimpan"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                btnSimpanPegawai.isEnabled = true
                Toast.makeText(this, "Gagal menyimpan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
