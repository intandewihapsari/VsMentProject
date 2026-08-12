package com.indri.vsmentproject.ui.manager.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.repository.LaporanRepository
import com.indri.vsmentproject.data.utils.Resource

class LaporanViewModel : ViewModel() {
    private val repo = LaporanRepository()
    private val _managerId = MutableLiveData<String>()

    // Memicu pengambilan data otomatis saat managerId diset
    val laporanResource: LiveData<Resource<List<LaporanModel>>> = _managerId.switchMap { id ->
        repo.getAllLaporan(id)
    }

    fun setManagerId(id: String) {
        _managerId.value = id
    }

    fun updateCatatanManager(managerId: String, id: String, catatan: String, onComplete: (Boolean) -> Unit) {
        repo.updateCatatan(managerId, id, catatan, onComplete)
    }

    fun updateStatusLaporan(managerId: String, id: String, status: String, onComplete: (Boolean) -> Unit) {
        repo.updateStatus(managerId, id, status, onComplete)
    }
}