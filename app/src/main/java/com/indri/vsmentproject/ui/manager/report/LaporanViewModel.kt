package com.indri.vsmentproject.ui.manager.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.repository.LaporanRepository
import com.indri.vsmentproject.data.utils.Resource

class LaporanViewModel : ViewModel() {
    private val repo = LaporanRepository()

    // Observasi langsung dari Repository menggunakan wrapper Resource
    val laporanResource: LiveData<Resource<List<LaporanModel>>> = repo.getAllLaporan()

    fun updateCatatanManager(id: String, catatan: String, onComplete: (Boolean) -> Unit) {
        // Repo panggil db.child("laporan").child(id).child("catatan_manager").setValue(catatan)
        repo.updateCatatan(id, catatan, onComplete)
    }
    fun updateStatusLaporan(id: String, status: String, onComplete: (Boolean) -> Unit) {
        repo.updateStatus(id, status, onComplete)
    }
}