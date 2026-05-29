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

## 📋 Deskripsi

**Toko Junior** adalah aplikasi Android Point of Sale (POS) yang dirancang untuk membantu pengelolaan toko secara menyeluruh. Aplikasi ini menyediakan fitur lengkap mulai dari manajemen produk berdasarkan kategori, sistem keranjang belanja (trolley), proses checkout, pencetakan nota via printer Bluetooth, hingga laporan penjualan harian. Dilengkapi juga dengan sistem e-wallet internal untuk top-up saldo dan transfer antar pengguna.

---

## ✨ Fitur Utama

### 🔐 Autentikasi
- **Login & Register** — Sistem autentikasi pengguna menggunakan Firebase Authentication
- **Auto-login** — Pengguna yang sudah login akan langsung masuk ke halaman utama
  <img width="1080" height="2436" alt="image" src="https://github.com/user-attachments/assets/abfb8375-8131-48f7-89c7-5710590d8b5a" />
  <img width="1080" height="2436" alt="image" src="https://github.com/user-attachments/assets/63614374-e07f-4aae-b8d8-00065bf3a0d5" />



### 🏠 Dashboard Utama
- **Salam & Saldo** — Menampilkan greeting personal dan saldo e-wallet pengguna
- **Tanggal Hari Ini** — Tampilan tanggal dengan format Indonesia
- **Quick Access Menu** — Akses cepat ke seluruh fitur dalam bentuk kartu navigasi

### 📦 Manajemen Produk & Kategori
- **CRUD Kategori** — Tambah, edit, hapus kategori produk
- **Manajemen Stok** — Pengelolaan stok produk per kategori dengan dukungan stok tanpa batas
- **Status Produk** — Pengaturan status aktif/nonaktif kategori
- **Upload Foto** — Upload gambar produk ke Firebase Storage

### 👥 Manajemen Pegawai
- **Data Pegawai** — Kelola data pegawai termasuk nama, role, alamat, umur, dan gender
- **Penugasan Cabang** — Penugasan pegawai ke cabang tertentu
- **Status Pegawai** — Atur status aktif/nonaktif pegawai

### 🏢 Manajemen Cabang
- **Data Cabang** — Kelola data cabang toko termasuk nama, alamat, dan telepon
- **Status Cabang** — Atur status aktif/nonaktif cabang

### 🛒 Trolley & Checkout
- **Keranjang Belanja** — Tambah produk ke keranjang dengan pengaturan jumlah (+/-)
- **Validasi Stok** — Pengecekan otomatis ketersediaan stok saat menambah kuantitas
- **Filter Produk Aktif** — Hanya produk dengan status aktif yang tampil di trolley
- **Proses Checkout** — Checkout dengan pembuatan laporan otomatis dan pengurangan stok

### 💰 Transaksi Keuangan
- **Top Up Saldo** — Isi ulang saldo e-wallet dengan format angka ribuan otomatis
- **Kirim Saldo** — Transfer saldo ke pengguna lain berdasarkan email
- **Minta Saldo** — Fitur permintaan saldo dari pengguna lain

### 🧾 Nota & Printer Bluetooth
- **Struk Digital** — Tampilan struk belanja setelah checkout
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

```
com.example.semester2/
├── MainApplication.kt          # Application class (Theme initialization)
├── auth/                        # Autentikasi
│   ├── LoginActivity.kt        # Halaman login
│   └── RegisterActivity.kt     # Halaman registrasi
├── kategori/                    # Fitur utama
│   ├── MainKategoriActivity.kt # Dashboard utama
│   ├── KategoriActivity.kt     # CRUD kategori/produk
│   ├── DataKategoriActivity.kt # Daftar kategori
│   ├── PegawaiActivity.kt      # CRUD pegawai
│   ├── DataPegawaiActivity.kt  # Daftar pegawai
│   ├── CabangActivity.kt       # CRUD cabang
│   ├── DataCabangActivity.kt   # Daftar cabang
│   ├── TrollyActivity.kt       # Keranjang belanja
│   ├── ReportActivity.kt       # Laporan penjualan
│   ├── ReceiptActivity.kt      # Struk/nota digital
│   └── PrinterActivity.kt      # Manajemen printer Bluetooth
├── transaksi/                   # Transaksi keuangan
│   ├── TopupActivity.kt        # Top up saldo
│   ├── KirimActivity.kt        # Transfer saldo
│   └── MintaActivity.kt        # Minta saldo
├── setting/                     # Pengaturan
│   ├── SettingActivity.kt      # Halaman pengaturan
│   └── AccountActivity.kt     # Manajemen akun
├── model/                       # Data model
│   ├── ModelUser.kt            # Model pengguna
│   ├── ModelProduk.kt          # Model produk
│   ├── ModelKategori.kt        # Model kategori (lokal)
│   ├── ModelLayanan.kt         # Model layanan
│   ├── ModelPegawai.kt         # Model pegawai
│   ├── ModelCabang.kt          # Model cabang
│   ├── ModelTrolly.kt          # Model item keranjang
│   └── ModelReport.kt          # Model laporan
├── adapter/                     # RecyclerView Adapters
│   ├── KategoriAdapter.kt     
│   ├── DataKategoriAdapter.kt  
│   ├── DataPegawaiAdapter.kt   
│   ├── DetailCabangAdapter.kt  
│   ├── TrollyAdapter.kt        
│   └── ReportAdapter.kt        
└── viewModels/                  # ViewModel (MVVM)
    ├── DataKategoriViewModel.kt
    ├── DataLayananViewModel.kt 
    └── DataPegawaiViewModel.kt  
```

