# Sistem Manajemen Inventaris & Peminjaman

Aplikasi berbasis Java untuk mengelola data inventaris, peminjaman, dan perawatan barang. Aplikasi ini menggunakan file CSV sebagai media penyimpanan data dan antarmuka berbasis teks.

## Fitur

- **Manajemen Inventaris**
  - Tambah, lihat, dan kelola data barang.
  - Data disimpan dalam `inventory.csv`.

- **Peminjaman Barang**
  - Formulir peminjaman barang.
  - Pencatatan data peminjam dan tanggal peminjaman.
  - Data disimpan dalam `borrowing.csv`.

- **Pengembalian Barang**
  - Formulir pengembalian.
  - Pemeriksaan ketersediaan barang.

- **Perawatan Barang**
  - Formulir perawatan untuk mencatat barang yang perlu diperbaiki.
  - Data disimpan dalam `maintenance.csv`.

- **Laporan**
  - Laporan peminjaman, perawatan, dan inventaris.
  - Cetak data dalam format teks.

## Struktur Proyek

```
src/
├── AvailabilityChecker.java
├── BorrowForm.java
├── BorrowManager.java
├── BorrowRecord.java
├── CSVManager.java
├── InventoryForm.java
├── InventoryItem.java
├── Main.java
├── MaintenanceForm.java
├── MaintenanceManager.java
├── MaintenanceRecord.java
├── PrintUtilities.java
├── ReportGenerator.java
├── ReturnForm.java

borrowing.csv
inventory.csv
maintenance.csv
README.md
```

## Cara Menjalankan

1. **Kompilasi Semua File Java**
   Jalankan perintah berikut di terminal:

   ```bash
   javac src/*.java
   ```

2. **Jalankan Program**
   Pastikan berada di direktori `src`, lalu jalankan:

   ```bash
   java Main
   ```

## Dependensi

Tidak menggunakan dependensi eksternal. Seluruh program ditulis menggunakan pustaka standar Java (`java.io`, `java.util`, dll).

## Format CSV

- **inventory.csv**
  ```
  ID,Nama,Kategori,Jumlah,Kondisi
  ```

- **borrowing.csv**
  ```
  ID Peminjaman,ID Barang,Nama Peminjam,Tanggal Pinjam
  ```

- **maintenance.csv**
  ```
  ID Perawatan,ID Barang,Deskripsi Masalah,Tanggal
  ```

## Kontributor

- Hilvan Yadhisnata
- M. Hafizul Hadi
- Pria Pamungkas
- Rizka Febriyanti
- Tanri Vebriansyah

## Lisensi

Proyek ini dibuat untuk keperluan pembelajaran dan tugas UTS. Bebas digunakan untuk edukasi.
