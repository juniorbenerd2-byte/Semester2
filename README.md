# 🏪 Toko Junior — Aplikasi Kasir & Manajemen Toko

<p align="center">
  <strong>Aplikasi Point of Sale (POS) berbasis Android untuk manajemen toko, transaksi keuangan, dan pencetakan nota via Bluetooth.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform Android">
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Language Kotlin">
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black" alt="Firebase">
  <img src="https://img.shields.io/badge/Min%20SDK-24-blue" alt="Min SDK 24">
  <img src="https://img.shields.io/badge/Target%20SDK-36-blue" alt="Target SDK 36">
  <img src="https://img.shields.io/badge/Version-1.0-green" alt="Version 1.0">
</p>

---

## Screenshot

The following is a gallery of all screenshots:

<p align="center">
  <img src="image_0.png" width="320" alt="Login Page">
  <img src="image_1.png" width="320" alt="Register Page">
  <img src="image_2.png" width="320" alt="Data Kategori Page">
  <img src="image_3.png" width="320" alt="Tambah Kategori Page">
  <img src="image_4.png" width="320" alt="Struk Pembayaran Page">
  <img src="image_5.png" width="320" alt="Pengaturan Akun Page">
  <img src="image_6.png" width="320" alt="Data Pegawai Page">
  <img src="image_7.png" width="320" alt="Tambah Cabang Page">
  <img src="image_8.png" width="320" alt="Top Up Saldo Page">
  <img src="image_9.png" width="320" alt="Kirim Saldo Page">
</p>

## 📋 Deskripsi

**Toko Junior** adalah aplikasi Android Point of Sale (POS) yang dirancang untuk membantu pengelolaan toko secara menyeluruh. Aplikasi ini menyediakan fitur lengkap mulai dari manajemen produk berdasarkan kategori, sistem keranjang belanja (trolley), proses checkout, pencetakan nota via printer Bluetooth, hingga laporan penjualan harian. Dilengkapi juga dengan sistem e-wallet internal untuk top-up saldo dan transfer antar pengguna.

---

## ✨ Fitur Utama

### 🔐 Autentikasi
- **Login & Register** — Sistem autentikasi pengguna menggunakan Firebase Authentication
  <br><img src="image_0.png" width="320" alt="Login Page">
  <br><img src="image_1.png" width="320" alt="Register Page">
- **Auto-login** — Pengguna yang sudah login akan langsung masuk ke halaman utama

### 🏠 Dashboard Utama
- **Salam & Saldo** — Menampilkan greeting personal dan saldo e-wallet pengguna
- **Tanggal Hari Ini** — Tampilan tanggal dengan format Indonesia
- **Quick Access Menu** — Akses cepat ke seluruh fitur dalam bentuk kartu navigasi

### 📦 Manajemen Produk & Kategori
- **CRUD Kategori** — Tambah, edit, hapus kategori produk
  <br><img src="image_2.png" width="320" alt="Data Kategori Page">
  <br><img src="image_3.png" width="320" alt="Tambah Kategori Page">
- **Manajemen Stok** — Pengelolaan stok produk per kategori dengan dukungan stok tanpa batas
- **Status Produk** — Pengaturan status aktif/nonaktif kategori
- **Upload Foto** — Upload gambar produk ke Firebase Storage

### 👥 Manajemen Pegawai
- **Data Pegawai** — Kelola data pegawai termasuk nama, role, alamat, umur, dan gender
  <br><img src="image_6.png" width="320" alt="Data Pegawai Page">
- **Penugasan Cabang** — Penugasan pegawai ke cabang tertentu
- **Status Pegawai** — Atur status aktif/nonaktif pegawai

### 🏢 Manajemen Cabang
- **Data Cabang** — Kelola data cabang toko termasuk nama, alamat, dan telepon
  <br><img src="image_7.png" width="320" alt="Tambah Cabang Page">
- **Status Cabang** — Atur status aktif/nonaktif cabang

### 🛒 Trolley & Checkout
- **Keranjang Belanja** — Tambah produk ke keranjang dengan pengaturan jumlah (+/-)
- **Validasi Stok** — Pengecekan otomatis ketersediaan stok saat menambah kuantitas
- **Filter Produk Aktif** — Hanya produk dengan status aktif yang tampil di trolley
- **Proses Checkout** — Checkout dengan pembuatan laporan otomatis dan pengurangan stok

### 💰 Transaksi Keuangan
- **Top Up Saldo** — Isi ulang saldo e-wallet dengan format angka ribuan otomatis
  <br><img src="image_8.png" width="320" alt="Top Up Saldo Page">
- **Kirim Saldo** — Transfer saldo ke pengguna lain berdasarkan email
  <br><img src="image_9.png" width="320" alt="Kirim Saldo Page">
- **Minta Saldo** — Fitur permintaan saldo dari pengguna lain

### 🧾 Nota & Printer Bluetooth
- **Struk Digital** — Tampilan struk belanja setelah checkout
  <br><img src="image_4.png" width="320" alt="Struk Pembayaran Page">
- **Cetak via Bluetooth** — Cetak nota langsung ke printer thermal Bluetooth
- **Riwayat Nota** — Simpan dan cetak ulang nota dari riwayat transaksi
- **Scan Perangkat** — Pencarian dan koneksi otomatis ke printer Bluetooth

### 📊 Laporan Penjualan
- **Laporan Harian** — Laporan omzet dan jumlah transaksi per hari
- **Filter Hari Ini / Kemarin** — Toggle filter periode laporan
- **Detail Transaksi** — Rincian setiap transaksi yang tercatat
- **Hapus Laporan** — Hapus laporan yang tidak diperlukan

### ⚙️ Pengaturan
- **Edit Profil** — Ubah nama dan data pengguna
  <br><img src="image_5.png" width="320" alt="Pengaturan Akun Page">
- **Tema Aplikasi** — Pilihan tema Terang, Gelap, atau Ikuti Sistem
- **Hapus Akun** — Penghapusan akun permanen dengan konfirmasi

---

## 🏗️ Arsitektur & Teknologi

### Tech Stack

| Teknologi | Detail |
|---|---|
| **Bahasa** | Kotlin |
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 36 |
| **Build System** | Gradle (Kotlin DSL) |
| **UI Framework** | Android Views + ConstraintLayout |
| **Backend** | Firebase Realtime Database |
| **Autentikasi** | Firebase Authentication |
| **Storage** | Firebase Cloud Storage |
| **AI** | Firebase AI (v17.12.0) |
| **Image Loading** | Glide 4.16.0 |
| **Arsitektur** | MVVM (ViewModel + LiveData) |

### Struktur Package
