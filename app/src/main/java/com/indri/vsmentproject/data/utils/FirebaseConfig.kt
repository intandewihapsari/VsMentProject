package com.indri.vsmentproject.data.utils

import com.google.firebase.database.FirebaseDatabase

object FirebaseConfig {

    // --- Root Nodes ---
    const val PATH_USER_MAPPING = "user_mapping"
    const val PATH_VILLA_MANAGEMENT = "villa_management"

    // --- Path Helpers ---
    fun getManagerRootPath(managerId: String) = "$PATH_VILLA_MANAGEMENT/$managerId"
    
    fun getBaseRef() = FirebaseDatabase.getInstance().reference
    
    fun getManagerRef(managerId: String) = getBaseRef().child(PATH_VILLA_MANAGEMENT).child(managerId)

    // Helper untuk Folder Master Data
    fun getMasterDataRef(managerId: String) = getManagerRef(managerId).child("master_data")
    fun getVillasRef(managerId: String) = getMasterDataRef(managerId).child(CHILD_VILLAS)
    fun getStaffsRef(managerId: String) = getMasterDataRef(managerId).child(CHILD_STAFFS)
    fun getTemplateTugasRef(managerId: String) = getMasterDataRef(managerId).child(CHILD_TEMPLATE_TUGAS)

    // Helper untuk Folder Operational
    fun getOperationalRef(managerId: String) = getManagerRef(managerId).child("operational")
    fun getTaskManagementRef(managerId: String) = getOperationalRef(managerId).child(CHILD_TASK_MANAGEMENT)
    fun getLaporanKerusakanRef(managerId: String) = getOperationalRef(managerId).child(CHILD_LAPORAN_KERUSAKAN)
    fun getNotifikasiRef(managerId: String) = getOperationalRef(managerId).child(CHILD_NOTIFIKASI)
    fun getSystemLogsRef(managerId: String) = getOperationalRef(managerId).child(CHILD_SYSTEM_LOGS)

    /**
     * Mengaktifkan sinkronisasi background untuk data krusial agar tersedia secara offline.
     */
    fun enableSync(managerId: String) {
        getTaskManagementRef(managerId).keepSynced(true)
        getNotifikasiRef(managerId).keepSynced(true)
        getLaporanKerusakanRef(managerId).keepSynced(true)
    }

    // --- Sub-Paths ---
    const val CHILD_MANAGER_PROFILE = "manager_profile"
    const val CHILD_OPERATIONAL = "operational"
    const val CHILD_VILLAS = "villas"
    const val CHILD_STAFFS = "staffs"
    const val CHILD_TEMPLATE_TUGAS = "template_tugas"
    const val CHILD_TASK_MANAGEMENT = "task_management"
    const val CHILD_LAPORAN_KERUSAKAN = "laporan_kerusakan"
    const val CHILD_NOTIFIKASI = "notifikasi"
    const val CHILD_SYSTEM_LOGS = "system_logs"
    const val CHILD_MASTER_DATA = "master_data"


    // --- Field Keys ---
    const val FIELD_ROLE = "role"
    const val FIELD_STATUS = "status"
    const val FIELD_BELONGS_TO_MANAGER = "belongs_to_manager"
    const val FIELD_TARGET_UID = "target_uid"

    // --- Status Operational ---
    const val STATUS_PENDING = "pending"
    const val STATUS_PROSES = "proses"
    const val STATUS_DONE = "selesai"
    const val STATUS_REJECTED = "ditolak"
}