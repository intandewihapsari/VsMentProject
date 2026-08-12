package com.indri.vsmentproject.ui.common.profile

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.user.ProfileSummary
import com.indri.vsmentproject.data.model.user.UserModel
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

        db.child(FirebaseConfig.PATH_USER_MAPPING).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(mappingSnapshot: DataSnapshot) {
                    if (!mappingSnapshot.exists()) return

                    val role = mappingSnapshot.child(FirebaseConfig.FIELD_ROLE).value.toString()
                    val belongsToManager = mappingSnapshot.child(FirebaseConfig.FIELD_BELONGS_TO_MANAGER).value.toString()

                    val profileRef = if (role == "manager") {
                        FirebaseConfig.getManagerRef(uid).child(FirebaseConfig.CHILD_MANAGER_PROFILE)
                    } else {
                        FirebaseConfig.getStaffsRef(belongsToManager).child(uid)
                    }

                    profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val user = userSnapshot.getValue(UserModel::class.java) ?: return
                            user.uid = uid
                            user.manager_id = if (role == "manager") uid else belongsToManager
                            _userData.postValue(user)

                            fetchSnapshotForStats(user.manager_id, user)
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchSnapshotForStats(managerId: String, user: UserModel) {
        FirebaseConfig.getManagerRef(managerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    calculateStats(snapshot, user)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun calculateStats(managerSnapshot: DataSnapshot, user: UserModel) {
        if (user.role == "manager") {
            val totalVilla = managerSnapshot.child(FirebaseConfig.CHILD_VILLAS).childrenCount.toInt()
            val totalStaff = managerSnapshot.child(FirebaseConfig.CHILD_STAFFS).childrenCount.toInt()
            val totalLaporanPending = managerSnapshot.child(FirebaseConfig.CHILD_LAPORAN_KERUSAKAN).children.count {
                it.child(FirebaseConfig.FIELD_STATUS).getValue(String::class.java)?.lowercase() == FirebaseConfig.STATUS_PENDING
            }
            _summary.postValue(ProfileSummary(totalVilla, totalStaff, totalLaporanPending))
        } else {
            var totalBeres = 0
            var sisaTugas = 0
            var totalLaporan = 0
            val uid = user.uid

            // Hitung Tugas (Struktur flat task_management)
            managerSnapshot.child(FirebaseConfig.CHILD_TASK_MANAGEMENT).children.forEach { tugas ->
                val staffIdInTask = tugas.child("staff_id").getValue(String::class.java) ?: ""
                val statusTugas = tugas.child(FirebaseConfig.FIELD_STATUS).getValue(String::class.java) ?: ""

                if (staffIdInTask == uid) {
                    if (statusTugas.equals(FirebaseConfig.STATUS_DONE, ignoreCase = true)) totalBeres++ else sisaTugas++
                }
            }

            // Hitung Laporan
            managerSnapshot.child(FirebaseConfig.CHILD_LAPORAN_KERUSAKAN).children.forEach { report ->
                val staffIdInReport = report.child("staff_id").getValue(String::class.java) ?: ""
                if (staffIdInReport == uid) totalLaporan++
            }

            _summary.postValue(ProfileSummary(totalBeres, totalLaporan, sisaTugas))
        }
    }

    fun updateFullProfile(name: String, phone: String, email: String, photoUrl: String? = null, onResult: (String) -> Unit) {
        val currentUser = _userData.value ?: return onResult("Gagal, data user belum termuat")
        val firebaseUser = auth.currentUser

        if (firebaseUser == null) {
            onResult("Sesi login kedaluwarsa, silakan login ulang")
            return
        }

        // 1. Update Email di Firebase Authentication Terlebih Dahulu
        firebaseUser.updateEmail(email).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {

                // 2. Jika Auth Sukses, Tentukan Reference Database Sesuai Role
                val profileRef = if (currentUser.role == "manager") {
                    FirebaseConfig.getManagerRef(currentUser.uid).child(FirebaseConfig.CHILD_MANAGER_PROFILE)
                } else {
                    FirebaseConfig.getStaffsRef(currentUser.manager_id).child(currentUser.uid)
                }

                // Susun data yang akan diupdate ke Database
                val updates = mutableMapOf<String, Any>("nama" to name, "telepon" to phone, "email" to email)
                photoUrl?.let { updates["foto_profil"] = it }

                // 3. Update Data di Realtime Database
                profileRef.updateChildren(updates).addOnSuccessListener {
                    // Update state lokal agar UI langsung berubah
                    currentUser.nama = name
                    currentUser.telepon = phone
                    currentUser.email = email
                    photoUrl?.let { currentUser.foto_profil = it }
                    _userData.postValue(currentUser)

                    onResult("Profil dan Email Authentication berhasil diperbarui")
                }.addOnFailureListener { dbError ->
                    onResult("Auth berhasil, tetapi gagal simpan biodata ke DB: ${dbError.message}")
                }

            } else {
                // Gagal update email di Authentication (Misal: format salah atau email sudah dipakai akun lain)
                onResult("Gagal memperbarui Email Auth: ${authTask.exception?.message}")
            }
        }
    }
}