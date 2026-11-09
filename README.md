# SentinelOps Android

Companion mobile app untuk Sentinel Ops (ITSM / security operations). Dibangun dengan Kotlin, Jetpack Compose, Material 3, Navigation Compose, dan ViewModel. Data masih offline-seed agar seluruh alur dapat diuji tanpa server web.

## Fitur yang dapat diuji

- **Login & role**: empat akun demo (`admin`, `secops`, `support`, `client_owner`), password `sentinel123`, error login, logout, dan pencatatan audit login.
- **Navigasi berbasis peran**: admin/secops melihat modul operasional penuh; support dan klien mendapat menu portal, insiden, status, runbook, dan pengaturan sesuai peran.
- **Dasbor**: KPI aktif/kritis/SLA/aset sehat, tren insiden, antrean prioritas, rekomendasi DSS, dan postur keamanan.
- **Insiden**: pencarian, filter, detail operasional, SLA, linimasa, eskalasi, serta transisi status berbasis alur (`OPEN → INVESTIGATING → CONTAINED → RESOLVED → CLOSED`).
- **Aset**: daftar/filter, detail telemetri, update kesehatan, temuan terkait, dan form tambah aset dengan validasi.
- **Pemindai**: pilih skenario (`CLEAN`, `SLOT_DEFACEMENT`, `SQLI_ERROR`, `MALWARE_BACKDOOR`, `SERVER_DOWN`, `SSL_EXPIRED`), progres, aturan deteksi, hasil temuan, dan pembuatan insiden otomatis yang idempoten.
- **Portal klien**: lapor masalah dengan validasi judul/penjelasan, kategori, aset, severity otomatis MEDIUM, dan nomor tiket.
- **DSS**: rekomendasi, matriks risiko, skor, dampak, target SLA, dan riwayat keputusan.
- **Runbook**: prosedur respons berlangkah.
- **Notifikasi**: status baca dan tandai semua.
- **Brankas**: kunci/buka, akses kredensial tercatat ke audit sensitif.
- **Status layanan**: daftar layanan dan halaman detail per slug.
- **Audit**: pencarian, entri sensitif, dan riwayat aksi.
- **Laporan & ekspor**: pratinjau insiden/aset/audit dan aksi ekspor CSV.
- **Pengaturan**: server, sinkronisasi, push, target SLA, dan ekspor.

## Build & tes

```powershell
./gradlew.bat assembleDebug
./gradlew.bat testDebugUnitTest
```

Tes unit mencakup login gagal/berhasil, alur status insiden, validasi portal, validasi aset, audit vault, filter, dan ekspor.

APK debug:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Catatan parity

Aplikasi ini memakai repository lokal di `SentinelViewModel`. Untuk produksi, ganti sumber data dengan API web Sentinel Ops; layar, validasi, dan alur bisnisnya sudah disusun mengikuti modul web.
