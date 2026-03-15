package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource
class NotificationRepository {
    private val db = FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_NOTIFIKASI)

    fun getMyNotifications(myUid: String): LiveData<Resource<List<NotifikasiModel>>> {
        val liveData = MutableLiveData<Resource<List<NotifikasiModel>>>()
        liveData.postValue(Resource.Loading())

        // GUNAKAN QUERY FILTER: Cari yang user_id nya sesuai UID kita
        val query = db.orderByChild("user_id").equalTo(myUid)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<NotifikasiModel>()
                for (data in snapshot.children) {
                    val notif = data.getValue(NotifikasiModel::class.java)
                    notif?.let {
                        // Jangan lupa copy ID-nya dari key Firebase (misal: NOTIF_J01)
                        list.add(it.copy(id = data.key ?: ""))
                    }
                }

                // Urutkan berdasarkan timestamp terbaru (karena reversed() aja gak cukup kalau ID-nya acak)
                val sortedList = list.sortedByDescending { it.timestamp }
                liveData.postValue(Resource.Success(sortedList))
            }

            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(Resource.Error(error.message))
            }
        })
        return liveData
    }
}