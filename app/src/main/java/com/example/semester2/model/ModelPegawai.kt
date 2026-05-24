package com.example.semester2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelPegawai(
    var idPegawai: String? = null,
    var namaPegawai: String? = null,
    var rolePegawai: String? = null,
    var statusPegawai: String? = null,
    var alamatPegawai: String? = null,
    var umurPegawai: Int? = null,
    var genderPegawai: String? = null
) : Parcelable
