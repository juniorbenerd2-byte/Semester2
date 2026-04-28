package com.example.semester2.kategori

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelKategori(
    var idKategori: String? = null,
    var namaKategori: String? = null,
    var statusKategori: String? = null
) : Parcelable