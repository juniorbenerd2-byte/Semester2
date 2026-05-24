package com.example.semester2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelLayanan(
    var idLayanan: String? = null,
    var namaLayanan: String? = null,
    var hargaLayanan: Long = 0,
    var statusLayanan: String? = null
) : Parcelable