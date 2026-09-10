# 🎵 NanzMusify

Aplikasi streaming musik modern berbasis **Android (Kotlin + Jetpack Compose)** yang ditenagai oleh **YouTube Music InnerTube API** serta didukung oleh **Jetpack Media3 (ExoPlayer)** untuk pemutaran audio di latar belakang (*background playback*).

---

## 🌟 Fitur Utama

- **🚀 InnerTube Engine**:
  - Akses katalog lagu, album, artis, dan playlist YouTube Music tanpa batasan.
  - Beranda dinamis: *Quick Picks*, *Trending*, dan *Explore*.
  - Ekstraksi *direct stream audio* (AAC / Opus) berkualitas tinggi.
  - Fitur *Up Next / Radio Queue* otomatis saat lagu diputar.
  - Lirik lagu (*Synchronized & Plain Lyrics*).
- **🎧 Pemutar Musik Canggih (Media3 ExoPlayer)**:
  - Background audio playback dengan foreground service & MediaSession.
  - Notifikasi kontrol media di status bar & lockscreen.
  - Fitur *Shuffle*, *Repeat (One / All)*, dan kontrol posisi *Seekbar*.
  - *Mini Player bar* yang dapat diperluas (*expandable*) ke tampilan fullscreen.
- **🔍 Pencarian Cepat & Cerdas**:
  - Real-time search dengan debouncing.
  - Filter pencarian berdasarkan Lagu, Video, Album, atau Artis.
- **📚 Koleksi & Offline Cache**:
  - Simpan lagu favorit ke database lokal (*Room Database*).
  - Riwayat lagu yang baru diputar (*Play History*).

---

## 🏗️ Struktur Proyek

```
NanzMusify/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/nanz/musify/
│       │   ├── NanzMusifyApp.kt              # Application & Service Locator
│       │   ├── MainActivity.kt               # Entrypoint & Navigation Host
│       │   ├── data/
│       │   │   ├── innertube/
│       │   │   │   ├── InnerTubeClient.kt    # HTTP Client YouTube Music InnerTube
│       │   │   │   └── models/Models.kt      # Data Models (Song, Album, Artist, Lyrics)
│       │   │   ├── db/
│       │   │   │   ├── Entities.kt           # Room DB Entities
│       │   │   │   ├── Dao.kt                # SongDao & PlaylistDao
│       │   │   │   └── AppDatabase.kt        # Room Database Instance
│       │   │   └── repository/
│       │   │       └── MusicRepository.kt    # Single Source of Truth
│       │   ├── player/
│       │   │   ├── MusicService.kt           # MediaSessionService (Background)
│       │   │   └── PlaybackManager.kt        # ExoPlayer State & Queue Controller
│       │   └── ui/
│       │       ├── components/               # MiniPlayer, SongItemRow, Cards
│       │       ├── navigation/               # Routes & Screen definitions
│       │       ├── screens/
│       │       │   ├── home/HomeScreen.kt
│       │       │   ├── search/SearchScreen.kt
│       │       │   ├── player/PlayerScreen.kt
│       │       │   └── library/LibraryScreen.kt
│       │       ├── theme/                    # Colors, Typography, Theme
│       │       └── viewmodels/               # MusicViewModel
│       └── res/
│           └── values/                       # strings.xml, colors.xml, themes.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🛠️ Tech Stack & Dependencies

- **Language**: Kotlin 2.0.20
- **UI Framework**: Jetpack Compose + Material 3
- **Audio Engine**: Jetpack Media3 (ExoPlayer 1.4.1)
- **Networking**: OkHttp 4.12.0 + Gson
- **Database**: Room Database 2.6.1 with KSP
- **Image Loader**: Coil Compose 2.7.0
- **Async & Reactive**: Kotlin Coroutines & StateFlow

---

## 🚀 Cara Menjalankan & Build

1. Buka folder `NanzMusify` di **Android Studio Ladybug / Koala** atau versi terbaru.
2. Tunggu sinkronisasi Gradle selesai (`Sync Project with Gradle Files`).
3. Hubungkan perangkat Android fisik atau jalankan Emulator (Min SDK: 24 Android 7.0+, Target SDK: 35 Android 15).
4. Klik tombol **Run (Shift + F10)** atau build APK dengan:
   ```bash
   ./gradlew assembleDebug
   ```
   File APK akan tersedia di `app/build/outputs/apk/debug/app-debug.apk`.
