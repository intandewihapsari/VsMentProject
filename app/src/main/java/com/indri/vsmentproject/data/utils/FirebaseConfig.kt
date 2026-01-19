package com.indri.vsmentproject.data.utils

object FirebaseConfig {

    // --- Path Utama User (Sesuai JSON: users/managers dan users/staffs) ---
    const val PATH_USERS = "users"
    const val PATH_MANAGERS = "$PATH_USERS/managers" // Pakai 's' sesuai JSON kamu
    const val PATH_STAFFS = "$PATH_USERS/staffs"     // Pakai 's' sesuai JSON kamu

    // --- Path Master Data ---
    const val PATH_MASTER_DATA = "master_data"
    const val PATH_VILLAS = "$PATH_MASTER_DATA/villas"

    // --- Path Operational ---
    const val PATH_OPERATIONAL = "operational"
    const val PATH_TASK_MANAGEMENT = "$PATH_OPERATIONAL/task_management"
    const val PATH_LAPORAN_KERUSAKAN = "$PATH_OPERATIONAL/laporan_kerusakan"
    const val PATH_NOTIFIKASI = "$PATH_OPERATIONAL/notifikasi"

    // --- Path System ---
    const val PATH_SYSTEM_LOGS = "system_logs/activity"

    // --- Field Keys (Penting untuk Query) ---
    const val FIELD_ROLE = "role"
    const val FIELD_STATUS = "status"
    const val FIELD_MANAGER_ID = "manager_id"
    const val FIELD_VILLA_ID = "villa_id"

    // Status Laporan (Sesuai JSON kamu)
    const val STATUS_WAITING = "Menunggu Validasi Manager"
    const val STATUS_DONE = "selesai"
}