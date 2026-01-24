package com.indri.vsmentproject.ui.manager.dashboard

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.AnalisisCepatModel
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.user.StaffModel
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

    // LiveData untuk Spinner Villa
    private val _villaList = MutableLiveData<List<VillaModel>>()
    val villaList: LiveData<List<VillaModel>> = _villaList

    // LiveData untuk Dialog Multi-Pilih Staff
    private val _staffList = MutableLiveData<List<StaffModel>>()
    val staffList: LiveData<List<StaffModel>> = _staffList

    private val _analisisNyata = MutableLiveData<AnalisisCepatModel>()

    fun setManagerUid(uid: String) {
        _managerUid.value = uid
    }

    /**
     * Mengambil data master (Villa dan Staff) sekaligus
     */
    fun getData() {
        getVillaList()
        getStaffList()
    }

    /**
     * Ambil daftar villa milik manager yang sedang login
     */
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

    /**
     * Ambil daftar semua staff untuk keperluan pengiriman notifikasi massal
     */
    private fun getStaffList() {
        db.child(FirebaseConfig.PATH_STAFFS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(StaffModel::class.java) }
                    _staffList.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun hitungAnalisisRealtime() {
        val pathTugas = db.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
        pathTugas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalSeluruhTugas = 0
                var totalSelesai = 0

                snapshot.children.forEach { villaSnap ->
                    val listTugas = villaSnap.child("list_tugas")
                    totalSeluruhTugas += listTugas.childrenCount.toInt()
                    listTugas.children.forEach { tugas ->
                        val status = tugas.child("status").value.toString()
                        if (status.equals("selesai", ignoreCase = true)) totalSelesai++
                    }
                }

                val progressPercent = if (totalSeluruhTugas > 0) (totalSelesai * 100 / totalSeluruhTugas) else 0
                _analisisNyata.postValue(AnalisisCepatModel(
                    progressTugas = "$progressPercent%",
                    jumlahLaporan = 0,
                    barangRusak = 0
                ))
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Kirim notifikasi ke satu, beberapa, atau seluruh staff
     */
    fun kirimNotifikasiMassal(
        uids: List<String>,
        judul: String,
        pesan: String,
        tipe: String,
        villaId: String,
        villaNama: String
    ) {
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val ref = db.child(FirebaseConfig.PATH_NOTIFIKASI)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val waktuSekarang = sdf.format(Date())

        if (uids.isEmpty()) {
            // Jika UIDs kosong, target_role diisi 'staff' agar semua staff menerima
            val id = ref.push().key ?: ""
            val data = NotifikasiModel(
                id = id,
                judul = judul,
                pesan = pesan,
                tipe = tipe,
                status_baca = false,
                waktu = waktuSekarang,
                target_uid = "",
                target_role = "staff",
                sender_id = senderUid,
                villa_id = villaId,
                villa_nama = villaNama
            )
            ref.child(id).setValue(data)
        } else {
            // Kirim spesifik ke UID yang dipilih (Looping)
            uids.forEach { targetUid ->
                val id = ref.push().key ?: ""
                val data = NotifikasiModel(
                    id = id,
                    judul = judul,
                    pesan = pesan,
                    tipe = tipe,
                    status_baca = false,
                    waktu = waktuSekarang,
                    target_uid = targetUid,
                    target_role = "",
                    sender_id = senderUid,
                    villa_id = villaId,
                    villa_nama = villaNama
                )
                ref.child(id).setValue(data)
            }
        }
    }

    val dashboardData: LiveData<Resource<List<DashboardItem>>> = _managerUid.switchMap { uid ->
        hitungAnalisisRealtime()
        getData() // Ambil Villa & Staff saat manager UID tersedia

        val mediator = MediatorLiveData<Resource<List<DashboardItem>>>()
        mediator.value = Resource.Loading()

        val notifSource = notifRepo.getMyNotifications(uid)
        val inventarisSource = taskRepo.getInventarisSummary()
        val pendingTaskSource = taskRepo.getAllPendingTasks()

        fun updateCombinedResult() {
            val notifRes = notifSource.value
            val analisisData = _analisisNyata.value
            val inventarisRes = inventarisSource.value
            val taskRes = pendingTaskSource.value

            if (notifRes !is Resource.Loading && inventarisRes !is Resource.Loading) {
                val items = mutableListOf<DashboardItem>()

                // 1. Notifikasi Urgent
                notifRes?.data?.let { if (it.isNotEmpty()) items.add(DashboardItem.NotifikasiUrgent(it)) }

                // 2. Analisis Progres
                analisisData?.let { items.add(DashboardItem.AnalisisCepat(it)) }

                // 3. Tombol Aksi (Tambah Tugas / Kirim Notif)
                items.add(DashboardItem.AksiCepat)

                // 4. Ringkasan Inventaris
                inventarisRes?.data?.let { items.add(DashboardItem.Inventaris(it)) }

                // 5. Daftar Tugas Pending (Top 5)
                taskRes?.data?.let { list ->
                    if (list.isNotEmpty()) {
                        val top5 = list.sortedByDescending { it.created_at }.take(5)
                        items.add(DashboardItem.TugasPending(top5))
                    }
                }

                mediator.value = Resource.Success(items)
            }
        }

        mediator.addSource(notifSource) { updateCombinedResult() }
        mediator.addSource(_analisisNyata) { updateCombinedResult() }
        mediator.addSource(inventarisSource) { updateCombinedResult() }
        mediator.addSource(pendingTaskSource) { updateCombinedResult() }
        mediator
    }
}