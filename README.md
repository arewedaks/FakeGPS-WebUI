# 📍 Remote Fake GPS System

Sistem Fake GPS (Mock Location) terpusat berbasis Android dan PHP WebUI. Project ini memungkinkan Anda untuk mengendalikan lokasi GPS dari banyak HP Android sekaligus, hanya dengan mengklik tombol melalui satu layar Dashboard (Web Server).

Sistem ini sangat cocok untuk manajemen device massal yang tersambung melalui satu jaringan Hotspot.

---

## 🛠 Konsep & Arsitektur

Sistem ini terbagi menjadi 2 komponen utama:
1. **Server PHP (Dashboard)**: Berjalan di HP Master (Android 11) menggunakan Web Server Lokal (misal: Modul Magisk PHP8 di port 80). Bertugas menyimpan titik lokasi.
2. **Aplikasi Android (APK)**: Berjalan di latar belakang sebagai *Service*. APK ini akan terus menyedot data koordinat dari Server PHP setiap 3 detik dan menyuntikkannya secara paksa ke dalam sistem GPS HP menggunakan API resmi `LocationManager`.

---

## 🚀 Panduan Instalasi (HP Master / Server Android 11)

HP Master bertugas sebagai penyedia Hotspot, penyedia Server Web PHP, sekaligus bisa menjadi target Fake GPS.

### 1. Setup Server PHP
1. Buka folder `php_server` yang ada di dalam project ini.
2. Copy/Pindahkan file `index.php` dan `api_lokasi.php` ke dalam *Document Root* web server Anda.
   *(Contoh untuk modul root PHP8: Pindahkan ke direktori `/data/adb/php8/files/www/tools/fakegps/`)*
3. Pastikan server PHP sudah berjalan normal di port `80` atau port pilihan Anda.

### 2. Setup APK untuk HP Master (Localhost)
1. Buka *source code* Android project ini.
2. Pergi ke file `app/src/main/java/com/project/fakegps/MockLocationService.java`.
3. Pastikan variabel `apiUrl` diisi dengan alamat localhost server PHP Anda:
   ```java
   private String apiUrl = "http://127.0.0.1/tools/fakegps/api_lokasi.php";
   ```
4. Lakukan Build APK (Task: `assembleDebug`).
5. Instal APK hasil *build* di HP Master Android 11 tersebut.
6. **Wajib:** Buka Pengaturan HP -> Opsi Pengembang (*Developer Options*) -> Cari "Pilih aplikasi lokasi palsu" (*Select mock location app*) -> Pilih aplikasi **FakeGPS**.

---

## 📱 Panduan Instalasi (HP Klien / Tersambung Hotspot)

Jika Anda memiliki HP ke-2, ke-3, dst yang tersambung ke Hotspot HP Master, Anda bisa membuat mereka semua "berpindah lokasi" secara bersamaan meniru arahan dari WebUI.

### 1. Setup APK untuk HP Klien
1. Buka kembali file `app/src/main/java/com/project/fakegps/MockLocationService.java`.
2. Ubah `apiUrl` menjadi IP Gateway dari Hotspot (IP milik HP Master). Biasanya `192.168.43.1` atau `192.168.x.x`. Contoh:
   ```java
   private String apiUrl = "http://192.168.43.1/tools/fakegps/api_lokasi.php";
   ```
3. Lakukan Build APK ulang untuk membuat **APK Versi Klien**.
4. Instal APK Klien ini di semua HP target yang terhubung ke Hotspot.
5. **Wajib:** Aktifkan izin "Aplikasi lokasi palsu" di Opsi Pengembang masing-masing HP Klien.

---

## 🎮 Cara Penggunaan (WebUI)

Setelah instalasi selesai, sistem sudah siap digunakan secara nyata!

1. Nyalakan Hotspot di HP Master.
2. Buka Browser (baik dari HP Master, HP Klien, maupun Laptop yang tersambung ke Hotspot).
3. Akses URL Dashboard WebUI:
   - Dari HP Master: `http://127.0.0.1/tools/fakegps/index.php`
   - Dari HP Klien/Laptop: `http://192.168.43.1/tools/fakegps/index.php`
4. **Cara Memalsukan Lokasi**:
   - Buka Google Maps, cari lokasi yang Anda inginkan (misal: Monas).
   - Salin angka *Latitude* dan *Longitude*-nya.
   - Paste angka tersebut ke dalam kolom yang tersedia di Dashboard WebUI.
   - Klik **Update Koordinat**.
5. **Fitur START / STOP**:
   - Tombol **▶ START**: Mengaktifkan penyuntikan Mock Location ke semua HP. HP akan berpindah lokasi detik itu juga.
   - Tombol **⏹ STOP**: Mencabut hak Mock Location. Seluruh HP akan kembali membaca sensor satelit GPS asli mereka. (Aplikasi APK akan tetap diam-diam berjalan di latar belakang tanpa mengganggu GPS asli sampai tombol Start ditekan lagi).

---

### 📝 Catatan Tambahan (Bypass Anti-Cheat)
Aplikasi ini murni menggunakan fitur resmi Opsi Pengembang dari sistem Android. Jika digunakan pada aplikasi biasa seperti WhatsApp, Maps, dll, lokasi akan sukses dipalsukan. 
Namun, jika target Anda adalah aplikasi yang memiliki proteksi Anti-Cheat ekstrim (misal Gojek Driver, Absensi, atau Pokemon Go), sistem Anti-Cheat mereka dapat mendeteksi "Opsi Pengembang" yang aktif. **Solusinya:** Gunakan *LSPosed Framework* beserta modul *Hide Mock Location* di HP target untuk menyembunyikan status "Mock Location" dari deteksi aplikasi-aplikasi ketat tersebut.
