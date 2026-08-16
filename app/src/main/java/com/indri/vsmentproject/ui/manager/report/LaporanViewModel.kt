package com.indri.vsmentproject.ui.manager.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.repository.LaporanRepository
import com.indri.vsmentproject.data.utils.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.indri.vsmentproject.data.utils.FirebaseConfig

class LaporanViewModel : ViewModel() {
    private val repo = LaporanRepository()
    
    private val _laporanResource = MutableLiveData<Resource<List<LaporanModel>>>()
    val laporanResource: LiveData<Resource<List<LaporanModel>>> = _laporanResource

    private var laporanListener: ValueEventListener? = null
    private var currentManagerId: String? = null

    fun setManagerId(id: String) {
        if (currentManagerId == id) return
        
        // Remove existing listener if any
        removeListener()
        
        currentManagerId = id
        _laporanResource.value = Resource.Loading()
        
        laporanListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val model = child.getValue(LaporanModel::class.java)
                    model?.id = child.key ?: ""
                    model
                }.sortedByDescending { it.created_at }
                _laporanResource.postValue(Resource.Success(list))
            }

            override fun onCancelled(error: DatabaseError) {
                _laporanResource.postValue(Resource.Error(error.message))
            }
        }
        
        FirebaseConfig.getLaporanKerusakanRef(id)
            .addValueEventListener(laporanListener!!)
    }

    private fun removeListener() {
        val mId = currentManagerId
        val listener = laporanListener
        if (mId != null && listener != null) {
            FirebaseConfig.getLaporanKerusakanRef(mId).removeEventListener(listener)
        }
    }

    fun updateCatatanManager(managerId: String, id: String, catatan: String, onComplete: (Boolean) -> Unit) {
        repo.updateCatatan(managerId, id, catatan, onComplete)
    }

    fun updateStatusLaporan(managerId: String, id: String, status: String, onComplete: (Boolean) -> Unit) {
        repo.updateStatus(managerId, id, status, onComplete)
    }

    override fun onCleared() {
        super.onCleared()
        removeListener()
    }
}
