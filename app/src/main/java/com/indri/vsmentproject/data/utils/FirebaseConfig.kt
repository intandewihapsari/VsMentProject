package com.indri.vsmentproject.data.utils
object FirebaseConfig {

    // --- Users ---
    const val PATH_USERS = "users"
    const val PATH_MANAGERS = "$PATH_USERS/managers"
    const val PATH_STAFFS = "$PATH_USERS/staffs"

    // --- Master Data ---
    const val PATH_MASTER_DATA = "master_data"
    const val PATH_VILLAS = "$PATH_MASTER_DATA/villas"

    // --- Operational ---
    const val PATH_OPERATIONAL = "operational"
    const val PATH_TASK_MANAGEMENT = "$PATH_OPERATIONAL/task_management"
    const val PATH_LAPORAN_KERUSAKAN = "$PATH_OPERATIONAL/laporan_kerusakan"
    const val PATH_NOTIFIKASI = "$PATH_OPERATIONAL/notifikasi"

    // --- System Logs ---
    const val PATH_SYSTEM_LOGS = "system_logs"
    const val PATH_ACTIVITY = "$PATH_SYSTEM_LOGS/activity"

    // --- Field Keys ---
    const val FIELD_ROLE = "role"
    const val FIELD_STATUS = "status"
    const val FIELD_MANAGER_ID = "manager_id"

    // Notifikasi
    const val FIELD_TARGET_UID = "target_uid"
    const val FIELD_TARGET_ROLE = "target_role"

    // --- Status ---
    const val STATUS_PENDING = "pending"
    const val STATUS_DONE = "selesai"
    const val STATUS_REJECTED = "ditolak"

    const val PATH_TASK_TEMPLATES = "task_templates"
}