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
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var tvTotalOmzet: TextView
    private lateinit var tvTotalTransaksi: TextView
    private lateinit var tvPeriodeReport: TextView
    private lateinit var rvReport: RecyclerView
    private lateinit var tvKosongReport: TextView
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var adapter: ReportAdapter

    private lateinit var myRef: DatabaseReference
    private var userId: String = ""
    
    // Konsisten menggunakan Locale "in" (Indonesia) sesuai TrollyActivity
    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (userId.isEmpty()) { finish(); return }

        initView()
        setupFirebase()
        setupToggle()
        
        // Tampilkan data Hari Ini secara default
        loadReportByDate(Date())
    }

    private fun initView() {
        tvTotalOmzet = findViewById(R.id.tvTotalOmzet)
        tvTotalTransaksi = findViewById(R.id.tvTotalTransaksi)
        tvPeriodeReport = findViewById(R.id.tvPeriodeReport)
        rvReport = findViewById(R.id.rvReport)
        tvKosongReport = findViewById(R.id.tvKosongReport)
        toggleGroup = findViewById(R.id.toggleGroupReport)

        adapter = ReportAdapter(ArrayList(), object : ReportAdapter.OnDeleteClickListener {
            override fun onDeleteClick(model: ModelReport) {
                showDeleteDialog(model)
            }
        })
        
        rvReport.layoutManager = LinearLayoutManager(this)
        rvReport.adapter = adapter
    }

    private fun setupFirebase() {
        myRef = FirebaseDatabase.getInstance().getReference("users_data").child(userId).child("report")
    }

    private fun setupToggle() {
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val calendar = Calendar.getInstance()
                if (checkedId == R.id.btnKemarin) {
                    calendar.add(Calendar.DATE, -1)
                }
                loadReportByDate(calendar.time)
            }
        }
    }

    private fun loadReportByDate(date: Date) {
        val targetDateString = dateFormatter.format(date)
        tvPeriodeReport.text = targetDateString

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<ModelReport>()
                var totalOmzet = 0L
                var totalTrans = 0

                for (data in snapshot.children) {
                    val report = data.getValue(ModelReport::class.java)
                    // PENTING: Karena di database ada Jam (12 Mei 2024, 14:00), 
                    // kita gunakan 'contains' untuk mencocokkan tanggal saja.
                    if (report != null && report.tanggalReport?.contains(targetDateString) == true) {
                        list.add(report)
                        totalOmzet += report.totalPenjualan ?: 0L
                        totalTrans += report.totalTransaksi ?: 0
                    }
                }
                updateUI(list, totalOmzet, totalTrans)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ReportActivity", "Error: ${error.message}")
            }
        })
    }

    private fun updateUI(list: ArrayList<ModelReport>, omzet: Long, trans: Int) {
        tvTotalOmzet.text = formatRupiah(omzet)
        tvTotalTransaksi.text = "$trans Transaksi"
        
        if (list.isEmpty()) {
            tvKosongReport.visibility = View.VISIBLE
            rvReport.visibility = View.GONE
        } else {
            tvKosongReport.visibility = View.GONE
            rvReport.visibility = View.VISIBLE
            adapter.updateData(list)
        }
    }

    private fun showDeleteDialog(model: ModelReport) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Laporan")
            .setMessage("Hapus data laporan?")
            .setPositiveButton("Hapus") { _, _ ->
                model.idReport?.let { myRef.child(it).removeValue() }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun formatRupiah(number: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            .format(number).replace("Rp", "Rp ").replace(",00", "")
    }
}
