package com.example.semester2.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.semester2.R
import com.example.semester2.model.ModelTrolly
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale

class TrollyAdapter(
    private var list: ArrayList<ModelTrolly>,
    private val onQtyChangeListener: OnQtyChangeListener
) : RecyclerView.Adapter<TrollyAdapter.ViewHolder>() {

    interface OnQtyChangeListener {
        fun onIncrease(item: ModelTrolly)
        fun onDecrease(item: ModelTrolly)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaProduk: TextView = view.findViewById(R.id.tv_nama_produk_trolly)
        val tvHargaSatuan: TextView = view.findViewById(R.id.tv_harga_satuan_trolly)
        val tvTotalHarga: TextView = view.findViewById(R.id.tv_total_harga_trolly)
        val tvQty: TextView = view.findViewById(R.id.tv_qty_trolly)
        val btnMinus: ImageButton = view.findViewById(R.id.btn_minus_trolly)
        val btnPlus: ImageButton = view.findViewById(R.id.btn_plus_trolly)
        val ivFotoProduk: ShapeableImageView = view.findViewById(R.id.img_trolly_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_trollycard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        holder.tvNamaProduk.text = item.namaProduk ?: "-"
        holder.tvHargaSatuan.text = "${formatRupiah(item.harga ?: 0L)} / pcs"
        holder.tvTotalHarga.text = formatRupiah(item.totalHarga ?: 0L)
        holder.tvQty.text = (item.jumlah ?: 0).toString()

        // Load image from Base64
        if (!item.fotoProduk.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(item.fotoProduk, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(holder.itemView.context)
                    .load(decodedImage)
                    .placeholder(R.drawable.category)
                    .error(R.drawable.category)
                    .into(holder.ivFotoProduk)
            } catch (e: Exception) {
                holder.ivFotoProduk.setImageResource(R.drawable.category)
            }
        } else {
            holder.ivFotoProduk.setImageResource(R.drawable.category)
        }

        holder.btnPlus.setOnClickListener {
            onQtyChangeListener.onIncrease(item)
        }

        holder.btnMinus.setOnClickListener {
            onQtyChangeListener.onDecrease(item)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ModelTrolly>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    private fun formatRupiah(number: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(number).replace("Rp", "Rp ").replace(",00", "")
    }
}
