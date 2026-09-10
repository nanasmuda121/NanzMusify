# 🎵 NanzMusify

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)

Aplikasi streaming musik modern berbasis **Android (Kotlin + Jetpack Compose)** yang ditenagai oleh **YouTube Music InnerTube API**, **LRCLIB Synchronized Lyrics**, serta didukung oleh **Jetpack Media3 (ExoPlayer)** untuk pemutaran audio di latar belakang (*background playback*).

---

## 🌟 Fitur Utama

- **🚀 InnerTube Engine**:
  - Akses katalog lagu, album, artis, dan playlist YouTube Music tanpa batasan.
  - Beranda dinamis: *Quick Picks*, *Trending*, dan *Explore*.
  - Ekstraksi *direct stream audio* (AAC / Opus) berkualitas tinggi.
  - Fitur *Up Next / Radio Queue* otomatis saat lagu diputar.
- **🎤 Lirik Sinkron Real-Time (LRCLIB Integration)**:
  - Lirik bergerak otomatis (*karaoke-style auto-scroll*) mengikuti alur lagu.
  - Teks lirik yang sedang dinyanyikan menyala terang dan membesar.
  - *Tap-to-Seek*: Klik baris lirik mana pun untuk langsung melompat ke detik tersebut.
- **🌟 Jelajah Artis & Album**:
  - Profil artis, top tracks, dan diskografi lengkap.
  - Halaman album & playlist dengan tombol *Play All*.
- **🎧 Pemutar Musik Canggih (Media3 ExoPlayer)**:
  - Background audio playback dengan foreground service & MediaSession.
  - Notifikasi kontrol media di status bar & lockscreen.
  - Fitur *Shuffle*, *Repeat (One / All)*, dan kontrol posisi *Seekbar*.
  - *Mini Player bar* yang dapat diperluas (*expandable*) ke tampilan fullscreen.
- **🔍 Pencarian Cepat & Filter Multi-Kategori**:
  - Real-time search dengan debouncing.
  - Filter chip: *Semua*, *Lagu*, *Artis*, *Album*, *Playlist*.
- **📚 Koleksi & Offline Cache**:
  - Simpan lagu favorit ke database lokal (*Room Database*).
  - Riwayat lagu yang baru diputar (*Play History*).

---

## 📜 Lisensi (License)

Proyek ini dilisensikan di bawah **[GNU Affero General Public License v3.0 (AGPL-3.0)](LICENSE)**.

> [!IMPORTANT]
> **Ketentuan Copyleft Ketat (AGPL-3.0):**
> * Siapa pun yang memodifikasi, mendistribusikan, atau menjalankan kode ini sebagai layanan jaringan/server/cloud **wajib** mempublikasikan seluruh kode sumber perubahannya secara terbuka (100% open source) di bawah lisensi AGPL-3.0 yang sama.
> * Dilarang menutup kode sumber turunan (*closed-source proprietary fork*).

---

Copyright (C) 2026 nanasmuda121. All rights reserved.
