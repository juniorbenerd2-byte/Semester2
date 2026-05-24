package com.example.semester2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.kategori.ModelKategori

class DataKategoriAdapter(private var list: ArrayList<ModelKategori>) : 
    RecyclerView.Adapter<DataKategoriAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(model: ModelKategori)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIdKategori: TextView = view.findViewById(R.id.tv_id_kategori)
        val tvNamaKategori: TextView = view.findViewById(R.id.tv_kategori)
        val cbStatus: CheckBox = view.findViewById(R.id.cb_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_kategoricard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvIdKategori.text = "ID: ${item.idKategori ?: "-"}"
        holder.tvNamaKategori.text = item.namaKategori ?: "-"
        holder.cbStatus.text = item.statusKategori ?: "Tidak Aktif"
        
        val isAktif = item.statusKategori?.equals("Aktif", ignoreCase = true) == true
        holder.cbStatus.isChecked = isAktif
        
        // Atur warna teks dan tombol berdasarkan status
        if (isAktif) {
            holder.cbStatus.setTextColor(Color.parseColor("#10B981")) // Hijau sesuai tema
            holder.cbStatus.buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981"))
        } else {
            holder.cbStatus.setTextColor(Color.parseColor("#EF4444")) // Merah
            holder.cbStatus.buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444"))
        }

        // Agar klik pada CheckBox tidak mengonsumsi event klik dari item
        holder.cbStatus.isClickable = false
        holder.cbStatus.isFocusable = false

        holder.itemView.setOnClickListener {
            listener?.onItemClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
    
    fun updateData(newList: List<ModelKategori>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
