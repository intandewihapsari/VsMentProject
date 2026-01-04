package com.indri.vsmentproject.UI.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.Data.Model.NotifikasiModel
import com.indri.vsmentproject.Data.Repository.MainRepository

class DashboardViewModel : ViewModel() {

    private val repo = MainRepository()
    private val masterDb = FirebaseDatabase.getInstance().getReference("master_data")

    val notifikasiUrgent: LiveData<List<NotifikasiModel>> = repo.loadNotifikasi().map { list ->
        list.filter { it.tipe == "urgent" }
    }

    // Di DashboardViewModel.kt
    fun getVillaList(): LiveData<List<String>> {
        val liveData = MutableLiveData<List<String>>()
        // Cek di Firebase Console, apakah namanya 'villa' atau 'villas'?
        FirebaseDatabase.getInstance().getReference("master_data/villa_list")
            .get().addOnSuccessListener { snapshot ->
                val list = mutableListOf<String>()
                for (child in snapshot.children) {
                    // Pastikan 'nama' sesuai dengan key di Firebase
                    val nama = child.child("nama").value.toString()
                    list.add(nama)
                }
                liveData.value = list
            }.addOnFailureListener {
                liveData.value = listOf("Gagal memuat Villa")
            }
        return liveData
    }

    fun getStaffList(): LiveData<List<String>> {
        val liveData = MutableLiveData<List<String>>()
        FirebaseDatabase.getInstance().getReference("master_data/staff")
            .get().addOnSuccessListener { snapshot ->
                val list = mutableListOf("Pilih Staff")
                snapshot.children.forEach {
                    list.add(it.child("nama").value.toString())
                }
                liveData.value = list
            }
        return liveData
    }

    // Fungsi Simpan Tugas menggunakan updateChildren (Atomic Update)
    fun simpanTugas(taskData: Map<String, Any>, villaNama: String, onSuccess: () -> Unit) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val villaRef = rootRef.child("operational/task_management").child(villaNama)
        val tasksRef = villaRef.child("tasks")

        // 1. Simpan tugas baru dengan ID unik
        val taskId = tasksRef.push().key ?: return
        tasksRef.child(taskId).setValue(taskData).addOnSuccessListener {

            // 2. Setelah simpan berhasil, ambil semua tugas di villa tersebut untuk hitung ulang metrics
            tasksRef.get().addOnSuccessListener { snapshot ->
                val total = snapshot.childrenCount.toInt()
                var selesai = 0

                for (task in snapshot.children) {
                    if (task.child("status").value.toString() == "selesai") {
                        selesai++
                    }
                }

                // 3. Hitung Persentase
                val persentase = if (total > 0) (selesai * 100 / total) else 0

                // 4. Update folder metrics secara otomatis
                val metricsUpdate = mapOf(
                    "total_tugas" to total,
                    "selesai" to selesai,
                    "persentase" to persentase.toString(),
                    "update_terakhir" to java.text.SimpleDateFormat(
                        "HH:mm",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())
                )

                villaRef.child("metrics").updateChildren(metricsUpdate).addOnSuccessListener {
                    onSuccess()
                }
            }
        }
    }
}