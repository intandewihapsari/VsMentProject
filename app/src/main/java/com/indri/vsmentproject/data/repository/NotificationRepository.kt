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

        // 🔥 GANTI: Dari "user_id" ke "sender_id"
        // Karena manager mau liat instruksi yang DIA KIRIM
        val query = db.orderByChild("sender_id").equalTo(myUid)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<NotifikasiModel>()
                for (data in snapshot.children) {
                    val notif = data.getValue(NotifikasiModel::class.java)
                    notif?.let {
                        list.add(it.copy(id = data.key ?: ""))
                    }
                }

                val sortedList = list.sortedByDescending { it.timestamp }
                liveData.postValue(Resource.Success(sortedList))

                // Debug buat mastiin datanya nyangkut berapa
                android.util.Log.d("REPO_NOTIF", "Dapet: ${sortedList.size} data buat sender: $myUid")
            }

            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(Resource.Error(error.message))
            }
        })
        return liveData
    }}