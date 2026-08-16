# Walkthrough - Staff UI Restoration & Header Alignment

Saya telah memperbaiki tampilan antarmuka Staff dengan mengembalikan desain **FAB Ring** (Siren dengan lingkaran peach) seperti semula dan memastikan **Header Bar** (judul halaman) konsisten dengan tampilan Manager tanpa adanya judul ganda.

## Perubahan Utama

### 1. Restorasi FAB Siren Ring
> [!IMPORTANT]
> Desain FAB dikembalikan menggunakan `FrameLayout` kustom untuk menampung efek "Peach Ring" di belakang ikon Siren, memberikan tampilan yang lebih menonjol di tengah menu.

- **Layout Fix**: Menghapus fitur `fabCradle` otomatis dari `BottomAppBar` yang sebelumnya menyebabkan masalah tumpang tindih visual.
- **Positioning**: Menempatkan FAB tepat di tengah menggunakan anchor `bottomAppBar` dengan margin bawah yang pas agar label menu di sampingnya tidak tertutup.

### 2. Header Bar Terpadu (Anti-Double Header)
- **Activity Level Title**: Judul halaman (seperti "Home", "Tugas", dll) kini dikelola sepenuhnya oleh `StaffActivity`, sama seperti `ManagerActivity`.
- **Cleanup**: Memastikan judul internal di dalam fragment (seperti "Home" di Dashboard) telah dihapus sehingga tidak muncul dua kali di layar.

### 3. Konsistensi Antar Role
- Antarmuka bagian atas (Header) untuk **Manager** dan **Staff** sekarang identik dalam hal tinggi (80dp), font, warna, dan padding. Ini memberikan kesan aplikasi yang lebih solid dan profesional.

## Hasil Pengujian
- **Tampilan FAB**: ✅ Sesuai permintaan (Siren + Ring Peach, di tengah).
- **Header**: ✅ Konsisten dan tidak double.
- **Build Status**: ✅ Success.

## Cara Verifikasi
1. Log in sebagai **Staff**.
2. Perhatikan tombol Siren di tengah menu bawah; pastikan memiliki lingkaran peach di belakangnya.
3. Berpindah antar menu (Home -> Tugas -> Profile) dan pastikan judul di bagian paling atas berubah dengan benar tanpa ada judul lain di bawahnya.
