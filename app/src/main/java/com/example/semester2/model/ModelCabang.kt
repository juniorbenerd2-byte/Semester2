package com.example.semester2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelCabang(
    var idCabang: String? = null,
    var namaCabang: String? = null,
    var alamatCabang: String? = null,
    var statusCabang: String? = null
) : Parcelable
