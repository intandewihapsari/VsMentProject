package com.indri.vsmentproject.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.TaskTemplateModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class TaskRepository {
    private val rootRef = FirebaseDatabase.getInstance().reference

    fun saveTaskWithNotification(
        managerId: String,
        villaId: String,
        tugasData: TugasModel,
        onComplete: (Boolean) -> Unit
    ) {
        val managerRoot = FirebaseConfig.getManagerRootPath(managerId)
        val operRoot = "$managerRoot/${FirebaseConfig.CHILD_OPERATIONAL}"
        val taskRef = rootRef.child(operRoot).child(FirebaseConfig.CHILD_TASK_MANAGEMENT).child(villaId).child("list_tugas")
        val notifRef = rootRef.child(operRoot).child(FirebaseConfig.CHILD_NOTIFIKASI)

        val newTugasKey = taskRef.push().key ?: return onComplete(false)
        val newNotifKey = notifRef.push().key ?: return onComplete(false)

        tugasData.id = newTugasKey

        val notifData = mapOf(
            "id" to newNotifKey,
            "judul" to "Tugas Baru di ${tugasData.villa_nama}",
            "pesan" to "Tugas: ${tugasData.tugas} (${tugasData.ruangan})",
            "sender_id" to managerId,
            "target_role" to "staff",
            "target_uid" to (tugasData.staff_id ?: ""),
            "timestamp" to System.currentTimeMillis(),
            "tipe" to "normal",
            "is_read" to false,
            "villa_id" to villaId,
            "villa_nama" to tugasData.villa_nama,
            "waktu" to tugasData.deadline
        )

        val childUpdates = hashMapOf<String, Any>(
            "$operRoot/${FirebaseConfig.CHILD_TASK_MANAGEMENT}/$villaId/list_tugas/$newTugasKey" to tugasData,
            "$operRoot/${FirebaseConfig.CHILD_NOTIFIKASI}/$newNotifKey" to notifData
        )

        rootRef.updateChildren(childUpdates).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun getAllPendingTasks(managerId: String): LiveData<Resource<List<TugasModel>>> {
        val liveData = MutableLiveData<Resource<List<TugasModel>>>()
        liveData.postValue(Resource.Loading())

        val pathRef = FirebaseConfig.getTaskManagementRef(managerId)


        pathRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allPending = mutableListOf<TugasModel>()

                snapshot.children.forEach { villaSnap ->
                    val villaId = villaSnap.key
                    val listTugasSnap = villaSnap.child("list_tugas")

                    listTugasSnap.children.forEach { tugasSnap ->
                        try {
                            val tugas = tugasSnap.getValue(TugasModel::class.java)
                            if (tugas != null) {
                                if (tugas.status.equals(FirebaseConfig.STATUS_PENDING, true)) {
                                    allPending.add(tugas.apply { id = tugasSnap.key ?: "" })
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FIREBASE_DEBUG", "Error pas nge-mapping TugasModel: ${e.message}")
                        }
                    }
                }

                liveData.postValue(Resource.Success(allPending))
            }

            override fun onCancelled(e: DatabaseError) {
                Log.e("FIREBASE_DEBUG", "Database Error: ${e.message}")
                liveData.postValue(Resource.Error(e.message))
            }
        })
        return liveData
    }

    fun updateTaskStatus(managerId: String, taskId: String, status: String, onComplete: (Boolean) -> Unit) {
        FirebaseConfig.getTaskManagementRef(managerId)
            .child(taskId)
            .child(FirebaseConfig.FIELD_STATUS)
            .setValue(status)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteTask(managerId: String, taskId: String, onComplete: (Boolean) -> Unit) {
        FirebaseConfig.getTaskManagementRef(managerId)
            .child(taskId)
            .removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun getVillaList(managerId: String, onResult: (DataSnapshot) -> Unit) {
        FirebaseConfig.getVillasRef(managerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) = onResult(snapshot)
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getTemplates(managerId: String, onResult: (List<TaskTemplateModel>) -> Unit) {
        FirebaseConfig.getTemplateTugasRef(managerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { child ->
                        child.getValue(TaskTemplateModel::class.java)?.apply {
                            id = child.key ?: ""
                        }
                    }
                    onResult(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }

    // B. Buat atau Simpan Master Template Baru
    fun saveTemplate(
        managerId: String,
        template: TaskTemplateModel,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = FirebaseConfig.getTemplateTugasRef(managerId)

        val targetRef = if (template.id.isEmpty()) ref.push() else ref.child(template.id)
        template.id = targetRef.key ?: ""

        targetRef.setValue(template).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // C. Hapus Master Template
    fun deleteTemplate(managerId: String, templateId: String, onComplete: (Boolean) -> Unit) {
        FirebaseConfig.getTemplateTugasRef(managerId)
            .child(templateId)
            .removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // D. TERAPKAN TEMPLATE INSTAN KE STAFF (Atomic Batch Update)
    // Sekali klik -> Semua item di template langsung jadi Tugas + Notifikasi Staff!
    // D. TERAPKAN TEMPLATE INSTAN KE STAFF (FIXED PATH)
    fun applyTemplateToStaff(
        managerId: String,
        villaId: String,
        villaNama: String,
        ruanganNama: String,
        selectedStaffs: List<Pair<String, String>>,
        template: TaskTemplateModel,
        deadline: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (template.list_tugas_item.isEmpty()) {
            return onComplete(false)
        }

        if (selectedStaffs.isEmpty()) {
            return onComplete(false)
        }

        val childUpdates = hashMapOf<String, Any>()
        val currentTime = System.currentTimeMillis()
        val managerRoot = FirebaseConfig.getManagerRootPath(managerId)
        val operRoot = "$managerRoot/${FirebaseConfig.CHILD_OPERATIONAL}"

        selectedStaffs.forEach { staffPair ->
            val staffId = staffPair.first
            val staffNama = staffPair.second

            template.list_tugas_item.forEach { tugasJudul ->
                val newTugasKey = rootRef.child(operRoot).child(FirebaseConfig.CHILD_TASK_MANAGEMENT).child(villaId).child("list_tugas").push().key ?: return@forEach
                val newNotifKey = rootRef.child(operRoot).child(FirebaseConfig.CHILD_NOTIFIKASI).push().key ?: return@forEach

                val tugasData = TugasModel(
                    id = newTugasKey,
                    manager_id = managerId,
                    villa_id = villaId,
                    villa_nama = villaNama,
                    ruangan = ruanganNama,
                    staff_id = staffId,
                    staff_nama = staffNama,
                    tugas = tugasJudul,
                    deadline = deadline,
                    deskripsi = "Instruksi dari Template: ${template.nama_template}",
                    prioritas = "Sedang",
                    kategori = "Routine",
                    status = FirebaseConfig.STATUS_PENDING,
                    created_at = currentTime,
                    completed_at = 0L,
                    is_validated = false
                )

                val notifData = mapOf(
                    "id" to newNotifKey,
                    "judul" to "Tugas Template Baru di $villaNama",
                    "pesan" to "Tugas: $tugasJudul ($ruanganNama)",
                    "sender_id" to managerId,
                    "target_role" to "staff",
                    "target_uid" to staffId,
                    "timestamp" to currentTime,
                    "tipe" to "normal",
                    "is_read" to false,
                    "villa_id" to villaId,
                    "villa_nama" to villaNama,
                    "waktu" to deadline
                )

                val absTaskPath = "$operRoot/${FirebaseConfig.CHILD_TASK_MANAGEMENT}/$villaId/list_tugas/$newTugasKey"
                val absNotifPath = "$operRoot/${FirebaseConfig.CHILD_NOTIFIKASI}/$newNotifKey"

                childUpdates[absTaskPath] = tugasData
                childUpdates[absNotifPath] = notifData
            }
        }

        if (childUpdates.isEmpty()) {
            return onComplete(false)
        }

        rootRef.updateChildren(childUpdates).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}
