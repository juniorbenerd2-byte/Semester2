package com.example.semester2.transaksi

import android.os.Bundle
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

        btnConfirm.setOnClickListener {
            val amountStr = etAmount.text.toString()
            if (amountStr.isEmpty()) {
                etAmount.error = "Masukkan nominal"
                return@setOnClickListener
            }

            val amount = amountStr.toLong()
            processTopup(amount)
        }
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
