package com.indri.vsmentproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource

class VillaRepository {

    // --- 1. CREATE: Tambah Villa Baru ---
    fun createVilla(managerId: String, villa: VillaModel, onComplete: (Boolean, String?) -> Unit) {
        // Langsung panggil referensi folder villas anaknya
        val villaRef = FirebaseConfig.getVillasRef(managerId)

        val newVillaKey = villaRef.push().key ?: return onComplete(false, "Gagal membuat ID Villa")
        val villaWithId = villa.apply { id = newVillaKey; manager_id = managerId }

        villaRef.child(newVillaKey).setValue(villaWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onComplete(true, null) else onComplete(false, task.exception?.message)
            }
    }

    // --- 2. READ: Mengambil Semua Villa Milik Manager ---
    fun getVillasByManager(managerId: String): LiveData<Resource<List<VillaModel>>> {
        val liveData = MutableLiveData<Resource<List<VillaModel>>>()
        liveData.postValue(Resource.Loading())

        // Langsung pasang listener di referensi folder villas
        FirebaseConfig.getVillasRef(managerId)
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

    // --- 3. UPDATE: Perbarui Data Villa ---
    fun updateVilla(managerId: String, villaId: String, updatedData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        // Masuk ke folder villas lalu tuju id unit villa spesifik di bawahnya
        FirebaseConfig.getVillasRef(managerId).child(villaId)
            .updateChildren(updatedData)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    // --- 4. DELETE: Hapus Unit Villa ---
    fun deleteVilla(managerId: String, villaId: String, onComplete: (Boolean) -> Unit) {
        // Masuk ke folder villas lalu hapus id unit villa spesifik di bawahnya
        FirebaseConfig.getVillasRef(managerId).child(villaId).removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}