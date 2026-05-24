package com.example.semester2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelProduk(
    val idProduk: String? = null,
    val namaProduk: String? = null,
    val hargaProduk: Int? = 0,
    val idkategori: String? = null,
    val idcabang: String? = null,
    val fotoproduk: String? = null,
    val jenisProduk: String? = null,
    val StokProduk: Int? = 0,
    val TanpaBatas: Boolean? = false,
    val statusProduk: String? = null,
    var jumlahterjual: Int? = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Parcelable
