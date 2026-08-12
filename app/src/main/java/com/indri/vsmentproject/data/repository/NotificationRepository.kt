package com.indri.vsmentproject.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class NotificationRepository {

    // --- 1. CREATE (Kirim Instruksi/Notifikasi Baru) ---
    fun createNotification(managerId: String, notif: NotifikasiModel, onComplete: (Boolean, String?) -> Unit) {
        // PERBAIKAN: Menggunakan helper yang langsung mengarah ke operational/notifikasi anaknya
        val notifRef = FirebaseConfig.getNotifikasiRef(managerId)

        val newNotifKey = notifRef.push().key
        if (newNotifKey == null) {
            onComplete(false, "Gagal membuat ID Notifikasi")
            return
        }

        val notifWithId = notif.apply { id = newNotifKey }

        notifRef.child(newNotifKey).setValue(notifWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    // --- 2. READ (Ambil Notifikasi) ---
    fun getMyNotifications(managerId: String, myUid: String, isManager: Boolean): LiveData<Resource<List<NotifikasiModel>>> {
        val liveData = MutableLiveData<Resource<List<NotifikasiModel>>>()
        liveData.postValue(Resource.Loading())

        // PERBAIKAN: Menggunakan helper yang langsung mengarah ke operational/notifikasi anaknya
        val notifRef = FirebaseConfig.getNotifikasiRef(managerId)

        val query = if (isManager) {
            notifRef.orderByChild("sender_id").equalTo(myUid)
        } else {
            notifRef.orderByChild(FirebaseConfig.FIELD_TARGET_UID).equalTo(myUid)
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { data ->
                    data.getValue(NotifikasiModel::class.java)?.apply { id = data.key ?: "" }
                }
                val sortedList = list.sortedByDescending { it.timestamp }
                liveData.postValue(Resource.Success(sortedList))
            }

            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(Resource.Error(error.message))
            }
        })
        return liveData
    }

    // --- 3. UPDATE (Tandai Notifikasi Sudah Dibaca - `is_read = true`) ---
    fun markAsRead(managerId: String, notifId: String, onComplete: (Boolean) -> Unit) {
        // PERBAIKAN: Menggunakan helper langsung ke id notifikasi spesifik di dalam folder yang benar
        FirebaseConfig.getNotifikasiRef(managerId)
            .child(notifId)
            .child("is_read")
            .setValue(true)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // --- 4. DELETE (Hapus Notifikasi) ---
    fun deleteNotification(managerId: String, notifId: String, onComplete: (Boolean) -> Unit) {
        // PERBAIKAN: Menggunakan helper langsung untuk menghapus data dari node yang tepat
        FirebaseConfig.getNotifikasiRef(managerId)
            .child(notifId)
            .removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}