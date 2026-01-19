package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.indri.vsmentproject.data.model.villa.VillaModel

class VillaRepository {
    private val db = FirebaseDatabase.getInstance().getReference("master_data/villas")

    // Ambil semua villa yang dikelola oleh Manager tertentu
    fun getVillasByManager(managerUid: String): LiveData<List<VillaModel>> {
        val liveData = MutableLiveData<List<VillaModel>>()

        db.orderByChild("manager_id").equalTo(managerUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(VillaModel::class.java) }
                    liveData.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        return liveData
    }

    // Simpan atau Update Villa (Termasuk URL Foto Cloudinary)
    fun saveVilla(villaId: String?, data: Map<String, Any>, onResult: (Boolean) -> Unit) {
        val id = villaId ?: db.push().key ?: ""
        val finalData = data.toMutableMap()
        finalData["id"] = id

        db.child(id).updateChildren(finalData).addOnCompleteListener {
            onResult(it.isSuccessful)
        }
    }

    // Hapus Villa
    fun deleteVilla(villaId: String, onResult: (Boolean) -> Unit) {
        db.child(villaId).removeValue().addOnCompleteListener {
            onResult(it.isSuccessful)
        }
    }
}