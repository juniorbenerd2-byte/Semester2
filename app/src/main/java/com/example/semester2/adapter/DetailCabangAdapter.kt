package com.example.semester2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.model.ModelCabang

class DetailCabangAdapter(private var list: ArrayList<ModelCabang>) : 
    RecyclerView.Adapter<DetailCabangAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(model: ModelCabang)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaCabang: TextView = view.findViewById(R.id.tv_nama_cabang)
        val tvAlamatCabang: TextView = view.findViewById(R.id.tv_alamat_cabang)
        val tvTelpCabang: TextView = view.findViewById(R.id.tv_telp_cabang)
        val tvStatusCabang: TextView = view.findViewById(R.id.tv_status_cabang)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNamaCabang.text = item.namaCabang ?: "-"
        holder.tvAlamatCabang.text = item.alamatCabang ?: "-"
        holder.tvTelpCabang.text = "Telp: ${item.teleponCabang ?: "-"}"
        holder.tvStatusCabang.text = item.statusCabang ?: "-"

        holder.itemView.setOnClickListener {
            listener?.onItemClick(item)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ModelCabang>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
