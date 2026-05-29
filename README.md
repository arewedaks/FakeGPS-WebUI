# FakeGPSWebUI 🌍

FakeGPSWebUI adalah sebuah aplikasi **Hybrid Pemalsu Lokasi (Fake GPS)** tingkat lanjut yang dikendalikan penuh secara *remote* melalui Web UI interaktif. Dirancang khusus untuk kompatibel secara sempurna mulai dari **Android 10 hingga Android 16**.

Aplikasi ini menggunakan konsep **Dual-Mode (Hybrid APK)**. Artinya, hanya dengan 1 APK yang sama, ia dapat beroperasi sebagai aplikasi Fake GPS standar (tanpa Root), atau berevolusi menjadi modul *System-Level Spoofing* super senyap menggunakan LSPosed (bagi perangkat yang sudah Root).

---

## ✨ Fitur Utama

- **Remote Control via Web UI:** Ubah koordinat, hentikan, atau mulai pemalsuan lokasi secara *real-time* langsung dari *browser* dengan antarmuka peta interaktif yang modern.
- **Indikator Detak Jantung (Heartbeat):** Web UI dapat membaca status hidup/matinya APK di latar belakang secara *real-time*. Jika APK ditutup paksa di HP, indikator di Web UI otomatis berubah menjadi offline dalam hitungan detik.
- **Anti-Rubber Banding:** Algoritma latar belakang (*background service*) yang menjaga agar GPS stabil dan tidak loncat-loncat kembali ke lokasi asli.
- **Sistem Hybrid (2-in-1):**
  - **Mode Non-Root:** Berjalan dengan mulus menggunakan opsi *Mock Location* bawaan Opsi Pengembang Android.
  - **Mode Root (LSPosed):** Menginjeksi (*hook*) langsung ke sistem dan mematikan sinyal deteksi `isFromMockProvider`, menjadikannya **100% Anti-Tuyul / Bypass** dari aplikasi keamanan, absen, transportasi, maupun *game*.

---

## 🚀 Panduan Instalasi (Hybrid)

Instalasi akan menyesuaikan dengan tingkat keistimewaan (hak akses) sistem Android Anda.

### Persiapan Web UI (Wajib Dilakukan)
1. Masukkan folder `php_server` ke dalam direktori aplikasi *Web Server* Anda di HP Android (seperti AWD Server, KSWEB, atau Termux PHP).
2. Buka dan akses `index.php` melalui *browser* lokal Anda (contoh: `http://127.0.0.1:8080/index.php`).
3. File pengaturan pusat `kordinat.json` akan otomatis tercipta. Anda siap mengontrol lokasi!

---

### Opsi 1: Instalasi Standar (Non-Root)
Ini adalah metode termudah jika HP Anda **tidak di-root**.

1. Instal APK FakeGPSWebUI di HP Anda.
2. Buka **Pengaturan (Settings)** > **Opsi Pengembang (Developer Options)**.
3. Cari menu **Pilih Aplikasi Lokasi Palsu (Select mock location app)**, lalu pilih **FakeGPSWebUI**.
4. Buka aplikasi FakeGPSWebUI. (Berikan izin Lokasi jika diminta oleh sistem Android).
5. Tekan tombol **Start** di dalam aplikasinya untuk mulai menjalankan *service* di latar belakang.
6. Pantau dan atur lokasi Anda sesuka hati melalui Web UI!

---

### Opsi 2: Instalasi Tingkat Sistem (Root + LSPosed)
Gunakan metode ini jika HP Anda sudah memiliki akses **Root (Magisk/KernelSU)** dan sudah terinstal modul **LSPosed Framework**. Mode ini sangat direkomendasikan untuk menembus aplikasi yang ketat.

*Catatan: Anda **TIDAK PERLU** lagi memilih aplikasi lokasi palsu di Opsi Pengembang jika menggunakan mode ini.*

1. Instal APK FakeGPSWebUI seperti biasa.
2. Buka aplikasi **LSPosed Manager**.
3. Masuk ke menu **Modules** dan cari modul **FakeGPSWebUI**.
4. **Aktifkan Modul** (*Enable Module*).
5. **Targetkan Aplikasi:** Pilih / centang aplikasi-aplikasi apa saja yang ingin Anda kelabui lokasinya (misal: Gojek, Grab, WhatsApp, atau Pokemon Go). 
6. **Restart / Mulai Ulang HP Anda.**
7. Setelah HP menyala, buka APK FakeGPSWebUI dan tekan **Start**. (Aplikasi kini menyebarkan lokasi palsunya secara rahasia melalui *ContentProvider*).
8. Buka Web UI Anda, atur lokasi, dan selamat menikmati GPS siluman Anda!

---

## 🛠️ Informasi Kompatibilitas
- Aplikasi ini secara spesifik ditulis dengan standar *Foreground Services* Android modern, memungkinkannya lolos dari *pemusnah aplikasi latar belakang* (*Battery Optimization*) di Android 12 hingga Android 16.
- Menangani dengan mulus sistem *Permissions* terbaru (seperti `POST_NOTIFICATIONS`) sejak Android 13.
