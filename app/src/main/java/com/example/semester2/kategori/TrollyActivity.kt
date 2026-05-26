package com.example.semester2.kategori

import android.content.Intent
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
    private lateinit var kategoriRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""

    private var rawTrollyList = ArrayList<ModelTrolly>()
    // Mengubah map untuk menyimpan objek ModelKategori lengkap
    private var activeKategoriMap = HashMap<String, ModelKategori>()

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
        kategoriRef = database.getReference("users_data").child(userId).child("kategori")

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

        fetchDataCombined()

        btnCheckout.setOnClickListener {
            checkout()
        }
    }

    private fun fetchDataCombined() {
        kategoriRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                activeKategoriMap.clear()
                for (ds in snapshot.children) {
                    val kategori = ds.getValue(ModelKategori::class.java)
                    if (kategori?.namaKategori != null) {
                        activeKategoriMap[kategori.namaKategori!!] = kategori
                    }
                }
                filterAndDisplayData()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                rawTrollyList.clear()
                if (snapshot.exists()) {
                    for (ds in snapshot.children) {
                        val item = ds.getValue(ModelTrolly::class.java)
                        if (item != null) rawTrollyList.add(item)
                    }
                }
                filterAndDisplayData()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filterAndDisplayData() {
        val filteredList = ArrayList<ModelTrolly>()
        var totalHarga = 0L
        var totalBarang = 0

        for (item in rawTrollyList) {
            val kategori = activeKategoriMap[item.namaProduk]
            // Item hanya muncul jika kategori Aktif dan Stok > 0
            if (kategori?.statusKategori == "Aktif") {
                filteredList.add(item)
                totalHarga += item.totalHarga ?: 0L
                totalBarang += item.jumlah ?: 0
            }
        }

        tvTotalItemsCount.text = "$totalBarang barang"
        tvTotalHargaTrolly.text = formatRupiah(totalHarga)

        if (filteredList.isEmpty()) {
            tvKosongTrolly.visibility = View.VISIBLE
            rvTrolly.visibility = View.GONE
            btnCheckout.isEnabled = false
        } else {
            tvKosongTrolly.visibility = View.GONE
            rvTrolly.visibility = View.VISIBLE
            btnCheckout.isEnabled = true
            adapter.updateData(filteredList)
        }
    }

    private fun updateItemQty(item: ModelTrolly, diff: Int) {
        val newQty = (item.jumlah ?: 0) + diff
        
        // Validasi stok sebelum menambah quantity
        val kategori = activeKategoriMap[item.namaProduk]
        if (diff > 0 && kategori != null) {
            if (newQty > kategori.stokKategori) {
                Toast.makeText(this, "Stok tidak mencukupi!", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val itemRef = myRef.child(item.idTrolly!!)
        if (newQty <= 0) {
            itemRef.removeValue()
        } else {
            val updatedPrice = (item.harga ?: 0L) * newQty
            val updates = mapOf<String, Any>("jumlah" to newQty, "totalHarga" to updatedPrice)
            itemRef.updateChildren(updates)
        }
    }

    private fun checkout() {
        val activeItems = rawTrollyList.filter { activeKategoriMap[it.namaProduk]?.statusKategori == "Aktif" }
        
        if (activeItems.isNotEmpty()) {
            // Cek stok sekali lagi sebelum benar-benar checkout
            for (item in activeItems) {
                val kategori = activeKategoriMap[item.namaProduk]
                if (kategori == null || (item.jumlah ?: 0) > kategori.stokKategori) {
                    Toast.makeText(this, "Gagal: Stok ${item.namaProduk} tidak mencukupi", Toast.LENGTH_LONG).show()
                    return
                }
            }

            var totalHargaCheckout = 0L
            for (item in activeItems) {
                totalHargaCheckout += item.totalHarga ?: 0L
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
                // KURANGI STOK KATEGORI DAN HAPUS ITEM TROLLY
                for (item in activeItems) {
                    val kategori = activeKategoriMap[item.namaProduk]
                    if (kategori != null) {
                        val stokBaru = kategori.stokKategori - (item.jumlah ?: 0)
                        kategoriRef.child(kategori.idKategori!!).child("stokKategori").setValue(stokBaru)
                    }
                    myRef.child(item.idTrolly!!).removeValue()
                }
                
                val intent = Intent(this, ReceiptActivity::class.java)
                intent.putParcelableArrayListExtra("CHECKOUT_ITEMS", ArrayList(activeItems))
                intent.putExtra("TOTAL_HARGA", totalHargaCheckout)
                startActivity(intent)
                finish()
                
                Toast.makeText(this, "Checkout Berhasil!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatRupiah(number: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(number).replace("Rp", "Rp ").replace(",00", "")
    }
}
