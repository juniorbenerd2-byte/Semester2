package com.example.semester2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelLayanan(
    var idLayanan: String? = null,
    var namaLayanan: String? = null,
    var hargaLayanan: Long? = 0L,
    var statusLayanan: String? = null,
    var urlFoto: String? = null,
    var cabang: String? = null
) : Parcelable {
    // Constructor kosong diperlukan oleh Firebase Realtime Database
    constructor() : this(null, null, 0L, null, null, null)
}