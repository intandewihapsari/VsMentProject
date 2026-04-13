package com.indri.vsmentproject.ui.common.profile

import android.util.Log
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

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val uid = auth.currentUser?.uid ?: return

                // 🔥 TARUH LOG DI SINI
                Log.d("PROFILE", "UID: $uid")
                Log.d("PROFILE", "MANAGER DATA: ${snapshot.child(FirebaseConfig.PATH_MANAGERS).child(uid).value}")
                Log.d("PROFILE", "STAFF DATA: ${snapshot.child(FirebaseConfig.PATH_STAFFS).child(uid).value}")

                var user = snapshot.child(FirebaseConfig.PATH_MANAGERS)
                    .child(uid)
                    .getValue(UserModel::class.java)

                if (user == null) {
                    user = snapshot.child(FirebaseConfig.PATH_STAFFS)
                        .child(uid)
                        .getValue(UserModel::class.java)
                }

                if (user == null) return

                user.uid = uid
                _userData.postValue(user)

                calculateStats(snapshot, user)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calculateStats(snapshot: DataSnapshot, user: UserModel) {

        if (user.role == "manager") {

            // ================= MANAGER =================
            val totalVilla = snapshot.child(FirebaseConfig.PATH_VILLAS)
                .childrenCount.toInt()

            val totalStaff = snapshot.child(FirebaseConfig.PATH_STAFFS)
                .childrenCount.toInt()

            val totalLaporanPending = snapshot.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)
                .children.count {
                    it.child("status").getValue(String::class.java)
                        ?.lowercase() != FirebaseConfig.STATUS_DONE
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
            var totalBeres = 0
            var totalLaporan = 0
            var sisaTugas = 0

            val uid = user.uid

            // ================= TASK =================
            val taskRef = snapshot.child(FirebaseConfig.PATH_TASK_MANAGEMENT)

            taskRef.children.forEach { task ->

                val staffId = task.child("staff_id")
                    .getValue(String::class.java) ?: ""

                val status = task.child(FirebaseConfig.FIELD_STATUS)
                    .getValue(String::class.java) ?: ""

                if (staffId == uid) {
                    if (status == FirebaseConfig.STATUS_DONE) {
                        totalBeres++
                    } else {
                        sisaTugas++
                    }
                }
            }

            // ================= LAPORAN =================
            val reportRef = snapshot.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)

            reportRef.children.forEach { report ->

                val staffId = report.child("staff_id")
                    .getValue(String::class.java) ?: ""

                val status = report.child("status")
                    .getValue(String::class.java) ?: ""

                if (staffId == uid && status != FirebaseConfig.STATUS_REJECTED) {
                    totalLaporan++
                }
            }

            _summary.postValue(
                ProfileSummary(
                    totalBeres,
                    totalLaporan,
                    sisaTugas
                )
            )
        }
    }

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

        photoUrl?.let {
            updates["foto_profil"] = it
        }

        db.child(path)
            .child(uid)
            .updateChildren(updates)
            .addOnSuccessListener {
                onResult("Profil berhasil diperbarui")
            }
            .addOnFailureListener {
                onResult("Gagal memperbarui profil")
            }
    }
}