---

## 🔧 Struktur Database Firebase

```
Firebase Realtime Database
├── users/
│   └── {userId}/
│       ├── uid: String
│       ├── nama: String
│       ├── email: String
│       ├── saldo: Long
│       └── fotoProfil: String?
│
└── users_data/
    └── {userId}/
        ├── kategori/
        │   └── {kategoriId}/
        │       ├── idKategori: String
        │       ├── namaKategori: String
        │       ├── hargaKategori: Long
        │       ├── stokKategori: Int
        │       ├── statusKategori: String
        │       └── fotoKategori: String?
        ├── pegawai/
        │   └── {pegawaiId}/
        │       ├── namaPegawai: String
        │       ├── rolePegawai: String
        │       ├── statusPegawai: String
        │       ├── alamatPegawai: String
        │       ├── umurPegawai: Int
        │       ├── genderPegawai: String
        │       └── idCabang: String
        ├── cabang/
        │   └── {cabangId}/
        │       ├── namaCabang: String
        │       ├── alamatCabang: String
        │       ├── teleponCabang: String
        │       └── statusCabang: String
        ├── trolly/
        │   └── {trollyId}/
        │       ├── namaProduk: String
        │       ├── jumlah: Int
        │       ├── harga: Long
        │       ├── totalHarga: Long
        │       └── fotoProduk: String?
        └── report/
            └── {reportId}/
                ├── idReport: String
                ├── totalPenjualan: Long
                ├── totalTransaksi: Int
                ├── tanggalReport: String
                └── items: List<ModelTrolly>
```

---

## 📱 Permissions

Aplikasi ini membutuhkan izin berikut:

| Permission | Kegunaan |
|---|---|
| `INTERNET` | Koneksi ke Firebase |
| `BLUETOOTH` | Koneksi printer Bluetooth |
| `BLUETOOTH_ADMIN` | Manajemen Bluetooth |
| `BLUETOOTH_CONNECT` | Koneksi perangkat Bluetooth (Android 12+) |
| `BLUETOOTH_SCAN` | Scan perangkat Bluetooth (Android 12+) |
| `ACCESS_FINE_LOCATION` | Diperlukan untuk scan Bluetooth |
| `ACCESS_COARSE_LOCATION` | Diperlukan untuk scan Bluetooth |

---

## 🚀 Cara Menjalankan

### Prasyarat
- **Android Studio** Ladybug (2024.2) atau yang lebih baru
- **JDK 11** atau yang lebih baru
- **Android SDK** dengan API Level 36
- Akun **Firebase** yang sudah dikonfigurasi

### Langkah-langkah

1. **Clone repository**
   ```bash
   git clone <repository-url>
   cd Semester2
   ```

2. **Konfigurasi Firebase**
   - Buat project di [Firebase Console](https://console.firebase.google.com/)
   - Aktifkan **Authentication** (Email/Password)
   - Aktifkan **Realtime Database**
   - Aktifkan **Cloud Storage**
   - Download file `google-services.json` dan letakkan di folder `app/`

3. **Buka di Android Studio**
   - File → Open → Pilih folder project
   - Tunggu Gradle sync selesai

4. **Jalankan Aplikasi**
   - Hubungkan perangkat Android atau gunakan emulator
   - Klik tombol **Run** ▶ atau tekan `Shift + F10`

---

## 📄 Lisensi

Project ini dibuat untuk keperluan tugas **Semester 2** — Pengembangan Aplikasi Mobile Android.

---

<p align="center">
  Dibuat dengan ❤️ menggunakan Kotlin & Firebase
</p>
