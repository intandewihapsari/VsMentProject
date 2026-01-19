package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource
class VillaRepository {
    private val db = FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_VILLAS)

    fun getVillasByManager(managerUid: String): LiveData<Resource<List<VillaModel>>> {
        val liveData = MutableLiveData<Resource<List<VillaModel>>>()
        liveData.postValue(Resource.Loading())

        db.orderByChild(FirebaseConfig.FIELD_MANAGER_ID).equalTo(managerUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(VillaModel::class.java) }
                    liveData.postValue(Resource.Success(list))
                }
                override fun onCancelled(error: DatabaseError) {
                    liveData.postValue(Resource.Error(error.message))
                }
            })
        return liveData
    }
}