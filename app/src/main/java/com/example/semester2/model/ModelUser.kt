package com.example.semester2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelUser(
    val uid: String? = null,
    val nama: String? = null,
    val email: String? = null,
    val saldo: Long = 0,
    val fotoProfil: String? = null
) : Parcelable
