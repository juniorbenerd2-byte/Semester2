package com.example.semester2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.semester2.R
import com.example.semester2.model.ModelPegawai

class DataPegawaiAdapter(private var list: ArrayList<ModelPegawai>) : 
    RecyclerView.Adapter<DataPegawaiAdapter.ViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(model: ModelPegawai)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIdPegawai: TextView = view.findViewById(R.id.tv_id_pegawai)
        val tvNamaPegawai: TextView = view.findViewById(R.id.tv_nama_pegawai)
        val tvRolePegawai: TextView = view.findViewById(R.id.tv_role_pegawai)
        val cbStatusPegawai: CheckBox = view.findViewById(R.id.cb_status_pegawai)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_pegawaicard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvIdPegawai.text = "ID: ${item.idPegawai ?: "-"}"
        holder.tvNamaPegawai.text = item.namaPegawai ?: "-"
        holder.tvRolePegawai.text = "Role: ${item.rolePegawai ?: "-"}"
        holder.cbStatusPegawai.text = item.statusPegawai ?: "Tidak Aktif"
        
        val isAktif = item.statusPegawai?.equals("Aktif", ignoreCase = true) == true
        holder.cbStatusPegawai.isChecked = isAktif
        
        if (isAktif) {
            holder.cbStatusPegawai.setTextColor(Color.parseColor("#10B981")) // Hijau
        } else {
            holder.cbStatusPegawai.setTextColor(Color.parseColor("#EF4444")) // Merah
        }

        holder.cbStatusPegawai.isClickable = false
        holder.cbStatusPegawai.isFocusable = false
        
        holder.itemView.setOnClickListener {
            listener?.onItemClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
    
    fun updateData(newList: List<ModelPegawai>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
