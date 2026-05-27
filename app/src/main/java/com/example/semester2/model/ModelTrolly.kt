package com.example.semester2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelTrolly(
    var idTrolly: String? = null,
    var namaProduk: String? = null,
    var jumlah: Int? = 0,
    var harga: Long? = 0L,
    var totalHarga: Long? = 0L,
    var fotoProduk: String? = null
) : Parcelable
