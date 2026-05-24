package com.example.semester2.kategori

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelKategori(
    var idKategori: String? = null,
    var namaKategori: String? = null,
    var jenisKategori: String? = null,
    var hargaKategori: Long = 0,
    var statusKategori: String? = null
) : Parcelable