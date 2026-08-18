package com.indri.vsmentproject.ui.manager.dashboard

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.inventory.InventarisModel
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.data.repository.*
import com.indri.vsmentproject.data.utils.Resource
import com.indri.vsmentproject.data.utils.FirebaseConfig
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardViewModel : ViewModel() {
    private val taskRepo = TaskRepository()
    private val notifRepo = NotificationRepository()

    private val activeListeners = mutableMapOf<Query, ValueEventListener>()

    private val _managerUid = MutableLiveData<String>()

    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    private val _staffList = MutableLiveData<List<UserModel>>()
    val staffList: LiveData<List<UserModel>> = _staffList

    private val _analisisNyata = MutableLiveData<AnalisisCepatModel>()

    private val _kirimNotifStatus = MutableLiveData<Resource<Boolean>>()
    val kirimNotifStatus: LiveData<Resource<Boolean>> get() = _kirimNotifStatus

    fun kirimNotifikasi(
        managerUid: String,
        villaNama: String,
        judul: String,
        pesan: String,
        isUrgent: Boolean
    ) {
        _kirimNotifStatus.value = Resource.Loading()

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        val waktuStr = sdf.format(java.util.Date(currentTime))

        val notif = NotifikasiModel(
            judul = judul,
            pesan = pesan,
            tipe = if (isUrgent) "urgent" else "info",
            is_read = false,
            waktu = waktuStr,
            timestamp = currentTime,
            target_role = "staff",
            sender_id = managerUid,
            villa_nama = villaNama
        )

        notifRepo.createNotification(managerUid, notif) { success, errorMessage ->
            if (success) {
                _kirimNotifStatus.postValue(Resource.Success(true))
            } else {
                _kirimNotifStatus.postValue(Resource.Error(errorMessage ?: "Gagal mengirim notifikasi"))
            }
        }
    }

    fun setManagerUid(uid: String) {
        _managerUid.value = uid
    }

    fun getData() {
        getVillaList()
        getStaffList()
    }

    fun getVillaList() {
        val uid = _managerUid.value ?: return
        val ref = FirebaseConfig.getVillasRef(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(VillaModel::class.java)?.apply { id = child.key ?: "" }
                }
                _villaList.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
    }

    fun getStaffList() {
        val uid = _managerUid.value ?: return
        val ref = FirebaseConfig.getStaffsRef(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(UserModel::class.java) }
                _staffList.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
    }

    private fun getLatestLaporan(uid: String): LiveData<LaporanModel?> {
        val result = MutableLiveData<LaporanModel?>()
        val ref = FirebaseConfig.getLaporanKerusakanRef(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Filter hanya yang pending/proses dan ambil yang paling baru berdasarkan created_at
                val latest = snapshot.children
                    .mapNotNull { child -> child.getValue(LaporanModel::class.java)?.apply { id = child.key ?: "" } }
                    .filter { it.status.lowercase() != FirebaseConfig.STATUS_DONE }
                    .maxByOrNull { it.created_at }
                result.postValue(latest)
            }
            override fun onCancelled(error: DatabaseError) { result.postValue(null) }
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
        return result
    }

    private fun hitungAnalisisRealtime(uid: String) {
        val ref = FirebaseConfig.getTaskManagementRef(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshotTugas: DataSnapshot) {
                var totalSeluruhTugas = 0
                var totalSelesai = 0

                snapshotTugas.children.forEach { villaSnap ->
                    val listTugas = villaSnap.child("list_tugas")
                    totalSeluruhTugas += listTugas.childrenCount.toInt()
                    listTugas.children.forEach { tugas ->
                        val status = tugas.child(FirebaseConfig.FIELD_STATUS).value.toString()
                        if (status.equals(FirebaseConfig.STATUS_DONE, ignoreCase = true)) totalSelesai++
                    }
                }
                val progressPercent = if (totalSeluruhTugas > 0) (totalSelesai * 100 / totalSeluruhTugas) else 0
                _analisisNyata.postValue(AnalisisCepatModel(progressTugas = "$progressPercent%"))
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
    }

    private fun getInventarisRealtime(uid: String): LiveData<InventarisModel> {
        val result = MutableLiveData<InventarisModel>()
        val ref = FirebaseConfig.getLaporanKerusakanRef(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var countRusak = 0
                var countHilang = 0
                var countHabis = 0
                snapshot.children.forEach { child ->
                    val status = child.child(FirebaseConfig.FIELD_STATUS).value?.toString()?.lowercase() ?: ""
                    // Hanya hitung jika belum selesai
                    if (status != FirebaseConfig.STATUS_DONE) {
                        val tipe = child.child("tipe_laporan").value?.toString()?.lowercase() ?: ""
                        when (tipe) {
                            "rusak" -> countRusak++
                            "hilang" -> countHilang++
                            "habis" -> countHabis++
                        }
                    }
                }
                result.postValue(InventarisModel(total_rusak = countRusak, total_hilang = countHilang, total_habis = countHabis))
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        activeListeners[ref] = listener
        return result
    }

    override fun onCleared() {
        super.onCleared()
        activeListeners.forEach { (query, listener) ->
            query.removeEventListener(listener)
        }
        activeListeners.clear()
    }

    val dashboardData: LiveData<Resource<List<DashboardItem>>> = _managerUid.switchMap { uid ->
        hitungAnalisisRealtime(uid)
        getData()

        val mediator = MediatorLiveData<Resource<List<DashboardItem>>>()
        mediator.value = Resource.Loading()

        val notifSource = notifRepo.getMyNotifications(uid, uid, isManager = true)
        val laporanSource = getLatestLaporan(uid)
        val pendingTaskSource = taskRepo.getAllPendingTasks(uid)
        val inventarisSource = getInventarisRealtime(uid)

        fun updateCombinedResult() {
            val items = mutableListOf<DashboardItem>()
            items.add(DashboardItem.AksiCepat)

            laporanSource.value?.let {
                val notif = NotifikasiModel(
                    id = it.id, 
                    judul = "Laporan Baru: ${it.tipe_laporan.uppercase()}", 
                    pesan = "${it.staff_nama}: ${it.nama_barang}", 
                    tipe = it.tipe_laporan.lowercase(), // Gunakan tipe laporan untuk warna kartu
                    waktu = it.waktu_lapor,
                    villa_nama = it.villa_nama
                )
                items.add(DashboardItem.NotifikasiUrgent(listOf(notif)))
            }

            inventarisSource.value?.let { items.add(DashboardItem.Inventaris(it)) }
            _analisisNyata.value?.let { items.add(DashboardItem.AnalisisCepat(it)) }

            if (pendingTaskSource.value is Resource.Success) {
                (pendingTaskSource.value as Resource.Success).data?.let { list ->
                    val groups = list.groupBy { it.villa_id }.map { (vid, tasks) -> VillaTugasGroup(vid, tasks.first().villa_nama, tasks) }
                    if (groups.isNotEmpty()) items.add(DashboardItem.TugasPending(groups))
                }
            }
            mediator.value = Resource.Success(items)
        }

        mediator.addSource(notifSource) { updateCombinedResult() }
        mediator.addSource(laporanSource) { updateCombinedResult() }
        mediator.addSource(_analisisNyata) { updateCombinedResult() }
        mediator.addSource(inventarisSource) { updateCombinedResult() }
        mediator.addSource(pendingTaskSource) { updateCombinedResult() }
        mediator
    }

    private fun parseDate(dateString: String?): Long {
        return try { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(dateString ?: "")?.time ?: 0L } catch (e: Exception) { 0L }
    }
}