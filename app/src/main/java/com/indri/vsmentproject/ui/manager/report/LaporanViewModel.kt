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

    fun updateStatusLaporan(laporanId: String, statusBaru: String, onComplete: (Boolean) -> Unit) {
        repo.updateStatus(laporanId, statusBaru, onComplete)
    }
}