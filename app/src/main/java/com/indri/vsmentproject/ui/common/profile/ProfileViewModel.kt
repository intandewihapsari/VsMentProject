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
    private val db = FirebaseDatabase.getInstance().reference

    fun getData() {
        val uid = auth.currentUser?.uid ?: return

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // 🔹 Ambil data user (manager / staff)
                var user = snapshot.child(FirebaseConfig.PATH_MANAGERS)
                    .child(uid)
                    .getValue(UserModel::class.java)

                if (user == null) {
                    user = snapshot.child(FirebaseConfig.PATH_STAFFS)
                        .child(uid)
                        .getValue(UserModel::class.java)
                }

                user?.let {
                    it.uid = uid
                    _userData.postValue(it)

                    // 🔥 Hitung statistik
                    calculateStats(snapshot, it)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calculateStats(snapshot: DataSnapshot, user: UserModel) {

        if (user.role == "manager") {

            // ================= MANAGER =================
            val totalVilla = snapshot
                .child(FirebaseConfig.PATH_VILLAS)
                .childrenCount.toInt()

            val totalStaff = snapshot
                .child(FirebaseConfig.PATH_STAFFS)
                .childrenCount.toInt()

            val totalLaporanPending = snapshot
                .child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)
                .children.count {
                    it.child(FirebaseConfig.FIELD_STATUS).value.toString() != FirebaseConfig.STATUS_DONE
                }

            _summary.postValue(
                ProfileSummary(
                    totalVilla,
                    totalStaff,
                    totalLaporanPending
                )
            )

        } else {

            // ================= STAFF =================
            var totalBeres = 0        // Tugas selesai
            var totalLaporan = 0      // Inisiatif lapor
            var sisaTugas = 0         // Tugas belum selesai

            val uid = user.uid

            // 🔹 HITUNG TUGAS
            val taskRef = snapshot.child(FirebaseConfig.PATH_TASK_MANAGEMENT)

            taskRef.children.forEach { task ->
                val workerId = task.child("worker_id").value.toString()
                val status = task.child(FirebaseConfig.FIELD_STATUS).value.toString()

                if (workerId == uid) {
                    if (status == FirebaseConfig.STATUS_DONE) {
                        totalBeres++
                    } else {
                        sisaTugas++
                    }
                }
            }

            // 🔹 HITUNG LAPORAN (INI YANG KEMARIN ERROR)
            val reportRef = snapshot.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)

            reportRef.children.forEach { report ->
                val staffId = report.child("staff_id").value.toString()
                val status = report.child("status").value.toString()

                // ✅ FIX: pakai staff_id, bukan reporter_id
                if (staffId == uid && status != "ditolak") {
                    totalLaporan++
                }
            }

            // 🔥 KIRIM KE UI
            _summary.postValue(
                ProfileSummary(
                    totalBeres,     // ➜ Tugas Beres
                    totalLaporan,   // ➜ Inisiatif Lapor
                    sisaTugas       // ➜ Sisa Tugas
                )
            )
        }
    }

    // ================= UPDATE PROFILE =================
    fun updateFullProfile(
        name: String,
        phone: String,
        email: String,
        photoUrl: String? = null,
        onResult: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return

        val path = if (_userData.value?.role == "manager") {
            FirebaseConfig.PATH_MANAGERS
        } else {
            FirebaseConfig.PATH_STAFFS
        }

        val updates = mutableMapOf<String, Any>(
            "nama" to name,
            "telepon" to phone,
            "email" to email
        )

        photoUrl?.let { updates["foto_profil"] = it }

        db.child(path)
            .child(uid)
            .updateChildren(updates)
            .addOnSuccessListener {
                onResult("Profil berhasil diperbarui")
            }
            .addOnFailureListener {
                onResult("Gagal memperbarui")
            }
    }
}