package com.indri.vsmentproject.ui.dashboard

import androidx.lifecycle.*
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.InventarisModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.repository.MainRepository
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel : ViewModel() {

    private val repo = MainRepository()
    private val db = FirebaseDatabase.getInstance()

    // Data Real-time
    private val _inventarisData = MutableLiveData<InventarisModel>()
    val inventarisData: LiveData<InventarisModel> = _inventarisData

    private val _listTugasPending = MutableLiveData<List<TugasModel>>()
    val listTugasPending: LiveData<List<TugasModel>> = _listTugasPending

    val notifikasiUrgent: LiveData<List<NotifikasiModel>> = repo.loadNotifikasi().map { list ->
        list.filter { it.tipe == "urgent" }
    }

    // Master Data untuk Spinner
    private val _villaList = MutableLiveData<List<String>>()
    val villaList: LiveData<List<String>> = _villaList

    private val _staffList = MutableLiveData<List<String>>()
    val staffList: LiveData<List<String>> = _staffList

    // --- MEDIATOR: Gabungan semua data untuk UI ---
    val combinedDashboardData = MediatorLiveData<List<DashboardItem>>().apply {
        addSource(notifikasiUrgent) { value = buildDashboardList() }
        addSource(inventarisData) { value = buildDashboardList() }
        addSource(listTugasPending) { value = buildDashboardList() }
    }

    init {
        loadInventarisSummary()
        loadTugasPending()
        observeMasterData()
    }

    private fun buildDashboardList(): List<DashboardItem> {
        val items = mutableListOf<DashboardItem>()
        items.add(DashboardItem.NotifikasiUrgent(notifikasiUrgent.value ?: emptyList()))
        items.add(DashboardItem.AnalisisCepat(emptyList())) // Sesuai permintaan dikosongkan
        items.add(DashboardItem.AksiCepat)

        inventarisData.value?.let { items.add(DashboardItem.Inventaris(it)) }

        val tasks = listTugasPending.value ?: emptyList()
        if (tasks.isNotEmpty()) items.add(DashboardItem.TugasPending(tasks))

        return items
    }

    private fun observeMasterData() {
        // Observe Villa
        db.getReference("master_data/villa_list").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _villaList.postValue(snapshot.children.map { it.child("nama").value.toString() })
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Observe Staff
        db.getReference("master_data/staff").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf("Pilih Staff")
                snapshot.children.forEach { list.add(it.child("nama").value.toString()) }
                _staffList.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadInventarisSummary() {
        db.getReference("operational/task_management").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var ganti = 0; var periksa = 0; var layak = 0
                for (villa in snapshot.children) {
                    for (task in villa.child("tasks").children) {
                        val status = task.child("status").value.toString()
                        val kategori = task.child("kategori").value.toString()
                        if (status == "selesai") layak++
                        else if (status == "pending") {
                            if (kategori == "Perbaikan") ganti++ else periksa++
                        }
                    }
                }
                _inventarisData.postValue(InventarisModel(ganti, periksa, layak))
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadTugasPending() {
        db.getReference("operational/task_management").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<TugasModel>()
                for (villa in snapshot.children) {
                    for (task in villa.child("tasks").children) {
                        if (task.child("status").value.toString() == "pending") {
                            list.add(TugasModel(
                                tugas = task.child("tugas").value.toString(),
                                status = "pending",
                                staff_nama = task.child("staff_nama").value.toString()
                            ))
                        }
                    }
                }
                _listTugasPending.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun simpanTugas(taskData: Map<String, Any>, villaNama: String, onSuccess: () -> Unit) {
        val villaRef = db.reference.child("operational/task_management").child(villaNama)
        val taskId = villaRef.child("tasks").push().key ?: return

        villaRef.child("tasks").child(taskId).setValue(taskData).addOnSuccessListener {
            // Hitung metrics otomatis (Trigger ulang)
            villaRef.child("tasks").get().addOnSuccessListener { snapshot ->
                val total = snapshot.childrenCount.toInt()
                val selesai = snapshot.children.count { it.child("status").value.toString() == "selesai" }
                val persentase = if (total > 0) (selesai * 100 / total) else 0

                val update = mapOf(
                    "total_tugas" to total,
                    "selesai" to selesai,
                    "persentase" to "$persentase%",
                    "update_terakhir" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                )
                villaRef.child("metrics").updateChildren(update).addOnSuccessListener { onSuccess() }
            }
        }
    }

    fun kirimNotifikasi(notifData: Map<String, Any>, onSuccess: () -> Unit) {
        db.getReference("operational/notifikasi").push().setValue(notifData).addOnSuccessListener { onSuccess() }
    }
}