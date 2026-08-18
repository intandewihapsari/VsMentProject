# Implementation Plan - Sinkronisasi Menyeluruh Pemetaan Data Firebase

Saya telah mengidentifikasi bahwa masalah bukti foto tidak muncul disebabkan oleh ketidakcocokan pemetaan (*mapping*) antara nama field di Firebase (snake_case) dengan properti di Kotlin. Meskipun beberapa field terlihat bekerja, tipe data `List` seperti `bukti_foto` sangat sensitif terhadap nama properti saat dikonversi otomatis oleh Firebase.

## Perubahan yang Diusulkan

### 1. Sinkronisasi Nama Properti (Model)
- **Masalah**: Firebase Realtime Database menggunakan konvensi Java Bean. Properti Kotlin `bukti_foto` akan dicari sebagai `buktiFoto` atau melalui getter `getBukti_foto()`, yang seringkali gagal mencocokkan field `bukti_foto` di database.
- **Solusi**: Saya akan menambahkan anotasi `@get:PropertyName` dan `@set:PropertyName` pada **seluruh** field yang mengandung garis bawah (`_`) di `TugasModel.kt`, `LaporanModel.kt`, dan `VillaModel.kt`. Ini adalah cara paling aman untuk menjamin data terbaca 100%.

### 2. Perbaikan Agregasi Foto (Adapter)
- Memastikan logika pengumpulan foto tidak terhenti jika salah satu field bernilai null.
- Menambahkan log deteksi sederhana (jika diperlukan) untuk memastikan daftar tugas dalam group tidak kosong.

### 3. Penguatan Alur Pengiriman (Staff)
- Memastikan saat Staf mengirim bukti, field `villa_id` dan `manager_id` selalu disertakan di dalam objek tugas agar Manajer bisa memfilternya dengan mudah.

## Rencana Aksi

### [MODIFY] [TugasModel.kt](file:///D:/KULIAH/VsMentProject/app/src/main/java/com/indri/vsmentproject/data/model/task/TugasModel.kt)
- Menambahkan `@PropertyName` untuk: `manager_id`, `villa_id`, `villa_nama`, `staff_id`, `staff_nama`, `created_at`, `completed_at`, `foto_tugas`, `foto_staff`, `bukti_foto`.

### [MODIFY] [LaporanModel.kt](file:///D:/KULIAH/VsMentProject/app/src/main/java/com/indri/vsmentproject/data/model/report/LaporanModel.kt)
- Menambahkan `@PropertyName` untuk properti serupa agar PDF laporan juga tetap stabil.

### [MODIFY] [ProgresVillaAdapter.kt](file:///D:/KULIAH/VsMentProject/app/src/main/java/com/indri/vsmentproject/ui/manager/task/progressVilla/ProgresVillaAdapter.kt)
- Melakukan pembersihan kecil pada logika `flatMap` untuk menangani kemungkinan list null secara lebih elegan.

## Rencana Verifikasi
1. **Verifikasi Data**: Setelah perubahan model, buka kembali halaman Progres. Data yang sudah ada di Firebase (seperti di screenshot Anda) seharusnya langsung muncul tanpa perlu upload ulang.
2. **Uji Coba PDF**: Pastikan foto-foto tersebut kini muncul di dokumen PDF yang dihasilkan.
