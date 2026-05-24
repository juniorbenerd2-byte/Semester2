package com.example.semester2.kategori

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.adapter.TrollyAdapter
import com.example.semester2.model.ModelReport
import com.example.semester2.model.ModelTrolly
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrollyActivity : AppCompatActivity() {

    private lateinit var rvTrolly: RecyclerView
    private lateinit var tvKosongTrolly: TextView
    private lateinit var tvTotalItemsCount: TextView
    private lateinit var tvTotalHargaTrolly: TextView
    private lateinit var btnCheckout: Button
    private lateinit var adapter: TrollyAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var reportRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trolly)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Sesi berakhir", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        rvTrolly = findViewById(R.id.rvTrolly)
        tvKosongTrolly = findViewById(R.id.tvKosongTrolly)
        tvTotalItemsCount = findViewById(R.id.tvTotalItemsCount)
        tvTotalHargaTrolly = findViewById(R.id.tvTotalHargaTrolly)
        btnCheckout = findViewById(R.id.btnCheckout)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("users_data").child(userId).child("trolly")
        reportRef = database.getReference("users_data").child(userId).child("report")

        adapter = TrollyAdapter(ArrayList(), object : TrollyAdapter.OnQtyChangeListener {
            override fun onIncrease(item: ModelTrolly) {
                updateItemQty(item, 1)
            }

            override fun onDecrease(item: ModelTrolly) {
                updateItemQty(item, -1)
            }
        })

        rvTrolly.layoutManager = LinearLayoutManager(this)
        rvTrolly.adapter = adapter

        fetchTrollyData()

        btnCheckout.setOnClickListener {
            checkout()
        }
    }

    private fun fetchTrollyData() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelTrolly>()
                var totalHarga = 0L
                var totalBarang = 0

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val item = dataSnapshot.getValue(ModelTrolly::class.java)
                        if (item != null) {
                            list.add(item)
                            totalHarga += item.totalHarga ?: 0L
                            totalBarang += item.jumlah ?: 0
                        }
                    }
                }

                tvTotalItemsCount.text = "$totalBarang barang"
                tvTotalHargaTrolly.text = formatRupiah(totalHarga)

                if (list.isEmpty()) {
                    tvKosongTrolly.visibility = View.VISIBLE
                    rvTrolly.visibility = View.GONE
                    btnCheckout.isEnabled = false
                } else {
                    tvKosongTrolly.visibility = View.GONE
                    rvTrolly.visibility = View.VISIBLE
                    btnCheckout.isEnabled = true
                    adapter.updateData(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TrollyActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun updateItemQty(item: ModelTrolly, diff: Int) {
        val newQty = (item.jumlah ?: 0) + diff
        val itemRef = myRef.child(item.idTrolly!!)

        if (newQty <= 0) {
            itemRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "${item.namaProduk} dihapus dari keranjang", Toast.LENGTH_SHORT).show()
                }
        } else {
            val updatedPrice = (item.harga ?: 0L) * newQty
            val updates = mapOf<String, Any>(
                "jumlah" to newQty,
                "totalHarga" to updatedPrice
            )
            itemRef.updateChildren(updates)
        }
    }

    private fun checkout() {
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var totalHargaCheckout = 0L
                    
                    for (dataSnapshot in snapshot.children) {
                        val item = dataSnapshot.getValue(ModelTrolly::class.java)
                        if (item != null) {
                            totalHargaCheckout += item.totalHarga ?: 0L
                        }
                    }

                    val newReportRef = reportRef.push()
                    val reportId = newReportRef.key
                    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
                    val tanggal = sdf.format(Date())

                    val reportData = ModelReport(
                        idReport = reportId,
                        totalPenjualan = totalHargaCheckout,
                        totalTransaksi = 1,
                        tanggalReport = tanggal
                    )

                    newReportRef.setValue(reportData).addOnSuccessListener {
                        myRef.removeValue().addOnSuccessListener {
                            Toast.makeText(this@TrollyActivity, "Checkout Berhasil! Total: ${formatRupiah(totalHargaCheckout)}", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this@TrollyActivity, "Gagal menyimpan laporan: ${it.message}", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@TrollyActivity, "Keranjang belanja kosong", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TrollyActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatRupiah(number: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(number).replace("Rp", "Rp ").replace(",00", "")
    }
}
