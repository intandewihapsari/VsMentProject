package com.indri.vsmentproject.ui.common.profile

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.user.*
import com.indri.vsmentproject.data.utils.FirebaseConfig

class ProfileViewModel : ViewModel() {
    private val _userData = MutableLiveData<UserModel>()
    val userData: LiveData<UserModel> = _userData

    private val _summary = MutableLiveData<ProfileSummary>()
    val summary: LiveData<ProfileSummary> = _summary

    private val auth = FirebaseAuth.getInstance()
    // PASTIKAN db merujuk ke getReference() tanpa child apapun agar bisa akses ROOT
    private val db = FirebaseDatabase.getInstance().reference

    fun getData() {
        val uid = auth.currentUser?.uid ?: return

        // Taruh listener di root database agar bisa baca 'users', 'master_data', dan 'operational' sekaligus
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 1. Cari data user di folder managers dulu, kalau ga ada cari di staffs
                var user = snapshot.child(FirebaseConfig.PATH_MANAGERS).child(uid).getValue(UserModel::class.java)
                if (user == null) {
                    user = snapshot.child(FirebaseConfig.PATH_STAFFS).child(uid).getValue(UserModel::class.java)
                }

                user?.let {
                    it.uid = uid // Simpan UID agar tidak hilang
                    _userData.postValue(it)
                    calculateStats(snapshot, it) // Kirim seluruh snapshot database ke sini
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calculateStats(snapshot: DataSnapshot, user: UserModel) {
        if (user.role == "manager") {
            // Stats buat Manager
            val vCount = snapshot.child(FirebaseConfig.PATH_VILLAS).childrenCount.toInt()
            val sCount = snapshot.child(FirebaseConfig.PATH_STAFFS).childrenCount.toInt()
            val lPending = snapshot.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN).children.count {
                it.child(FirebaseConfig.FIELD_STATUS).value.toString() != FirebaseConfig.STATUS_DONE
            }
            _summary.postValue(ProfileSummary(vCount, sCount, lPending))
        } else {
            // --- FIX TOTAL UNTUK STAFF (JUSTIN) ---
            var totalBeres = 0
            var totalLaporan = 0
            var sisaTugas = 0

            // Tarik data dari folder operational/task_management
            val taskRef = snapshot.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            taskRef.children.forEach { task ->
                // Di JSON kamu pakai 'worker_id'
                if (task.child("worker_id").value.toString() == user.uid) {
                    val status = task.child(FirebaseConfig.FIELD_STATUS).value.toString()
                    if (status == FirebaseConfig.STATUS_DONE) totalBeres++ else sisaTugas++
                }
            }

            // Tarik data dari folder operational/laporan_kerusakan
            val reportRef = snapshot.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)
            reportRef.children.forEach { report ->
                // Di JSON kamu pakai 'reporter_id'
                if (report.child("reporter_id").value.toString() == user.uid) {
                    totalLaporan++
                }
            }

            // Hasil akhirnya dikirim ke UI
            _summary.postValue(ProfileSummary(totalBeres, totalLaporan, sisaTugas))
        }
    }

    // Fungsi update (Gunakan path dinamis)
    fun updateFullProfile(name: String, phone: String, email: String, photoUrl: String? = null, onResult: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val path = if (_userData.value?.role == "manager") FirebaseConfig.PATH_MANAGERS else FirebaseConfig.PATH_STAFFS

        val updates = mutableMapOf<String, Any>(
            "nama" to name,
            "telepon" to phone,
            "email" to email
        )
        photoUrl?.let { updates["foto_profil"] = it }

        db.child(path).child(uid).updateChildren(updates).addOnSuccessListener {
            onResult("Profil berhasil diperbarui")
        }.addOnFailureListener { onResult("Gagal memperbarui") }
    }
}