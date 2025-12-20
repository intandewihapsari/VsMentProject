package com.indri.vsmentproject.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.indri.vsmentproject.Data.NotifikasiModel
import com.indri.vsmentproject.Repository.MainRepository

class DashboardViewModel : ViewModel() {

    private val repo = MainRepository()

    val notifikasiUrgent: LiveData<List<NotifikasiModel>> =
        repo.loadNotifikasi().map { list ->
            list.filter { it.tipe == "urgent" }
        }
}