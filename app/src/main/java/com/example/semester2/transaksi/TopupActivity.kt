package com.example.semester2.transaksi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.semester2.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class TopupActivity : AppCompatActivity() {

    private lateinit var etAmount: TextInputEditText
    private lateinit var btnConfirm: Button
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topup)

        etAmount = findViewById(R.id.etAmountTopup)
        btnConfirm = findViewById(R.id.btnConfirmTopup)

        findViewById<ImageButton>(R.id.btnBackTopup).setOnClickListener { finish() }

        // Tambahkan TextWatcher untuk format ribuan (titik)
        setupAmountFormatter()

        btnConfirm.setOnClickListener {
            // Ambil teks, hilangkan semua titik sebelum dikonversi ke angka
            val cleanString = etAmount.text.toString().replace(".", "")
            
            if (cleanString.isEmpty()) {
                etAmount.error = "Masukkan nominal"
                return@setOnClickListener
            }

            try {
                val amount = cleanString.toLong()
                processTopup(amount)
            } catch (e: Exception) {
                Toast.makeText(this, "Format angka tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAmountFormatter() {
        etAmount.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etAmount.removeTextChangedListener(this)

                    // Hilangkan titik yang ada agar tidak double
                    val cleanString = s.toString().replace(".", "")

                    if (cleanString.isNotEmpty()) {
                        try {
                            val parsed = cleanString.toLong()
                            // Format ke gaya Indonesia (pemisah ribuan menggunakan titik)
                            val formatted = NumberFormat.getNumberInstance(Locale("id", "ID")).format(parsed)

                            current = formatted
                            etAmount.setText(formatted)
                            etAmount.setSelection(formatted.length) // Pindahkan kursor ke akhir
                        } catch (e: Exception) {
                            // Jika bukan angka, abaikan
                        }
                    }

                    etAmount.addTextChangedListener(this)
                }
            }
        })
    }

    private fun processTopup(amount: Long) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.child(userId)

        userRef.child("saldo").runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val currentSaldo = currentData.getValue(Long::class.java) ?: 0L
                currentData.value = currentSaldo + amount
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    Toast.makeText(this@TopupActivity, "Top Up Berhasil!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TopupActivity, "Gagal: ${error?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
