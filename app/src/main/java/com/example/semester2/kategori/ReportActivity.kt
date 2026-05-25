package com.example.semester2.kategori

import android.os.Bundle
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
import kotlin.collections.ArrayList

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
    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
    
    private var allReportList = ArrayList<ModelReport>()
    private var currentFilterDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (userId.isEmpty()) { finish(); return }

        initView()
        setupFirebase()
        
        // Default filter: Hari ini
        currentFilterDate = dateFormatter.format(Date())
        tvPeriodeReport.text = currentFilterDate
        
        setupToggle()
        fetchInitialData()
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

    private fun fetchInitialData() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allReportList.clear()
                for (ds in snapshot.children) {
                    val report = ds.getValue(ModelReport::class.java)
                    if (report != null) allReportList.add(report)
                }
                filterData(currentFilterDate)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupToggle() {
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val calendar = Calendar.getInstance()
                if (checkedId == R.id.btnKemarin) {
                    calendar.add(Calendar.DATE, -1)
                }
                // Memperbaiki BUG: menggunakan variabel 'calendar' yang benar
                currentFilterDate = dateFormatter.format(calendar.time)
                tvPeriodeReport.text = currentFilterDate
                filterData(currentFilterDate)
            }
        }
    }

    private fun filterData(dateStr: String) {
        val filteredList = ArrayList<ModelReport>()
        var omzet = 0L
        var trans = 0

        for (report in allReportList) {
            // Menggunakan contains agar data dengan format "Tanggal, Jam" tetap terbaca
            if (report.tanggalReport?.contains(dateStr) == true) {
                filteredList.add(report)
                omzet += report.totalPenjualan ?: 0L
                trans += report.totalTransaksi ?: 0
            }
        }

        tvTotalOmzet.text = formatRupiah(omzet)
        tvTotalTransaksi.text = "$trans Transaksi"

        if (filteredList.isEmpty()) {
            tvKosongReport.visibility = View.VISIBLE
            rvReport.visibility = View.GONE
        } else {
            tvKosongReport.visibility = View.GONE
            rvReport.visibility = View.VISIBLE
            adapter.updateData(filteredList)
        }
    }

    private fun showDeleteDialog(model: ModelReport) {
        AlertDialog.Builder(this)
            .setTitle("Hapus")
            .setMessage("Hapus laporan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                model.idReport?.let { myRef.child(it).removeValue() }
            }.setNegativeButton("Batal", null).show()
    }

    private fun formatRupiah(number: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(number).replace("Rp", "Rp ").replace(",00", "")
    }
}
