package com.example.semester2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelReport(
    var idReport: String? = null,
    var totalPenjualan: Long? = 0L,
    var totalTransaksi: Int? = 0,
    var tanggalReport: String? = null
) : Parcelable
