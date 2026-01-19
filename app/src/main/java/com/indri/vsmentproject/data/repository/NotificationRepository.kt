package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.indri.vsmentproject.data.model.notification.NotifikasiModel

class NotificationRepository {
    private val db = FirebaseDatabase.getInstance().reference

    fun getMyNotifications(myUid: String): LiveData<List<NotifikasiModel>> {
        val liveData = MutableLiveData<List<NotifikasiModel>>()

        // Mengambil notifikasi khusus untuk UID Manager tersebut
        db.child("operational/notifikasi").child(myUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(NotifikasiModel::class.java)
                    }
                    liveData.postValue(list.reversed()) // Notif terbaru di atas
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        return liveData
    }
}