package com.example.semester2.kategori

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.adapter.ReportAdapter
import com.example.semester2.model.ModelReport
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

class ReportActivity : AppCompatActivity() {

    private lateinit var tvTotalOmzet: TextView
    private lateinit var tvTotalTransaksi: TextView
    private lateinit var tvPeriodeReport: TextView
    private lateinit var rvReport: RecyclerView
    private lateinit var tvKosongReport: TextView
    private lateinit var adapter: ReportAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Sesi berakhir", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvTotalOmzet = findViewById(R.id.tvTotalOmzet)
        tvTotalTransaksi = findViewById(R.id.tvTotalTransaksi)
        tvPeriodeReport = findViewById(R.id.tvPeriodeReport)
        rvReport = findViewById(R.id.rvReport)
        tvKosongReport = findViewById(R.id.tvKosongReport)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("users_data").child(userId).child("report")

        // Inisialisasi adapter dengan listener hapus
        adapter = ReportAdapter(ArrayList(), object : ReportAdapter.OnDeleteClickListener {
            override fun onDeleteClick(model: ModelReport) {
                showDeleteDialog(model)
            }
        })
        
        rvReport.layoutManager = LinearLayoutManager(this)
        rvReport.adapter = adapter

        // Set default date
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
        tvPeriodeReport.text = sdf.format(Date())

        fetchReportData()
    }

    private fun showDeleteDialog(model: ModelReport) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Laporan")
            .setMessage("Apakah Anda yakin ingin menghapus data laporan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusReport(model)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusReport(model: ModelReport) {
        model.idReport?.let { id ->
            myRef.child(id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchReportData() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelReport>()
                var totalOmzet = 0L
                var totalTrans = 0

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val report = dataSnapshot.getValue(ModelReport::class.java)
                        if (report != null) {
                            list.add(report)
                            totalOmzet += report.totalPenjualan ?: 0L
                            totalTrans += report.totalTransaksi ?: 0
                        }
                    }
                }

                tvTotalOmzet.text = formatRupiah(totalOmzet)
                tvTotalTransaksi.text = "$totalTrans Transaksi"

                if (list.isEmpty()) {
                    tvKosongReport.visibility = View.VISIBLE
                    rvReport.visibility = View.GONE
                } else {
                    tvKosongReport.visibility = View.GONE
                    rvReport.visibility = View.VISIBLE
                    adapter.updateData(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReportActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun formatRupiah(number: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(number).replace("Rp", "Rp ").replace(",00", "")
    }
}
