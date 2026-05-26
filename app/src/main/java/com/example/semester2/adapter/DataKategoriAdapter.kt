package com.example.semester2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.kategori.ModelKategori
import com.google.android.material.textfield.TextInputEditText

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
        val etNama: TextInputEditText = view.findViewById(R.id.etNamaKategori)
        val etHarga: TextInputEditText = view.findViewById(R.id.etHargaKategori)
        val etStok: TextInputEditText = view.findViewById(R.id.etStokKategori)
        val rbAktif: RadioButton = view.findViewById(R.id.rbAktif)
        val rbTidakAktif: RadioButton = view.findViewById(R.id.rbTidakAktif)
        val btnSimpan: View = view.findViewById(R.id.btnSimpan)
        val tvTitle: TextView = view.findViewById(R.id.tvTitleKategori)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_kategori, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        // Menampilkan Data
        holder.tvTitle.text = "Stok: ${item.stokKategori}"
        holder.tvTitle.visibility = View.VISIBLE
        
        holder.etNama.setText(item.namaKategori ?: "-")
        holder.etHarga.setText("Rp ${item.hargaKategori}")
        holder.etStok.setText(item.stokKategori.toString())
        
        // Nonaktifkan input agar hanya bisa dilihat di list
        holder.etNama.isEnabled = false
        holder.etHarga.isEnabled = false
        holder.etStok.isEnabled = false
        
        if (item.statusKategori?.equals("Aktif", ignoreCase = true) == true) {
            holder.rbAktif.isChecked = true
        } else {
            holder.rbTidakAktif.isChecked = true
        }
        
        holder.rbAktif.isEnabled = false
        holder.rbTidakAktif.isEnabled = false
        holder.btnSimpan.visibility = View.GONE

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
