package com.indri.vsmentproject.ui.manager.dashboard

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.inventory.InventarisModel
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.repository.*
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.data.utils.FirebaseConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel : ViewModel() {
    private val taskRepo = TaskRepository()
    private val notifRepo = NotificationRepository()
    private val db = FirebaseDatabase.getInstance().reference

    private val _managerUid = MutableLiveData<String>()

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<UserModel>>()
    val staffList: LiveData<List<UserModel>> = _staffList

    private val _analisisNyata = MutableLiveData<AnalisisCepatModel>()

    fun setManagerUid(uid: String) {
        _managerUid.value = uid
    }

    fun getData() {
        getVillaList()
        getStaffList()
    }

    fun getVillaList() {
        val uid = _managerUid.value ?: return
        db.child(FirebaseConfig.PATH_VILLAS)
            .orderByChild(FirebaseConfig.FIELD_MANAGER_ID)
            .equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<VillaModel>()
                    snapshot.children.forEach { child ->
                        child.getValue(VillaModel::class.java)?.let {
                            it.id = child.key ?: ""
                            list.add(it)
                        }
                    }
                    _villaList.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getStaffList() {
        db.child(FirebaseConfig.PATH_STAFFS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(UserModel::class.java) }
                    _staffList.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getLatestLaporan(): LiveData<LaporanModel?> {
        val result = MutableLiveData<LaporanModel?>()
        db.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val latest = snapshot.children
                        .mapNotNull { child ->
                            child.getValue(LaporanModel::class.java)?.apply {
                                id = child.key ?: ""
                            }
                        }
                        .filter {
                            val tipe = it.tipe_laporan?.lowercase()
                            tipe == "rusak" || tipe == "hilang"
                        }
                        .maxByOrNull { it.id ?: "" }
                    result.postValue(latest)
                }
                override fun onCancelled(error: DatabaseError) {
                    result.postValue(null)
                }
            })
        return result
    }

    private fun hitungAnalisisRealtime() {
        val pathTugas = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
        val pathLaporan = db.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)

        pathTugas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshotTugas: DataSnapshot) {
                var totalSeluruhTugas = 0
                var totalSelesai = 0

                snapshotTugas.children.forEach { villaSnap ->
                    val listTugas = villaSnap.child("list_tugas")
                    totalSeluruhTugas += listTugas.childrenCount.toInt()
                    listTugas.children.forEach { tugas ->
                        val status = tugas.child("status").value.toString()
                        if (status.equals("selesai", ignoreCase = true)) {
                            totalSelesai++
                        }
                    }
                }

                val progressPercent = if (totalSeluruhTugas > 0) (totalSelesai * 100 / totalSeluruhTugas) else 0

                pathLaporan.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshotLaporan: DataSnapshot) {
                        _analisisNyata.postValue(AnalisisCepatModel(progressTugas = "$progressPercent%"))
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getInventarisRealtime(): LiveData<InventarisModel> {
        val result = MutableLiveData<InventarisModel>()
        db.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var countRusak = 0
                    var countHilang = 0
                    var countHabis = 0

                    snapshot.children.forEach { child ->
                        val tipe = child.child("tipe_laporan").value?.toString()?.lowercase() ?: ""
                        when (tipe) {
                            "rusak" -> countRusak++
                            "hilang" -> countHilang++
                            "habis" -> countHabis++
                        }
                    }

                    // 🔥 GUNAKAN NAMED ARGUMENTS AGAR TIDAK ERROR
                    // Kita cuma isi bagian totalnya saja, sisanya pakai default value ("")
                    result.postValue(InventarisModel(
                        total_rusak = countRusak,
                        total_hilang = countHilang,
                        total_habis = countHabis
                    ))
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        return result
    }

    val dashboardData: LiveData<Resource<List<DashboardItem>>> = _managerUid.switchMap { uid ->
        hitungAnalisisRealtime()
        getData()

        val mediator = MediatorLiveData<Resource<List<DashboardItem>>>()
        mediator.value = Resource.Loading()

        val notifSource = notifRepo.getMyNotifications(uid)
        val laporanSource = getLatestLaporan()
        val pendingTaskSource = taskRepo.getAllPendingTasks()
        val inventarisSource = getInventarisRealtime()

        fun updateCombinedResult() {
            val notifRes = notifSource.value
            val analisisData = _analisisNyata.value
            val taskRes = pendingTaskSource.value
            val inventarisData = inventarisSource.value
            val laporanData = laporanSource.value

            if (notifRes !is Resource.Loading) {
                val items = mutableListOf<DashboardItem>()

                // 1. Aksi Cepat (SESUAI REQUEST: PALING ATAS)
                items.add(DashboardItem.AksiCepat)

                // 2. Notifikasi Urgent (Laporan Rusak/Hilang terbaru)
                laporanData?.let {
                    val notif = NotifikasiModel(
                        id = it.id,
                        judul = "Laporan ${it.tipe_laporan}",
                        pesan = "${it.nama_barang} - ${it.deskripsi}",
                        tipe = it.tipe_laporan,
                        is_read = false,
                        waktu = it.waktu_lapor,
                        sender_id = it.staff_id,
                        villa_id = it.villa_id,
                        villa_nama = it.villa_nama
                        // target_uid dan target_role akan pakai default value-nya
                    )
                    items.add(DashboardItem.NotifikasiUrgent(listOf(notif)))
                }

                // 3. Ringkasan Inventaris (YANG BARU DITAMBAHKAN)
                inventarisData?.let {
                    items.add(DashboardItem.Inventaris(it))
                }

                // 4. Analisis Progres
                analisisData?.let {
                    items.add(DashboardItem.AnalisisCepat(it))
                }

                // 5. Daftar Tugas Pending
                taskRes?.data?.let { list ->
                    if (list.isNotEmpty()) {
                        val top5 = list.sortedByDescending { it.id }.take(5)
                        items.add(DashboardItem.TugasPending(top5))
                    }
                }

                mediator.value = Resource.Success(items)
            }
        }

        // Membersihkan source lama sebelum menambah yang baru untuk mencegah crash "different observer"
        mediator.apply {
            removeSource(notifSource)
            removeSource(laporanSource)
            removeSource(_analisisNyata)
            removeSource(inventarisSource)
            removeSource(pendingTaskSource)

            addSource(notifSource) { updateCombinedResult() }
            addSource(laporanSource) { updateCombinedResult() }
            addSource(_analisisNyata) { updateCombinedResult() }
            addSource(inventarisSource) { updateCombinedResult() }
            addSource(pendingTaskSource) { updateCombinedResult() }
        }

        mediator
    }
}