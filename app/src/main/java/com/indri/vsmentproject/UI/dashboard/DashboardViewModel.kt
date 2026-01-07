package com.indri.vsmentproject.UI.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.indri.vsmentproject.Data.Model.InventarisModel
import com.indri.vsmentproject.Data.Model.NotifikasiModel
import com.indri.vsmentproject.Data.Model.TugasModel
import com.indri.vsmentproject.Data.Repository.MainRepository

class DashboardViewModel : ViewModel() {

    private val repo = MainRepository()
    private val masterDb = FirebaseDatabase.getInstance().getReference("master_data")

    private val _inventarisData = MutableLiveData<InventarisModel>()
    val inventarisData: LiveData<InventarisModel> = _inventarisData

    private val _listTugasPending = MutableLiveData<List<TugasModel>>()
    val listTugasPending: LiveData<List<TugasModel>> = _listTugasPending

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
    fun kirimNotifikasi(notifData: Map<String, Any>, onSuccess: () -> Unit) {
        val db = FirebaseDatabase.getInstance().getReference("operational/notifikasi")

        // push() agar setiap notifikasi punya ID unik (N001, N002, dst secara otomatis)
        db.push().setValue(notifData)
            .addOnSuccessListener { onSuccess() }
    }

    // Fungsi untuk list target (Semua Staff + Daftar Staff Spesifik)
    fun getTargetNotifList(): LiveData<List<String>> {
        val liveData = MutableLiveData<List<String>>()
        FirebaseDatabase.getInstance().getReference("master_data/staff")
            .get().addOnSuccessListener { snapshot ->
                val list = mutableListOf("Semua Staff") // Opsi default
                snapshot.children.forEach {
                    list.add(it.child("nama").value.toString())
                }
                liveData.value = list
            }
        return liveData
    }

    fun loadInventarisSummary() {
        val ref = FirebaseDatabase.getInstance().getReference("operational/task_management")

        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                var ganti = 0
                var periksa = 0
                var layak = 0

                // Loop semua Villa
                for (villaSnapshot in snapshot.children) {
                    val tasksSnapshot = villaSnapshot.child("tasks")
                    // Loop semua Tugas di dalam villa tersebut
                    for (taskSnapshot in tasksSnapshot.children) {
                        val status = taskSnapshot.child("status").value?.toString() ?: ""
                        val kategori = taskSnapshot.child("kategori").value?.toString() ?: ""

                        if (status == "selesai") {
                            layak++
                        } else if (status == "pending") {
                            if (kategori == "Perbaikan") ganti++ else periksa++
                        }
                    }
                }
                // Kirim hasil hitungan ke LiveData
                _inventarisData.postValue(InventarisModel(ganti, periksa, layak))
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    fun loadTugasPending() {
        val ref = FirebaseDatabase.getInstance().getReference("operational/task_management")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<TugasModel>()
                for (villaSnapshot in snapshot.children) {
                    val tasksSnapshot = villaSnapshot.child("tasks")
                    for (taskSnapshot in tasksSnapshot.children) {
                        val status = taskSnapshot.child("status").value?.toString() ?: ""
                        val namaTugas = taskSnapshot.child("tugas").value?.toString() ?: "Tugas Tanpa Nama"
                        val staff = taskSnapshot.child("staff_nama").value?.toString() ?: "Belum ada PIC"

                        // Filter: Hanya ambil yang statusnya pending
                        if (status == "pending") {
                            val model = TugasModel(
                                tugas = namaTugas,
                                status = status,
                                staff_nama = staff
                            )
                            list.add(model)
                        }
                    }
                }
                _listTugasPending.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

}