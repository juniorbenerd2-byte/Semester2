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

class KirimActivity : AppCompatActivity() {

    private lateinit var etEmailPenerima: TextInputEditText
    private lateinit var etAmountKirim: TextInputEditText
    private lateinit var btnConfirmKirim: Button
    
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kirim)

        etEmailPenerima = findViewById(R.id.etEmailPenerima)
        etAmountKirim = findViewById(R.id.etAmountKirim)
        btnConfirmKirim = findViewById(R.id.btnConfirmKirim)

        findViewById<ImageButton>(R.id.btnBackKirim).setOnClickListener { finish() }

        // Tambahkan format ribuan otomatis
        setupAmountFormatter()

        btnConfirmKirim.setOnClickListener {
            val email = etEmailPenerima.text.toString().trim()
            // Hilangkan titik sebelum dikonversi ke angka
            val amountStr = etAmountKirim.text.toString().replace(".", "").trim()

            if (email.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toLong()
                if (amount <= 0) {
                    Toast.makeText(this, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                findRecipientAndTransfer(email, amount)
            } catch (e: Exception) {
                Toast.makeText(this, "Format angka tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAmountFormatter() {
        etAmountKirim.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etAmountKirim.removeTextChangedListener(this)

                    val cleanString = s.toString().replace(".", "")

                    if (cleanString.isNotEmpty()) {
                        try {
                            val parsed = cleanString.toLong()
                            val formatted = NumberFormat.getNumberInstance(Locale("id", "ID")).format(parsed)

                            current = formatted
                            etAmountKirim.setText(formatted)
                            etAmountKirim.setSelection(formatted.length)
                        } catch (e: Exception) {}
                    }

                    etAmountKirim.addTextChangedListener(this)
                }
            }
        })
    }

    private fun findRecipientAndTransfer(email: String, amount: Long) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        database.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val recipientSnapshot = snapshot.children.first()
                        val recipientId = recipientSnapshot.key ?: return
                        
                        if (recipientId == currentUserId) {
                            Toast.makeText(this@KirimActivity, "Tidak bisa mengirim ke diri sendiri", Toast.LENGTH_SHORT).show()
                            return
                        }
                        
                        executeTransfer(currentUserId, recipientId, amount)
                    } else {
                        Toast.makeText(this@KirimActivity, "Penerima tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@KirimActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun executeTransfer(senderId: String, recipientId: String, amount: Long) {
        val senderRef = database.child(senderId)
        val recipientRef = database.child(recipientId)

        senderRef.child("saldo").runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val currentSaldo = currentData.getValue(Long::class.java) ?: 0L
                if (currentSaldo < amount) {
                    return com.google.firebase.database.Transaction.abort()
                }
                currentData.value = currentSaldo - amount
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    recipientRef.child("saldo").runTransaction(object : com.google.firebase.database.Transaction.Handler {
                        override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                            val currentSaldo = currentData.getValue(Long::class.java) ?: 0L
                            currentData.value = currentSaldo + amount
                            return com.google.firebase.database.Transaction.success(currentData)
                        }

                        override fun onComplete(e: DatabaseError?, c: Boolean, s: DataSnapshot?) {
                            if (c) {
                                Toast.makeText(this@KirimActivity, "Transfer Berhasil!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                senderRef.child("saldo").setValue(snapshot?.getValue(Long::class.java)?.plus(amount))
                                Toast.makeText(this@KirimActivity, "Gagal mengirim ke penerima", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                } else {
                    Toast.makeText(this@KirimActivity, "Saldo tidak mencukupi", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
