package com.example.semester2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

    // ViewHolder disesuaikan dengan ID yang ada di activity_layanancard.xml
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoto: ImageView = view.findViewById(R.id.iv_layanan_foto)
        val tvIdLayanan: TextView = view.findViewById(R.id.tv_id_layanan)
        val tvNamaLayanan: TextView = view.findViewById(R.id.tv_layanan)
        val tvHargaLayanan: TextView = view.findViewById(R.id.tv_harga_layanan)
        val tvStatusLayanan: TextView = view.findViewById(R.id.tv_status_layanan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_layanancard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // 1. Set ID dan Nama
        holder.tvIdLayanan.text = "ID: ${item.idLayanan ?: "-"}"
        holder.tvNamaLayanan.text = item.namaLayanan ?: "-"

        // 2. Set Harga (Gunakan formatRupiah yang aman dari Null)
        holder.tvHargaLayanan.text = formatRupiah(item.hargaLayanan)

        // 3. Set Status (Diubah dari CheckBox ke TextView sesuai XML)
        val status = item.statusLayanan ?: "Tidak Aktif"
        holder.tvStatusLayanan.text = status

        // 4. Atur Warna Status
        if (status.equals("Aktif", ignoreCase = true)) {
            holder.tvStatusLayanan.setTextColor(Color.parseColor("#10B981")) // Hijau
        } else {
            holder.tvStatusLayanan.setTextColor(Color.parseColor("#EF4444")) // Merah
        }

        // 5. Set Foto menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(item.urlFoto) // Pastikan di ModelLayanan ada field urlFoto
            .into(holder.ivFoto)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(item)
        }
    }

    // Perbaikan: Parameter Long? agar tidak crash jika harga kosong di Firebase
    private fun formatRupiah(number: Long?): String {
        return try {
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            numberFormat.format(number ?: 0L).replace("Rp", "Rp ")
        } catch (e: Exception) {
            "Rp 0"
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ModelLayanan>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}