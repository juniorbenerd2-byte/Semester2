package com.example.semester2.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.semester2.R
import com.example.semester2.kategori.ModelKategori
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale

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
        val tvNama: TextView = view.findViewById(R.id.tv_kategori)
        val tvJenis: TextView = view.findViewById(R.id.tv_jenis_kategori)
        val tvHarga: TextView = view.findViewById(R.id.tv_harga_kategori)
        val cbStatus: CheckBox = view.findViewById(R.id.cb_status)
        val ivFoto: ShapeableImageView = view.findViewById(R.id.iv_item_foto_kategori)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Menggunakan layout kartu, bukan layout form activity_kategori
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_kategoricard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.namaKategori ?: "-"
        holder.tvJenis.text = "Jenis: ${item.jenisKategori ?: "-"}"
        holder.tvHarga.text = formatRupiah(item.hargaKategori)
        holder.cbStatus.text = item.statusKategori ?: "-"
        
        val isAktif = item.statusKategori?.equals("Aktif", ignoreCase = true) == true
        holder.cbStatus.isChecked = isAktif
        holder.cbStatus.isClickable = false
        holder.cbStatus.isFocusable = false

        if (!item.fotoKategori.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(item.fotoKategori, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(holder.itemView.context)
                    .load(decodedImage)
                    .placeholder(R.drawable.category)
                    .error(R.drawable.category)
                    .into(holder.ivFoto)
            } catch (e: Exception) {
                holder.ivFoto.setImageResource(R.drawable.category)
            }
        } else {
            holder.ivFoto.setImageResource(R.drawable.category)
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(item)
        }
    }

    private fun formatRupiah(number: Long): String {
        val localeID = Locale("id", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number).replace("Rp", "Rp ")
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ModelKategori>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
