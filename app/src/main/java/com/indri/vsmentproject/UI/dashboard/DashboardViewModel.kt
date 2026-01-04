package com.indri.vsmentproject.UI.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.indri.vsmentproject.Data.Model.NotifikasiModel
import com.indri.vsmentproject.Data.Repository.MainRepository

class DashboardViewModel : ViewModel() {

    private val repo = MainRepository()

    val notifikasiUrgent: LiveData<List<NotifikasiModel>> =
        repo.loadNotifikasi().map { list ->
            list.filter { it.tipe == "urgent" }
        }
}