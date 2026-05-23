package com.example.semester2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.kategori.ModelKategori

class KategoriAdapter(private var list: ArrayList<ModelKategori>) : 
    RecyclerView.Adapter<KategoriAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(model: ModelKategori)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tv_kategori)
        val cbStatus: CheckBox = view.findViewById(R.id.cb_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_kategoricard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.namaKategori ?: "-"
        holder.cbStatus.text = item.statusKategori ?: "-"
        
        val isAktif = item.statusKategori?.equals("Aktif", ignoreCase = true) == true
        holder.cbStatus.isChecked = isAktif
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
