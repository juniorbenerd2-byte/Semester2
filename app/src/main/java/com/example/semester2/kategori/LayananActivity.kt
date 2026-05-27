package com.example.semester2.kategori

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
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
import com.example.semester2.model.ModelCabang
import com.example.semester2.model.ModelLayanan
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class LayananActivity : AppCompatActivity() {

    private lateinit var tvTitleLayanan: TextView
    private lateinit var etNamaLayanan: TextInputEditText
    private lateinit var etHargaLayanan: TextInputEditText
    private lateinit var spinnerCabang: AutoCompleteTextView
    private lateinit var radioGroupStatus: RadioGroup
    private lateinit var rbAktif: RadioButton
    private lateinit var rbTidakAktif: RadioButton
    private lateinit var btnSimpan: Button
    private lateinit var btnHapusLayanan: Button

    private lateinit var database: DatabaseReference
    private lateinit var cabangRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private var editLayanan: ModelLayanan? = null
    private var listCabang = ArrayList<String>()
    private lateinit var adapterCabang: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_layanan)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Sesi berakhir", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        setupFirebase()
        setupHargaFormatter()
        setupCabangSpinner()
        setupListeners()
        checkEditMode()
    }

    private fun initView() {
        tvTitleLayanan = findViewById(R.id.tvTitleLayanan)
        etNamaLayanan = findViewById(R.id.etNamaLayanan)
        etHargaLayanan = findViewById(R.id.etHargaLayanan)
        spinnerCabang = findViewById(R.id.spinnerCabang)
        radioGroupStatus = findViewById(R.id.radioGroupStatus)
        rbAktif = findViewById(R.id.rbAktif)
        rbTidakAktif = findViewById(R.id.rbTidakAktif)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapusLayanan = findViewById(R.id.btnHapusLayanan)

        findViewById<ImageButton>(R.id.btnBackLayanan).setOnClickListener { finish() }

        val mainView = findViewById<View>(R.id.main_layanan)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupCabangSpinner() {
        adapterCabang = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listCabang)
        spinnerCabang.setAdapter(adapterCabang)

        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                for (ds in snapshot.children) {
                    val cabang = ds.getValue(ModelCabang::class.java)
                    cabang?.namaCabang?.let { listCabang.add(it) }
                }
                adapterCabang.notifyDataSetChanged()
                
                // Jika sedang edit, set text spinner sesuai data yang ada
                editLayanan?.cabang?.let {
                    spinnerCabang.setText(it, false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LayananActivity, "Gagal memuat cabang", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupHargaFormatter() {
        etHargaLayanan.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etHargaLayanan.removeTextChangedListener(this)
                    val cleanString = s.toString().replace(".", "")
                    if (cleanString.isNotEmpty()) {
                        try {
                            val parsed = cleanString.toLong()
                            val formatted = NumberFormat.getNumberInstance(Locale("id", "ID")).format(parsed)
                            current = formatted
                            etHargaLayanan.setText(formatted)
                            etHargaLayanan.setSelection(formatted.length)
                        } catch (e: Exception) {}
                    }
                    etHargaLayanan.addTextChangedListener(this)
                }
            }
        })
    }

    private fun checkEditMode() {
        if (intent.hasExtra("EXTRA_LAYANAN")) {
            editLayanan = IntentCompat.getParcelableExtra(intent, "EXTRA_LAYANAN", ModelLayanan::class.java)
            btnHapusLayanan.visibility = View.VISIBLE
            populateFields()
        }
    }

    private fun populateFields() {
        editLayanan?.let { layanan ->
            tvTitleLayanan.text = "Edit ${layanan.namaLayanan}"
            btnSimpan.text = "Perbarui"
            etNamaLayanan.setText(layanan.namaLayanan)
            val formattedHarga = NumberFormat.getNumberInstance(Locale("id", "ID")).format(layanan.hargaLayanan)
            etHargaLayanan.setText(formattedHarga)
            if (layanan.statusLayanan?.equals("Aktif", ignoreCase = true) == true) rbAktif.isChecked = true else rbTidakAktif.isChecked = true
            
            // Cabang diset di setupCabangSpinner setelah data cabang dimuat
        }
    }

    private fun setupFirebase() {
        database = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("layanan")
        cabangRef = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("cabang")
    }

    private fun setupListeners() {
        btnSimpan.setOnClickListener { simpanLayanan() }
        btnHapusLayanan.setOnClickListener { showDeleteDialog() }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Layanan")
            .setMessage("Hapus layanan ini secara permanen?")
            .setPositiveButton("Hapus") { _, _ -> hapusLayanan() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusLayanan() {
        editLayanan?.idLayanan?.let { id ->
            database.child(id).removeValue().addOnSuccessListener {
                Toast.makeText(this, "Layanan dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun simpanLayanan() {
        val nama = etNamaLayanan.text.toString().trim()
        val hargaStr = etHargaLayanan.text.toString().replace(".", "").trim()
        val cabang = spinnerCabang.text.toString().trim()
        val selectedStatusId = radioGroupStatus.checkedRadioButtonId

        if (nama.isEmpty() || hargaStr.isEmpty() || cabang.isEmpty() || selectedStatusId == -1) {
            Toast.makeText(this, "Harap lengkapi data", Toast.LENGTH_SHORT).show()
            return
        }

        val harga = hargaStr.toLongOrNull() ?: 0L
        val status = if (selectedStatusId == R.id.rbAktif) "Aktif" else "Tidak Aktif"
        val myRef = if (editLayanan != null) database.child(editLayanan!!.idLayanan!!) else database.push()
        val id = if (editLayanan != null) editLayanan!!.idLayanan!! else myRef.key ?: ""

        val data = ModelLayanan(id, nama, harga, status, editLayanan?.urlFoto, cabang)
        btnSimpan.isEnabled = false
        myRef.setValue(data).addOnSuccessListener {
            Toast.makeText(this, "Berhasil disimpan", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            btnSimpan.isEnabled = true
            Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
