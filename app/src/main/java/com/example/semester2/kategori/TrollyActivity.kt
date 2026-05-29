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
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    private var activeKategoriMap = HashMap<String, ModelKategori>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trolly)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) { finish(); return }

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
            override fun onIncrease(item: ModelTrolly) { updateItemQty(item, 1) }
            override fun onDecrease(item: ModelTrolly) { updateItemQty(item, -1) }
        })

        rvTrolly.layoutManager = LinearLayoutManager(this)
        rvTrolly.adapter = adapter

        fetchDataCombined()

        btnCheckout.setOnClickListener { checkout() }
    }

    private fun fetchDataCombined() {
        kategoriRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                activeKategoriMap.clear()
                for (ds in snapshot.children) {
                    val kategori = ds.getValue(ModelKategori::class.java)
                    if (kategori?.namaKategori != null) activeKategoriMap[kategori.namaKategori!!] = kategori
                }
                filterAndDisplayData()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                rawTrollyList.clear()
                for (ds in snapshot.children) {
                    val item = ds.getValue(ModelTrolly::class.java)
                    if (item != null) rawTrollyList.add(item)
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
            // Checkout hanya aktif jika ada barang yang dipilih (jumlah > 0)
            btnCheckout.isEnabled = totalBarang > 0
            adapter.updateData(filteredList)
        }
    }

    private fun updateItemQty(item: ModelTrolly, diff: Int) {
        val newQty = (item.jumlah ?: 0) + diff
        if (newQty < 0) return // Tidak boleh negatif

        val kategori = activeKategoriMap[item.namaProduk]
        if (diff > 0 && kategori != null && newQty > kategori.stokKategori) {
            Toast.makeText(this, "Stok terbatas! Sisa: ${kategori.stokKategori}", Toast.LENGTH_SHORT).show()
            return
        }
        
        val itemRef = myRef.child(item.idTrolly!!)
        val updates = mapOf(
            "jumlah" to newQty,
            "totalHarga" to (item.harga ?: 0L) * newQty
        )
        itemRef.updateChildren(updates)
    }

    private fun checkout() {
        // Hanya checkout item yang jumlahnya > 0
        val activeItemsToBuy = rawTrollyList.filter { 
            val kategori = activeKategoriMap[it.namaProduk]
            kategori?.statusKategori == "Aktif" && (it.jumlah ?: 0) > 0 
        }
        
        if (activeItemsToBuy.isEmpty()) {
            Toast.makeText(this, "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        var totalHargaCheckout = 0L
        activeItemsToBuy.forEach { totalHargaCheckout += it.totalHarga ?: 0L }

        val newReportRef = reportRef.push()
        val reportId = newReportRef.key
        val tanggal = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

        val reportData = ModelReport(
            idReport = reportId,
            totalPenjualan = totalHargaCheckout,
            totalTransaksi = activeItemsToBuy.size,
            tanggalReport = tanggal,
            items = activeItemsToBuy
        )

        newReportRef.setValue(reportData).addOnSuccessListener {
            activeItemsToBuy.forEach { item ->
                val kategori = activeKategoriMap[item.namaProduk]
                if (kategori != null) {
                    // Update Stok di Kategori
                    val sisaStok = kategori.stokKategori - (item.jumlah ?: 0)
                    kategoriRef.child(kategori.idKategori!!).child("stokKategori").setValue(sisaStok)
                }
                
                // Reset jumlah di Trolly menjadi 0 (jangan hapus itemnya)
                myRef.child(item.idTrolly!!).updateChildren(mapOf(
                    "jumlah" to 0,
                    "totalHarga" to 0L
                ))
            }
            
            val intent = Intent(this, ReceiptActivity::class.java).apply {
                putParcelableArrayListExtra("CHECKOUT_ITEMS", ArrayList(activeItemsToBuy))
                putExtra("TOTAL_HARGA", totalHargaCheckout)
            }
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Checkout gagal: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatRupiah(number: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(number).replace(",00", "")
    }
}
