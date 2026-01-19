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

        db.child(myUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(NotifikasiModel::class.java) }
                // Urutkan dari yang terbaru
                liveData.postValue(Resource.Success(list.reversed()))
            }
            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(Resource.Error(error.message))
            }
        })
        return liveData
    }
}