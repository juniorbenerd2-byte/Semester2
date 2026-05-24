package com.example.semester2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.model.ModelReport
import java.text.NumberFormat
import java.util.Locale

class ReportAdapter(
    private var list: ArrayList<ModelReport>,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    interface OnDeleteClickListener {
        fun onDeleteClick(model: ModelReport)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTanggalReport: TextView = view.findViewById(R.id.tv_tanggal_report)
        val tvTransaksiCount: TextView = view.findViewById(R.id.tv_transaksi_count_report)
        val tvTotalPenjualan: TextView = view.findViewById(R.id.tv_total_penjualan_report)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteReport)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_reportcard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvTanggalReport.text = item.tanggalReport ?: "-"
        holder.tvTransaksiCount.text = "${item.totalTransaksi ?: 0} Transaksi"
        holder.tvTotalPenjualan.text = formatRupiah(item.totalPenjualan ?: 0L)
        
        holder.btnDelete.setOnClickListener {
            onDeleteClickListener.onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ModelReport>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    private fun formatRupiah(number: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(number).replace("Rp", "Rp ").replace(",00", "")
    }
}
