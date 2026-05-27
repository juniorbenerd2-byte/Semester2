package com.example.semester2.kategori

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.semester2.R
import com.example.semester2.model.ModelTrolly
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.NumberFormat
import java.util.*

class KategoriActivity : AppCompatActivity() {

    private lateinit var tvTitleKategori: TextView
    private lateinit var etNamaKategori: TextInputEditText
    private lateinit var radioGroupJenisKategori: RadioGroup
    private lateinit var rbMakanan: RadioButton
    private lateinit var rbMinuman: RadioButton
    private lateinit var etHargaKategori: TextInputEditText
    private lateinit var etStokKategori: TextInputEditText
    private lateinit var radioGroupStatus: RadioGroup
    private lateinit var rbAktif: RadioButton
    private lateinit var rbTidakAktif: RadioButton
    private lateinit var btnSimpan: Button
    private lateinit var btnHapusKategori: Button
    private lateinit var ivFotoKategori: ShapeableImageView
    private lateinit var fabAddPhotoKategori: FloatingActionButton

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private var editKategori: ModelKategori? = null
    private var imageBase64: String? = null
    private lateinit var loadingDialog: ProgressDialog

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            convertImageToBase64(it)
        }
    }

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

        loadingDialog = ProgressDialog(this).apply {
            setMessage("Memproses...")
            setCancelable(false)
        }

        initView()
        setupFirebase()
        setupHargaFormatter()
        setupListeners()
        checkEditMode()
    }

    private fun initView() {
        tvTitleKategori = findViewById(R.id.tvTitleKategori)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        radioGroupJenisKategori = findViewById(R.id.radioGroupJenisKategori)
        rbMakanan = findViewById(R.id.rbMakanan)
        rbMinuman = findViewById(R.id.rbMinuman)
        etHargaKategori = findViewById(R.id.etHargaKategori)
        etStokKategori = findViewById(R.id.etStokKategori)
        radioGroupStatus = findViewById(R.id.radioGroupStatus)
        rbAktif = findViewById(R.id.rbAktif)
        rbTidakAktif = findViewById(R.id.rbTidakAktif)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapusKategori = findViewById(R.id.btnHapusKategori)
        ivFotoKategori = findViewById(R.id.ivFotoKategori)
        fabAddPhotoKategori = findViewById(R.id.fabAddPhotoKategori)

        val mainView = findViewById<View>(R.id.main_kategori)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun convertImageToBase64(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val resizedBitmap = resizeBitmap(bitmap, 400)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            ivFotoKategori.setImageBitmap(resizedBitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun setupHargaFormatter() {
        etHargaKategori.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etHargaKategori.removeTextChangedListener(this)
                    val cleanString = s.toString().replace(".", "")
                    if (cleanString.isNotEmpty()) {
                        try {
                            val parsed = cleanString.toLong()
                            val formatted = NumberFormat.getNumberInstance(Locale("id", "ID")).format(parsed)
                            current = formatted
                            etHargaKategori.setText(formatted)
                            etHargaKategori.setSelection(formatted.length)
                        } catch (e: Exception) {}
                    }
                    etHargaKategori.addTextChangedListener(this)
                }
            }
        })
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
            if (kategori.jenisKategori?.equals("Makanan", ignoreCase = true) == true) {
                rbMakanan.isChecked = true
            } else if (kategori.jenisKategori?.equals("Minuman", ignoreCase = true) == true) {
                rbMinuman.isChecked = true
            }
            val formattedHarga = NumberFormat.getNumberInstance(Locale("id", "ID")).format(kategori.hargaKategori)
            etHargaKategori.setText(formattedHarga)
            etStokKategori.setText(kategori.stokKategori.toString())
            if (kategori.statusKategori?.equals("Aktif", ignoreCase = true) == true) {
                rbAktif.isChecked = true
            } else {
                rbTidakAktif.isChecked = true
            }
            if (!kategori.fotoKategori.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(kategori.fotoKategori, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ivFotoKategori.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    ivFotoKategori.setImageResource(R.drawable.category)
                }
            }
        }
    }

    private fun setupFirebase() {
        database = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("kategori")
    }

    private fun setupListeners() {
        btnSimpan.setOnClickListener { simpanData() }
        btnHapusKategori.setOnClickListener { showDeleteDialog() }
        fabAddPhotoKategori.setOnClickListener { getImage.launch("image/*") }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Apakah Anda yakin ingin menghapus kategori ini?")
            .setPositiveButton("Hapus") { _, _ -> hapusKategori() }
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

    private fun simpanData() {
        val namaKategori = etNamaKategori.text.toString().trim()
        val hargaString = etHargaKategori.text.toString().replace(".", "").trim()
        val stokString = etStokKategori.text.toString().trim()
        val selectedJenisId = radioGroupJenisKategori.checkedRadioButtonId
        if (selectedJenisId == -1 || namaKategori.isEmpty() || hargaString.isEmpty() || stokString.isEmpty()) {
            Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val hargaKategori = hargaString.toLongOrNull() ?: 0L
        val stokKategori = stokString.toIntOrNull() ?: 0
        val jenisKategori = if (selectedJenisId == R.id.rbMakanan) "Makanan" else "Minuman"
        val status = if (radioGroupStatus.checkedRadioButtonId == R.id.rbAktif) "Aktif" else "Tidak Aktif"
        val finalFoto = imageBase64 ?: editKategori?.fotoKategori

        loadingDialog.setMessage("Menyimpan data...")
        loadingDialog.show()

        val isEdit = editKategori != null
        val myRef = if (isEdit) database.child(editKategori!!.idKategori!!) else database.push()
        val kategoriId = if (isEdit) editKategori!!.idKategori!! else myRef.key ?: ""

        val kategoriData = ModelKategori(kategoriId, namaKategori, jenisKategori, hargaKategori, stokKategori, status, finalFoto)

        myRef.setValue(kategoriData).addOnSuccessListener {
            loadingDialog.dismiss()
            if (!isEdit) {
                val trollyRef = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("trolly").push()
                val trollyId = trollyRef.key ?: ""
                val trollyData = ModelTrolly(trollyId, namaKategori, 1, hargaKategori, hargaKategori, finalFoto)
                trollyRef.setValue(trollyData)
            }
            Toast.makeText(this, if (isEdit) "Kategori diperbarui" else "Kategori disimpan", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            loadingDialog.dismiss()
            Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
