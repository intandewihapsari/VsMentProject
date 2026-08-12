package com.indri.vsmentproject.data.utils

import com.google.firebase.database.FirebaseDatabase

object DatabaseSeeder {

    private val db = FirebaseDatabase.getInstance().reference

    /**
     * Menyuntikkan data operasional lengkap sesuai struktur JSON baru:
     * - master_data (staffs, villas, template_tugas)
     * - operational (task_management, laporan_kerusakan, notifikasi, system_logs)
     */
    fun seedMassiveOperationalData() {
        val managerId = "MIzMQOz1nZOar8jFYHZUVBGJF0q1" // Sesuaikan dengan UID Manager Anda
        val managerRoot = FirebaseConfig.getManagerRootPath(managerId)
        val childUpdates = hashMapOf<String, Any>()

        // 1. SEED USER MAPPING
        val userMappings = mapOf(
            managerId to mapOf("role" to "manager", "belongs_to_manager" to managerId),
            "STF_001" to mapOf("role" to "staff", "belongs_to_manager" to managerId),
            "STF_002" to mapOf("role" to "staff", "belongs_to_manager" to managerId)
        )
        userMappings.forEach { (uid, data) ->
            childUpdates["${FirebaseConfig.PATH_USER_MAPPING}/$uid"] = data
        }

        // 2. SEED MANAGER PROFILE
        childUpdates["$managerRoot/${FirebaseConfig.CHILD_MANAGER_PROFILE}"] = mapOf(
            "uid" to managerId,
            "nama" to "Indri Manager",
            "email" to "manager@vsment.com",
            "role" to "manager",
            "status" to "aktif"
        )

        // 3. SEED MASTER DATA
        val masterPath = "$managerRoot/master_data"

        // A. Staffs
        val staffs = mapOf(
            "STF_001" to mapOf("uid" to "STF_001", "nama" to "Budi Staff", "posisi" to "Housekeeping", "manager_id" to managerId),
            "STF_002" to mapOf("uid" to "STF_002", "nama" to "Ani Staff", "posisi" to "Teknisi", "manager_id" to managerId)
        )
        staffs.forEach { (id, data) -> childUpdates["$masterPath/staffs/$id"] = data }

        // B. Villas
        val villas = mapOf(
            "VILLA_001" to mapOf("id" to "VILLA_001", "nama" to "Bohemian Luxury", "manager_id" to managerId, "area" to listOf("Dapur", "Kamar 1")),
            "VILLA_002" to mapOf("id" to "VILLA_002", "nama" to "Kurara Capsule", "manager_id" to managerId, "area" to listOf("Kolam", "Lobby"))
        )
        villas.forEach { (id, data) -> childUpdates["$masterPath/villas/$id"] = data }

        // C. Templates
        childUpdates["$masterPath/template_tugas/TEMP_01"] = mapOf(
            "id" to "TEMP_01",
            "nama" to "Daily Cleaning",
            "tasks" to listOf("Sapu", "Pel", "Lap Kaca")
        )

        // 4. SEED OPERATIONAL DATA
        val operPath = "$managerRoot/operational"

        // A. Task Management (Nested structure sesuai JSON Anda)
        villas.forEach { (vId, vData) ->
            val villaName = vData["nama"].toString()
            
            // Summary / Analisis
            childUpdates["$operPath/task_management/$vId/summary"] = mapOf(
                "total" to 10,
                "completed" to 7,
                "progress" to "70%"
            )

            // List Tugas
            for (i in 1..3) {
                val taskId = "TASK_${vId}_$i"
                childUpdates["$operPath/task_management/$vId/list_tugas/$taskId"] = mapOf(
                    "id" to taskId,
                    "villa_id" to vId,
                    "villa_nama" to villaName,
                    "staff_id" to "STF_001",
                    "staff_nama" to "Budi Staff",
                    "tugas" to "Tugas ke-$i di $villaName",
                    "status" to if (i == 1) "selesai" else "pending",
                    "created_at" to System.currentTimeMillis()
                )
            }
        }

        // B. Laporan Kerusakan
        childUpdates["$operPath/laporan_kerusakan/LAP_001"] = mapOf(
            "id" to "LAP_001",
            "villa_id" to "VILLA_001",
            "villa_nama" to "Bohemian Luxury",
            "staff_nama" to "Budi Staff",
            "nama_barang" to "AC Bocor",
            "status" to "pending",
            "waktu_lapor" to "2024-04-21 10:00"
        )

        // C. Notifikasi
        childUpdates["$operPath/notifikasi/NOTIF_001"] = mapOf(
            "id" to "NOTIF_001",
            "judul" to "Instruksi Baru",
            "pesan" to "Cek villa 001 sekarang",
            "is_read" to false,
            "target_uid" to "STF_001",
            "timestamp" to System.currentTimeMillis()
        )

        // EKSEKUSI
        db.updateChildren(childUpdates).addOnSuccessListener {
            println("SEEDER: Berhasil menyuntikkan data operasional lengkap!")
        }
    }
}