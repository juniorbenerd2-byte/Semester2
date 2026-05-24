package com.example.semester2.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.model.ModelLayanan
import java.text.NumberFormat
import java.util.Locale

class DataLayananAdapter(private var list: ArrayList<ModelLayanan>) : 
    RecyclerView.Adapter<DataLayananAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(model: ModelLayanan)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIdLayanan: TextView = view.findViewById(R.id.tv_id_layanan)
        val tvNamaLayanan: TextView = view.findViewById(R.id.tv_layanan)
        val tvHargaLayanan: TextView = view.findViewById(R.id.tv_harga_layanan)
        val cbStatus: CheckBox = view.findViewById(R.id.cb_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_layanancard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvIdLayanan.text = "ID: ${item.idLayanan ?: "-"}"
        holder.tvNamaLayanan.text = item.namaLayanan ?: "-"
        holder.tvHargaLayanan.text = formatRupiah(item.hargaLayanan)
        
        holder.cbStatus.text = item.statusLayanan ?: "Tidak Aktif"
        
        val isAktif = item.statusLayanan?.equals("Aktif", ignoreCase = true) == true
        holder.cbStatus.isChecked = isAktif
        
        if (isAktif) {
            holder.cbStatus.setTextColor(Color.parseColor("#10B981"))
            holder.cbStatus.buttonTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
        } else {
            holder.cbStatus.setTextColor(Color.parseColor("#EF4444"))
            holder.cbStatus.buttonTintList = ColorStateList.valueOf(Color.parseColor("#EF4444"))
        }

        holder.cbStatus.isClickable = false
        holder.cbStatus.isFocusable = false

        holder.itemView.setOnClickListener {
            listener?.onItemClick(item)
        }
    }

    private fun formatRupiah(number: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace("Rp", "Rp ")
    }

    override fun getItemCount(): Int = list.size
    
    fun updateData(newList: List<ModelLayanan>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
