package com.example.semester2.model// Pastikan nama variabel di sini SAMA PERSIS dengan kunci yang ada di Firebase
data class ModelLayanan(
    var idLayanan: String? = null,
    var namaLayanan: String? = null,
    var hargaLayanan: Long? = 0L,
    var statusLayanan: String? = null,
    var urlFoto: String? = null // Tambahkan ini agar tidak error di adapter
) {
    // Constructor kosong diperlukan oleh Firebase Realtime Database
    constructor() : this(null, null, 0L, null, null)
}